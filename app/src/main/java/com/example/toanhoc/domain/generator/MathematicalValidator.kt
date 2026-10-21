package com.example.toanhoc.domain.generator

import com.example.toanhoc.domain.model.AcademicProblem
import com.example.toanhoc.domain.model.AnswerMode
import kotlin.math.abs

data class ValidationResult(
    val isValid: Boolean,
    val reason: String = ""
)

/**
 * Independent Mathematical & Pedagogical Validator inspecting generated AcademicProblem instances.
 *
 * Đảm bảo 6 tiêu chuẩn khắt khe:
 *  1. Mọi SolutionStep đều hợp lệ, có đáp án đúng không rỗng và khớp hoàn toàn với correctOptionIndex.
 *  2. Đúng 4 phương án lựa chọn, không có đáp án âm (ngoài chương trình Tiểu học).
 *  3. 4 phương án phải KHÁC NHAU VỀ GIÁ TRỊ TOÁN HỌC (1/2 và 2/4, hoặc 3,5 và 3,50 được tính là trùng).
 *  4. Không chứa chuỗi lỗi (NaN, Infinity, null, undefined) hoặc distractor lỗi.
 *  5. Kiểm định ràng buộc chuyên biệt theo từng khối lớp (Không nhớ ở Lớp 1, Có nhớ ở Lớp 2, Số dư < Số chia ở Lớp 3-4).
 */
class MathematicalValidator {

    companion object {
        private val FORBIDDEN_TOKENS = listOf("NaN", "Infinity", "null", "undefined")
        private val FRACTION_REGEX = Regex("""^(\d+)\s*/\s*(\d+)(\D*)$""")
        private val DECIMAL_REGEX = Regex("""^(\d+),(\d+)(\D*)$""")
        private val SPACED_INT_REGEX = Regex("""^(\d[\d\s]*\d)(\D*)$""")
        private val REMAINDER_ANSWER_REGEX = Regex("""^(\d+)\s*\(dư\s*(\d+)\)$""")
        private val DIVISION_EXPR_REGEX = Regex("""(\d[\d\s]*)\s*:\s*(\d[\d\s]*)""")
    }

    fun validateProblem(problem: AcademicProblem): ValidationResult {
        if (problem.statementVi.isBlank()) {
            return ValidationResult(false, "Problem statementVi is blank.")
        }

        if (FORBIDDEN_TOKENS.any { problem.statementVi.contains(it, ignoreCase = true) }) {
            return ValidationResult(false, "Problem statement contains forbidden token: '${problem.statementVi}'.")
        }

        if (problem.steps.isEmpty()) {
            return ValidationResult(false, "Problem contains no solution steps.")
        }

        // 1. Kiểm tra toàn bộ các bước giải (không chỉ bước cuối)
        for ((index, step) in problem.steps.withIndex()) {
            val expectedAnswer = step.expectedAnswer.trim()

            if (expectedAnswer.isEmpty()) {
                return ValidationResult(false, "Step ${index + 1}: Expected answer is empty.")
            }

            if (expectedAnswer.startsWith("-")) {
                return ValidationResult(false, "Step ${index + 1}: Negative expected answer '$expectedAnswer' is invalid for Primary Math.")
            }

            if (FORBIDDEN_TOKENS.any { expectedAnswer.contains(it, ignoreCase = true) }) {
                return ValidationResult(false, "Step ${index + 1}: Expected answer contains invalid token '$expectedAnswer'.")
            }

            if (step.answerMode == AnswerMode.MULTIPLE_CHOICE_4) {
                val options = step.options.map { it.trim() }

                if (options.size != 4) {
                    return ValidationResult(false, "Step ${index + 1}: Multiple choice options count is not 4 (found ${options.size}).")
                }

                if (options.any { it.isEmpty() || it.startsWith("-") }) {
                    return ValidationResult(false, "Step ${index + 1}: Options contain empty or negative value: $options.")
                }

                if (options.any { opt -> FORBIDDEN_TOKENS.any { opt.contains(it, ignoreCase = true) } }) {
                    return ValidationResult(false, "Step ${index + 1}: Options contain forbidden token: $options.")
                }

                // Kiểm tra trùng lặp chuỗi trực tiếp
                if (options.distinct().size != 4) {
                    return ValidationResult(false, "Step ${index + 1}: Multiple choice options contain exact duplicates: $options.")
                }

                // Kiểm tra trùng lặp về GIÁ TRỊ TOÁN HỌC (VD: "1/2" == "2/4", "4,50" == "4,5", "1 000" == "1000")
                val canonicalKeys = options.map { canonicalValueKey(it) }
                if (canonicalKeys.toSet().size != 4) {
                    return ValidationResult(
                        false,
                        "Step ${index + 1}: Multiple choice options contain mathematically equivalent values: $options -> $canonicalKeys."
                    )
                }

                if (!options.contains(expectedAnswer)) {
                    return ValidationResult(
                        false,
                        "Step ${index + 1}: Expected answer '$expectedAnswer' is not present in options $options."
                    )
                }

                if (step.correctOptionIndex !in 0..3 || options[step.correctOptionIndex] != expectedAnswer) {
                    return ValidationResult(
                        false,
                        "Step ${index + 1}: correctOptionIndex (${step.correctOptionIndex}) does not point to expectedAnswer '$expectedAnswer' in $options."
                    )
                }

                // Kiểm tra distractor rác dạng "<expectedAnswer> (sai...)" nhưng KHÔNG chặn chính expectedAnswer (như "5 (dư 2)")
                for (opt in options) {
                    if (opt != expectedAnswer && opt.contains("(") && opt.contains(")") && opt.startsWith(expectedAnswer)) {
                        return ValidationResult(false, "Step ${index + 1}: Invalid fallback distractor option found: '$opt'.")
                    }
                }
            }
        }

        // 2. Kiểm tra các ràng buộc toán học đặc thù theo từng Node (Pedagogical Domain Constraints)
        when {
            problem.nodeId.contains("G1_06") ||
                    problem.nodeId.contains("G1_07") ||
                    problem.nodeId.contains("NO_CARRY") -> {
                val validation = validateG1NoCarry(problem)
                if (!validation.isValid) return validation
            }
            problem.nodeId.contains("G2_03") || problem.nodeId.contains("WITH_CARRY") -> {
                val validation = validateG2WithCarry(problem)
                if (!validation.isValid) return validation
            }
            problem.nodeId.contains("G3_04") ||
                    problem.nodeId.contains("REMAINDER") ||
                    problem.steps.last().expectedAnswer.contains("(dư") -> {
                val validation = validateDivisionWithRemainder(problem)
                if (!validation.isValid) return validation
            }
        }

        return ValidationResult(true, "Problem is mathematically and pedagogically valid.")
    }

