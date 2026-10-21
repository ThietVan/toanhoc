package com.example.toanhoc.domain.engine

/**
 * Bayesian Knowledge Tracing (BKT) Psychometric Engine.
 * Calculates posterior knowledge probability P(L_t) after student response observation.
 */
class BktEngine(
    val pT: Double = 0.15, // Probability of learning transition
    val pS: Double = 0.10, // Probability of slip (knowing but making error)
    val pG: Double = 0.20  // Probability of guess (not knowing but getting correct)
) {
    /**
     * Updates knowledge mastery probability P(L) based on observed answer correctness.
     * @param priorL Prior probability P(L_{t-1}) of mastery [0.0, 1.0]
     * @param isCorrect Whether the student answered correctly
     * @return Updated posterior probability P(L_t) clamped to [0.0, 1.0]
     */
    fun updateMastery(priorL: Double, isCorrect: Boolean): Double {
        val boundedPrior = priorL.coerceIn(0.01, 0.99)

        val posteriorGivenObs = if (isCorrect) {
            val numerator = boundedPrior * (1.0 - pS)
            val denominator = numerator + ((1.0 - boundedPrior) * pG)
            numerator / denominator
        } else {
            val numerator = boundedPrior * pS
            val denominator = numerator + ((1.0 - boundedPrior) * (1.0 - pG))
            numerator / denominator
        }

        val updatedL = posteriorGivenObs + ((1.0 - posteriorGivenObs) * pT)
        return updatedL.coerceIn(0.0, 1.0)
    }
}
