package com.example.toanhoc.domain.generator

import com.example.toanhoc.domain.model.AcademicTier
import com.example.toanhoc.domain.model.ScaffoldingLevel
import org.junit.Assert.assertTrue
import org.junit.Test

class MathematicalValidatorTest {

    private val engine = ParametricProblemEngine(randomSeed = 42L)
    private val validator = MathematicalValidator()

    @Test
    fun `mathematical validator approves all generated problems for grades 1 to 5`() {
        for (grade in 1..5) {
            for (i in 1..10) {
                val problem = engine.generateProblemForNode(
                    nodeId = "G${grade}_0${i}",
                    grade = grade,
                    scaffoldingLevel = ScaffoldingLevel.INDEPENDENT,
                    academicTier = AcademicTier.TRUNG_BINH
                )

                val result = validator.validateProblem(problem)
                assertTrue(
                    "Problem ${problem.problemId} for grade $grade failed validation: ${result.reason}",
                    result.isValid
                )
            }
        }
    }

    @Test
    fun `grade 1 no carry problems strictly satisfy no carry condition`() {
        for (i in 1..20) {
            val problem = engine.generateProblemForNode(
                nodeId = "G1_06_ADD_SUB_100_NO_CARRY",
                grade = 1
            )
            val result = validator.validateProblem(problem)
            assertTrue("G1_06 problem failed no-carry validation: ${result.reason}", result.isValid)
        }
    }

    @Test
    fun `grade 2 with carry problems strictly satisfy carry condition`() {
        for (i in 1..20) {
            val problem = engine.generateProblemForNode(
                nodeId = "G2_03_ADD_SUB_100_WITH_CARRY",
                grade = 2
            )
            val result = validator.validateProblem(problem)
            assertTrue("G2_03 problem failed carry validation: ${result.reason}", result.isValid)
        }
    }

    @Test
    fun `grade 3 remainder problems strictly satisfy remainder condition 1 to R less than B`() {
        for (i in 1..20) {
            val problem = engine.generateProblemForNode(
                nodeId = "G3_04_DIVISION_WITH_REMAINDER",
                grade = 3
            )
            val result = validator.validateProblem(problem)
            assertTrue("G3_04 problem failed remainder validation: ${result.reason}", result.isValid)
        }
    }
}
