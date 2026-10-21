package com.example.toanhoc

import com.example.toanhoc.domain.engine.FireScheduler
import org.junit.Assert.assertTrue
import org.junit.Test

class FireSchedulerTest {

    private val scheduler = FireScheduler()

    @Test
    fun `incorrect response yields higher priority than correct response`() {
        val priorityIncorrect = scheduler.calculateRetrievalPriority(
            kcMastery = 0.5,
            isLastCorrect = false,
            responseLatencyMs = 3000L
        )

        val priorityCorrect = scheduler.calculateRetrievalPriority(
            kcMastery = 0.5,
            isLastCorrect = true,
            responseLatencyMs = 3000L
        )

        assertTrue(priorityIncorrect > priorityCorrect)
    }
}
