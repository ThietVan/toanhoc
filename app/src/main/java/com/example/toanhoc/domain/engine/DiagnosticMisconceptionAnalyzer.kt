package com.example.toanhoc.domain.engine

/**
 * Diagnostic Misconception Analyzer.
 * Identifies cognitive bug patterns from student answer variations and maps them to Socratic explanations.
 */
class DiagnosticMisconceptionAnalyzer {

    data class AnalysisResult(
        val bugTag: CognitiveBugTag?,
        val socraticExplanationVi: String
    )

    /**
     * Analyzes student answer against predefined misconception trap options.
     * @param studentInput Answer input submitted by student
     * @param correctAnswer Expected correct answer
     * @param misconceptionTraps Map of known incorrect answer values to CognitiveBugTag
     */
    fun analyzeAnswer(
        studentInput: String,
        correctAnswer: String,
        misconceptionTraps: Map<String, CognitiveBugTag> = emptyMap()
    ): AnalysisResult {
        val cleanedInput = studentInput.trim().lowercase()
        val cleanedCorrect = correctAnswer.trim().lowercase()

        if (cleanedInput == cleanedCorrect) {
            return AnalysisResult(
                bugTag = null,
                socraticExplanationVi = "Đáp án chính xác."
            )
        }

        // Check explicit trap map match
        val matchedTrap = misconceptionTraps[cleanedInput]
        if (matchedTrap != null) {
            return AnalysisResult(
                bugTag = matchedTrap,
                socraticExplanationVi = matchedTrap.socraticExplanationVi
            )
        }

        // Automatic pattern detection heuristics
        val detectedBug = inferBugPatternFromInput(cleanedInput)
        return AnalysisResult(
            bugTag = detectedBug,
            socraticExplanationVi = detectedBug?.socraticExplanationVi
                ?: "Đọc kỹ đề bài, rồi so sánh từng đáp án với điều đề bài hỏi nhé."
        )
    }

    /**
     * Heuristic pattern detection for common mathematical traps.
     */
    private fun inferBugPatternFromInput(input: String): CognitiveBugTag? {
        return when {
            // Fraction pattern: e.g. "a/b" where both numerator and denominator were added directly
            input.contains("/") -> {
                val parts = input.split("/")
                if (parts.size == 2 && parts[0].toIntOrNull() != null && parts[1].toIntOrNull() != null) {
                    CognitiveBugTag.ERR_FRACTION_ADD_BOTH_NUM_DEN
                } else null
            }
            else -> null
        }
    }
}
