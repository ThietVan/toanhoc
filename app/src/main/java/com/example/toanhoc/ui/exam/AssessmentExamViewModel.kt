package com.example.toanhoc.ui.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toanhoc.data.repository.KnowledgeRepository
import com.example.toanhoc.data.seed.CurriculumSeedData
import com.example.toanhoc.domain.generator.ParametricProblemEngine
import com.example.toanhoc.domain.model.AcademicTier
import com.example.toanhoc.domain.model.ExamMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel orchestrating Assessment Exams for Level-Up Gatekeeper & Parent Verification.
 * Draws 10-question comprehensive tests across all 10 core nodes of the selected grade level.
 */
class AssessmentExamViewModel(
    private val repository: KnowledgeRepository? = null,
    private val problemEngine: ParametricProblemEngine = ParametricProblemEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamUiState())
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startExam(mode: ExamMode, grade: Int, targetTier: AcademicTier) {
        val gradeNodes = CurriculumSeedData.seedNodes.filter { it.grade == grade }
        val problemsList = if (gradeNodes.isNotEmpty()) {
            gradeNodes.take(10).map { node ->
                problemEngine.generateProblemForNode(
                    nodeId = node.nodeId,
                    grade = grade,
                    academicTier = targetTier
                )
            }
        } else {
            (1..10).map { index ->
                problemEngine.generateProblemForNode(
                    nodeId = "EXAM_${grade}_$index",
                    grade = grade,
                    academicTier = targetTier
                )
            }
        }

        _uiState.value = ExamUiState(
            examMode = mode,
            targetTier = targetTier,
            grade = grade,
            problems = problemsList,
            currentProblemIndex = 0,
            studentAnswersMap = emptyMap(),
            totalSecondsRemaining = 900,
            isSubmitted = false,
            scoreOutOf10 = 0,
            isPassed = false
        )

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.totalSecondsRemaining > 0 && !_uiState.value.isSubmitted) {
                delay(1000L)
                _uiState.update { it.copy(totalSecondsRemaining = it.totalSecondsRemaining - 1) }
            }
            if (!_uiState.value.isSubmitted && _uiState.value.totalSecondsRemaining <= 0) {
                submitExam()
            }
        }
    }

    fun onAnswerInput(answer: String) {
        if (_uiState.value.isSubmitted) return
        val currentIdx = _uiState.value.currentProblemIndex
        _uiState.update {
            it.copy(studentAnswersMap = it.studentAnswersMap + (currentIdx to answer))
        }
    }

    fun onNextProblem() {
        _uiState.update {
            if (it.currentProblemIndex + 1 < it.problems.size) {
                it.copy(currentProblemIndex = it.currentProblemIndex + 1)
            } else it
        }
    }

    fun onPreviousProblem() {
        _uiState.update {
            if (it.currentProblemIndex > 0) {
                it.copy(currentProblemIndex = it.currentProblemIndex - 1)
            } else it
        }
    }

    fun submitExam() {
        val state = _uiState.value
        if (state.isSubmitted) return

        var correctCount = 0
        state.problems.forEachIndexed { index, problem ->
            val expected = problem.steps.lastOrNull()?.expectedAnswer?.trim() ?: ""
            val actual = state.studentAnswersMap[index]?.trim() ?: ""
            val isCorrect = actual.equals(expected, ignoreCase = true)
            if (isCorrect) {
                correctCount++
            } else {
                // Nếu sai câu thuộc chặng/bài nào, chặng đó phải học lại từ đầu (Khóa lại mastery)
                viewModelScope.launch {
                    repository?.resetNodeMasteryForRelearning(problem.nodeId)
                }
            }
        }

        val passed = correctCount >= 8

        _uiState.update {
            it.copy(
                isSubmitted = true,
                scoreOutOf10 = correctCount,
                isPassed = passed
            )
        }

        // If Level-Up Exam Passed, upgrade student's AcademicTier in Room DB!
        if (state.examMode == ExamMode.LEVEL_UP_EXAM && passed) {
            viewModelScope.launch {
                val profile = repository?.getStudentProfile()
                if (profile != null) {
                    repository.saveStudentProfile(
                        profile.copy(currentAcademicTier = state.targetTier.name)
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
