package com.example.toanhoc.domain.engine

import com.example.toanhoc.data.local.entity.PrerequisiteEdgeEntity
import com.example.toanhoc.data.local.entity.StudentMasteryEntity

/**
 * Fractional Implicit Repetition (FIRe) Spaced Repetition Engine operating on DAG.
 * Handles exponential stability scaling, backward credit propagation, and root weakness discovery.
 */
class FireSpacedRepetitionEngine {

    data class RepetitionUpdateResult(
        val updatedMasteries: Map<String, StudentMasteryEntity>,
        val rootWeaknessNodeId: String? = null
    )

    /**
     * Processes student response and propagates memory stability changes across DAG edges.
     */
    fun processResponse(
        nodeId: String,
        isCorrect: Boolean,
        responseLatencyMs: Long,
        intrinsicDifficulty: Float,
        allMasteries: Map<String, StudentMasteryEntity>,
        prerequisiteEdges: List<PrerequisiteEdgeEntity>,
        currentTimeMs: Long = System.currentTimeMillis()
    ): RepetitionUpdateResult {
        val mutableMasteries = allMasteries.toMutableMap()
        val currentMastery = mutableMasteries[nodeId] ?: StudentMasteryEntity(nodeId = nodeId)

        if (isCorrect) {
            // Exponential stability increase based on difficulty and speed
            val speedFactor = (2500.0f / responseLatencyMs.coerceAtLeast(500L).toFloat()).coerceIn(0.5f, 2.0f)
            val difficultyFactor = (intrinsicDifficulty + 3.0f) / 3.0f
            val growthMultiplier = 1.0f + (1.2f * speedFactor * difficultyFactor)

            val newStability = (currentMastery.memoryStabilityDays * growthMultiplier).coerceIn(1.0f, 365.0f)
            val nextReview = currentTimeMs + (newStability * 86_400_000L).toLong()

            mutableMasteries[nodeId] = currentMastery.copy(
                memoryStabilityDays = newStability,
                lastPracticedEpochMs = currentTimeMs,
                nextReviewEpochMs = nextReview
            )

            // Propagate implicit credit upward to parent prerequisite nodes
            propagateImplicitCreditUpward(
                childNodeId = nodeId,
                parentEdges = prerequisiteEdges,
                masteries = mutableMasteries,
                currentTimeMs = currentTimeMs
            )

            return RepetitionUpdateResult(updatedMasteries = mutableMasteries)
        } else {
            // Decrease stability on error
            val newStability = (currentMastery.memoryStabilityDays * 0.5f).coerceAtLeast(1.0f)
            val nextReview = currentTimeMs + (newStability * 86_400_000L).toLong()

            mutableMasteries[nodeId] = currentMastery.copy(
                memoryStabilityDays = newStability,
                lastPracticedEpochMs = currentTimeMs,
                nextReviewEpochMs = nextReview
            )

            // Diagnostic Root Weakness Traversal
            val rootWeakness = findRootWeakness(nodeId, prerequisiteEdges, mutableMasteries)

            return RepetitionUpdateResult(
                updatedMasteries = mutableMasteries,
                rootWeaknessNodeId = rootWeakness
            )
        }
    }

    /**
     * Recursively propagates implicit credit to parent nodes to prevent redundant drills.
     */
    private fun propagateImplicitCreditUpward(
        childNodeId: String,
        parentEdges: List<PrerequisiteEdgeEntity>,
        masteries: MutableMap<String, StudentMasteryEntity>,
        currentTimeMs: Long
    ) {
        val incomingEdges = parentEdges.filter { it.childNodeId == childNodeId }
        for (edge in incomingEdges) {
            val parentId = edge.parentNodeId
            val parentMastery = masteries[parentId] ?: StudentMasteryEntity(nodeId = parentId)

            val weight = edge.implicitCreditWeight.coerceIn(0.1f, 0.8f)
            val creditDays = parentMastery.memoryStabilityDays * (1.0f + (0.5f * weight))
            val newParentStability = creditDays.coerceIn(1.0f, 365.0f)
            val newParentNextReview = currentTimeMs + (newParentStability * 86_400_000L).toLong()

            masteries[parentId] = parentMastery.copy(
                memoryStabilityDays = newParentStability,
                nextReviewEpochMs = newParentNextReview
            )

            // Recurse up the DAG
            propagateImplicitCreditUpward(parentId, parentEdges, masteries, currentTimeMs)
        }
    }

    /**
     * Recursively backtracks DAG prerequisites to find the deepest unmastered root cause node.
     */
    fun findRootWeakness(
        nodeId: String,
        parentEdges: List<PrerequisiteEdgeEntity>,
        masteries: Map<String, StudentMasteryEntity>
    ): String {
        val directParents = parentEdges.filter { it.childNodeId == nodeId }.map { it.parentNodeId }

        for (parentId in directParents) {
            val parentMastery = masteries[parentId]
            if (parentMastery == null || !parentMastery.isMastered || parentMastery.pKnow < 0.60f) {
                // Parent is weak, search deeper into grand-parents
                return findRootWeakness(parentId, parentEdges, masteries)
            }
        }

        // If all parents are mastered or no parents exist, the weakness is at current node
        return nodeId
    }
}
