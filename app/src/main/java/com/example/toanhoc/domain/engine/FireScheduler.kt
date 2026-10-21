package com.example.toanhoc.domain.engine

import com.example.toanhoc.domain.model.KnowledgeComponent

/**
 * Fractional Implicit Repetition (FIRe) Spaced Retrieval Scheduler.
 * Ranks knowledge components for retrieval based on BKT mastery, error history, and response latency.
 */
class FireScheduler {

    /**
     * Calculates priority score for retrieval. Higher score = higher priority for Phase 3 review.
     */
    fun calculateRetrievalPriority(
        kcMastery: Double,
        isLastCorrect: Boolean,
        responseLatencyMs: Long,
        targetLatencyMs: Long = 2500L
    ): Double {
        val masteryPenalty = 1.0 - kcMastery
        val errorWeight = if (!isLastCorrect) 2.5 else 1.0
        val latencyRatio = (responseLatencyMs.toDouble() / targetLatencyMs.toDouble()).coerceIn(0.5, 3.0)

        return (masteryPenalty * 2.0) + (errorWeight * 1.5) + (latencyRatio * 1.0)
    }

    /**
     * Selects top knowledge components requiring immediate spaced retrieval.
     */
    fun selectRetrievalKcs(
        allKcs: List<KnowledgeComponent>,
        masteryMap: Map<String, Double>,
        latencyMap: Map<String, Long>
    ): List<KnowledgeComponent> {
        return allKcs.sortedByDescending { kc ->
            val mastery = masteryMap[kc.id] ?: 0.25
            val latency = latencyMap[kc.id] ?: 3000L
            calculateRetrievalPriority(kcMastery = mastery, isLastCorrect = true, responseLatencyMs = latency)
        }
    }
}
