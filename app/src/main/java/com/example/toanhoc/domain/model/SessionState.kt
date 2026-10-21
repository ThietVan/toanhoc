package com.example.toanhoc.domain.model

enum class SessionPhase(val displayName: String, val durationSeconds: Int) {
    AUTOMATICITY("Pha 1: Automaticity (Phản xạ tính nhẩm)", 180),
    WORKED_EXAMPLE("Pha 2: Worked Example & Scaffolding", 480),
    FIRE_RETRIEVAL("Pha 3: FIRe Spaced Retrieval", 240),
    SUMMARY("Tổng kết phiên học", 0)
}

data class SessionState(
    val gradeLevel: Int = 1,
    val currentPhase: SessionPhase = SessionPhase.AUTOMATICITY,
    val totalSecondsRemaining: Int = 900,
    val phaseSecondsRemaining: Int = 180,
    val currentProblem: AcademicProblem? = null,
    val userInput: String = "",
    val isAnswerEvaluated: Boolean = false,
    val isLastAnswerCorrect: Boolean = false,
    val totalAnswered: Int = 0,
    val correctCount: Int = 0,
    val averageLatencyMs: Long = 0L,
    val kcMasteryMap: Map<String, Double> = emptyMap()
)
