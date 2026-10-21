package com.example.toanhoc.domain.engine

import com.example.toanhoc.data.local.entity.PrerequisiteEdgeEntity
import com.example.toanhoc.data.local.entity.StudentMasteryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FireSpacedRepetitionEngineTest {

    private val engine = FireSpacedRepetitionEngine()

    @Test
    fun `correct response increases stability and propagates upward to parent`() {
        val parentId = "G1_NUM_01"
        val childId = "G1_NUM_02"

        val initialMasteries = mapOf(
            parentId to StudentMasteryEntity(nodeId = parentId, memoryStabilityDays = 2.0f),
            childId to StudentMasteryEntity(nodeId = childId, memoryStabilityDays = 1.0f)
        )

        val edges = listOf(
            PrerequisiteEdgeEntity(parentNodeId = parentId, childNodeId = childId, implicitCreditWeight = 0.5f)
        )

        val result = engine.processResponse(
            nodeId = childId,
            isCorrect = true,
            responseLatencyMs = 1200L,
            intrinsicDifficulty = 0.0f,
            allMasteries = initialMasteries,
            prerequisiteEdges = edges
        )

        val childUpdated = result.updatedMasteries[childId]
        val parentUpdated = result.updatedMasteries[parentId]

        assertNotNull(childUpdated)
        assertNotNull(parentUpdated)

        assertTrue(childUpdated!!.memoryStabilityDays > 1.0f)
        assertTrue(parentUpdated!!.memoryStabilityDays > 2.0f)
    }

    @Test
    fun `findRootWeakness identifies deepest unmastered parent node`() {
        val grandParentId = "G1_NUM_01"
        val parentId = "G1_NUM_02"
        val childId = "G1_NUM_03"

        val masteries = mapOf(
            grandParentId to StudentMasteryEntity(nodeId = grandParentId, isMastered = false, pKnow = 0.30f),
            parentId to StudentMasteryEntity(nodeId = parentId, isMastered = false, pKnow = 0.40f),
            childId to StudentMasteryEntity(nodeId = childId, isMastered = false, pKnow = 0.20f)
        )

        val edges = listOf(
            PrerequisiteEdgeEntity(parentNodeId = grandParentId, childNodeId = parentId, implicitCreditWeight = 0.5f),
            PrerequisiteEdgeEntity(parentNodeId = parentId, childNodeId = childId, implicitCreditWeight = 0.5f)
        )

        val rootCause = engine.findRootWeakness(nodeId = childId, parentEdges = edges, masteries = masteries)
        assertEquals(grandParentId, rootCause)
    }
}
