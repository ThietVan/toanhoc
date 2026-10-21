package com.example.toanhoc.domain.usecase

import com.example.toanhoc.domain.model.SessionState

data class SessionProgressSummary(
    val accuracyPercentage: Int,
    val averageLatencyMs: Long,
    val isAutomaticityAchieved: Boolean,
    val totalAnswered: Int,
    val correctCount: Int,
    val averageMastery: Double
)

/**
 * UseCase computing final 15-minute practice session progress analytics.
 */
class GetSessionProgressUseCase {
    operator fun invoke(state: SessionState): SessionProgressSummary {
        val total = state.totalAnswered.coerceAtLeast(1)
        val accuracy = (state.correctCount * 100) / total
        val avgMastery = if (state.kcMasteryMap.isNotEmpty()) {
            state.kcMasteryMap.values.average()
        } else {
            0.5
        }

        return SessionProgressSummary(
            accuracyPercentage = accuracy,
            averageLatencyMs = state.averageLatencyMs,
            isAutomaticityAchieved = state.averageLatencyMs in 1..2500L && accuracy >= 80,
            totalAnswered = state.totalAnswered,
            correctCount = state.correctCount,
            averageMastery = avgMastery
        )
    }
}
