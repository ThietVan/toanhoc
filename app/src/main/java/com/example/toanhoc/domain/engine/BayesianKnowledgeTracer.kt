package com.example.toanhoc.domain.engine

import kotlin.math.exp
import kotlin.math.ln

/**
 * Bayesian Knowledge Tracer (BKT) combined with Item Response Theory (IRT 2PL) engine.
 * Computes posterior mastery P(L_t) and selects item difficulty within Zone of Proximal Development (ZPD).
 */
class BayesianKnowledgeTracer {

    /**
     * Updates knowledge mastery probability P(L_t) according to standard BKT formulation.
     */
    fun updateMastery(
        pKnowPrev: Float,
        isCorrect: Boolean,
        pTransit: Float = 0.20f,
        pSlip: Float = 0.08f,
        pGuess: Float = 0.05f,
        responseLatencyMs: Long = 2000L,
        maxLatencyMs: Long = 5000L
    ): Float {
        val lPrev = pKnowPrev.coerceIn(0.01f, 0.99f)
        val s = pSlip.coerceIn(0.01f, 0.50f)
        val g = pGuess.coerceIn(0.01f, 0.50f)
        val t = pTransit.coerceIn(0.01f, 0.50f)

        // Effective correctness factors in whether student stayed within latency bound
        val isEffectiveCorrect = isCorrect && (responseLatencyMs <= maxLatencyMs)

        val pLearnedGivenObs = if (isEffectiveCorrect) {
            val pObsCorrect = (lPrev * (1.0f - s)) + ((1.0f - lPrev) * g)
            (lPrev * (1.0f - s)) / pObsCorrect
        } else {
            val pObsWrong = (lPrev * s) + ((1.0f - lPrev) * (1.0f - g))
            (lPrev * s) / pObsWrong
        }

        val lNew = pLearnedGivenObs + ((1.0f - pLearnedGivenObs) * t)
        return lNew.coerceIn(0.0f, 1.0f)
    }

    /**
     * IRT 2-Parameter Logistic (2PL) Item Response Function.
     * Calculates probability of correct response P(theta, a, b).
     * @param theta Student ability level
     * @param a Item discrimination parameter
     * @param b Item difficulty parameter
     */
    fun calculateIrtSuccessProbability(theta: Float, a: Float, b: Float): Float {
        val exponent = (-a * (theta - b)).coerceIn(-20.0f, 20.0f)
        return (1.0f / (1.0f + exp(exponent)))
    }

    /**
     * Estimates current student ability theta [-3.0, +3.0] based on mastery probability P(L).
     */
    fun estimateStudentTheta(pKnow: Float): Float {
        val boundedPKnow = pKnow.coerceIn(0.05f, 0.95f)
        // Logit transformation mapping P(L) to theta scale [-3.0, +3.0]
        return (ln(boundedPKnow / (1.0f - boundedPKnow))).coerceIn(-3.0f, 3.0f)
    }

    /**
     * Selects target item difficulty 'b' to place the question in Zone of Proximal Development (ZPD 0.70 to 0.75).
     */
    fun selectTargetDifficulty(
        theta: Float,
        a: Float = 1.0f,
        targetZpdSuccessRate: Float = 0.72f
    ): Float {
        val pTarget = targetZpdSuccessRate.coerceIn(0.70f, 0.75f)
        val logitTarget = ln(pTarget / (1.0f - pTarget))
        val targetB = theta - (logitTarget / a.coerceAtLeast(0.1f))
        return targetB.coerceIn(-3.0f, 3.0f)
    }
}
