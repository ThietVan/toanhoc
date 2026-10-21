package com.example.toanhoc.domain.usecase

import com.example.toanhoc.domain.engine.BktEngine
import com.example.toanhoc.domain.engine.ParametricProblemGenerator
import com.example.toanhoc.domain.model.ScaffoldingLevel
import com.example.toanhoc.domain.model.SessionPhase
import com.example.toanhoc.domain.model.SessionState

/**
 * UseCase for evaluating student input, updating psychometrics via BKT, and serving next problem.
 */
class SubmitAnswerUseCase(
    private val bktEngine: BktEngine = BktEngine(),
    private val generator: ParametricProblemGenerator = ParametricProblemGenerator()
) {
    operator fun invoke(currentState: SessionState, responseLatencyMs: Long): SessionState {
        val problem = currentState.currentProblem ?: return currentState
        val lastStep = problem.steps.lastOrNull()
        val expectedAnswer = lastStep?.expectedAnswer ?: ""

        val cleanedInput = currentState.userInput.trim()
        val cleanedCorrect = expectedAnswer.trim()
        val isCorrect = cleanedInput.equals(cleanedCorrect, ignoreCase = true)

        // Update BKT Mastery for KC
        val currentKcMastery = currentState.kcMasteryMap[problem.nodeId] ?: 0.25
        val updatedMastery = bktEngine.updateMastery(currentKcMastery, isCorrect)
        val newMasteryMap = currentState.kcMasteryMap.toMutableMap().apply {
            put(problem.nodeId, updatedMastery)
        }

        // Calculate running average latency
        val newTotalAnswered = currentState.totalAnswered + 1
        val newCorrectCount = if (isCorrect) currentState.correctCount + 1 else currentState.correctCount
        val newAverageLatency = ((currentState.averageLatencyMs * currentState.totalAnswered) + responseLatencyMs) / newTotalAnswered

        // Select next problem scaffolding level based on active phase
        val nextScaffolding = when (currentState.currentPhase) {
            SessionPhase.AUTOMATICITY -> ScaffoldingLevel.FLUENCY_DRILL
            SessionPhase.WORKED_EXAMPLE -> if (isCorrect) ScaffoldingLevel.FADED_STEP else ScaffoldingLevel.WORKED_EXAMPLE
            SessionPhase.FIRE_RETRIEVAL -> ScaffoldingLevel.INDEPENDENT
            SessionPhase.SUMMARY -> ScaffoldingLevel.INDEPENDENT
        }

        val nextProblem = generator.generateProblem(grade = currentState.gradeLevel, level = nextScaffolding)

        return currentState.copy(
            userInput = "",
            isAnswerEvaluated = true,
            isLastAnswerCorrect = isCorrect,
            totalAnswered = newTotalAnswered,
            correctCount = newCorrectCount,
            averageLatencyMs = newAverageLatency,
            kcMasteryMap = newMasteryMap,
            currentProblem = nextProblem
        )
    }
}
