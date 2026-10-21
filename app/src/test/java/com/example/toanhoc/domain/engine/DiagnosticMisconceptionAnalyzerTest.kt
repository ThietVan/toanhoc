package com.example.toanhoc.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DiagnosticMisconceptionAnalyzerTest {

    private val analyzer = DiagnosticMisconceptionAnalyzer()

    @Test
    fun `correct answer returns null bug tag`() {
        val result = analyzer.analyzeAnswer(
            studentInput = "12",
            correctAnswer = "12"
        )

        assertNull(result.bugTag)
        assertEquals("Đáp án chính xác.", result.socraticExplanationVi)
    }

    @Test
    fun `matching explicit trap returns correct cognitive bug tag`() {
        val traps = mapOf(
            "14" to CognitiveBugTag.ERR_ORDER_OF_OPERATIONS
        )

        val result = analyzer.analyzeAnswer(
            studentInput = "14",
            correctAnswer = "10",
            misconceptionTraps = traps
        )

        assertEquals(CognitiveBugTag.ERR_ORDER_OF_OPERATIONS, result.bugTag)
        assertNotNull(result.socraticExplanationVi)
    }

    @Test
    fun `fraction pattern input detects fraction addition misconception`() {
        val result = analyzer.analyzeAnswer(
            studentInput = "4/7",
            correctAnswer = "5/12"
        )

        assertEquals(CognitiveBugTag.ERR_FRACTION_ADD_BOTH_NUM_DEN, result.bugTag)
    }
}
