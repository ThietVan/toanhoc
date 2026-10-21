package com.example.toanhoc.ui.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toanhoc.domain.curriculum.Curricula
import com.example.toanhoc.domain.curriculum.ExamQuestion
import com.example.toanhoc.domain.curriculum.SemesterExamBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SemesterExamState(
    val grade: Int = 1,
    val semester: Int = 1,
    val loading: Boolean = true,
    val questions: List<ExamQuestion> = emptyList(),
    val answers: Map<Int, String> = emptyMap(),
    val index: Int = 0,
    val elapsedSeconds: Int = 0,
    val submitted: Boolean = false
) {
    val correctCount: Int
        get() = questions.indices.count { i -> answers[i] == questions[i].problem.steps.first().expectedAnswer }
    val percent: Int get() = if (questions.isEmpty()) 0 else correctCount * 100 / questions.size
    val passed: Boolean get() = percent >= PASS_PERCENT
    /** Bài có ít nhất một câu sai (hoặc bỏ trống). */
    val wrongLessonIds: List<String>
        get() = questions.indices
            .filter { i -> answers[i] != questions[i].problem.steps.first().expectedAnswer }
            .map { questions[it].lessonId }
            .distinct()

    companion object { const val PASS_PERCENT = 80 }
}

/** Bài kiểm tra học kì: 20 câu nâng cao, không gợi ý, nộp bài mới chấm. */
class SemesterExamViewModel(
    private val builder: SemesterExamBuilder = SemesterExamBuilder()
) : ViewModel() {

    private val _state = MutableStateFlow(SemesterExamState())
    val state: StateFlow<SemesterExamState> = _state.asStateFlow()
    private var timer: Job? = null

    fun start(grade: Int, semester: Int) {
        _state.value = SemesterExamState(grade = grade, semester = semester, loading = true)
        viewModelScope.launch {
            val curriculum = Curricula.forGrade(grade) ?: return@launch
            val qs = withContext(Dispatchers.Default) { builder.build(curriculum, semester) }
            _state.update { it.copy(loading = false, questions = qs) }
            timer?.cancel()
            timer = launch {
                while (!_state.value.submitted) {
                    delay(1000)
                    _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
            }
        }
    }

    fun answer(option: String) {
        if (_state.value.submitted) return
        _state.update { it.copy(answers = it.answers + (it.index to option)) }
    }

    fun goTo(i: Int) = _state.update { it.copy(index = i.coerceIn(0, (it.questions.size - 1).coerceAtLeast(0))) }

    fun submit() {
        _state.update { it.copy(submitted = true, index = 0) }
        timer?.cancel()
    }

    override fun onCleared() {
        timer?.cancel()
        super.onCleared()
    }
}