    /**
     * Chuẩn hóa chuỗi đáp án về khóa giá trị toán học duy nhất:
     *  - "2/4" hoặc "2/4 công việc" -> "1/2" hoặc "1/2 công việc"
     *  - "3/1" -> "3"
     *  - "4,50 cm" -> "4,5 cm"
     *  - "12 000 đồng" -> "12000 đồng"
     */
    private fun canonicalValueKey(raw: String): String {
        val s = raw.trim().replace(Regex("""\s+"""), " ")

        // 1. Phân số: rút gọn tối giản
        val fracMatch = FRACTION_REGEX.find(s)
        if (fracMatch != null) {
            val n = fracMatch.groupValues[1].toLongOrNull()
            val d = fracMatch.groupValues[2].toLongOrNull()
            val unit = fracMatch.groupValues[3].trim()
            if (n != null && d != null && d > 0L) {
                val g = gcd(n, d).coerceAtLeast(1L)
                val rn = n / g
                val rd = d / g
                val numPart = if (rd == 1L) "$rn" else "$rn/$rd"
                return if (unit.isEmpty()) numPart else "$numPart $unit"
            }
        }

        // 2. Số thập phân: cắt chữ số 0 thừa ở cuối phần thập phân
        val decMatch = DECIMAL_REGEX.find(s)
        if (decMatch != null) {
            val intPart = decMatch.groupValues[1].replace(" ", "")
            val fracPart = decMatch.groupValues[2].trimEnd('0')
            val unit = decMatch.groupValues[3].trim()
            val numPart = if (fracPart.isEmpty()) intPart else "$intPart,$fracPart"
            return if (unit.isEmpty()) numPart else "$numPart $unit"
        }

        // 3. Số nguyên có dấu cách phân tách hàng nghìn (12 000 -> 12000)
        val intMatch = SPACED_INT_REGEX.find(s)
        if (intMatch != null) {
            val digits = intMatch.groupValues[1].replace(" ", "")
            val unit = intMatch.groupValues[2].trim()
            return if (unit.isEmpty()) digits else "$digits $unit"
        }

        return s
    }

    private fun gcd(a: Long, b: Long): Long = if (b == 0L) abs(a) else gcd(b, a % b)

