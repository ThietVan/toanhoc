package com.example.toanhoc.data

import com.example.toanhoc.data.local.entity.KnowledgeNodeEntity
import com.example.toanhoc.data.local.entity.PrerequisiteEdgeEntity
import com.example.toanhoc.data.local.entity.StudentMasteryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class KnowledgeRepositoryTest {

    @Test
    fun `test DAG knowledge node entity values`() {
        val node = KnowledgeNodeEntity(
            nodeId = "G1_NUM_01",
            grade = 1,
            domainCategory = "ARITHMETIC",
            titleVi = "Cộng trong phạm vi 10",
            axiomSummaryVi = "Cộng hai số tự nhiên có tổng không vượt quá 10",
            intrinsicDifficulty = -2.0f,
            discrimination = 1.2f,
            generatorType = "GEN_G1_ADD_10"
        )

        assertEquals("G1_NUM_01", node.nodeId)
        assertEquals(1, node.grade)
        assertEquals("ARITHMETIC", node.domainCategory)
        assertEquals(-2.0f, node.intrinsicDifficulty, 0.001f)
    }

    @Test
    fun `test prerequisite edge implicit credit weight`() {
        val edge = PrerequisiteEdgeEntity(
            parentNodeId = "G1_NUM_01",
            childNodeId = "G1_NUM_02",
            implicitCreditWeight = 0.5f
        )

        assertEquals("G1_NUM_01", edge.parentNodeId)
        assertEquals("G1_NUM_02", edge.childNodeId)
        assertEquals(0.5f, edge.implicitCreditWeight, 0.001f)
    }

    @Test
    fun `test student mastery initial values`() {
        val mastery = StudentMasteryEntity(nodeId = "G1_NUM_01")
        assertEquals(0.15f, mastery.pKnow, 0.001f)
        assertFalse(mastery.isMastered)
    }
}
