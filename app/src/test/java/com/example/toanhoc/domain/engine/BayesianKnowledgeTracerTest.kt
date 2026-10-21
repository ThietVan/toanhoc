package com.example.toanhoc.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BayesianKnowledgeTracerTest {

    private val tracer = BayesianKnowledgeTracer()

    @Test
    fun `bkt update increases mastery on correct fast response`() {
        val priorL = 0.30f
        val newL = tracer.updateMastery(
            pKnowPrev = priorL,
            isCorrect = true,
            responseLatencyMs = 1500L
        )
        assertTrue("New mastery ($newL) should be greater than prior ($priorL)", newL > priorL)
    }

    @Test
    fun `bkt update decreases mastery on incorrect response`() {
        val priorL = 0.80f
        val newL = tracer.updateMastery(
            pKnowPrev = priorL,
            isCorrect = false,
            responseLatencyMs = 2000L
        )
        assertTrue("New mastery ($newL) should be less than prior ($priorL)", newL < priorL)
    }

    @Test
    fun `irt 2pl probability calculation is valid`() {
        val probEqual = tracer.calculateIrtSuccessProbability(theta = 0.0f, a = 1.0f, b = 0.0f)
        assertEquals(0.50f, probEqual, 0.01f)

        val probHigherAbility = tracer.calculateIrtSuccessProbability(theta = 1.0f, a = 1.0f, b = 0.0f)
        assertTrue(probHigherAbility > 0.50f)
    }

    @Test
    fun `target difficulty selection aligns with ZPD range 0_70 to 0_75`() {
        val theta = 0.5f
        val bTarget = tracer.selectTargetDifficulty(theta = theta, a = 1.0f, targetZpdSuccessRate = 0.72f)
        val pAchieved = tracer.calculateIrtSuccessProbability(theta = theta, a = 1.0f, b = bTarget)

        assertEquals(0.72f, pAchieved, 0.02f)
    }
}
