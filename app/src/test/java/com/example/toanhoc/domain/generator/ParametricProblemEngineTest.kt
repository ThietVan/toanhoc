package com.example.toanhoc.domain.generator

import com.example.toanhoc.data.seed.CurriculumSeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParametricProblemEngineTest {

    private val engine = ParametricProblemEngine(randomSeed = 42L)

    @Test
    fun `generates valid academic problem for Grade 1`() {
        val problem = engine.generateProblemForNode(nodeId = "G1_01_SPATIAL_SHAPES", grade = 1)
        assertEquals("G1_01_SPATIAL_SHAPES", problem.nodeId)
        assertTrue(problem.statementVi.isNotEmpty())
        assertTrue(problem.steps.isNotEmpty())
    }

    @Test
    fun `generates valid academic problem for Grade 4 fraction node`() {
        val problem = engine.generateProblemForNode(nodeId = "G4_07_FRACTIONS_FOUR_OPS", grade = 4)
        assertEquals("G4_07_FRACTIONS_FOUR_OPS", problem.nodeId)
        assertTrue(problem.steps.isNotEmpty())
    }

    @Test
    fun `generates valid academic problem for Grade 5 speed motion node`() {
        val problem = engine.generateProblemForNode(nodeId = "G5_08_TIME_OPS_AND_MOTION", grade = 5)
        assertEquals("G5_08_TIME_OPS_AND_MOTION", problem.nodeId)
        assertTrue(problem.blueprintSpec != null)
    }

    @Test
    fun `test G4_10_BAR_CHART_PROBABILITY option generation`() {
        val problem = engine.generateProblemForNode(nodeId = "G4_10_BAR_CHART_PROBABILITY", grade = 4)
        println("Generated options for G4_10: ${problem.steps.first().options}")
        assertEquals(4, problem.steps.first().options.size)
    }

    @Test
    fun `generates valid problem for all 50 core nodes in seed data`() {
        val nodes = CurriculumSeedData.seedNodes
        assertEquals(50, nodes.size)

        var failedCount = 0
        for (node in nodes) {
            val problem = engine.generateProblemForNode(nodeId = node.nodeId, grade = node.grade)
            for ((idx, step) in problem.steps.withIndex()) {
                if (step.options.size != 4) {
                    println("FAILED NODE: ${node.nodeId} step $idx options count: ${step.options.size}, options: ${step.options}")
                    failedCount++
                }
            }
        }
        assertEquals("All nodes must have 4 options", 0, failedCount)
    }
}

