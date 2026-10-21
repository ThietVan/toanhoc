package com.example.toanhoc.ui.session

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toanhoc.data.local.entity.DailySessionLogEntity
import com.example.toanhoc.data.repository.KnowledgeRepository
import com.example.toanhoc.data.seed.CurriculumSeedData
import com.example.toanhoc.domain.curriculum.Curricula
import com.example.toanhoc.domain.curriculum.LessonProblemGenerator
import com.example.toanhoc.domain.curriculum.LessonStage
import com.example.toanhoc.domain.engine.BayesianKnowledgeTracer
import com.example.toanhoc.domain.engine.DiagnosticMisconceptionAnalyzer
import com.example.toanhoc.domain.engine.FireSpacedRepetitionEngine
import com.example.toanhoc.domain.generator.ParametricProblemEngine
import com.example.toanhoc.domain.model.RoundLevel
import com.example.toanhoc.domain.model.ScaffoldingLevel
import com.example.toanhoc.domain.model.SessionPhase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel orchestrating Round Practice with Upward Count-Up Timer and Strict 12-Question Completion rules.
 */
class Session900sViewModel(
    private val repository: KnowledgeRepository? = null,
    private val problemEngine: ParametricProblemEngine = ParametricProblemEngine(),
    private val bktTracer: BayesianKnowledgeTracer = BayesianKnowledgeTracer(),
    private val fireEngine: FireSpacedRepetitionEngine = FireSpacedRepetitionEngine(),
    private val misconceptionAnalyzer: DiagnosticMisconceptionAnalyzer = DiagnosticMisconceptionAnalyzer(),
    private val timeProvider: () -> Long = {
        try {
            SystemClock.elapsedRealtime()
        } catch (_: Throwable) {
            System.currentTimeMillis()
        }
    }
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var stepStartRealtimeMs: Long = timeProvider()

    private val lessonGenerator = LessonProblemGenerator(problemEngine)

    private companion object {
        const val CORRECT_HOLD_MS = 900L
        const val MAX_LATENCY_MS = 120_000L
    }

    /**
     * Gọi khi con chọn sai LẦN ĐẦU ở một bước (để làm báo cáo "dạng hay sai" cho phụ huynh).
     * Tham số: lessonId (null nếu luyện theo nhóm kiến thức), nodeId, đề bài, đáp án con chọn, đáp án đúng.
     */
    var onFirstWrongAnswer: ((String?, String, String, String, String) -> Unit)? = null

    /** Câu tiếp theo cho bài trong sách đang làm, hoặc null nếu đang luyện theo nhóm kiến thức. */
    private fun nextLessonProblem(index: Int): com.example.toanhoc.domain.model.AcademicProblem? {
        val state = _uiState.value
        val id = state.lessonId ?: return null
        val stage = state.lessonStage?.let { runCatching { LessonStage.valueOf(it) }.getOrNull() } ?: return null
        val (curriculum, lesson) = Curricula.findLesson(id) ?: return null
        return lessonGenerator.next(curriculum, lesson, stage, index)
    }

    /**
     * Bài tương tự cho gợi ý: cùng dạng (cùng khung đề khi bỏ các con số), khác số, khác đáp án.
     * Ưu tiên bộ sinh đề của bài trong sách; không có thì sinh theo nhóm kiến thức của câu.
     */
    private fun similarExample(problem: com.example.toanhoc.domain.model.AcademicProblem): com.example.toanhoc.domain.model.AcademicProblem? {
        val state = _uiState.value
        fun shape(t: String) = t.replace(Regex("[0-9]+([.,][0-9]+)?"), "#").replace(Regex("\\b[IVXLCDM]+\\b"), "R")
        val target = shape(problem.statementVi)
        val expected = problem.steps.firstOrNull()?.expectedAnswer
        var fallback: com.example.toanhoc.domain.model.AcademicProblem? = null
        repeat(40) {
            val c = runCatching {
                nextLessonProblem(state.totalAnsweredCount) ?: problemEngine.generateProblemForNode(
                    nodeId = problem.nodeId, grade = state.gradeLevel,
                    scaffoldingLevel = problem.scaffoldingLevel, academicTier = problem.academicTier
                )
            }.getOrNull() ?: return@repeat
            if (c.statementVi == problem.statementVi || c.steps.firstOrNull()?.expectedAnswer == expected) return@repeat
            if (shape(c.statementVi) == target) return c
            if (fallback == null && c.nodeId == problem.nodeId) fallback = c
        }
        return fallback
    }

    /** Bắt đầu Dạng 1 / Dạng 2 / Ôn tập của một bài trong sách. */
    fun startLessonSession(lessonId: String, stage: LessonStage) {
        val (curriculum, lesson) = Curricula.findLesson(lessonId) ?: return
        startSession(curriculum.grade, lesson.sources.first().nodeId)
        _uiState.update {
            it.copy(
                lessonId = lessonId,
                lessonStage = stage.name,
                currentProblem = lessonGenerator.next(curriculum, lesson, stage, 0)
            )
        }
    }

    fun startSession(grade: Int, targetNodeId: String? = null) {
        val gradeNodes = CurriculumSeedData.seedNodes.filter { it.grade == grade }
        val selectedNodeId = targetNodeId ?: gradeNodes.firstOrNull()?.nodeId ?: "G${grade}_01"

        val firstProblem = problemEngine.generateProblemForNode(
            nodeId = selectedNodeId,
            grade = grade,
            scaffoldingLevel = ScaffoldingLevel.FLUENCY_DRILL
        )

        _uiState.value = SessionUiState(
            gradeLevel = grade,
            currentPhase = SessionPhase.AUTOMATICITY,
            currentRoundLevel = RoundLevel.LEVEL_1_FOUNDATION,
            roundCorrectCount = 0,
            roundTotalCount = 0,
            elapsedSeconds = 0, // Count-up timer starting at 0
            currentProblem = firstProblem,
            currentStepIndex = 0,
            userInput = "",
            isCurrentStepError = false,
            isPaused = false,
            currentHintLevel = 0, wrongCount = 0, exampleProblem = null, theoryExample = null,
            consecutiveStuckCount = 0,
            totalAnsweredCount = 0,
            correctAnsweredCount = 0,
            isSessionFinished = false,
            isRoundPassed = false
        )

        stepStartRealtimeMs = timeProvider()
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!_uiState.value.isSessionFinished) {
                delay(1000L)

                if (!_uiState.value.isPaused) {
                    _uiState.update { state ->
                        state.copy(elapsedSeconds = state.elapsedSeconds + 1)
                    }

                    if (_uiState.value.elapsedSeconds % 10 == 0) {
                        saveSessionProgressToDb()
                    }
                }
            }
            saveSessionProgressToDb()
        }
    }

    fun onPauseToggle(paused: Boolean) {
        _uiState.update { it.copy(isPaused = paused) }
        saveSessionProgressToDb()
        if (!paused) {
            stepStartRealtimeMs = timeProvider()
        }
    }

    fun onSaveAndExitSession(onNavigateBack: () -> Unit) {
        saveSessionProgressToDb()
        timerJob?.cancel()
        onNavigateBack()
    }

    fun onToggleBottomSheet(open: Boolean) {
        val st = _uiState.value
        val problem = st.currentProblem
        // Lý thuyết luôn minh hoạ bằng một bài CÙNG DẠNG, KHÁC SỐ – không giải sẵn câu con đang làm
        val example = if (open && problem != null) st.exampleProblem ?: st.theoryExample ?: runCatching { similarExample(problem) }.getOrNull() else st.theoryExample
        _uiState.update { it.copy(isBottomSheetOpen = open, theoryExample = example) }
    }

    fun onStepBackOrUndo() {
        if (_uiState.value.isPaused) return
        _uiState.update { state ->
            if (state.currentStepIndex > 0) {
                state.copy(
                    currentStepIndex = state.currentStepIndex - 1,
                    userInput = "",
                    isCurrentStepError = false,
                    detectedBugTag = null,
                    socraticExplanationVi = null,
                    currentHintLevel = 0, wrongCount = 0, exampleProblem = null, theoryExample = null
                )
            } else {
                state.copy(
                    userInput = "",
                    isCurrentStepError = false,
                    detectedBugTag = null,
                    socraticExplanationVi = null,
                    currentHintLevel = 0, wrongCount = 0, exampleProblem = null, theoryExample = null
                )
            }
        }
    }

    fun onDontKnowOrSkipClicked() {
        val state = _uiState.value
        if (state.isPaused) return
        val problem = state.currentProblem ?: return
        val currentStep = problem.steps.getOrNull(state.currentStepIndex) ?: return

        when {
            state.currentPhase == SessionPhase.AUTOMATICITY && state.lessonId == null -> {
                val expected = currentStep.expectedAnswer
                _uiState.update {
                    it.copy(
                        userInput = expected,
                        socraticExplanationVi = "Đáp án đúng là $expected. Luyện tập thêm phản xạ nhẩm."
                    )
                }

                viewModelScope.launch {
                    delay(1200L)
                    advanceToNextProblem(isCorrect = false)
                }
            }

            state.currentHintLevel == 0 && state.examplesUsed < SessionUiState.MAX_EXAMPLES -> {
                // Gợi ý = một bài tương tự đã giải sẵn; con tự áp dụng vào câu của mình (vẫn tính đúng nếu làm đúng)
                val example = similarExample(problem)
                _uiState.update {
                    it.copy(
                        currentHintLevel = 1,
                        exampleProblem = example,
                        examplesUsed = it.examplesUsed + 1,
                        socraticExplanationVi = if (example == null) "Gợi ý: ${currentStep.promptVi}" else null
                    )
                }
            }

            state.currentHintLevel <= 1 -> {
                _uiState.update {
                    it.copy(
                        currentHintLevel = 2,
                        userInput = currentStep.expectedAnswer,
                        socraticExplanationVi = "Lời giải chi tiết: ${currentStep.promptVi} → ${currentStep.mathExpression} = ${currentStep.expectedAnswer}."
                    )
                }
            }

            else -> {
                advanceToNextProblem(isCorrect = false, isAssistedSkip = true)
            }
        }
    }

    /** Toán tư duy: hiện lời giải ngay (khóa chọn đáp án), chưa sang câu mới. */
    fun revealSolution() {
        val state = _uiState.value
        if (state.isPaused || state.currentHintLevel >= 2 || state.feedbackCorrect) return
        val step = state.currentProblem?.steps?.getOrNull(state.currentStepIndex) ?: return
        _uiState.update { it.copy(currentHintLevel = 2, userInput = step.expectedAnswer) }
    }

    /** Toán tư duy: xem phân tích / làm câu sinh đôi xong → sang câu tiếp (câu này tính là chưa đúng lần đầu). */
    fun continueAfterReveal() {
        val state = _uiState.value
        if (state.isPaused || state.isSessionFinished || state.currentProblem == null) return
        advanceToNextProblem(isCorrect = false, isAssistedSkip = true)
        stepStartRealtimeMs = timeProvider()
    }

    fun submitOptionChoice(optionText: String) {
        if (_uiState.value.isPaused || _uiState.value.isSessionFinished || _uiState.value.feedbackCorrect) return
        val latencyMs = timeProvider() - stepStartRealtimeMs
        val state = _uiState.value
        val problem = state.currentProblem ?: return
        val currentStep = problem.steps.getOrNull(state.currentStepIndex) ?: return

        val input = optionText.trim()
        val expected = currentStep.expectedAnswer.trim()
        val isCorrect = input.equals(expected, ignoreCase = true)

        _uiState.update { it.copy(userInput = input) }

        if (isCorrect) {
            val isFirstTryCorrect = !state.isCurrentStepError && state.currentHintLevel <= 1
            val nextTotal = state.totalAnsweredCount + 1
            val nextCorrect = if (isFirstTryCorrect) state.correctAnsweredCount + 1 else state.correctAnsweredCount
            val newAvgLatency = ((state.averageLatencyMs * state.totalAnsweredCount) + latencyMs) / nextTotal

            if (state.currentStepIndex + 1 < problem.steps.size) {
                _uiState.update {
                    it.copy(
                        currentStepIndex = it.currentStepIndex + 1,
                        userInput = "",
                        isCurrentStepError = false,
                        detectedBugTag = null,
                        socraticExplanationVi = null,
                        currentHintLevel = 0, wrongCount = 0, exampleProblem = null, theoryExample = null,
                        consecutiveStuckCount = 0,
                        totalAnsweredCount = nextTotal,
                        correctAnsweredCount = nextCorrect,
                        averageLatencyMs = newAvgLatency
                    )
                }
            } else {
                // Hiện "Chính xác!" một nhịp rồi mới sang câu mới; trong lúc này không nhận thêm lượt bấm
                _uiState.update { it.copy(feedbackCorrect = true) }
                viewModelScope.launch {
                    delay(CORRECT_HOLD_MS)
                    _uiState.update { it.copy(feedbackCorrect = false) }
                    advanceToNextProblem(isCorrect = isFirstTryCorrect, measuredLatencyMs = latencyMs)
                    stepStartRealtimeMs = timeProvider()
                }
                return
            }
            stepStartRealtimeMs = timeProvider()
        } else {
            if (!state.isCurrentStepError) {
                runCatching { onFirstWrongAnswer?.invoke(state.lessonId, problem.nodeId, problem.statementVi, input, expected) }
            }
            val analysis = misconceptionAnalyzer.analyzeAnswer(
                studentInput = input,
                correctAnswer = expected,
                misconceptionTraps = currentStep.bugRules
            )

            viewModelScope.launch {
                repository?.recordAttemptResult(
                    nodeId = problem.nodeId,
                    isCorrect = false,
                    responseLatencyMs = latencyMs,
                    bugTag = analysis.bugTag?.tagCode
                )
            }

            _uiState.update {
                it.copy(
                    isCurrentStepError = true,
                    wrongCount = it.wrongCount + 1,
                    detectedBugTag = analysis.bugTag,
                    socraticExplanationVi = analysis.socraticExplanationVi
                )
            }
        }

        saveSessionProgressToDb()
    }

    /**
     * [measuredLatencyMs]: thời gian con làm câu này, đo ngay lúc trả lời (trước nhịp hiện "Chính xác!").
     * null = tự đo từ lúc hiện câu.
     */
    private fun advanceToNextProblem(isCorrect: Boolean, isAssistedSkip: Boolean = false, measuredLatencyMs: Long? = null) {
        val state = _uiState.value
        val problem = state.currentProblem ?: return
        val latencyMs = measuredLatencyMs ?: (timeProvider() - stepStartRealtimeMs)

        val newTotalAnswered = state.totalAnsweredCount + 1
        // Thời gian trung bình mỗi câu (bỏ qua lúc con để máy quá lâu: tính tối đa 2 phút/câu)
        val newAvgLatency = ((state.averageLatencyMs * state.totalAnsweredCount) + latencyMs.coerceIn(0L, MAX_LATENCY_MS)) / newTotalAnswered
        val isFirstTry = isCorrect && !isAssistedSkip && !state.isCurrentStepError && state.currentHintLevel <= 1
        val newCorrectCount = if (isFirstTry) state.correctAnsweredCount + 1 else state.correctAnsweredCount

        // Check if 12 questions complete for this round
        if (newTotalAnswered >= 12) {
            val accuracy = (newCorrectCount * 100) / 12
            val isRoundPassed = accuracy >= 80

            // Record BKT attempt and mastery status ONLY IF 12 QUESTIONS ARE COMPLETED
            viewModelScope.launch {
                repository?.recordAttemptResult(
                    nodeId = problem.nodeId,
                    isCorrect = isRoundPassed,
                    responseLatencyMs = latencyMs
                )

                repository?.getStudentProfile()?.let { profile ->
                    val updatedProfile = profile.copy(
                        totalFocusMinutesLast7Days = profile.totalFocusMinutesLast7Days + ((state.elapsedSeconds / 60).coerceAtLeast(1))
                    )
                    repository.saveStudentProfile(updatedProfile)
                }
            }

            _uiState.update {
                it.copy(
                    totalAnsweredCount = 12,
                    correctAnsweredCount = newCorrectCount,
                    averageLatencyMs = newAvgLatency,
                    isSessionFinished = true,
                    isRoundPassed = isRoundPassed,
                    currentPhase = SessionPhase.SUMMARY
                )
            }
            saveSessionProgressToDb()
            return
        }

        // Record attempt progress for question < 12 without unlocking mastery yet
        viewModelScope.launch {
            repository?.recordAttemptResult(
                nodeId = problem.nodeId,
                isCorrect = if (isAssistedSkip) false else isCorrect,
                responseLatencyMs = latencyMs
            )
        }

        val newStuckCount = if (isAssistedSkip || !isCorrect) state.consecutiveStuckCount + 1 else 0

        val nextScaffolding = when {
            newStuckCount >= 2 -> ScaffoldingLevel.WORKED_EXAMPLE
            state.currentPhase == SessionPhase.AUTOMATICITY -> ScaffoldingLevel.FLUENCY_DRILL
            state.currentPhase == SessionPhase.WORKED_EXAMPLE -> if (isCorrect) ScaffoldingLevel.FADED_STEP else ScaffoldingLevel.WORKED_EXAMPLE
            else -> ScaffoldingLevel.INDEPENDENT
        }

        val nextProblem = nextLessonProblem(newTotalAnswered) ?: problemEngine.generateProblemForNode(
            nodeId = problem.nodeId,
            grade = state.gradeLevel,
            scaffoldingLevel = nextScaffolding
        )

        _uiState.update {
            it.copy(
                currentProblem = nextProblem,
                currentStepIndex = 0,
                userInput = "",
                isCurrentStepError = false,
                detectedBugTag = null,
                socraticExplanationVi = null,
                currentHintLevel = 0, wrongCount = 0, exampleProblem = null, theoryExample = null,
                consecutiveStuckCount = newStuckCount,
                totalAnsweredCount = newTotalAnswered,
                correctAnsweredCount = newCorrectCount,
                averageLatencyMs = newAvgLatency
            )
        }

        stepStartRealtimeMs = timeProvider()
        saveSessionProgressToDb()
    }

    fun onKeyPress(key: String) {
        if (_uiState.value.isPaused || _uiState.value.isSessionFinished) return
        _uiState.update { it.copy(userInput = it.userInput + key, isCurrentStepError = false) }
    }

    fun onBackspace() {
        if (_uiState.value.isPaused || _uiState.value.isSessionFinished) return
        _uiState.update {
            if (it.userInput.isNotEmpty()) {
                it.copy(userInput = it.userInput.dropLast(1), isCurrentStepError = false)
            } else it
        }
    }

    fun submitStepAnswer() {
        if (_uiState.value.isPaused || _uiState.value.isSessionFinished) return
        val latencyMs = timeProvider() - stepStartRealtimeMs
        val state = _uiState.value
        val problem = state.currentProblem ?: return
        val currentStep = problem.steps.getOrNull(state.currentStepIndex) ?: return

        val input = state.userInput.trim()
        val expected = currentStep.expectedAnswer.trim()
        val isCorrect = input.equals(expected, ignoreCase = true)

        if (isCorrect) {
            val nextTotal = state.totalAnsweredCount + 1
            val nextCorrect = state.correctAnsweredCount + 1
            val newAvgLatency = ((state.averageLatencyMs * state.totalAnsweredCount) + latencyMs) / nextTotal

            if (state.currentStepIndex + 1 < problem.steps.size) {
                _uiState.update {
                    it.copy(
                        currentStepIndex = it.currentStepIndex + 1,
                        userInput = "",
                        isCurrentStepError = false,
                        detectedBugTag = null,
                        socraticExplanationVi = null,
                        currentHintLevel = 0, wrongCount = 0, exampleProblem = null, theoryExample = null,
                        consecutiveStuckCount = 0,
                        totalAnsweredCount = nextTotal,
                        correctAnsweredCount = nextCorrect,
                        averageLatencyMs = newAvgLatency
                    )
                }
            } else {
                advanceToNextProblem(isCorrect = true, measuredLatencyMs = latencyMs)
            }
            stepStartRealtimeMs = timeProvider()
        } else {
            val analysis = misconceptionAnalyzer.analyzeAnswer(
                studentInput = input,
                correctAnswer = expected,
                misconceptionTraps = currentStep.bugRules
            )

            viewModelScope.launch {
                repository?.recordAttemptResult(
                    nodeId = problem.nodeId,
                    isCorrect = false,
                    responseLatencyMs = latencyMs,
                    bugTag = analysis.bugTag?.tagCode
                )
            }

            _uiState.update {
                it.copy(
                    isCurrentStepError = true,
                    detectedBugTag = analysis.bugTag,
                    socraticExplanationVi = analysis.socraticExplanationVi
                )
            }
        }

        saveSessionProgressToDb()
    }

    private fun saveSessionProgressToDb() {
        val state = _uiState.value
        val completedSec = state.elapsedSeconds.coerceIn(0, 7200)
        val isDone = state.totalAnsweredCount >= 12

        viewModelScope.launch {
            repository?.saveDailySession(
                DailySessionLogEntity(
                    dateIso = LocalDate.now().toString(),
                    completedSeconds = completedSec,
                    phase1Accuracy = if (state.totalAnsweredCount > 0) (state.correctAnsweredCount.toFloat() / state.totalAnsweredCount) else 0f,
                    phase1AvgLatencyMs = state.averageLatencyMs,
                    phase2TargetNodeId = state.currentProblem?.nodeId ?: "",
                    isCompleted = isDone
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveSessionProgressToDb()
        timerJob?.cancel()
    }
}
