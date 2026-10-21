package com.example.toanhoc.ui.session

import com.example.toanhoc.domain.engine.CognitiveBugTag
import com.example.toanhoc.domain.model.AcademicProblem
import com.example.toanhoc.domain.model.RoundLevel
import com.example.toanhoc.domain.model.SessionPhase

/**
 * UI State representation for Mastery-Based Stage Round Practice with Upward Stopwatch Timer.
 */
data class SessionUiState(
    val gradeLevel: Int = 1,
    val currentPhase: SessionPhase = SessionPhase.AUTOMATICITY,
    val currentRoundLevel: RoundLevel = RoundLevel.LEVEL_1_FOUNDATION,
    val roundCorrectCount: Int = 0,
    val roundTotalCount: Int = 0,
    val elapsedSeconds: Int = 0, // Count-up timer (00:00 -> 00:01 -> ...)
    val currentProblem: AcademicProblem? = null,
    val currentStepIndex: Int = 0,
    val userInput: String = "",
    val isCurrentStepError: Boolean = false,
    val isPaused: Boolean = false,
    val isBottomSheetOpen: Boolean = false,
    val currentHintLevel: Int = 0, // 0 = None, 1 = Hint, 2 = Worked Explanation
    val consecutiveStuckCount: Int = 0,
    val detectedBugTag: CognitiveBugTag? = null,
    val socraticExplanationVi: String? = null,
    val totalAnsweredCount: Int = 0,
    val correctAnsweredCount: Int = 0,
    val averageLatencyMs: Long = 0L,
    val isSessionFinished: Boolean = false,
    val isRoundPassed: Boolean = false,
    /** Đang làm một bước của bài trong sách (null = luyện theo nhóm kiến thức như cũ). */
    val lessonId: String? = null,
    val lessonStage: String? = null,
    /** Vừa chọn đúng: giữ ô xanh ~0,9 giây trước khi sang câu mới (chống bấm vội sang câu sau). */
    val feedbackCorrect: Boolean = false,
    /** Số lần chọn sai ở bước hiện tại (lần 1: chỉ nhắc; từ lần 2 mới lộ đáp án + lời giải). */
    val wrongCount: Int = 0,
    /** Gợi ý = một bài tương tự (cùng dạng, khác số) có lời giải từng bước. */
    val exampleProblem: com.example.toanhoc.domain.model.AcademicProblem? = null,
    /** Số lần đã xem ví dụ trong lượt (tối đa [MAX_EXAMPLES]). */
    val examplesUsed: Int = 0,
    /** Ví dụ cùng dạng dùng cho nút Lý thuyết (sinh khi mở, giữ nguyên trong câu hiện tại). */
    val theoryExample: com.example.toanhoc.domain.model.AcademicProblem? = null
) {
    companion object { const val MAX_EXAMPLES = 3 }
}
