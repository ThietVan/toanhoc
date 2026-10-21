package com.example.toanhoc.domain.model

import com.example.toanhoc.domain.generator.ParametricProblemEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerModeAndTierTest {

    private val engine = ParametricProblemEngine(randomSeed = 100L)

    @Test
    fun `academic tier enum parses correctly`() {
        val tier = AcademicTier.fromString("GIOI")
        assertEquals(AcademicTier.GIOI, tier)
        assertEquals("GIỎI", tier.titleVi)
    }

    @Test
    fun `problem generator respects AcademicTier and generates options for Multiple Choice`() {
        val problemG1 = engine.generateProblemForNode(
            nodeId = "G1_TEST",
            grade = 1,
            academicTier = AcademicTier.TRUNG_BINH
        )

        assertEquals(AcademicTier.TRUNG_BINH, problemG1.academicTier)
        assertTrue(problemG1.steps.isNotEmpty())

        val step = problemG1.steps.first()
        assertEquals(AnswerMode.MULTIPLE_CHOICE_4, step.answerMode)
        assertEquals(4, step.options.size)
        assertTrue(step.options.contains(step.expectedAnswer))
    }

    @Test
    fun `all grades 1 to 5 generate 100 percent multiple choice options`() {
        for (grade in 1..5) {
            val problem = engine.generateProblemForNode(
                nodeId = "G${grade}_MC_TEST",
                grade = grade,
                academicTier = AcademicTier.GIOI
            )

            val step = problem.steps.first()
            assertEquals(AnswerMode.MULTIPLE_CHOICE_4, step.answerMode)
            assertEquals(4, step.options.size)
            assertTrue(step.options.contains(step.expectedAnswer))
        }
    }
}
