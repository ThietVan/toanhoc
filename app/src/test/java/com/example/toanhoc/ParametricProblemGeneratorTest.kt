package com.example.toanhoc

import com.example.toanhoc.domain.engine.ParametricProblemGenerator
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ParametricProblemGeneratorTest {

    private val generator = ParametricProblemGenerator()

    @Test
    fun `generates valid problem for each grade 1 to 5`() {
        for (grade in 1..5) {
            val problem = generator.generateProblem(grade)
            assertNotNull(problem.problemId)
            assertNotNull(problem.nodeId)
            assertTrue(problem.statementVi.isNotEmpty())
            assertTrue(problem.steps.isNotEmpty())
        }
    }
}
