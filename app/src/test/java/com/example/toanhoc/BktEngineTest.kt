package com.example.toanhoc

import com.example.toanhoc.domain.engine.BktEngine
import org.junit.Assert.assertTrue
import org.junit.Test

class BktEngineTest {

    private val engine = BktEngine()

    @Test
    fun `correct answer increases mastery probability`() {
        val prior = 0.30
        val posterior = engine.updateMastery(priorL = prior, isCorrect = true)
        assertTrue("Posterior ($posterior) should be greater than prior ($prior)", posterior > prior)
    }

    @Test
    fun `incorrect answer decreases mastery probability`() {
        val prior = 0.70
        val posterior = engine.updateMastery(priorL = prior, isCorrect = false)
        assertTrue("Posterior ($posterior) should be less than prior ($prior)", posterior < prior)
    }
}
