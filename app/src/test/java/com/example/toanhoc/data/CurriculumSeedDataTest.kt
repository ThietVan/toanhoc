package com.example.toanhoc.data

import com.example.toanhoc.data.seed.CurriculumSeedData
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumSeedDataTest {

    @Test
    fun `seed data contains nodes for all 5 grades`() {
        val nodes = CurriculumSeedData.seedNodes
        assertTrue(nodes.isNotEmpty())

        for (grade in 1..5) {
            val gradeNodes = nodes.filter { it.grade == grade }
            assertTrue("Grade $grade should have seed nodes", gradeNodes.isNotEmpty())
        }
    }

    @Test
    fun `seed edges form valid DAG relationships`() {
        val edges = CurriculumSeedData.seedEdges
        val nodeIds = CurriculumSeedData.seedNodes.map { it.nodeId }.toSet()

        assertTrue(edges.isNotEmpty())
        for (edge in edges) {
            assertTrue("Parent node ${edge.parentNodeId} must exist", nodeIds.contains(edge.parentNodeId))
            assertTrue("Child node ${edge.childNodeId} must exist", nodeIds.contains(edge.childNodeId))
        }
    }
}