    /**
     * Lớp 1 (G1_06, G1_07): Cộng, trừ trong phạm vi 100 tuyệt đối KHÔNG CÓ NHỚ / KHÔNG MƯỢN.
     */
    private fun validateG1NoCarry(problem: AcademicProblem): ValidationResult {
        val expr = problem.steps.firstOrNull()?.mathExpression ?: ""
        if (expr.contains("+")) {
            val parts = expr.split("+").map { it.trim().toIntOrNull() }
            if (parts.size == 2 && parts[0] != null && parts[1] != null) {
                val a = parts[0]!!
                val b = parts[1]!!
                if ((a % 10) + (b % 10) >= 10 || a + b > 100) {
                    return ValidationResult(false, "G1 addition contains unwanted carry or exceeds 100: $a + $b")
                }
            }
        } else if (expr.contains("-")) {
            val parts = expr.split("-").map { it.trim().toIntOrNull() }
            if (parts.size == 2 && parts[0] != null && parts[1] != null) {
                val a = parts[0]!!
                val b = parts[1]!!
                if (a < b || (a % 10) < (b % 10) || (a / 10) < (b / 10)) {
                    return ValidationResult(false, "G1 subtraction contains unwanted borrow or negative result: $a - $b")
                }
            }
        }
        return ValidationResult(true)
    }

    /**
     * Lớp 2 (G2_03): Cộng có nhớ (hàng đơn vị >= 10) hoặc Trừ có nhớ (hàng đơn vị số bị trừ < số trừ) trong phạm vi 100.
     */
    private fun validateG2WithCarry(problem: AcademicProblem): ValidationResult {
        val expr = problem.steps.firstOrNull()?.mathExpression ?: ""
        if (expr.contains("+")) {
            val parts = expr.split("+").map { it.trim().toIntOrNull() }
            if (parts.size == 2 && parts[0] != null && parts[1] != null) {
                val a = parts[0]!!
                val b = parts[1]!!
                if ((a % 10) + (b % 10) < 10 || a + b > 100) {
                    return ValidationResult(false, "G2_03 addition lacks required carry in units or exceeds 100: $a + $b")
                }
            }
        } else if (expr.contains("-")) {
            val parts = expr.split("-").map { it.trim().toIntOrNull() }
            if (parts.size == 2 && parts[0] != null && parts[1] != null) {
                val a = parts[0]!!
                val b = parts[1]!!
                if (a <= b || (a % 10) >= (b % 10)) {
                    return ValidationResult(false, "G2_03 subtraction lacks required borrow in units: $a - $b")
                }
            }
        }
        return ValidationResult(true)
    }

    /**
     * Lớp 3 & Lớp 4 (G3_04, G4_03): Phép chia có dư phải thỏa mãn:
     *  - 0 < Số dư < Số chia
     *  - Số bị chia = Số chia × Thương + Số dư
     */
    private fun validateDivisionWithRemainder(problem: AcademicProblem): ValidationResult {
        val lastStep = problem.steps.lastOrNull() ?: return ValidationResult(true)
        val expected = lastStep.expectedAnswer.trim()

        val match = REMAINDER_ANSWER_REGEX.find(expected)
        if (match != null) {
            val q = match.groupValues[1].toIntOrNull() ?: return ValidationResult(false, "Invalid quotient in '$expected'")
            val rem = match.groupValues[2].toIntOrNull() ?: return ValidationResult(false, "Invalid remainder in '$expected'")

            if (rem <= 0) {
                return ValidationResult(false, "Division remainder must be >= 1, found $rem in '$expected'")
            }

            // Đối chiếu với Số bị chia và Số chia trong biểu thức nếu có
            val exprSource = "${lastStep.mathExpression} ${problem.statementVi}"
            val divMatch = DIVISION_EXPR_REGEX.find(exprSource)
            if (divMatch != null) {
                val dividend = divMatch.groupValues[1].replace(" ", "").toIntOrNull()
                val divisor = divMatch.groupValues[2].replace(" ", "").toIntOrNull()
                if (dividend != null && divisor != null && divisor > 0) {
                    if (rem >= divisor) {
                        return ValidationResult(
                            false,
                            "Remainder ($rem) must be strictly less than divisor ($divisor) in '$expected'"
                        )
                    }
                    if (divisor * q + rem != dividend) {
                        return ValidationResult(
                            false,
                            "Division identity failed: $divisor * $q + $rem != $dividend"
                        )
                    }
                }
            }
        }
        return ValidationResult(true)
    }
}