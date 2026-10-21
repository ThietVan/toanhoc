package com.example.toanhoc.domain.usecase

import com.example.toanhoc.domain.engine.ParametricProblemGenerator
import com.example.toanhoc.domain.model.ScaffoldingLevel
import com.example.toanhoc.domain.model.SessionPhase
import com.example.toanhoc.domain.model.SessionState

/**
 * UseCase initiating a 15-minute (900-second) deliberate practice session for a given Grade.
 */
class StartSessionUseCase(
    private val generator: ParametricProblemGenerator = ParametricProblemGenerator()
) {
    operator fun invoke(grade: Int): SessionState {
        val initialProblem = generator.generateProblem(grade = grade, level = ScaffoldingLevel.INDEPENDENT)
        return SessionState(
            gradeLevel = grade,
            currentPhase = SessionPhase.AUTOMATICITY,
            totalSecondsRemaining = 900,
            phaseSecondsRemaining = 180,
            currentProblem = initialProblem,
            userInput = "",
            isAnswerEvaluated = false,
            isLastAnswerCorrect = false,
            totalAnswered = 0,
            correctCount = 0,
            averageLatencyMs = 0L
        )
    }
}
