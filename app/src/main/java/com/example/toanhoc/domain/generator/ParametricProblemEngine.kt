package com.example.toanhoc.domain.generator

import com.example.toanhoc.domain.engine.CognitiveBugTag
import com.example.toanhoc.domain.model.AcademicProblem
import com.example.toanhoc.domain.model.AcademicTier
import com.example.toanhoc.domain.model.AnswerMode
import com.example.toanhoc.domain.model.ScaffoldingLevel
import com.example.toanhoc.domain.model.SolutionStep
import com.example.toanhoc.domain.model.VisualBlueprintSpec
import com.example.toanhoc.domain.model.VisualBlueprintType
import kotlin.math.abs
import kotlin.random.Random

/**
 * Parametric Math Problem Engine - 50 node (Toán tiểu học lớp 1-5).
 * Chuẩn hóa toàn diện theo Chương trình GDPT 2018 - Bộ sách "Kết nối tri thức với cuộc sống" (2026).
 *
 * Nguyên tắc chuẩn hóa:
 *  - Mọi bài luôn có đúng 4 đáp án KHÁC NHAU VỀ GIÁ TRỊ (phân số bằng nhau được coi là trùng).
 *  - Không sinh đáp án âm, không sinh bài có kết quả vô lý (âm, 0 khi không hợp lý).
 *  - Số thập phân dùng dấu phẩy (3,5); số từ 5 chữ số trở lên ngăn cách hàng nghìn bằng dấu cách.
 *  - Đơn vị chuẩn SGK: km/giờ, cm², dm², m², ha, km², cm³, dm³, m³, đồng, °C...
 *  - Nhân vật xuyên suốt bộ sách Kết nối tri thức: Việt, Mai, Nam, Mi, Rô-bốt.
 *  - Mỗi node có nhiều dạng bài (variant); AcademicTier chỉnh phạm vi số;
 *    ScaffoldingLevel khác INDEPENDENT sẽ kèm "Gợi ý" trong đề bước 1.
 *  - Mỗi đáp án nhiễu mô phỏng một lỗi tư duy phổ biến (quên nhớ, nhầm chu vi/diện tích, đặt sai cột...).
 */
class ParametricProblemEngine(
    randomSeed: Long = System.currentTimeMillis() xor System.nanoTime()
) {
    private val rng = Random(randomSeed)
    private val validator = MathematicalValidator()

    companion object {
        private const val MAX_ATTEMPTS = 30

        val ALL_NODE_IDS: List<String> = listOf(
            "G1_01_SPATIAL_SHAPES", "G1_02_NUM_0_10_COMPARE", "G1_03_SPLIT_COMBINE_10",
            "G1_04_ADD_SUB_10_MEANING", "G1_05_PLACE_VALUE_100", "G1_06_ADD_SUB_100_NO_CARRY",
            "G1_07_WORD_PROBLEMS_G1", "G1_08_MEASURE_LENGTH_CM", "G1_09_TIME_CLOCK_CALENDAR",
            "G1_10_PATTERNS_COUNTING",
            "G2_01_TERMS_AND_NUMBER_LINE", "G2_02_MAKE_10_ADD_SUB", "G2_03_ADD_SUB_100_WITH_CARRY",
            "G2_04_WORD_MORE_LESS", "G2_05_MUL_DIV_2_5", "G2_06_NUM_1000_ADD_SUB",
            "G2_07_GEOMETRY_G2", "G2_08_MEASURE_DM_M_KM_KG_L", "G2_09_TIME_MONEY_G2",
            "G2_10_STATS_PROBABILITY_G2",
            "G3_01_MUL_DIV_TABLES_3_9", "G3_02_FIND_UNKNOWN_COMPONENT", "G3_03_TIMES_MORE_LESS_FRAC",
            "G3_04_DIVISION_WITH_REMAINDER", "G3_05_ORDER_OF_OPERATIONS", "G3_06_NUM_100K_ROUNDING_ROMAN",
            "G3_07_TWO_STEP_WORD_PROBLEMS", "G3_08_GEOMETRY_MIDPOINT_CIRCLE",
            "G3_09_PERIMETER_AREA_RECT_SQUARE", "G3_10_MEASURE_STATS_G3",
            "G4_01_MILLIONS_CLASS_ROUNDING", "G4_02_ALGEBRAIC_EXPRESSIONS_LAWS", "G4_03_MUL_DIV_2_DIGITS",
            "G4_04_AVERAGE_AND_UNIT_METHOD", "G4_05_SUM_DIFFERENCE_MODEL", "G4_06_FRACTIONS_CONCEPT_COMPARE",
            "G4_07_FRACTIONS_FOUR_OPS", "G4_08_ANGLES_PARALLEL_SHAPES", "G4_09_MEASURE_TON_M2_CENTURY",
            "G4_10_BAR_CHART_PROBABILITY",
            "G5_01_DECIMAL_FRACTIONS_MIXED", "G5_02_DECIMALS_PLACE_ROUNDING", "G5_03_DECIMALS_FOUR_OPS",
            "G5_04_SUM_DIFF_RATIO_PROBLEMS", "G5_05_PERCENTAGE_MAP_SCALE", "G5_06_TRIANGLE_TRAPEZOID_CIRCLE",
            "G5_07_SURFACE_AREA_VOLUME_3D", "G5_08_TIME_OPS_AND_MOTION", "G5_09_PIE_CHART_PROBABILITY_RATIO",
            "G5_10_COMPREHENSIVE_HEURISTICS"
        )

        private val LEGACY_ALIASES = mapOf(
            "G1_NUM_01" to "G1_03_SPLIT_COMBINE_10",
            "G1_ADD_01" to "G1_04_ADD_SUB_10_MEANING",
            "G1_FENCE_01" to "G1_10_PATTERNS_COUNTING",
            "G2_ADD_01" to "G2_03_ADD_SUB_100_WITH_CARRY",
            "G2_BAR_01" to "G2_04_WORD_MORE_LESS",
            "G3_OPS_01" to "G3_05_ORDER_OF_OPERATIONS",
            "G4_FRAC_01" to "G4_07_FRACTIONS_FOUR_OPS",
            "G5_SPEED_01" to "G5_08_TIME_OPS_AND_MOTION"
        )

        /** Tuyến nhân vật đặc trưng của bộ SGK Kết nối tri thức với cuộc sống */
        private val NAMES = listOf("Việt", "Mai", "Nam", "Mi", "Rô-bốt", "An", "Bình", "Lan")
        private val ITEMS = listOf("viên bi", "quả cam", "quyển vở", "chiếc nhãn vở", "bông hoa", "cây bút chì")
        private val WEEKDAYS = listOf("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ nhật")
        private val FRACTION_BASES = listOf(1 to 2, 1 to 3, 2 to 3, 1 to 4, 3 to 4, 2 to 5, 3 to 5, 1 to 5)
    }

    /** Ngữ cảnh sinh bài: giúp các hàm sinh bài ngắn gọn. t: 0 = dễ, 1 = trung bình, 2 = khó. */
    private class Ctx(val nodeId: String, val level: ScaffoldingLevel, val tier: AcademicTier, val t: Int)

    // ------------------------------------------------------------------------------------------
    // PUBLIC API
    // ------------------------------------------------------------------------------------------

    fun generateProblemForNode(
        nodeId: String,
        grade: Int,
        scaffoldingLevel: ScaffoldingLevel = ScaffoldingLevel.INDEPENDENT,
        academicTier: AcademicTier = AcademicTier.TRUNG_BINH
    ): AcademicProblem {
        // Toán tư duy (T3_xx): bộ sinh đề riêng theo chuyên đề
        if (com.example.toanhoc.domain.thinking.ThinkingEngine.supports(nodeId))
            return com.example.toanhoc.domain.thinking.ThinkingEngine.generate(nodeId, academicTier, scaffoldingLevel, rng)
        val targetNodeId = LEGACY_ALIASES[nodeId] ?: nodeId
        repeat(MAX_ATTEMPTS) {
            val candidate = runCatching {
                generateRaw(targetNodeId, grade, scaffoldingLevel, academicTier)
            }.getOrNull()
            if (candidate != null && validator.validateProblem(candidate).isValid) {
                return candidate
            }
        }
        return getFallbackProblem(targetNodeId, grade, scaffoldingLevel, academicTier)
    }

    /**
     * Tự kiểm tra: sinh mỗi node nhiều lần với mọi tier/scaffolding, trả về danh sách lỗi (rỗng = tốt).
     * Có thể gọi trong unit test: assertTrue(engine.runSelfCheck().isEmpty()).
     */
    fun runSelfCheck(runsPerCombination: Int = 40): List<String> {
        val errors = linkedSetOf<String>()
        for (node in ALL_NODE_IDS) {
            val grade = node.substring(1, 2).toInt()
            for (tier in AcademicTier.values()) {
                for (level in ScaffoldingLevel.values()) {
                    repeat(runsPerCombination) {
                        try {
                            val p = generateRaw(node, grade, level, tier)
                            val s = p.steps.first()
                            val keys = s.options.map { valueKey(it) }
                            when {
                                s.options.size != 4 -> errors += "$node: số đáp án = ${s.options.size}"
                                keys.toSet().size != 4 -> errors += "$node: đáp án trùng giá trị ${s.options}"
                                s.expectedAnswer !in s.options -> errors += "$node: đáp án đúng không nằm trong options"
                                s.options.any { it.trim().startsWith("-") } -> errors += "$node: có đáp án âm ${s.options}"
                            }
                        } catch (e: Exception) {
                            errors += "$node: ${e.javaClass.simpleName} - ${e.message}"
                        }
                    }
                }
            }
        }
        return errors.toList()
    }

    // ------------------------------------------------------------------------------------------
    // ROUTING
    // ------------------------------------------------------------------------------------------

    private fun generateRaw(
        nodeId: String,
        grade: Int,
        level: ScaffoldingLevel,
        tier: AcademicTier
    ): AcademicProblem {
        val g = Regex("^G([1-5])_").find(nodeId)?.groupValues?.get(1)?.toInt() ?: grade.coerceIn(1, 5)
        val t = tierIdx(tier)
        val ctx = Ctx(nodeId, level, tier, t)
        return when (g) {
            1 -> generateGrade1Node(ctx)
            2 -> generateGrade2Node(ctx)
            3 -> generateGrade3Node(ctx)
            4 -> generateGrade4Node(ctx)
            else -> generateGrade5Node(ctx)
        }
    }

    // ------------------------------------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------------------------------------

    private fun tierIdx(tier: AcademicTier): Int =
        (tier.ordinal - AcademicTier.TRUNG_BINH.ordinal).coerceIn(-1, 1) + 1

    private fun rr(t: Int, easy: IntRange, normal: IntRange, hard: IntRange): Int {
        val r = when (t) {
            0 -> easy
            1 -> normal
            else -> hard
        }
        return rng.nextInt(r.first, r.last + 1)
    }

    private fun <T> choose(list: List<T>): T = list[rng.nextInt(list.size)]

    private fun gcd(a: Long, b: Long): Long = if (b == 0L) abs(a) else gcd(b, a % b)

    /** 1234567 -> "1 234 567"; số dưới 5 chữ số giữ nguyên (theo cách viết SGK Kết nối tri thức). */
    private fun fmtInt(n: Number): String {
        val v = n.toLong()
        val s = abs(v).toString()
        val body = if (s.length >= 5) s.reversed().chunked(3).joinToString(" ").reversed() else s
        return if (v < 0) "-$body" else body
    }

    private fun fmtMoney(n: Number): String = "${fmtInt(n)} đồng"

    /** value tính theo đơn vị 10^-scale. fmtDecimal(46, 1) = "4,6". trim=true bỏ chữ số 0 thừa cuối. */
    private fun fmtDecimal(value: Number, scale: Int, trim: Boolean = false): String {
        val v = value.toLong()
        if (scale <= 0) return fmtInt(v)
        val s = abs(v).toString().padStart(scale + 1, '0')
        val intPart = s.substring(0, s.length - scale)
        var frac = s.substring(s.length - scale)
        if (trim) frac = frac.trimEnd('0')
        val body = if (frac.isEmpty()) intPart else "$intPart,$frac"
        return if (v < 0) "-$body" else body
    }

    /** Phân số tối giản dạng chuỗi: fracStr(6, 8) = "3/4"; mẫu = 1 thì chỉ trả về tử. */
    private fun fracStr(n: Number, d: Number): String {
        val nn = n.toLong()
        val dd = d.toLong()
        val g = gcd(nn, dd).coerceAtLeast(1L)
        val a = nn / g
        val b = dd / g
        return if (b == 1L) "$a" else "$a/$b"
    }

    private fun roundTo(n: Int, unit: Int): Int = ((n + unit / 2) / unit) * unit

    private fun toRoman(n: Int): String {
        val vals = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val syms = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
        var x = n
        val sb = StringBuilder()
        for (i in vals.indices) {
            while (x >= vals[i]) {
                sb.append(syms[i])
                x -= vals[i]
            }
        }
        return sb.toString()
    }

    /** Giá trị nếu học sinh cộng tất cả ký hiệu mà quên quy tắc số nhỏ đứng trước (IV -> 6). */
    private fun romanNaive(n: Int): Int = toRoman(n).fold(0) { acc, ch ->
        acc + when (ch) {
            'I' -> 1; 'V' -> 5; 'X' -> 10; 'L' -> 50; 'C' -> 100; 'D' -> 500; else -> 1000
        }
    }

    /** Đọc số có 2 chữ số (10..99) chuẩn chính tả SGK Toán 1 Kết nối tri thức (mốt, tư, lăm, mươi). */
    private fun readTwoDigitVi(n: Int): String {
        val digitNames = listOf("không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín")
        val ten = n / 10
        val unit = n % 10
        val tensPart = if (ten == 1) "mười" else "${digitNames[ten]} mươi"
        val unitPart = when {
            unit == 0 -> ""
            unit == 1 && ten >= 2 -> " mốt"
            unit == 4 && ten >= 2 -> " tư"
            unit == 5 -> " lăm"
            else -> " ${digitNames[unit]}"
        }
        return tensPart + unitPart
    }

    /** Khóa so sánh giá trị: phân số bằng nhau (1/2 và 2/4) cho cùng một khóa. */
    private fun valueKey(s: String): String {
        val t = s.trim()
        val m = Regex("^(\\d+)/(\\d+)(.*)$").find(t) ?: return t
        val n = m.groupValues[1].toLongOrNull()
        val d = m.groupValues[2].toLongOrNull()
        if (n == null || d == null || d == 0L) return t
        val g = gcd(n, d).coerceAtLeast(1L)
        return "${n / g}/${d / g}${m.groupValues[3]}"
    }

    /**
     * Tạo đúng 4 đáp án khác nhau về giá trị (đáp án đúng luôn có mặt), rồi xáo trộn.
     * Chỉ đệm số khi đáp án đúng có dạng "số + đơn vị" và nhỏ hơn 1000.
     * Nếu không đủ 4 đáp án -> ném lỗi để vòng lặp sinh bài thử lại.
     */
    private fun generate4Options(correct: String, distractors: List<String>): List<String> {
        val picked = LinkedHashMap<String, String>()
        picked[valueKey(correct)] = correct
        for (d in distractors) {
            if (picked.size >= 4) break
            val s = d.trim()
            if (s.isEmpty() || s.startsWith("-")) continue
            val k = valueKey(s)
            if (!picked.containsKey(k)) picked[k] = s
        }
        if (picked.size < 4) {
            val m = Regex("^(\\d+)(\\D*)$").find(correct)
            if (m != null) {
                val n = m.groupValues[1].toIntOrNull()
                if (n != null && n < 1000) {
                    val unit = m.groupValues[2]
                    for (d in intArrayOf(1, -1, 2, -2, 3, -3, 10, -10)) {
                        if (picked.size >= 4) break
                        val v = n + d
                        if (v <= 0) continue
                        val s = "$v$unit"
                        val k = valueKey(s)
                        if (!picked.containsKey(k)) picked[k] = s
                    }
                }
            }
        }
        check(picked.size == 4) { "Không đủ 4 đáp án khác nhau cho '$correct'" }
        return picked.values.toList().shuffled(rng)
    }

    /** Đáp án số nguyên (có đơn vị). Ứng viên < min hoặc trùng đáp án đúng bị loại. */
    private fun intOptions(correct: Int, cands: List<Int>, unit: String = "", min: Int = 0): List<String> {
        val ds = cands.filter { it != correct && it >= min }.distinct().map { "${fmtInt(it)}$unit" }
        return generate4Options("${fmtInt(correct)}$unit", ds)
    }

    private fun moneyOptions(correct: Int, cands: List<Int>): List<String> {
        val ds = cands.filter { it != correct && it > 0 }.distinct().map { fmtMoney(it) }
        return generate4Options(fmtMoney(correct), ds)
    }

    private fun Ctx.mk(
        statement: String,
        correct: String,
        options: List<String>,
        expr: String = "",
        bp: VisualBlueprintType = VisualBlueprintType.NONE,
        hint: String? = null,
        bugRules: Map<String, CognitiveBugTag> = emptyMap(),
        spec: VisualBlueprintSpec? = null
    ): AcademicProblem {
        val idx = options.indexOf(correct)
        require(idx >= 0) { "Đáp án đúng '$correct' không nằm trong $options" }
        require(options.size == 4 && options.toSet().size == 4) { "Phải có 4 đáp án khác nhau: $options" }
        val label = if (expr.isNotEmpty()) expr else statement
        val prompt = if (hint != null && level != ScaffoldingLevel.INDEPENDENT) {
            "Chọn phương án đúng. Gợi ý: $hint"
        } else {
            "Chọn phương án đúng:"
        }
        return AcademicProblem(
            problemId = "${nodeId}_${rng.nextInt(100000)}",
            nodeId = nodeId,
            scaffoldingLevel = level,
            academicTier = tier,
            statementVi = statement,
            blueprintSpec = spec ?: VisualBlueprintSpec(
                type = bp,
                primaryLabel = label,
                paramValue1 = 0.0,
                paramValue2 = 20.0,
                paramValue3 = correct.toDoubleOrNull() ?: 10.0
            ),
            steps = listOf(
                SolutionStep(
                    stepIndex = 1,
                    promptVi = prompt,
                    mathExpression = label,
                    expectedAnswer = correct,
                    answerMode = AnswerMode.MULTIPLE_CHOICE_4,
                    options = options,
                    correctOptionIndex = idx,
                    bugRules = bugRules
                )
            )
        )
    }

    private fun roleOptions(correct: String): List<String> {
        val roles = listOf("Số hạng", "Tổng", "Số bị trừ", "Số trừ", "Hiệu")
        return generate4Options(correct, roles.filter { it != correct }.shuffled(rng))
    }

    private fun distinctFractions(count: Int): List<Pair<Int, Int>> {
        val out = mutableListOf<Pair<Int, Int>>()
        val keys = mutableSetOf<String>()
        var guard = 0
        while (out.size < count && guard++ < 300) {
            val d = rng.nextInt(2, 10)
            val n = rng.nextInt(1, d)
            if (keys.add(fracStr(n, d))) out.add(n to d)
        }
        check(out.size == count)
        return out
    }

    private fun fracCompare(x: Pair<Int, Int>, y: Pair<Int, Int>): Int =
        (x.first * y.second).compareTo(y.first * x.second)

    private fun threePercents(): List<Int> {
        repeat(300) {
            val a = rng.nextInt(2, 13) * 5
            val b = rng.nextInt(2, 13) * 5
            val c = 100 - a - b
            if (c >= 10 && c % 5 == 0 && setOf(a, b, c).size == 3) return listOf(a, b, c)
        }
        return listOf(50, 30, 20)
    }

    // ------------------------------------------------------------------------------------------
    // GRADE 1 (SGK Toán 1 - Kết nối tri thức với cuộc sống)
    // ------------------------------------------------------------------------------------------
    private fun generateGrade1Node(ctx: Ctx): AcademicProblem = with(ctx) {
        when (nodeId) {
            // Chủ đề: Vị trí, định hướng không gian, Hình phẳng (vuông, tròn, tam giác, chữ nhật) & Khối (lập phương, hộp chữ nhật)
            "G1_01_SPATIAL_SHAPES" -> {
                val flatShapes = listOf("Hình vuông", "Hình tròn", "Hình tam giác", "Hình chữ nhật")
                val solidShapes = listOf("Khối lập phương", "Khối hộp chữ nhật")
                when (rng.nextInt(5)) {
                    0 -> {
                        val row = (flatShapes + solidShapes).shuffled(rng).take(3)
                        val posIdx = rng.nextInt(3)
                        val posName = listOf("bên trái", "ở giữa", "bên phải")[posIdx]
                        val correct = row[posIdx]
                        val ds = (flatShapes + solidShapes).filter { it != correct }.shuffled(rng)
                        mk(
                            "Rô-bốt xếp 3 hình theo thứ tự từ trái sang phải: ${row[0]}, ${row[1]}, ${row[2]}. Hình nào ở vị trí $posName?",
                            correct, generate4Options(correct, row + ds),
                            spec = VisualBlueprintSpec(
                                type = VisualBlueprintType.SPATIAL_SHAPES,
                                primaryLabel = row[0],
                                secondaryLabel = row[1]
                            ),
                            hint = "Quan sát từ trái sang phải: hình đầu tiên ở bên trái, hình thứ hai ở giữa, hình thứ ba ở bên phải."
                        )
                    }
                    1 -> {
                        val solid = choose(solidShapes)
                        val flats = flatShapes.shuffled(rng).take(3)
                        mk(
                            "Trong các hình dưới đây, hình nào là khối hình học (không phải hình phẳng)?",
                            solid, generate4Options(solid, flats),
                            spec = VisualBlueprintSpec(type = VisualBlueprintType.SPATIAL_SHAPES, primaryLabel = solid),
                            hint = "Khối lập phương và khối hộp chữ nhật là các khối hình."
                        )
                    }
                    2 -> {
                        val (obj, ans) = choose(
                            listOf(
                                "con xúc xắc" to "Khối lập phương",
                                "khối rubik vuông" to "Khối lập phương",
                                "hộp sữa giấy" to "Khối hộp chữ nhật",
                                "viên gạch xây nhà" to "Khối hộp chữ nhật",
                                "hộp đựng bút màu" to "Khối hộp chữ nhật"
                            )
                        )
                        val ds = (flatShapes + solidShapes).filter { it != ans }.shuffled(rng)
                        mk(
                            "Đồ vật '$obj' của bạn Nam có dạng hình gì?",
                            ans, generate4Options(ans, ds),
                            hint = "Khối lập phương có 6 mặt đều là hình vuông; khối hộp chữ nhật có các mặt là hình chữ nhật."
                        )
                    }
                    3 -> {
                        val (obj, ans) = choose(
                            listOf(
                                "mặt đồng hồ tròn" to "Hình tròn",
                                "chiếc đĩa tròn" to "Hình tròn",
                                "chiếc khăn mùi soa vuông" to "Hình vuông",
                                "viên gạch hoa lát nền vuông" to "Hình vuông",
                                "lá cờ Tổ quốc" to "Hình chữ nhật",
                                "bìa quyển sách Toán 1" to "Hình chữ nhật",
                                "biển báo giao thông có 3 cạnh" to "Hình tam giác"
                            )
                        )
                        val ds = (flatShapes + solidShapes).filter { it != ans }.shuffled(rng)
                        mk("Trong thực tế, '$obj' có dạng hình gì?", ans, generate4Options(ans, ds))
                    }
                    else -> {
                        val (q, ans) = choose(
                            listOf(
                                "Hình phẳng nào có 3 cạnh và 3 góc?" to "Hình tam giác",
                                "Hình phẳng nào có 4 cạnh bằng nhau?" to "Hình vuông",
                                "Hình phẳng nào có đường bao cong tròn, không có góc?" to "Hình tròn"
                            )
                        )
                        val ds = (flatShapes + solidShapes).filter { it != ans }.shuffled(rng)
                        mk(q, ans, generate4Options(ans, ds))
                    }
                }
            }

            // Chủ đề 1: Các số từ 0 đến 10, so sánh, sắp thứ tự
            "G1_02_NUM_0_10_COMPARE" -> {
                val four = (0..10).shuffled(rng).take(4)
                when (rng.nextInt(6)) {
                    0 -> {
                        val c = four.maxOrNull()!!
                        mk(
                            "Số nào lớn nhất trong các số sau: ${four.joinToString(", ")}?", "$c",
                            intOptions(c, four), bp = VisualBlueprintType.NUMBER_LINE,
                            hint = "Số đứng càng xa số 0 trên tia số thì càng lớn."
                        )
                    }
                    1 -> {
                        val c = four.minOrNull()!!
                        mk(
                            "Số nào bé nhất trong các số sau: ${four.joinToString(", ")}?", "$c",
                            intOptions(c, four), bp = VisualBlueprintType.NUMBER_LINE,
                            hint = "Số đứng càng gần số 0 trên tia số thì càng bé."
                        )
                    }
                    2 -> {
                        val a = rng.nextInt(0, 10)
                        mk(
                            "Số liền sau của $a là số nào?", "${a + 1}",
                            intOptions(a + 1, listOf(a, a - 1, a + 2, a - 2).filter { it in 0..10 }),
                            bp = VisualBlueprintType.NUMBER_LINE,
                            hint = "Số liền sau hơn số đã cho 1 đơn vị."
                        )
                    }
                    3 -> {
                        val a = rng.nextInt(1, 11)
                        mk(
                            "Số liền trước của $a là số nào?", "${a - 1}",
                            intOptions(a - 1, listOf(a, a + 1, a - 2, a + 2).filter { it in 0..10 }),
                            bp = VisualBlueprintType.NUMBER_LINE,
                            hint = "Số liền trước kém số đã cho 1 đơn vị."
                        )
                    }
                    4 -> {
                        val a = rng.nextInt(0, 11)
                        val b = if (rng.nextInt(4) == 0) a else rng.nextInt(0, 11)
                        val correct = when {
                            a > b -> "$a lớn hơn $b"
                            a < b -> "$a bé hơn $b"
                            else -> "$a bằng $b"
                        }
                        val ds = listOf("$a lớn hơn $b", "$a bé hơn $b", "$a bằng $b", "Không so sánh được")
                        mk(
                            "Khi so sánh hai số $a và $b, nhận xét nào đúng?",
                            correct, generate4Options(correct, ds),
                            bp = VisualBlueprintType.NUMBER_LINE
                        )
                    }
                    else -> {
                        val asc = rng.nextBoolean()
                        val sorted = if (asc) four.sorted() else four.sortedDescending()
                        val correct = sorted.joinToString("; ")
                        val d1 = sorted.reversed().joinToString("; ")
                        val d2 = listOf(sorted[1], sorted[0], sorted[2], sorted[3]).joinToString("; ")
                        val d3 = listOf(sorted[0], sorted[2], sorted[1], sorted[3]).joinToString("; ")
                        val orderText = if (asc) "từ bé đến lớn" else "từ lớn đến bé"
                        mk(
                            "Sắp xếp các số ${four.joinToString(", ")} theo thứ tự $orderText:",
                            correct, generate4Options(correct, listOf(d1, d2, d3)),
                            bp = VisualBlueprintType.NUMBER_LINE,
                            hint = if (asc) "Viết số bé nhất đầu tiên, số lớn nhất cuối cùng."
                            else "Viết số lớn nhất đầu tiên, số bé nhất cuối cùng."
                        )
                    }
                }
            }

            // Tách - Gộp số trong phạm vi 10
            "G1_03_SPLIT_COMBINE_10" -> {
                val total = rr(t, 3..6, 5..9, 7..10)
                val p1 = rng.nextInt(1, total)
                val p2 = total - p1
                when (rng.nextInt(4)) {
                    0 -> mk(
                        "Tách $total gồm $p1 và số mấy?", "$p2",
                        intOptions(p2, listOf(p2 + 1, p2 - 1, total, p1), min = 1),
                        "$total - $p1", VisualBlueprintType.SINGAPORE_BAR_MODEL,
                        hint = "Lấy $total bớt đi $p1."
                    )
                    1 -> mk(
                        "Gộp $p1 và $p2 được số mấy?", "$total",
                        intOptions(total, listOf(total + 1, total - 1, p1, p2), min = 1),
                        "$p1 + $p2", VisualBlueprintType.SINGAPORE_BAR_MODEL,
                        hint = "Gộp là làm phép cộng: $p1 + $p2."
                    )
                    2 -> {
                        val correct = "$p1 và $p2"
                        val ds = listOf(
                            "$p1 và ${p2 + 1}",
                            "${p1 + 1} và ${p2 + 1}",
                            "$total và $p1",
                            "${(p1 - 1).coerceAtLeast(1)} và $p2"
                        )
                        mk(
                            "Số $total gồm mấy và mấy?",
                            correct, generate4Options(correct, ds),
                            bp = VisualBlueprintType.SINGAPORE_BAR_MODEL,
                            hint = "Tìm hai số gộp lại được $total."
                        )
                    }
                    else -> {
                        val item = choose(ITEMS)
                        mk(
                            "Mai có $total $item chia vào 2 hộp. Hộp thứ nhất có $p1 $item. Hỏi hộp thứ hai có mấy $item?",
                            "$p2", intOptions(p2, listOf(total, p1, p2 + 1, p2 - 1), min = 1),
                            "$total - $p1", VisualBlueprintType.SINGAPORE_BAR_MODEL,
                            hint = "Tách $total thành $p1 và phần còn lại."
                        )
                    }
                }
            }

            // Chủ đề 3: Phép cộng, phép trừ trong phạm vi 10
            "G1_04_ADD_SUB_10_MEANING" -> when (rng.nextInt(6)) {
                0 -> {
                    val a = rng.nextInt(1, 9)
                    val b = rng.nextInt(1, 11 - a)
                    val sum = a + b
                    mk(
                        "Tính: $a + $b = ?", "$sum",
                        intOptions(sum, listOf(sum + 1, sum - 1, abs(a - b), sum + 2)),
                        "$a + $b", VisualBlueprintType.NUMBER_LINE,
                        hint = "Đếm thêm $b bước từ số $a."
                    )
                }
                1 -> {
                    val a = rng.nextInt(2, 11)
                    val b = rng.nextInt(1, a)
                    val diff = a - b
                    mk(
                        "Tính: $a - $b = ?", "$diff",
                        intOptions(diff, listOf(diff + 1, diff - 1, a + b, diff + 2)),
                        "$a - $b", VisualBlueprintType.NUMBER_LINE,
                        hint = "Đếm lùi $b bước từ số $a."
                    )
                }
                2 -> {
                    val a = rng.nextInt(1, 11)
                    when (rng.nextInt(3)) {
                        0 -> mk(
                            "Tính: $a + 0 = ?", "$a",
                            intOptions(a, listOf(0, a + 1, a - 1, 10)),
                            "$a + 0", hint = "Số nào cộng với 0 cũng bằng chính số đó."
                        )
                        1 -> mk(
                            "Tính: $a - 0 = ?", "$a",
                            intOptions(a, listOf(0, a - 1, a + 1, 1)),
                            "$a - 0", hint = "Số nào trừ đi 0 cũng bằng chính số đó."
                        )
                        else -> mk(
                            "Tính: $a - $a = ?", "0",
                            intOptions(0, listOf(a, 1, a + 1, 2)),
                            "$a - $a", hint = "Hai số bằng nhau trừ đi nhau thì bằng 0."
                        )
                    }
                }
                3 -> {
                    val total = rng.nextInt(4, 11)
                    val a = rng.nextInt(1, total)
                    val x = total - a
                    if (rng.nextBoolean()) {
                        mk(
                            "Tìm số thích hợp thay cho dấu ?: $a + ? = $total", "$x",
                            intOptions(x, listOf(total, a, x + 1, x - 1), min = 1),
                            "$a + ? = $total", VisualBlueprintType.NUMBER_LINE,
                            hint = "Lấy $total trừ đi $a."
                        )
                    } else {
                        mk(
                            "Tìm số thích hợp thay cho dấu ?: $total - ? = $a", "$x",
                            intOptions(x, listOf(total, a, x + 1, x - 1), min = 1),
                            "$total - ? = $a", VisualBlueprintType.NUMBER_LINE,
                            hint = "Tách $total gồm $a và mấy?"
                        )
                    }
                }
                4 -> {
                    val a = rng.nextInt(2, 8)
                    val b = rng.nextInt(1, 11 - a)
                    val c = rng.nextInt(1, a + b)
                    val r = a + b - c
                    mk(
                        "Tính: $a + $b - $c = ?", "$r",
                        intOptions(r, listOf(a + b, r + 1, r + 2, r - 1)),
                        "$a + $b - $c", VisualBlueprintType.NUMBER_LINE,
                        hint = "Tính từ trái sang phải: $a + $b = ${a + b}, rồi trừ $c."
                    )
                }
                else -> {
                    val a = rng.nextInt(5, 11)
                    val b = rng.nextInt(1, a)
                    val c = rng.nextInt(1, 11 - (a - b))
                    val r = a - b + c
                    mk(
                        "Tính: $a - $b + $c = ?", "$r",
                        intOptions(r, listOf(a - b, r + 1, r - 1, a + b)),
                        "$a - $b + $c", VisualBlueprintType.NUMBER_LINE,
                        hint = "Tính từ trái sang phải: $a - $b = ${a - b}, rồi cộng $c."
                    )
                }
            }

            // Chủ đề 5: Các số đến 100 (Chục và đơn vị, đọc viết, so sánh)
            "G1_05_PLACE_VALUE_100" -> {
                val tens = rng.nextInt(1, 10)
                val units = rng.nextInt(0, 10)
                val num = tens * 10 + units
                when (rng.nextInt(6)) {
                    0 -> {
                        val correct = "$tens chục và $units đơn vị"
                        val ds = listOf(
                            "$units chục và $tens đơn vị",
                            "${tens + 1} chục và $units đơn vị",
                            "$tens chục và ${(units + 1) % 10} đơn vị",
                            "${(tens % 9) + 1} chục và ${(units + 2) % 10} đơn vị"
                        )
                        mk(
                            "Số $num gồm mấy chục và mấy đơn vị?",
                            correct, generate4Options(correct, ds),
                            hint = "Chữ số hàng chục cho biết số chục, chữ số hàng đơn vị cho biết số đơn vị."
                        )
                    }
                    1 -> {
                        val u = rng.nextInt(1, 10)
                        val t2 = rng.nextInt(2, 10)
                        val n2 = t2 * 10 + u
                        mk(
                            "Số gồm $t2 chục và $u đơn vị là số nào?", "$n2",
                            intOptions(n2, listOf(u * 10 + t2, n2 + 10, n2 - 10, t2 * 10), min = 1),
                            hint = "Viết chữ số hàng chục ở bên trái, chữ số hàng đơn vị ở bên phải."
                        )
                    }
                    2 -> {
                        val t2 = rng.nextInt(2, 10)
                        val u2 = rng.nextInt(1, 10)
                        val n2 = t2 * 10 + u2
                        mk(
                            "Số thích hợp điền vào chỗ chấm: $n2 = ${t2 * 10} + ... là:", "$u2",
                            intOptions(u2, listOf(t2, u2 * 10, n2, t2 + u2), min = 1),
                            "$n2 = ${t2 * 10} + ?",
                            hint = "Số $n2 gồm ${t2 * 10} và $u2."
                        )
                    }
                    3 -> {
                        val n2 = rng.nextInt(11, 100)
                        val word = readTwoDigitVi(n2)
                        val rev = (n2 % 10) * 10 + (n2 / 10)
                        mk(
                            "Số '$word' được viết là:", "$n2",
                            intOptions(n2, listOf(rev, n2 + 10, n2 - 1, n2 + 1), min = 10)
                        )
                    }
                    4 -> {
                        val k = rng.nextInt(1, 11)
                        val total = k * 10
                        mk(
                            "$k chục bằng bao nhiêu đơn vị?", "$total",
                            intOptions(total, listOf(k, total + 10, (total - 10).coerceAtLeast(10), k * 10 + 5), min = 1),
                            hint = "1 chục = 10, 10 chục = 100."
                        )
                    }
                    else -> {
                        val d1 = rng.nextInt(2, 6)
                        val d2 = rng.nextInt(d1 + 1, 10)
                        val four = listOf(d1 * 10 + d2, d2 * 10 + d1, d1 * 10 + d1, d2 * 10 + d2)
                        val maxVal = d2 * 10 + d2
                        mk(
                            "Trong các số ${four.shuffled(rng).joinToString(", ")}, số nào lớn nhất?", "$maxVal",
                            intOptions(maxVal, four),
                            hint = "So sánh số chục trước; nếu số chục bằng nhau thì so sánh số đơn vị."
                        )
                    }
                }
            }

            // Chủ đề 6: Phép cộng, phép trừ không nhớ trong phạm vi 100
            "G1_06_ADD_SUB_100_NO_CARRY" -> when (rng.nextInt(6)) {
                0 -> {
                    val tA = rng.nextInt(1, 8)
                    val tB = rng.nextInt(1, 10 - tA)
                    val a = tA * 10
                    val b = tB * 10
                    val sum = a + b
                    mk(
                        "Tính nhẩm: $a + $b = ?", "$sum",
                        intOptions(sum, listOf(tA + tB, sum + 10, sum - 10, abs(a - b)), min = 1),
                        "$a + $b",
                        hint = "$tA chục + $tB chục = ${tA + tB} chục = $sum."
                    )
                }
                1 -> {
                    val tA = rng.nextInt(3, 10)
                    val tB = rng.nextInt(1, tA)
                    val a = tA * 10
                    val b = tB * 10
                    val diff = a - b
                    mk(
                        "Tính nhẩm: $a - $b = ?", "$diff",
                        intOptions(diff, listOf(tA - tB, a + b, diff + 10, diff - 10), min = 1),
                        "$a - $b",
                        hint = "$tA chục - $tB chục = ${tA - tB} chục = $diff."
                    )
                }
                2 -> {
                    val tA = rng.nextInt(1, 10)
                    val uA = rng.nextInt(0, 8)
                    val b = rng.nextInt(1, 10 - uA)
                    val a = tA * 10 + uA
                    val sum = a + b
                    val wrongTens = (tA + b) * 10 + uA
                    mk(
                        "Đặt tính rồi tính: $a + $b = ?", "$sum",
                        intOptions(sum, listOf(wrongTens, sum + 1, sum - 1, sum + 10), min = 1),
                        "$a + $b", VisualBlueprintType.VERTICAL_ARITHMETIC,
                        hint = "Viết $b thẳng cột với chữ số hàng đơn vị ($uA): lấy $uA + $b = ${uA + b}, hạ $tA chục."
                    )
                }
                3 -> {
                    val tA = rng.nextInt(2, 10)
                    val uA = rng.nextInt(2, 10)
                    val b = rng.nextInt(1, uA + 1)
                    val a = tA * 10 + uA
                    val diff = a - b
                    val wrongTens = (tA - 1) * 10 + uA
                    mk(
                        "Đặt tính rồi tính: $a - $b = ?", "$diff",
                        intOptions(diff, listOf(wrongTens, a + b, diff + 1, diff + 10), min = 1),
                        "$a - $b", VisualBlueprintType.VERTICAL_ARITHMETIC,
                        hint = "Trừ ở hàng đơn vị: $uA - $b = ${uA - b}, giữ nguyên $tA chục."
                    )
                }
                4 -> {
                    val uA = rng.nextInt(1, 9)
                    val uB = rng.nextInt(0, 10 - uA)
                    val tA = rr(t, 1..4, 2..6, 3..7)
                    val tB = rng.nextInt(1, 10 - tA)
                    val a = tA * 10 + uA
                    val b = tB * 10 + uB
                    val sum = a + b
                    mk(
                        "Đặt tính rồi tính: $a + $b", "$sum",
                        intOptions(sum, listOf(sum - 10, sum + 10, sum + 1, sum - 1), min = 1),
                        "$a + $b", VisualBlueprintType.VERTICAL_ARITHMETIC,
                        hint = "Cộng hàng đơn vị trước ($uA + $uB = ${uA + uB}), rồi cộng hàng chục ($tA + $tB = ${tA + tB})."
                    )
                }
                else -> {
                    val tA = rng.nextInt(3, 10)
                    val uA = rng.nextInt(2, 10)
                    val tB = rng.nextInt(1, tA)
                    val uB = rng.nextInt(0, uA + 1)
                    val a = tA * 10 + uA
                    val b = tB * 10 + uB
                    val r = a - b
                    mk(
                        "Đặt tính rồi tính: $a - $b", "$r",
                        intOptions(r, listOf(a + b, r + 10, r - 10, r + 1), min = 1),
                        "$a - $b", VisualBlueprintType.VERTICAL_ARITHMETIC,
                        hint = "Trừ hàng đơn vị trước ($uA - $uB = ${uA - uB}), rồi trừ hàng chục ($tA - $tB = ${tA - tB})."
                    )
                }
            }

            // Toán có lời văn Lớp 1 (Kiểm soát chặt 100% KHÔNG NHỚ)
            "G1_07_WORD_PROBLEMS_G1" -> {
                val name = choose(NAMES)
                val friend = choose(NAMES.filter { it != name })
                val item = choose(ITEMS)
                when (rng.nextInt(4)) {
                    0 -> {
                        val tA = rr(t, 0..1, 1..4, 2..6)
                        val uA = rng.nextInt(1, 8)
                        val tB = if (t == 0) 0 else rng.nextInt(0, 9 - tA)
                        val uB = rng.nextInt(1, 10 - uA)
                        val a = tA * 10 + uA
                        val b = tB * 10 + uB
                        val total = a + b
                        mk(
                            "$name có $a $item, $friend cho $name thêm $b $item. Hỏi $name có tất cả bao nhiêu $item?",
                            "$total", intOptions(total, listOf(abs(a - b), total + 2, total - 1, total + 10), min = 1),
                            "$a + $b", VisualBlueprintType.SINGAPORE_BAR_MODEL,
                            hint = "Thêm vào nghĩa là làm phép cộng: $a + $b."
                        )
                    }
                    1 -> {
                        val tA = rr(t, 1..2, 2..5, 4..8)
                        val uA = rng.nextInt(2, 10)
                        val tB = rng.nextInt(0, tA)
                        val uB = rng.nextInt(1, uA)
                        val a = tA * 10 + uA
                        val give = tB * 10 + uB
                        val rest = a - give
                        mk(
                            "$name có $a $item, $name cho $friend $give $item. Hỏi $name còn lại bao nhiêu $item?",
                            "$rest", intOptions(rest, listOf(a + give, rest + 10, rest + 1, rest - 1), min = 1),
                            "$a - $give", VisualBlueprintType.SINGAPORE_BAR_MODEL,
                            hint = "Cho đi nghĩa là làm phép trừ: $a - $give."
                        )
                    }
                    2 -> {
                        val tA = rr(t, 1..2, 1..4, 2..5)
                        val uA = rng.nextInt(1, 8)
                        val tB = rng.nextInt(1, 9 - tA)
                        val uB = rng.nextInt(1, 10 - uA)
                        val a = tA * 10 + uA
                        val b = tB * 10 + uB
                        val total = a + b
                        mk(
                            "$name có $a $item, $friend có $b $item. Hỏi cả hai bạn có tất cả bao nhiêu $item?",
                            "$total", intOptions(total, listOf(abs(a - b), total + 10, total - 1, a), min = 1),
                            "$a + $b", VisualBlueprintType.SINGAPORE_BAR_MODEL,
                            hint = "Gộp lại là làm phép cộng: $a + $b."
                        )
                    }
                    else -> {
                        val tTotal = rr(t, 2..3, 3..6, 5..9)
                        val uTotal = rng.nextInt(3, 10)
                        val tPart = rng.nextInt(1, tTotal)
                        val uPart = rng.nextInt(1, uTotal)
                        val total = tTotal * 10 + uTotal
                        val part = tPart * 10 + uPart
                        val remain = total - part
                        mk(
                            "Nhóm của $name gấp được $total chiếc máy bay giấy, trong đó có $part chiếc màu xanh, còn lại là màu đỏ. Hỏi có bao nhiêu chiếc máy bay màu đỏ?",
                            "$remain", intOptions(remain, listOf(total + part, remain + 10, remain - 1, remain + 1), min = 1),
                            "$total - $part", VisualBlueprintType.SINGAPORE_BAR_MODEL,
                            hint = "Lấy tổng số máy bay ($total) trừ đi số máy bay màu xanh ($part)."
                        )
                    }
                }
            }

            // Chủ đề 7: Độ dài và đo độ dài (Xăng-ti-mét)
            "G1_08_MEASURE_LENGTH_CM" -> when (rng.nextInt(5)) {
                0 -> {
                    val a = rng.nextInt(4, 15)
                    val b = rng.nextInt(2, 15 - a + 5)
                    val sum = a + b
                    mk(
                        "Tính: $a cm + $b cm = ? cm", "$sum cm",
                        intOptions(sum, listOf(abs(a - b), sum - 1, sum + 1, sum + 2), " cm", 1),
                        "$a cm + $b cm", VisualBlueprintType.NUMBER_LINE,
                        hint = "Cộng hai số đo rồi giữ nguyên đơn vị cm."
                    )
                }
                1 -> {
                    val b = rng.nextInt(2, 11)
                    val big = b + rng.nextInt(2, 10)
                    val r = big - b
                    mk(
                        "Tính: $big cm - $b cm = ? cm", "$r cm",
                        intOptions(r, listOf(r + 1, r - 1, big + b, r + 2), " cm", 1),
                        "$big cm - $b cm", VisualBlueprintType.NUMBER_LINE
                    )
                }
                2 -> {
                    val len = rng.nextInt(4, 16)
                    mk(
                        "Việt đặt cây bút chì dọc theo thước thẳng có vạch chia xăng-ti-mét: một đầu bút ở vạch số 0, đầu kia ở vạch số $len. Cây bút chì dài bao nhiêu xăng-ti-mét?",
                        "$len cm", intOptions(len, listOf(len - 1, len + 1, len + 2, 10), " cm", 1),
                        bp = VisualBlueprintType.NUMBER_LINE,
                        hint = "Đặt từ vạch 0 đến vạch số mấy thì vật dài bấy nhiêu xăng-ti-mét."
                    )
                }
                3 -> {
                    val start = rng.nextInt(1, 5)
                    val len = rng.nextInt(4, 12)
                    val end = start + len
                    mk(
                        "Rô-bốt đo một băng giấy dọc theo thước kẻ: một đầu ở vạch số $start, đầu kia ở vạch số $end (đơn vị cm). Băng giấy dài bao nhiêu xăng-ti-mét?",
                        "$len cm", intOptions(len, listOf(end, end + start, len + 1, len - 1), " cm", 1),
                        "$end cm - $start cm", VisualBlueprintType.NUMBER_LINE,
                        hint = "Lấy số ở vạch cuối ($end) trừ đi số ở vạch đầu ($start)."
                    )
                }
                else -> {
                    val b = rng.nextInt(3, 10)
                    val big = b + rng.nextInt(4, 11)
                    val r = big - b
                    mk(
                        "Sợi dây của Mai dài $big cm, Mai cắt đi $b cm. Hỏi sợi dây còn lại dài bao nhiêu xăng-ti-mét?",
                        "$r cm", intOptions(r, listOf(big + b, r + 1, r - 1, big), " cm", 1),
                        "$big cm - $b cm", VisualBlueprintType.NUMBER_LINE
                    )
                }
            }

            // Xem giờ đúng và xem lịch tuần lễ (Chủ đề 6)
            "G1_09_TIME_CLOCK_CALENDAR" -> when (rng.nextInt(5)) {
                0 -> {
                    val hour = rng.nextInt(1, 13)
                    val next = (hour % 12) + 1
                    val prev = if (hour == 1) 12 else hour - 1
                    val correct = "$hour giờ"
                    val ds = listOf("$next giờ", "$prev giờ", "12 giờ", "${((hour + 5) % 12) + 1} giờ")
                    mk(
                        "Đồng hồ có kim ngắn chỉ số $hour, kim dài chỉ số 12. Đồng hồ chỉ mấy giờ?",
                        correct, generate4Options(correct, ds),
                        hint = "Kim dài chỉ số 12 là giờ đúng; kim ngắn chỉ số mấy thì là bấy nhiêu giờ."
                    )
                }
                1 -> {
                    val hour = rng.nextInt(1, 12)
                    mk(
                        "Lúc $hour giờ đúng, kim dài của đồng hồ chỉ vào số mấy?", "12",
                        intOptions(12, listOf(hour, 6, 3, 1), min = 1),
                        hint = "Khi đồng hồ chỉ giờ đúng, kim dài luôn chỉ vào số 12."
                    )
                }
                2 -> {
                    val i = rng.nextInt(1, 6)
                    if (rng.nextBoolean()) {
                        val correct = WEEKDAYS[i + 1]
                        val ds = listOf(WEEKDAYS[i - 1], WEEKDAYS[i], WEEKDAYS[(i + 2) % 7], WEEKDAYS[(i + 3) % 7])
                        mk(
                            "Hôm nay là ${WEEKDAYS[i]}. Ngày mai là thứ mấy?",
                            correct, generate4Options(correct, ds),
                            hint = "Ngày mai là ngày kế tiếp ngay sau hôm nay."
                        )
                    } else {
                        val correct = WEEKDAYS[i - 1]
                        val ds = listOf(WEEKDAYS[i + 1], WEEKDAYS[i], WEEKDAYS[(i + 2) % 7], WEEKDAYS[(i + 3) % 7])
                        mk(
                            "Hôm nay là ${WEEKDAYS[i]}. Hôm qua là thứ mấy?",
                            correct, generate4Options(correct, ds),
                            hint = "Hôm qua là ngày liền trước hôm nay."
                        )
                    }
                }
                3 -> {
                    val day = rng.nextInt(5, 25)
                    val dayOfWeek = choose(WEEKDAYS.dropLast(1))
                    if (rng.nextBoolean()) {
                        val correct = "Ngày ${day + 1}"
                        val ds = listOf("Ngày ${day - 1}", "Ngày $day", "Ngày ${day + 2}", "Ngày ${day + 7}")
                        mk(
                            "Tờ lịch hôm nay ghi $dayOfWeek, ngày $day. Hỏi ngày mai là ngày mấy?",
                            correct, generate4Options(correct, ds),
                            hint = "Ngày mai tăng thêm 1 ngày so với hôm nay."
                        )
                    } else {
                        val correct = "Ngày ${day - 1}"
                        val ds = listOf("Ngày ${day + 1}", "Ngày $day", "Ngày ${day - 2}", "Ngày ${day + 2}")
                        mk(
                            "Tờ lịch hôm nay ghi $dayOfWeek, ngày $day. Hỏi hôm qua là ngày mấy?",
                            correct, generate4Options(correct, ds),
                            hint = "Hôm qua lùi lại 1 ngày so với hôm nay."
                        )
                    }
                }
                else -> {
                    mk(
                        "Một tuần lễ có tất cả bao nhiêu ngày?", "7 ngày",
                        intOptions(7, listOf(5, 6, 8, 10), " ngày", 1),
                        hint = "Từ Thứ Hai đến Chủ nhật có 7 ngày."
                    )
                }
            }

            // Quy luật dãy số (đếm thêm 1, 2, 5, 10) & quy luật dãy hình
            "G1_10_PATTERNS_COUNTING" -> {
                val step = choose(listOf(1, 2, 5, 10))
                when (rng.nextInt(4)) {
                    0 -> {
                        val start = when (step) {
                            10 -> rng.nextInt(1, 6) * 10
                            5 -> rng.nextInt(1, 10) * 5
                            2 -> rng.nextInt(1, 20) * 2
                            else -> rng.nextInt(1, 50)
                        }
                        val s = List(4) { start + step * it }
                        mk(
                            "Tìm số tiếp theo trong dãy số: ${s[0]}; ${s[1]}; ${s[2]}; ...", "${s[3]}",
                            intOptions(s[3], listOf(s[2] + 1, s[3] + step, s[3] - 1, s[2] + 2), min = 1),
                            bp = VisualBlueprintType.NUMBER_LINE,
                            hint = "Đếm thêm $step đơn vị cho mỗi số tiếp theo."
                        )
                    }
                    1 -> {
                        val start = when (step) {
                            10 -> rng.nextInt(1, 6) * 10
                            5 -> rng.nextInt(1, 10) * 5
                            else -> rng.nextInt(2, 30)
                        }
                        val s = List(4) { start + step * it }
                        mk(
                            "Tìm số còn thiếu trong dãy số: ${s[0]}; ${s[1]}; ...; ${s[3]}", "${s[2]}",
                            intOptions(s[2], listOf(s[2] + 1, s[2] - 1, s[1] + 1, s[3] + step), min = 1),
                            bp = VisualBlueprintType.NUMBER_LINE,
                            hint = "Hai số liên tiếp hơn kém nhau $step đơn vị."
                        )
                    }
                    2 -> {
                        val start = when (step) {
                            10 -> rng.nextInt(5, 10) * 10
                            5 -> rng.nextInt(6, 15) * 5
                            else -> rng.nextInt(20, 60)
                        }
                        val s = List(4) { start - step * it }
                        mk(
                            "Tìm số tiếp theo trong dãy số giảm dần: ${s[0]}; ${s[1]}; ${s[2]}; ...", "${s[3]}",
                            intOptions(s[3], listOf(s[2] - 1, s[3] - step, s[3] + 1, s[2] + step), min = 1),
                            bp = VisualBlueprintType.NUMBER_LINE,
                            hint = "Mỗi số đứng sau kém số đứng trước $step đơn vị."
                        )
                    }
                    else -> {
                        val shapes = listOf("Hình vuông", "Hình tròn", "Hình tam giác", "Hình chữ nhật").shuffled(rng)
                        val a = shapes[0]
                        val b = shapes[1]
                        if (rng.nextBoolean()) {
                            mk(
                                "Mi xếp các hình theo quy luật: $a, $b, $a, $b, $a, ... Hình tiếp theo là hình gì?",
                                b, generate4Options(b, shapes),
                                hint = "Hai hình '$a' và '$b' được xếp xen kẽ lặp lại."
                            )
                        } else {
                            mk(
                                "Mi xếp các hình theo quy luật: $a, $a, $b, $a, $a, ... Hình tiếp theo là hình gì?",
                                b, generate4Options(b, shapes),
                                hint = "Cứ 2 '$a' thì đến 1 '$b'."
                            )
                        }
                    }
                }
            }

            else -> {
                val a = rng.nextInt(3, 8)
                val b = rng.nextInt(1, 10 - a)
                val sum = a + b
                mk(
                    "Tính kết quả phép tính: $a + $b = ?", "$sum",
                    intOptions(sum, listOf(sum + 1, sum - 1, sum + 2)), "$a + $b"
                )
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // GRADE 2 (SGK Toán 2 - Kết nối tri thức với cuộc sống)
    // ------------------------------------------------------------------------------------------
    private fun generateGrade2Node(ctx: Ctx): AcademicProblem = with(ctx) {
        when (nodeId) {
            // Chủ đề 1: Số hạng - Tổng, Số bị trừ - Số trừ - Hiệu, Tia số, Số liền trước/sau
            "G2_01_TERMS_AND_NUMBER_LINE" -> when (rng.nextInt(4)) {
                0 -> {
                    val a = rng.nextInt(15, 50)
                    val b = rng.nextInt(12, 40)
                    val s = a + b
                    val pick = rng.nextInt(3)
                    val x = when (pick) { 0 -> a; 1 -> b; else -> s }
                    val correct = if (pick == 2) "Tổng" else "Số hạng"
                    mk(
                        "Trong phép tính $a + $b = $s, số $x được gọi là gì?",
                        correct, roleOptions(correct),
                        hint = "Trong phép cộng a + b = c: a và b là các số hạng, c là tổng."
                    )
                }
                1 -> {
                    val a = rng.nextInt(30, 100)
                    val b = rng.nextInt(11, a - 9)
                    val d = a - b
                    val pick = rng.nextInt(3)
                    val x = when (pick) { 0 -> a; 1 -> b; else -> d }
                    val correct = when (pick) { 0 -> "Số bị trừ"; 1 -> "Số trừ"; else -> "Hiệu" }
                    mk(
                        "Trong phép tính $a - $b = $d, số $x được gọi là gì?",
                        correct, roleOptions(correct),
                        hint = "Trong phép trừ a - b = c: a là số bị trừ, b là số trừ, c là hiệu."
                    )
                }
                2 -> {
                    val a = rng.nextInt(25, 65)
                    val b = rng.nextInt(10, 25)
                    if (rng.nextBoolean()) {
                        val sum = a + b
                        mk(
                            "Biết hai số hạng lần lượt là $a và $b. Tổng của hai số đó là:", "$sum",
                            intOptions(sum, listOf(a - b, sum + 10, sum - 1, sum + 1), min = 1),
                            "$a + $b", hint = "Muốn tìm tổng, ta cộng hai số hạng lại."
                        )
                    } else {
                        val diff = a - b
                        mk(
                            "Biết số bị trừ là $a, số trừ là $b. Hiệu của hai số đó là:", "$diff",
                            intOptions(diff, listOf(a + b, diff + 10, diff - 1, diff + 1), min = 1),
                            "$a - $b", hint = "Muốn tìm hiệu, ta lấy số bị trừ trừ đi số trừ."
                        )
                    }
                }
                else -> {
                    val n = rng.nextInt(11, 100)
                    val isPrev = rng.nextBoolean()
                    val ans = if (isPrev) n - 1 else n + 1
                    mk(
                        "Trên tia số, số liền ${if (isPrev) "trước" else "sau"} của $n là số nào?", "$ans",
                        intOptions(ans, listOf(n, n + 1, n - 1, n - 2, n + 2, n - 10), min = 1),
                        bp = VisualBlueprintType.NUMBER_LINE,
                        hint = if (isPrev) "Số liền trước kém số đã cho 1 đơn vị." else "Số liền sau hơn số đã cho 1 đơn vị."
                    )
                }
            }

            // Chủ đề 2: Phép cộng, phép trừ (qua 10) trong phạm vi 20 (Phương pháp tách số làm tròn 10 của KNTT)
            "G2_02_MAKE_10_ADD_SUB" -> when (rng.nextInt(3)) {
                0 -> {
                    val a = rng.nextInt(6, 10)
                    val b = rng.nextInt(11 - a, 10)
                    val sum = a + b
                    val need = 10 - a
                    val rem = b - need
                    mk(
                        "Tính nhẩm (qua 10): $a + $b = ?", "$sum",
                        intOptions(sum, listOf(sum - 1, sum + 1, a + 10, sum - 2), min = 1),
                        "$a + $b",
                        hint = "Tách $b = $need + $rem; lấy $a + $need = 10, rồi 10 + $rem = $sum."
                    )
                }
                1 -> {
                    val s = rng.nextInt(11, 19)
                    val u = s % 10
                    val b = rng.nextInt(u + 1, 10)
                    val r = s - b
                    val rem = b - u
                    mk(
                        "Tính nhẩm (qua 10): $s - $b = ?", "$r",
                        intOptions(r, listOf(r - 1, r + 1, r + 10, b - u), min = 1),
                        "$s - $b",
                        hint = "Tách $b = $u + $rem; lấy $s - $u = 10, rồi 10 - $rem = $r."
                    )
                }
                else -> {
                    val a = rng.nextInt(7, 10)
                    val b = rng.nextInt(11 - a, 10)
                    val sum = a + b
                    mk(
                        "Mai có $a bông hoa, Việt có $b bông hoa. Hỏi hai bạn có tất cả bao nhiêu bông hoa?",
                        "$sum", intOptions(sum, listOf(sum - 1, sum + 1, abs(a - b), sum + 2), min = 1),
                        "$a + $b",
                        hint = "Lấy $a cộng $b (gộp cho đủ 10 rồi cộng với số còn lại)."
                    )
                }
            }

            // Chủ đề 4: Phép cộng, phép trừ (có nhớ) trong phạm vi 100
            "G2_03_ADD_SUB_100_WITH_CARRY" -> if (rng.nextBoolean()) {
                val uA = rng.nextInt(5, 10)
                val uB = rng.nextInt(10 - uA, 10)
                val tA = rr(t, 1..3, 2..5, 3..6)
                val tB = rr(t, 0..2, 1..3, 1..3)
                val a = tA * 10 + uA
                val b = tB * 10 + uB
                val sum = a + b
                val wrongNoCarry = (tA + tB) * 10 + ((uA + uB) % 10)
                mk(
                    "Đặt tính rồi tính tổng: $a + $b = ?", "$sum",
                    intOptions(sum, listOf(wrongNoCarry, sum + 10, sum - 1, sum + 1), min = 1),
                    "$a + $b", VisualBlueprintType.VERTICAL_ARITHMETIC,
                    hint = "Hàng đơn vị: $uA + $uB = ${uA + uB}, viết ${(uA + uB) % 10} nhớ 1 sang hàng chục."
                )
            } else {
                val tA = rng.nextInt(3, 10)
                val uA = rng.nextInt(0, 8)
                val uB = rng.nextInt(uA + 1, 10)
                val tB = rng.nextInt(0, tA)
                val a = tA * 10 + uA
                val b = tB * 10 + uB
                val r = a - b
                val wrongNoBorrow = (tA - tB) * 10 + (uB - uA)
                val wrongForgetReturn = (tA - tB) * 10 + (uA + 10 - uB)
                mk(
                    "Đặt tính rồi tính hiệu: $a - $b = ?", "$r",
                    intOptions(r, listOf(wrongNoBorrow, wrongForgetReturn, r - 10, r + 1), min = 1),
                    "$a - $b", VisualBlueprintType.VERTICAL_ARITHMETIC,
                    hint = "Hàng đơn vị: $uA không trừ được $uB, lấy ${uA + 10} - $uB = ${uA + 10 - uB}, viết ${uA + 10 - uB} nhớ 1."
                )
            }

            // Bài toán về nhiều hơn, ít hơn một số đơn vị
            "G2_04_WORD_MORE_LESS" -> {
                val a = rng.nextInt(20, 61)
                val diff = rng.nextInt(5, 21)
                when (rng.nextInt(4)) {
                    0 -> mk(
                        "Lớp 2A trồng được $a cây hoa. Lớp 2B trồng được nhiều hơn lớp 2A là $diff cây hoa. Hỏi lớp 2B trồng được bao nhiêu cây hoa?",
                        "${a + diff}", intOptions(a + diff, listOf(a - diff, a, a + diff + 10, a + diff - 1), min = 1),
                        "$a + $diff", VisualBlueprintType.SINGAPORE_BAR_MODEL,
                        hint = "Bài toán về nhiều hơn: lấy số cây của lớp 2A cộng với $diff."
                    )
                    1 -> mk(
                        "Việt gấp được $a chiếc thuyền giấy. Nam gấp được ít hơn Việt $diff chiếc. Hỏi Nam gấp được bao nhiêu chiếc thuyền giấy?",
                        "${a - diff}", intOptions(a - diff, listOf(a + diff, a, a - diff + 10, a - diff - 1), min = 1),
                        "$a - $diff", VisualBlueprintType.SINGAPORE_BAR_MODEL,
                        hint = "Bài toán về ít hơn: lấy số thuyền của Việt trừ đi $diff."
                    )
                    2 -> mk(
                        "Mai có $a nhãn vở và nhiều hơn Mi $diff nhãn vở. Hỏi Mi có bao nhiêu nhãn vở?",
                        "${a - diff}", intOptions(a - diff, listOf(a + diff, a, a - diff + 2, a - diff - 1), min = 1),
                        "$a - $diff", VisualBlueprintType.SINGAPORE_BAR_MODEL,
                        hint = "Mai nhiều hơn Mi nghĩa là Mi ít hơn Mai $diff nhãn vở. Ta làm phép trừ."
                    )
                    else -> {
                        val b = a + diff
                        mk(
                            "Bao gạo nếp nặng $b kg, bao gạo tẻ nặng $a kg. Hỏi bao gạo nếp nặng hơn bao gạo tẻ bao nhiêu ki-lô-gam?",
                            "$diff kg", intOptions(diff, listOf(a + b, b, a, diff + 2), " kg", 1),
                            "$b - $a", VisualBlueprintType.SINGAPORE_BAR_MODEL,
                            hint = "Muốn biết nặng hơn bao nhiêu, ta lấy số lớn trừ đi số bé."
                        )
                    }
                }
            }

            // Chủ đề 8: Phép nhân, phép chia (Bảng nhân 2, 5; Bảng chia 2, 5; Thừa số - Tích; Số bị chia - Số chia - Thương)
            "G2_05_MUL_DIV_2_5" -> {
                val f1 = if (rng.nextBoolean()) 2 else 5
                val f2 = rng.nextInt(2, 11)
                val prod = f1 * f2
                when (rng.nextInt(5)) {
                    0 -> mk(
                        "Tính: $f1 × $f2 = ?", "$prod",
                        intOptions(prod, listOf(prod - f1, prod + f1, f1 + f2, prod + 1), min = 1),
                        "$f1 × $f2", hint = "$f1 được lấy $f2 lần."
                    )
                    1 -> mk(
                        "Tính: $prod : $f1 = ?", "$f2",
                        intOptions(f2, listOf(f2 + 1, f2 - 1, f2 + 2, prod - f1), min = 1),
                        "$prod : $f1", hint = "Nhẩm bảng nhân $f1: $f1 × $f2 = $prod nên $prod : $f1 = $f2."
                    )
                    2 -> mk(
                        "Mỗi bàn học có $f1 bạn ngồi. Hỏi $f2 bàn học như vậy có tất cả bao nhiêu bạn?",
                        "$prod", intOptions(prod, listOf(prod - f1, prod + f1, f1 + f2, prod + 1), min = 1),
                        "$f1 × $f2", hint = "$f1 bạn được lấy $f2 lần, ta làm phép nhân $f1 × $f2."
                    )
                    3 -> mk(
                        "Cô giáo chia đều $prod quyển vở cho $f1 bạn. Hỏi mỗi bạn được mấy quyển vở?",
                        "$f2", intOptions(f2, listOf(prod - f1, f2 + 1, f2 - 1, f1), min = 1),
                        "$prod : $f1", hint = "Chia đều cho $f1 bạn, ta lấy $prod : $f1."
                    )
                    else -> {
                        val roles = listOf("Thừa số", "Tích", "Số bị chia", "Số chia", "Thương")
                        if (rng.nextBoolean()) {
                            val askProd = rng.nextBoolean()
                            val x = if (askProd) prod else f1
                            val correct = if (askProd) "Tích" else "Thừa số"
                            mk(
                                "Trong phép nhân $f1 × $f2 = $prod, số $x được gọi là gì?",
                                correct, generate4Options(correct, roles.filter { it != correct }.shuffled(rng))
                            )
                        } else {
                            val pick = rng.nextInt(3)
                            val x = when (pick) { 0 -> prod; 1 -> f1; else -> f2 }
                            val correct = when (pick) { 0 -> "Số bị chia"; 1 -> "Số chia"; else -> "Thương" }
                            mk(
                                "Trong phép chia $prod : $f1 = $f2, số $x được gọi là gì?",
                                correct, generate4Options(correct, roles.filter { it != correct }.shuffled(rng))
                            )
                        }
                    }
                }
            }

            // Chủ đề 10 & 11: Các số trong phạm vi 1000, Cộng trừ trong phạm vi 1000
            "G2_06_NUM_1000_ADD_SUB" -> when (rng.nextInt(5)) {
                0 -> {
                    val a = rr(t, 100..300, 100..500, 200..650)
                    val b = rng.nextInt(100, minOf(400, 1000 - a))
                    val sum = a + b
                    mk(
                        "Tính tổng trong phạm vi 1000: $a + $b = ?", "$sum",
                        intOptions(sum, listOf(sum - 100, sum + 100, sum - 10, sum + 10), min = 1), "$a + $b",
                        VisualBlueprintType.VERTICAL_ARITHMETIC,
                        hint = "Đặt tính thẳng cột hàng đơn vị, hàng chục, hàng trăm rồi cộng từ phải sang trái."
                    )
                }
                1 -> {
                    val a = rng.nextInt(300, 1000)
                    val b = rng.nextInt(100, a - 99)
                    val r = a - b
                    mk(
                        "Tính hiệu trong phạm vi 1000: $a - $b = ?", "$r",
                        intOptions(r, listOf(r - 100, r + 100, r - 10, r + 10, a + b), min = 1), "$a - $b",
                        VisualBlueprintType.VERTICAL_ARITHMETIC,
                        hint = "Đặt tính thẳng cột rồi trừ từ phải sang trái."
                    )
                }
                2 -> {
                    val h = rng.nextInt(1, 10)
                    val ten = rng.nextInt(0, 10)
                    val u = rng.nextInt(1, 10)
                    val n = h * 100 + ten * 10 + u
                    mk(
                        "Số gồm $h trăm, $ten chục và $u đơn vị viết là:", "$n",
                        intOptions(n, listOf(h * 100 + u * 10 + ten, u * 100 + ten * 10 + h, n + 10, n + 100), min = 100)
                    )
                }
                3 -> {
                    val n = rng.nextInt(101, 999)
                    val after = rng.nextBoolean()
                    val correct = if (after) n + 1 else n - 1
                    mk(
                        "Số liền ${if (after) "sau" else "trước"} của $n là số nào?", "$correct",
                        intOptions(correct, listOf(n, n + 1, n - 1, n + 10, n - 10, correct + 10), min = 1)
                    )
                }
                else -> {
                    val four = generateSequence { rng.nextInt(100, 1000) }.distinct().take(4).toList()
                    val askMax = rng.nextBoolean()
                    val c = if (askMax) four.maxOrNull()!! else four.minOrNull()!!
                    mk(
                        "Số nào ${if (askMax) "lớn nhất" else "bé nhất"} trong các số sau: ${four.joinToString(", ")}?",
                        "$c", intOptions(c, four),
                        hint = "So sánh chữ số hàng trăm trước, rồi đến hàng chục, hàng đơn vị."
                    )
                }
            }

            // Chủ đề 5 & 9: Đường gấp khúc, Hình tứ giác, Khối trụ, Khối cầu
            "G2_07_GEOMETRY_G2" -> when (rng.nextInt(5)) {
                0, 1 -> {
                    val n = if (rng.nextBoolean()) 3 else 4
                    val segs = List(n) { rr(t, 2..6, 3..9, 4..12) }
                    val total = segs.sum()
                    val txt = segs.joinToString(", ") { "$it cm" }
                    mk(
                        "Một đường gấp khúc gồm $n đoạn thẳng có độ dài lần lượt là $txt. Độ dài đường gấp khúc đó là:",
                        "$total cm", intOptions(total, listOf(total - segs[0], total + 2, total - 2, total + 1), " cm", 1),
                        bp = VisualBlueprintType.GEOMETRY_GRID,
                        hint = "Độ dài đường gấp khúc bằng tổng độ dài các đoạn thẳng thành phần."
                    )
                }
                2 -> {
                    val s = List(3) { rr(t, 3..8, 4..12, 6..15) }
                    val p = s.sum()
                    mk(
                        "Tính chu vi hình tam giác có độ dài các cạnh là ${s[0]} cm, ${s[1]} cm và ${s[2]} cm:",
                        "$p cm", intOptions(p, listOf(s[0] + s[1], p + 2, p - 2, s[0] * 3), " cm", 1),
                        bp = VisualBlueprintType.GEOMETRY_GRID,
                        hint = "Chu vi hình tam giác bằng tổng độ dài 3 cạnh."
                    )
                }
                3 -> {
                    val s = List(4) { rr(t, 3..8, 4..12, 6..15) }
                    val p = s.sum()
                    mk(
                        "Tính chu vi hình tứ giác có độ dài các cạnh là ${s[0]} cm, ${s[1]} cm, ${s[2]} cm và ${s[3]} cm:",
                        "$p cm", intOptions(p, listOf(p - s[3], p + 2, p - 2, s[0] * 4), " cm", 1),
                        bp = VisualBlueprintType.GEOMETRY_GRID,
                        hint = "Chu vi hình tứ giác bằng tổng độ dài 4 cạnh."
                    )
                }
                else -> {
                    // Khối trụ & Khối cầu (Bài 46 - Toán 2 KNTT)
                    val (obj, ans) = choose(
                        listOf(
                            "quả bóng đá" to "Khối cầu",
                            "viên bi ve tròn" to "Khối cầu",
                            "quả địa cầu" to "Khối cầu",
                            "lon nước ngọt" to "Khối trụ",
                            "hộp sữa đặc bằng sắt" to "Khối trụ",
                            "khúc gỗ tròn thẳng" to "Khối trụ"
                        )
                    )
                    val all = listOf("Khối cầu", "Khối trụ", "Khối lập phương", "Khối hộp chữ nhật")
                    mk(
                        "Trong thực tế, '$obj' có dạng hình khối gì?",
                        ans, generate4Options(ans, all.filter { it != ans })
                    )
                }
            }

            // Đơn vị đo: dm, m, km, kg, l (Chuẩn phạm vi số đến 1000 của Lớp 2)
            "G2_08_MEASURE_DM_M_KM_KG_L" -> when (rng.nextInt(5)) {
                0 -> {
                    val m = rng.nextInt(1, 10)
                    mk(
                        "Điền số thích hợp: $m m = ... dm", "${m * 10} dm",
                        intOptions(m * 10, listOf(m * 100, m, m * 10 + 5, m + 10), " dm", 1),
                        hint = "1 m = 10 dm."
                    )
                }
                1 -> {
                    val m = rng.nextInt(1, 10)
                    mk(
                        "Điền số thích hợp: ${m * 10} dm = ... m", "$m m",
                        intOptions(m, listOf(m * 10, m * 100, m + 1, m + 2), " m", 1),
                        hint = "10 dm = 1 m."
                    )
                }
                2 -> {
                    val m = rng.nextInt(1, 10)
                    mk(
                        "Điền số thích hợp: $m m = ... cm", "${m * 100} cm",
                        intOptions(m * 100, listOf(m * 10, m, m * 100 + 10, m * 100 - 10), " cm", 1),
                        hint = "1 m = 100 cm."
                    )
                }
                3 -> {
                    // Lớp 2 chỉ học số đến 1000 nên 1 km = 1000 m là chuẩn tuyệt đối
                    mk(
                        "Đổi đơn vị đo độ dài: 1 km = ... m", "1000 m",
                        intOptions(1000, listOf(100, 10, 500, 200), " m", 1),
                        hint = "1 ki-lô-mét bằng 1000 mét."
                    )
                }
                else -> {
                    val unit = choose(listOf("kg", "l"))
                    val a = rng.nextInt(15, 60)
                    val b = rng.nextInt(10, 40)
                    val sum = a + b
                    val text = if (unit == "kg") {
                        "Bao gạo thứ nhất nặng $a kg, bao thứ hai nặng $b kg. Cả hai bao gạo nặng bao nhiêu ki-lô-gam?"
                    } else {
                        "Can thứ nhất đựng $a l dầu, can thứ hai đựng $b l dầu. Cả hai can đựng bao nhiêu lít dầu?"
                    }
                    mk(
                        text, "$sum $unit",
                        intOptions(sum, listOf(abs(a - b), sum + 10, sum - 1, a), " $unit", 1),
                        "$a $unit + $b $unit"
                    )
                }
            }

            // Chủ đề 6: Ngày - giờ, Giờ - phút (15 phút, 30 phút), Ngày - tháng, Tiền Việt Nam (phạm vi 1000 đồng)
            "G2_09_TIME_MONEY_G2" -> when (rng.nextInt(5)) {
                0 -> {
                    val hour = rng.nextInt(1, 12)
                    val correct = "$hour giờ rưỡi"
                    val ds = listOf("${hour + 1} giờ rưỡi", "$hour giờ", "${hour + 1} giờ", "${hour + 2} giờ rưỡi")
                    mk(
                        "Đồng hồ chỉ $hour giờ 30 phút hay còn gọi là mấy giờ?",
                        correct, generate4Options(correct, ds),
                        hint = "30 phút còn gọi là 'rưỡi'."
                    )
                }
                1 -> {
                    val hour = rng.nextInt(1, 12)
                    val m = choose(listOf(0, 15, 30))
                    val num = when (m) { 15 -> 3; 30 -> 6; else -> 12 }
                    val time = if (m == 0) "$hour giờ đúng" else "$hour giờ $m phút"
                    mk(
                        "Khi đồng hồ chỉ $time, kim dài (kim phút) chỉ vào số mấy?", "$num",
                        generate4Options("$num", listOf("3", "6", "9", "12")),
                        hint = "Kim dài chỉ số 12 là giờ đúng, chỉ số 3 là 15 phút, chỉ số 6 là 30 phút."
                    )
                }
                2 -> {
                    val (q, ans, ds) = choose(
                        listOf(
                            Triple("Một ngày có tất cả bao nhiêu giờ?", "24 giờ", listOf(12, 60, 30, 10)),
                            Triple("Một giờ có bao nhiêu phút?", "60 phút", listOf(30, 24, 100, 15))
                        )
                    )
                    val unit = if (ans.endsWith("giờ")) " giờ" else " phút"
                    val numAns = ans.substringBefore(" ").toInt()
                    mk(q, ans, intOptions(numAns, ds, unit, 1))
                }
                3 -> {
                    val notes = listOf(100, 200, 500)
                    val a = choose(notes)
                    val b = choose(notes)
                    val sum = a + b
                    mk(
                        "Trong heo đất của Mi có một tờ ${fmtMoney(a)} và một tờ ${fmtMoney(b)}. Hỏi Mi có tất cả bao nhiêu tiền?",
                        fmtMoney(sum), moneyOptions(sum, listOf(sum + 100, sum + 200, sum - 100, 1000)),
                        "$a + $b"
                    )
                }
                else -> {
                    val price = choose(listOf(200, 300, 400, 500, 600, 700, 800))
                    val change = 1000 - price
                    mk(
                        "Việt mua một chiếc nhãn vở giá ${fmtMoney(price)} và đưa cho cô bán hàng tờ ${fmtMoney(1000)}. Cô bán hàng cần trả lại Việt bao nhiêu tiền?",
                        fmtMoney(change), moneyOptions(change, listOf(price, change + 100, (change - 100).coerceAtLeast(100), 900)),
                        "1000 - $price"
                    )
                }
            }

            // Chủ đề 13: Thu thập, phân loại, kiểm đếm số liệu; Biểu đồ tranh; Chắc chắn, có thể, không thể
            "G2_10_STATS_PROBABILITY_G2" -> when (rng.nextInt(3)) {
                0, 1 -> {
                    val names = listOf("Táo", "Cam", "Chuối", "Xoài")
                    val counts = generateSequence { rng.nextInt(3, 16) }.distinct().take(4).toList()
                    val most = rng.nextBoolean()
                    val idx = if (most) counts.indexOf(counts.maxOrNull()!!) else counts.indexOf(counts.minOrNull()!!)
                    val table = names.indices.joinToString(", ") { "${names[it]}: ${counts[it]} quả" }
                    val correct = names[idx]
                    mk(
                        "Rô-bốt kiểm đếm các loại quả trong rổ: $table. Loại quả nào có số lượng ${if (most) "nhiều nhất" else "ít nhất"}?",
                        correct, generate4Options(correct, names.filter { it != correct }.shuffled(rng))
                    )
                }
                else -> {
                    val red = rng.nextInt(2, 8)
                    val blue = rng.nextInt(2, 8)
                    val (stmt, correct) = when (rng.nextInt(3)) {
                        0 -> "Trong hộp chỉ có $red quả bóng màu đỏ. Nam không nhìn vào hộp, lấy ra 1 quả bóng. Khả năng Nam lấy được quả bóng màu đỏ là:" to "Chắc chắn"
                        1 -> "Trong hộp chỉ có $red quả bóng màu đỏ. Nam không nhìn vào hộp, lấy ra 1 quả bóng. Khả năng Nam lấy được quả bóng màu xanh là:" to "Không thể"
                        else -> "Trong hộp có $red quả bóng màu đỏ và $blue quả bóng màu xanh. Nam lấy ngẫu nhiên 1 quả bóng. Khả năng Nam lấy được quả bóng màu xanh là:" to "Có thể"
                    }
                    val all = listOf("Chắc chắn", "Có thể", "Không thể", "Chưa biết")
                    mk(stmt, correct, generate4Options(correct, all.filter { it != correct }))
                }
            }

            else -> {
                val a = rng.nextInt(15, 45)
                val b = rng.nextInt(12, 35)
                val sum = a + b
                mk(
                    "Tính kết quả: $a + $b = ?", "$sum",
                    intOptions(sum, listOf(sum + 10, sum - 10, sum + 1)), "$a + $b"
                )
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // GRADE 3 (SGK Toán 3 - Kết nối tri thức với cuộc sống)
    // ------------------------------------------------------------------------------------------
    private fun generateGrade3Node(ctx: Ctx): AcademicProblem = with(ctx) {
        when (nodeId) {
            // Bảng nhân, bảng chia từ 3 đến 9; Nhân/chia số có 2, 3 chữ số với số có 1 chữ số
            "G3_01_MUL_DIV_TABLES_3_9" -> {
                val a = rng.nextInt(3, 10)
                val b = rng.nextInt(2, 10)
                val prod = a * b
                when (rng.nextInt(4)) {
                    0 -> mk(
                        "Tính nhẩm: $a × $b = ?", "$prod",
                        intOptions(prod, listOf(prod - a, prod + b, prod + a, prod - b), min = 1), "$a × $b"
                    )
                    1 -> mk(
                        "Tính nhẩm: $prod : $a = ?", "$b",
                        intOptions(b, listOf(b + 1, b - 1, b + 2, a), min = 1), "$prod : $a"
                    )
                    2 -> {
                        val m = rr(t, 12..30, 21..85, 105..240)
                        val k = rng.nextInt(2, 7)
                        val res = m * k
                        mk(
                            "Đặt tính rồi tính: $m × $k = ?", "$res",
                            intOptions(res, listOf(res - k, res + k, res + 10, res - 10), min = 1),
                            "$m × $k", VisualBlueprintType.VERTICAL_ARITHMETIC,
                            hint = "Nhân lần lượt từ phải sang trái (hàng đơn vị, hàng chục...)."
                        )
                    }
                    else -> mk(
                        "Mỗi hộp có $a chiếc bút màu. Hỏi $b hộp như vậy có tất cả bao nhiêu chiếc bút màu?",
                        "$prod", intOptions(prod, listOf(prod - a, prod + a, a + b, prod + b), min = 1), "$a × $b"
                    )
                }
            }

            // Bài 2, 12: Tìm thành phần trong phép cộng, phép trừ, phép nhân, phép chia
            "G3_02_FIND_UNKNOWN_COMPONENT" -> when (rng.nextInt(6)) {
                0 -> {
                    val b = rng.nextInt(2, 10)
                    val x = rng.nextInt(10, 46)
                    mk(
                        "Tìm thừa số chưa biết trong phép tính: ? × $b = ${x * b}", "$x",
                        intOptions(x, listOf(x + b, x * b * b, x - b, x + 1), min = 1),
                        "? × $b = ${x * b}",
                        hint = "Muốn tìm thừa số chưa biết, ta lấy tích chia cho thừa số kia."
                    )
                }
                1 -> {
                    val b = rng.nextInt(2, 10)
                    val q = rng.nextInt(5, 51)
                    val x = q * b
                    mk(
                        "Tìm số bị chia trong phép tính: ? : $b = $q", "$x",
                        intOptions(x, listOf(q, q + b, x + b, x - b), min = 1),
                        "? : $b = $q",
                        hint = "Muốn tìm số bị chia, ta lấy thương nhân với số chia."
                    )
                }
                2 -> {
                    val q = rng.nextInt(3, 10)
                    val x = rng.nextInt(3, 10)
                    val dividend = q * x
                    mk(
                        "Tìm số chia trong phép tính: $dividend : ? = $q", "$x",
                        intOptions(x, listOf(dividend - q, dividend + q, x + 1, x - 1), min = 1),
                        "$dividend : ? = $q",
                        hint = "Muốn tìm số chia, ta lấy số bị chia chia cho thương."
                    )
                }
                3 -> {
                    val a = rng.nextInt(100, 501)
                    val s = rng.nextInt(a + 100, 1000)
                    val x = s - a
                    mk(
                        "Tìm số hạng chưa biết: ? + $a = $s", "$x",
                        intOptions(x, listOf(s + a, a, x + 10, x - 10), min = 1),
                        "? + $a = $s",
                        hint = "Muốn tìm một số hạng, ta lấy tổng trừ đi số hạng kia."
                    )
                }
                4 -> {
                    val a = rng.nextInt(100, 501)
                    val d = rng.nextInt(100, 401)
                    val x = d + a
                    mk(
                        "Tìm số bị trừ: ? - $a = $d", "$x",
                        intOptions(x, listOf(abs(d - a), a, x + 10, x - 10), min = 1),
                        "? - $a = $d",
                        hint = "Muốn tìm số bị trừ, ta lấy hiệu cộng với số trừ."
                    )
                }
                else -> {
                    val a = rng.nextInt(200, 901)
                    val d = rng.nextInt(100, a - 99)
                    val x = a - d
                    mk(
                        "Tìm số trừ: $a - ? = $d", "$x",
                        intOptions(x, listOf(a + d, d, x + 10, x - 10), min = 1),
                        "$a - ? = $d",
                        hint = "Muốn tìm số trừ, ta lấy số bị trừ trừ đi hiệu."
                    )
                }
            }

            // Bài 11, 14, 26: Gấp một số lên một số lần; Giảm một số đi một số lần; Một phần mấy
            "G3_03_TIMES_MORE_LESS_FRAC" -> {
                val k = rng.nextInt(2, 10)
                when (rng.nextInt(4)) {
                    0 -> {
                        val a = rng.nextInt(6, 15)
                        mk(
                            "Gấp $a lên $k lần ta được số nào?", "${a * k}",
                            intOptions(a * k, listOf(a + k, a * (k - 1), a * k + k, a * k - a), min = 1),
                            "$a × $k", hint = "Muốn gấp một số lên một số lần, ta lấy số đó nhân với số lần."
                        )
                    }
                    1 -> {
                        val a = rng.nextInt(3, 15)
                        val p = a * k
                        mk(
                            "Giảm $p đi $k lần ta được số nào?", "$a",
                            intOptions(a, listOf(p - k, a + 1, a - 1, k), min = 1),
                            "$p : $k", hint = "Muốn giảm một số đi một số lần, ta lấy số đó chia cho số lần."
                        )
                    }
                    2 -> {
                        val a = rng.nextInt(3, 13)
                        mk(
                            "Đoạn thẳng AB dài ${a * k} cm, đoạn thẳng CD dài $a cm. Độ dài đoạn thẳng AB gấp mấy lần độ dài đoạn thẳng CD?",
                            "$k lần", intOptions(k, listOf(k + 1, k - 1, a, a * k - a), " lần", 1),
                            "${a * k} : $a", hint = "Lấy độ dài đoạn lớn chia cho độ dài đoạn bé."
                        )
                    }
                    else -> {
                        val m = rng.nextInt(3, 13)
                        val n = k * m
                        mk(
                            "Đã tô màu 1/$k số ngôi sao trong $n ngôi sao. Hỏi đã tô màu bao nhiêu ngôi sao?",
                            "$m", intOptions(m, listOf(n - k, m + 1, k, n - m), min = 1),
                            "$n : $k", hint = "Tìm 1/$k của $n ngôi sao: ta lấy $n chia cho $k."
                        )
                    }
                }
            }

            // Bài 25: Phép chia hết, phép chia có dư
            "G3_04_DIVISION_WITH_REMAINDER" -> {
                val divisor = rr(t, 3..5, 4..9, 6..9)
                val quotient = rr(t, 2..6, 3..9, 5..15)
                val remainder = rng.nextInt(1, divisor)
                val dividend = divisor * quotient + remainder
                when (rng.nextInt(4)) {
                    0 -> {
                        val correct = "$quotient (dư $remainder)"
                        val ds = listOf(
                            "$quotient (dư ${remainder + 1})",
                            "${quotient + 1} (dư $remainder)",
                            "${quotient - 1} (dư $remainder)",
                            "$quotient (dư 0)"
                        )
                        mk(
                            "Thực hiện phép chia có dư: $dividend : $divisor = ?",
                            correct, generate4Options(correct, ds),
                            "$dividend : $divisor", hint = "Số dư luôn bé hơn số chia ($divisor)."
                        )
                    }
                    1 -> mk(
                        "Việt có $dividend viên bi, Việt chia đều vào các hộp, mỗi hộp $divisor viên bi. Hỏi chia được nhiều nhất mấy hộp nguyên như vậy?",
                        "$quotient hộp", intOptions(quotient, listOf(quotient + 1, quotient - 1, remainder, quotient + 2), " hộp", 1),
                        "$dividend : $divisor", hint = "Thực hiện phép chia $dividend : $divisor = $quotient (dư $remainder)."
                    )
                    2 -> mk(
                        "Một đoàn khách gồm $dividend người muốn qua sông, mỗi chuyến thuyền chở được nhiều nhất $divisor người (không kể người lái). Hỏi cần ít nhất bao nhiêu chuyến thuyền để chở hết đoàn khách?",
                        "${quotient + 1} chuyến",
                        intOptions(quotient + 1, listOf(quotient, quotient - 1, quotient + 2, remainder), " chuyến", 1),
                        "$dividend : $divisor",
                        hint = "$dividend : $divisor = $quotient (dư $remainder người). Còn dư $remainder người nên cần thêm 1 chuyến thuyền nữa."
                    )
                    else -> mk(
                        "Trong một phép chia có dư với số chia là $divisor, số dư lớn nhất có thể là bao nhiêu?",
                        "${divisor - 1}", intOptions(divisor - 1, listOf(divisor, divisor + 1, divisor - 2, 1), min = 1),
                        hint = "Số dư luôn bé hơn số chia, nên số dư lớn nhất kém số chia 1 đơn vị."
                    )
                }
            }

            // Bài 30: Làm quen với biểu thức, Tính giá trị của biểu thức
            "G3_05_ORDER_OF_OPERATIONS" -> when (rng.nextInt(4)) {
                0 -> {
                    val a = rr(t, 5..15, 10..30, 20..60)
                    val b = rng.nextInt(4, 10)
                    val c = rng.nextInt(3, 9)
                    val r = a + b * c
                    mk(
                        "Tính giá trị của biểu thức: $a + $b × $c = ?", "$r",
                        intOptions(r, listOf((a + b) * c, a + b + c, r + b, r - c), min = 1), "$a + $b × $c",
                        hint = "Biểu thức có phép cộng và phép nhân thì thực hiện phép nhân trước, phép cộng sau."
                    )
                }
                1 -> {
                    val a = rng.nextInt(5, 21)
                    val b = rng.nextInt(3, 10)
                    val c = rng.nextInt(2, 9)
                    val r = (a + b) * c
                    mk(
                        "Tính giá trị của biểu thức: ($a + $b) × $c = ?", "$r",
                        intOptions(r, listOf(a + b * c, a + b + c, r + c, r - b), min = 1), "($a + $b) × $c",
                        hint = "Khi biểu thức có dấu ngoặc ( ), ta thực hiện phép tính trong ngoặc trước."
                    )
                }
                2 -> {
                    val b = rng.nextInt(2, 7)
                    val c = rng.nextInt(2, 7)
                    val a = b * c + rng.nextInt(5, 31)
                    val r = a - b * c
                    mk(
                        "Tính giá trị của biểu thức: $a - $b × $c = ?", "$r",
                        intOptions(r, listOf((a - b) * c, a - b - c, r + b, r - 1), min = 1), "$a - $b × $c",
                        hint = "Thực hiện phép nhân $b × $c trước, rồi lấy $a trừ đi kết quả."
                    )
                }
                else -> {
                    val b = rng.nextInt(2, 10)
                    val q = rng.nextInt(3, 13)
                    val a = b * q
                    val c = rng.nextInt(2, 10)
                    val r = q + c
                    mk(
                        "Tính giá trị của biểu thức: $a : $b + $c = ?", "$r",
                        intOptions(r, listOf(q * c, r + 1, r - 1, a + c), min = 1), "$a : $b + $c",
                        hint = "Thực hiện phép chia $a : $b trước, rồi cộng với $c."
                    )
                }
            }

            // Chủ đề 8 & 13: Các số đến 10 000 và 100 000, Làm tròn số, Số La Mã (I đến XX)
            "G3_06_NUM_100K_ROUNDING_ROMAN" -> when (rng.nextInt(4)) {
                0 -> {
                    val n = rng.nextInt(4, 21) // Chuẩn Lớp 3 KNTT: Số La Mã đến XX (20)
                    val ds = listOf(romanNaive(n), n + 1, n - 1, n + 2).filter { it != n && it > 0 }
                    mk(
                        "Số La Mã ${toRoman(n)} đọc là số mấy?", "$n",
                        intOptions(n, ds, min = 1),
                        hint = "I = 1, V = 5, X = 10, IV = 4, IX = 9."
                    )
                }
                1 -> {
                    val n = rng.nextInt(3, 21)
                    val ds = listOf(n + 1, n - 1, n + 2, n - 2).filter { it in 1..20 }.map { toRoman(it) }
                    mk(
                        "Số $n được viết bằng chữ số La Mã là:",
                        toRoman(n), generate4Options(toRoman(n), ds)
                    )
                }
                2 -> {
                    val unit = choose(listOf(10, 100, 1000, 10000))
                    var n: Int
                    do { n = rng.nextInt(1200, 98000) } while (n % unit == 0)
                    val r = roundTo(n, unit)
                    val floor = (n / unit) * unit
                    val cands = listOf(floor, floor + unit, n, roundTo(n, unit * 10), floor - unit)
                    val label = when (unit) {
                        10 -> "hàng chục"; 100 -> "hàng trăm"; 1000 -> "hàng nghìn"; else -> "hàng chục nghìn"
                    }
                    mk(
                        "Làm tròn số ${fmtInt(n)} đến $label ta được:",
                        fmtInt(r), intOptions(r, cands, min = 1),
                        hint = "Quan sát chữ số ngay bên phải hàng cần làm tròn: từ 5 trở lên thì làm tròn lên, bé hơn 5 thì làm tròn xuống."
                    )
                }
                else -> {
                    val a = rng.nextInt(15000, 60000)
                    val b = rng.nextInt(10000, 35000)
                    val sum = a + b
                    mk(
                        "Tính: ${fmtInt(a)} + ${fmtInt(b)} = ?", fmtInt(sum),
                        intOptions(sum, listOf(sum + 1000, sum - 1000, sum + 100, a - b), min = 1),
                        "${fmtInt(a)} + ${fmtInt(b)}", VisualBlueprintType.VERTICAL_ARITHMETIC
                    )
                }
            }

            // Bài 15: Bài toán giải bằng hai bước tính
            "G3_07_TWO_STEP_WORD_PROBLEMS" -> when (rng.nextInt(4)) {
                0 -> {
                    val boxes = rng.nextInt(4, 9)
                    val per = rng.nextInt(6, 10)
                    val total = boxes * per
                    val sold = rng.nextInt(10, minOf(25, total))
                    val remain = total - sold
                    mk(
                        "Cửa hàng có $boxes thùng cam, mỗi thùng có $per quả cam. Cửa hàng đã bán đi $sold quả cam. Hỏi cửa hàng còn lại bao nhiêu quả cam?",
                        "$remain quả", intOptions(remain, listOf(total, total + sold, remain + per, remain - 1), " quả", 1),
                        hint = "Bước 1: Tìm tổng số cam trong $boxes thùng ($boxes × $per). Bước 2: Trừ đi số quả đã bán."
                    )
                }
                1 -> {
                    val p = rng.nextInt(6, 21)
                    val k = rng.nextInt(2, 6)
                    val total = p + p * k
                    mk(
                        "Ngăn trên có $p quyển sách. Số sách ở ngăn dưới gấp $k lần số sách ở ngăn trên. Hỏi cả hai ngăn có tất cả bao nhiêu quyển sách?",
                        "$total quyển", intOptions(total, listOf(p * k, p + k, total + p, total - p - 1), " quyển", 1),
                        bp = VisualBlueprintType.SINGAPORE_BAR_MODEL,
                        hint = "Bước 1: Tìm số sách ngăn dưới ($p × $k). Bước 2: Cộng số sách của cả hai ngăn."
                    )
                }
                2 -> {
                    val a = rng.nextInt(15, 35)
                    val diff = rng.nextInt(5, 15)
                    val b = a + diff
                    val total = a + b
                    mk(
                        "Tổ Một trồng được $a cây, Tổ Hai trồng được nhiều hơn Tổ Một $diff cây. Hỏi cả hai tổ trồng được tất cả bao nhiêu cây?",
                        "$total cây", intOptions(total, listOf(b, a - diff + a, total + diff, total - 2), " cây", 1),
                        bp = VisualBlueprintType.SINGAPORE_BAR_MODEL,
                        hint = "Bước 1: Tìm số cây Tổ Hai ($a + $diff). Bước 2: Cộng số cây của cả hai tổ."
                    )
                }
                else -> {
                    val price = choose(listOf(3000, 4000, 5000, 6000))
                    val n = rng.nextInt(2, 6)
                    val total = price * n
                    val paid = if (total < 20000) 20000 else 50000
                    val change = paid - total
                    mk(
                        "Mai mua $n chiếc bút mực, mỗi chiếc giá ${fmtMoney(price)}. Mai đưa cho cô bán hàng tờ ${fmtMoney(paid)}. Hỏi cô bán hàng phải trả lại Mai bao nhiêu tiền?",
                        fmtMoney(change), moneyOptions(change, listOf(total, paid + total, change + 1000, change - 1000)),
                        hint = "Bước 1: Tính số tiền mua $n chiếc bút. Bước 2: Lấy ${fmtMoney(paid)} trừ đi số tiền mua bút."
                    )
                }
            }

            // Chủ đề 3 & 7: Điểm ở giữa, Trung điểm của đoạn thẳng; Hình tròn, tâm, bán kính, đường kính; Góc vuông
            "G3_08_GEOMETRY_MIDPOINT_CIRCLE" -> when (rng.nextInt(4)) {
                0 -> {
                    val r = rng.nextInt(4, 16)
                    mk(
                        "Hình tròn tâm O có bán kính $r cm. Đường kính của hình tròn đó dài bao nhiêu xăng-ti-mét?",
                        "${r * 2} cm", intOptions(r * 2, listOf(r, r * 2 + 2, r * 3, r + 2), " cm", 1),
                        bp = VisualBlueprintType.GEOMETRY_GRID,
                        hint = "Trong một hình tròn, độ dài đường kính gấp 2 lần độ dài bán kính."
                    )
                }
                1 -> {
                    val r = rng.nextInt(4, 16)
                    mk(
                        "Hình tròn tâm O có đường kính ${r * 2} cm. Bán kính của hình tròn đó dài bao nhiêu xăng-ti-mét?",
                        "$r cm", intOptions(r, listOf(r * 2, r * 4, r + 2, r - 1), " cm", 1),
                        bp = VisualBlueprintType.GEOMETRY_GRID,
                        hint = "Độ dài bán kính bằng một nửa (1/2) độ dài đường kính."
                    )
                }
                2 -> {
                    val half = rng.nextInt(3, 16)
                    mk(
                        "M là trung điểm của đoạn thẳng AB dài ${half * 2} cm. Độ dài đoạn thẳng AM bằng bao nhiêu xăng-ti-mét?",
                        "$half cm", intOptions(half, listOf(half * 2, half * 4, half + 1, half - 1), " cm", 1),
                        bp = VisualBlueprintType.GEOMETRY_GRID,
                        hint = "Trung điểm M chia đoạn thẳng AB thành hai phần bằng nhau: AM = MB = AB : 2."
                    )
                }
                else -> {
                    mk(
                        "Hình chữ nhật và hình vuông đều có mấy góc vuông?", "4 góc vuông",
                        intOptions(4, listOf(2, 3, 1, 6), " góc vuông", 1),
                        bp = VisualBlueprintType.GEOMETRY_GRID,
                        hint = "Dùng ê ke kiểm tra: cả 4 góc ở 4 đỉnh đều là góc vuông."
                    )
                }
            }

            // Chủ đề 9 & 14: Chu vi, diện tích hình chữ nhật, hình vuông (kể cả bài toán ngược)
            "G3_09_PERIMETER_AREA_RECT_SQUARE" -> {
                val a = rng.nextInt(5, 13)
                val b = rng.nextInt(2, a)
                val s = rng.nextInt(3, 16)
                when (rng.nextInt(6)) {
                    0 -> {
                        val area = a * b
                        val correct = "$area cm²"
                        val ds = listOf("${(a + b) * 2} cm²", "$area cm", "${area + 5} cm²", "${a + b} cm²")
                        mk(
                            "Tính diện tích hình chữ nhật có chiều dài $a cm và chiều rộng $b cm:",
                            correct, generate4Options(correct, ds), bp = VisualBlueprintType.GEOMETRY_GRID,
                            hint = "Muốn tính diện tích hình chữ nhật, ta lấy chiều dài nhân với chiều rộng (cùng đơn vị đo)."
                        )
                    }
                    1 -> {
                        val p = (a + b) * 2
                        val correct = "$p cm"
                        val ds = listOf("${a * b} cm", "${a + b} cm", "$p cm²", "${2 * a + b} cm")
                        mk(
                            "Tính chu vi hình chữ nhật có chiều dài $a cm và chiều rộng $b cm:",
                            correct, generate4Options(correct, ds), bp = VisualBlueprintType.GEOMETRY_GRID,
                            hint = "Muốn tính chu vi hình chữ nhật, ta lấy chiều dài cộng với chiều rộng (cùng đơn vị đo) rồi nhân với 2."
                        )
                    }
                    2 -> {
                        val p = s * 4
                        val correct = "$p cm"
                        val ds = listOf("${s * s} cm", "${s * 2} cm", "$p cm²", "${s * 3} cm")
                        mk(
                            "Tính chu vi hình vuông có độ dài cạnh là $s cm:",
                            correct, generate4Options(correct, ds), bp = VisualBlueprintType.GEOMETRY_GRID,
                            hint = "Muốn tính chu vi hình vuông, ta lấy độ dài một cạnh nhân với 4."
                        )
                    }
                    3 -> {
                        val area = s * s
                        val correct = "$area cm²"
                        val ds = listOf("${s * 4} cm²", "$area cm", "${s * 2} cm²", "${area + 4} cm²")
                        mk(
                            "Tính diện tích hình vuông có độ dài cạnh là $s cm:",
                            correct, generate4Options(correct, ds), bp = VisualBlueprintType.GEOMETRY_GRID,
                            hint = "Muốn tính diện tích hình vuông, ta lấy độ dài một cạnh nhân với chính nó."
                        )
                    }
                    4 -> {
                        // Bài toán ngược hay thi học kỳ: Cho chu vi hình vuông -> tính cạnh hoặc diện tích
                        val p = s * 4
                        val area = s * s
                        val correct = "$area cm²"
                        val ds = listOf("$s cm²", "${p * p} cm²", "${s * 2} cm²", "$area cm")
                        mk(
                            "Một hình vuông có chu vi là $p cm. Diện tích của hình vuông đó là:",
                            correct, generate4Options(correct, ds), bp = VisualBlueprintType.GEOMETRY_GRID,
                            hint = "Bước 1: Tìm cạnh hình vuông ($p : 4 = $s cm). Bước 2: Tính diện tích ($s × $s)."
                        )
                    }
                    else -> {
                        // Đổi về cùng đơn vị đo trước khi tính (Ví dụ: chiều dài dm, chiều rộng cm)
                        val dm = rng.nextInt(1, 4)
                        val lenCm = dm * 10
                        val widCm = rng.nextInt(3, 9)
                        val area = lenCm * widCm
                        val correct = "$area cm²"
                        val ds = listOf("${dm * widCm} cm²", "${(lenCm + widCm) * 2} cm²", "${(dm + widCm) * 2} cm²", "$area cm")
                        mk(
                            "Một hình chữ nhật có chiều dài $dm dm và chiều rộng $widCm cm. Diện tích hình chữ nhật đó là:",
                            correct, generate4Options(correct, ds), bp = VisualBlueprintType.GEOMETRY_GRID,
                            hint = "Đổi $dm dm = $lenCm cm, rồi lấy $lenCm × $widCm."
                        )
                    }
                }
            }

            // Chủ đề 4 & 15: Đơn vị mm, g, ml, nhiệt độ (°C), Bảng số liệu thống kê & Khả năng xảy ra
            "G3_10_MEASURE_STATS_G3" -> when (rng.nextInt(6)) {
                0 -> {
                    val cm = rng.nextInt(2, 10)
                    mk(
                        "Đổi đơn vị đo độ dài: $cm cm = ... mm", "${cm * 10} mm",
                        intOptions(cm * 10, listOf(cm * 100, cm, cm * 1000, cm + 10), " mm", 1),
                        hint = "1 cm = 10 mm."
                    )
                }
                1 -> {
                    val k = rng.nextInt(2, 10)
                    mk(
                        "Đổi đơn vị đo khối lượng: $k kg = ... g", "${fmtInt(k * 1000)} g",
                        intOptions(k * 1000, listOf(k * 100, k * 10, k * 10000), " g", 1),
                        hint = "1 kg = 1000 g."
                    )
                }
                2 -> {
                    val k = rng.nextInt(2, 10)
                    mk(
                        "Đổi đơn vị đo dung tích: $k l = ... ml", "${fmtInt(k * 1000)} ml",
                        intOptions(k * 1000, listOf(k * 100, k * 10, k * 10000), " ml", 1),
                        hint = "1 l = 1000 ml."
                    )
                }
                3 -> {
                    val t1 = rng.nextInt(20, 32)
                    val diff = rng.nextInt(3, 8)
                    val t2 = t1 + diff
                    mk(
                        "Buổi sáng nhiệt độ không khí là ${t1}°C, buổi trưa nhiệt độ tăng thêm ${diff}°C. Hỏi buổi trưa nhiệt độ là bao nhiêu độ C?",
                        "${t2}°C", intOptions(t2, listOf(t1 - diff, t2 + 2, t2 - 1, t1), "°C", 1)
                    )
                }
                4 -> {
                    val classes = listOf("3A", "3B", "3C", "3D")
                    val counts = generateSequence { rng.nextInt(18, 41) }.distinct().take(4).toList()
                    val idx = counts.indexOf(counts.maxOrNull()!!)
                    val table = classes.indices.joinToString(", ") { "${classes[it]}: ${counts[it]} quyển" }
                    val correct = classes[idx]
                    mk(
                        "Bảng thống kê số sách quyên góp của khối 3: $table. Lớp nào quyên góp được nhiều sách nhất?",
                        correct, generate4Options(correct, classes.filter { it != correct }.shuffled(rng))
                    )
                }
                else -> {
                    val g1 = rng.nextInt(200, 500)
                    val g2 = rng.nextInt(300, 500)
                    val total = g1 + g2
                    mk(
                        "Một gói bánh cân nặng $g1 g, một gói kẹo cân nặng $g2 g. Cả hai gói cân nặng bao nhiêu gam?",
                        "$total g", intOptions(total, listOf(total - 100, total + 100, g1, abs(g2 - g1)), " g", 1)
                    )
                }
            }

            else -> {
                val a = rng.nextInt(10, 30)
                val b = rng.nextInt(4, 9)
                val c = rng.nextInt(3, 8)
                val r = a + b * c
                mk(
                    "Tính giá trị biểu thức: $a + $b × $c = ?", "$r",
                    intOptions(r, listOf((a + b) * c, a + b + c, r + 1)), "$a + $b × $c"
                )
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // GRADE 4 (SGK Toán 4 - Kết nối tri thức với cuộc sống)
    // ------------------------------------------------------------------------------------------
    private fun generateGrade4Node(ctx: Ctx): AcademicProblem = with(ctx) {
        when (nodeId) {
            "G4_01_MILLIONS_CLASS_ROUNDING" -> {
                val m = rr(t, 1..20, 1..99, 10..999)
                val n = m * 1_000_000 + rng.nextInt(10_000, 999_999)
                when (rng.nextInt(4)) {
                    0 -> {
                        val correct = "$m triệu"
                        val ds = listOf("${m + 1} triệu", "${m * 10} triệu", "${m * 100} triệu", "${(m - 1).coerceAtLeast(1)} triệu")
                        mk(
                            "Lớp triệu của số ${fmtInt(n)} có giá trị là bao nhiêu?",
                            correct, generate4Options(correct, ds),
                            hint = "Ba chữ số từ phải sang trái tạo thành một lớp: lớp đơn vị, lớp nghìn, rồi đến lớp triệu."
                        )
                    }
                    1 -> {
                        var digits: List<Int>
                        do { digits = (1..9).shuffled(rng).take(7) } while (false)
                        val num = digits.fold(0) { acc, d -> acc * 10 + d }
                        val d = digits[1]
                        val ds = listOf(digits[0], digits[2], digits[3], digits[4])
                        mk(
                            "Trong số ${fmtInt(num)}, chữ số nào thuộc hàng trăm nghìn?",
                            "$d", intOptions(d, ds),
                            hint = "Lớp nghìn gồm hàng nghìn, hàng chục nghìn và hàng trăm nghìn."
                        )
                    }
                    2 -> {
                        val unit = choose(listOf(10_000, 100_000))
                        var x = n
                        if (x % unit == 0) x += 1500
                        val r = roundTo(x, unit)
                        val floor = (x / unit) * unit
                        val label = if (unit == 10_000) "hàng chục nghìn" else "hàng trăm nghìn"
                        mk(
                            "Làm tròn số ${fmtInt(x)} đến $label ta được:",
                            fmtInt(r),
                            intOptions(r, listOf(floor, floor + unit, x, roundTo(x, unit * 10), floor - unit), min = 1),
                            hint = "Khi làm tròn số đến $label, ta so sánh chữ số ở hàng liền sau với 5."
                        )
                    }
                    else -> {
                        val a = rng.nextInt(1, 10)
                        val b = rng.nextInt(1, 10)
                        val c = rng.nextInt(1, 10)
                        val v = a * 1_000_000 + b * 100_000 + c * 1_000
                        val correct = fmtInt(v)
                        val ds = listOf(
                            fmtInt(a * 100_000 + b * 10_000 + c * 100),
                            fmtInt(a * 1_000_000 + b * 10_000 + c * 1_000),
                            fmtInt(a * 1_000_000 + b * 100_000 + c * 100)
                        )
                        mk(
                            "Số gồm $a triệu, $b trăm nghìn và $c nghìn được viết là:",
                            correct, generate4Options(correct, ds),
                            hint = "Viết lần lượt giá trị từng hàng: hàng triệu, hàng trăm nghìn, hàng nghìn..."
                        )
                    }
                }
            }

            "G4_02_ALGEBRAIC_EXPRESSIONS_LAWS" -> when (rng.nextInt(3)) {
                0 -> {
                    val a = rng.nextInt(10, 50)
                    val b = rng.nextInt(5, 30)
                    val c = rng.nextInt(2, 10)
                    val res = (a + b) * c
                    mk(
                        "Tính giá trị của biểu thức (a + b) × c với a = $a, b = $b, c = $c:",
                        "$res", intOptions(res, listOf(a + b * c, a * c + b, res + 10, res - 10), min = 1),
                        "($a + $b) × $c",
                        hint = "Thay a = $a, b = $b, c = $c vào biểu thức rồi tính từ trong ngoặc ra ngoài."
                    )
                }
                1 -> {
                    val a = rng.nextInt(12, 48)
                    val b = rng.nextInt(15, 55)
                    val sum = a + b
                    mk(
                        "Áp dụng tính chất giao hoán của phép cộng: $a + $b = $b + ... Số điền vào chỗ chấm là:",
                        "$a", intOptions(a, listOf(b, sum, abs(a - b), a + 1), min = 1),
                        hint = "Tính chất giao hoán: Khi đổi chỗ các số hạng trong một tổng thì tổng không thay đổi."
                    )
                }
                else -> {
                    val a = rng.nextInt(3, 10)
                    val b = rng.nextInt(10, 40)
                    val c = rng.nextInt(10, 40)
                    val res = a * (b + c)
                    mk(
                        "Biết a × (b + c) = a × b + a × c. Tính nhanh: $a × $b + $a × $c = ?",
                        "$res", intOptions(res, listOf(a * b + c, a + b + c, res + 10, res - 10), min = 1),
                        "$a × ($b + $c)",
                        hint = "Đưa về dạng $a × ($b + $c) = $a × ${b + c} = $res."
                    )
                }
            }

            "G4_03_MUL_DIV_2_DIGITS" -> when (rng.nextInt(3)) {
                0 -> {
                    val a = rng.nextInt(12, 45)
                    val b = rng.nextInt(11, 25)
                    val prod = a * b
                    mk(
                        "Đặt tính rồi tính: $a × $b = ?", "$prod",
                        intOptions(prod, listOf(prod - a, prod + b, prod + 10, prod - 10), min = 1),
                        "$a × $b", VisualBlueprintType.VERTICAL_ARITHMETIC,
                        hint = "Nhân tích riêng thứ nhất, tích riêng thứ hai lùi sang trái 1 cột rồi cộng hai tích riêng."
                    )
                }
                1 -> {
                    val b = rng.nextInt(12, 35)
                    val q = rng.nextInt(11, 45)
                    val a = b * q
                    mk(
                        "Đặt tính rồi tính: $a : $b = ?", "$q",
                        intOptions(q, listOf(q + 1, q - 1, q + 10, b), min = 1),
                        "$a : $b", VisualBlueprintType.VERTICAL_ARITHMETIC,
                        hint = "Chia lần lượt từng lượt chia từ trái sang phải."
                    )
                }
                else -> {
                    val n = rng.nextInt(100, 999)
                    val sumDigits = n.toString().map { it.toString().toInt() }.sum()
                    val div3 = sumDigits % 3 == 0
                    val div5 = n % 10 == 0 || n % 10 == 5
                    val correct = when {
                        div3 && div5 -> "Chia hết cho cả 3 và 5"
                        div3 -> "Chia hết cho 3"
                        div5 -> "Chia hết cho 5"
                        else -> "Không chia hết cho 3 và 5"
                    }
                    val ds = listOf("Chia hết cho cả 3 và 5", "Chia hết cho 3", "Chia hết cho 5", "Không chia hết cho 3 và 5")
                    mk(
                        "Nhận xét nào đúng về số $n?",
                        correct, generate4Options(correct, ds),
                        hint = "Số chia hết cho 5 có tận cùng là 0 hoặc 5. Số chia hết cho 3 có tổng các chữ số chia hết cho 3."
                    )
                }
            }

            "G4_04_AVERAGE_AND_UNIT_METHOD" -> when (rng.nextInt(2)) {
                0 -> {
                    val count = choose(listOf(3, 4))
                    val base = rng.nextInt(20, 60)
                    val list = List(count) { base + rng.nextInt(-10, 11) }
                    val sum = list.sum()
                    val avg = sum / count
                    val exactList = list.toMutableList()
                    exactList[0] += (avg * count - sum)
                    val finalSum = exactList.sum()
                    val finalAvg = finalSum / count
                    mk(
                        "Tìm số trung bình cộng của các số: ${exactList.joinToString(", ")}",
                        "$finalAvg", intOptions(finalAvg, listOf(finalSum, finalAvg + 2, finalAvg - 2, exactList[0]), min = 1),
                        hint = "Muốn tìm số trung bình cộng, ta tính tổng các số rồi chia cho số các số hạng ($count)."
                    )
                }
                else -> {
                    val n1 = rng.nextInt(3, 8)
                    val price = rng.nextInt(5, 20) * 1000
                    val total1 = n1 * price
                    val n2 = n1 + rng.nextInt(2, 6)
                    val total2 = n2 * price
                    mk(
                        "Mua $n1 quyển vở hết ${fmtMoney(total1)}. Hỏi mua $n2 quyển vở như thế hết bao nhiêu tiền?",
                        fmtMoney(total2), moneyOptions(total2, listOf(total1, price, total2 + 5000, total2 - 5000)),
                        hint = "Bước 1 (Rút về đơn vị): Giá 1 quyển vở = $total1 : $n1. Bước 2: Nhân với $n2."
                    )
                }
            }

            "G4_05_SUM_DIFFERENCE_MODEL" -> {
                val diff = rng.nextInt(4, 20) * 2
                val small = rng.nextInt(10, 50)
                val big = small + diff
                val sum = big + small
                val askBig = rng.nextBoolean()
                val ans = if (askBig) big else small
                val label = if (askBig) "Số lớn" else "Số bé"
                mk(
                    "Hai số có tổng bằng $sum và hiệu bằng $diff. $label là:",
                    "$ans", intOptions(ans, listOf(if (askBig) small else big, sum, diff, ans + 2), min = 1),
                    bp = VisualBlueprintType.SINGAPORE_BAR_MODEL,
                    hint = if (askBig) "Số lớn = (Tổng + Hiệu) : 2" else "Số bé = (Tổng - Hiệu) : 2"
                )
            }

            "G4_06_FRACTIONS_CONCEPT_COMPARE" -> when (rng.nextInt(3)) {
                0 -> {
                    val d = choose(listOf(4, 6, 8, 10, 12, 15, 20))
                    val k = rng.nextInt(2, 5)
                    val n = rng.nextInt(1, d)
                    val rawN = n * k
                    val rawD = d * k
                    val correct = fracStr(rawN, rawD)
                    val ds = listOf("$rawN/$rawD", "$n/$rawD", "$rawN/$d", "${n + 1}/$d")
                    mk(
                        "Rút gọn phân số $rawN/$rawD về phân số tối giản ta được:",
                        correct, generate4Options(correct, ds),
                        hint = "Chia cả tử số và mẫu số cho cùng một số tự nhiên lớn hơn 1."
                    )
                }
                1 -> {
                    val pairs = distinctFractions(2)
                    val f1 = pairs[0]
                    val f2 = pairs[1]
                    val cmp = fracCompare(f1, f2)
                    val str1 = "${f1.first}/${f1.second}"
                    val str2 = "${f2.first}/${f2.second}"
                    val correct = when {
                        cmp > 0 -> "$str1 lớn hơn $str2"
                        cmp < 0 -> "$str1 bé hơn $str2"
                        else -> "$str1 bằng $str2"
                    }
                    val ds = listOf("$str1 lớn hơn $str2", "$str1 bé hơn $str2", "$str1 bằng $str2")
                    mk(
                        "So sánh hai phân số $str1 và $str2, nhận xét nào đúng?",
                        correct, generate4Options(correct, ds),
                        hint = "Quy đồng mẫu số hai phân số rồi so sánh tử số."
                    )
                }
                else -> {
                    val d = rng.nextInt(3, 10)
                    val n = rng.nextInt(1, d)
                    val total = d * rng.nextInt(2, 8)
                    val res = (total / d) * n
                    mk(
                        "Tìm $n/$d của $total:",
                        "$res", intOptions(res, listOf(total, total / d, res + 2, res - 1), min = 1),
                        hint = "Muốn tìm $n/$d của số $total, ta lấy $total : $d × $n."
                    )
                }
            }

            "G4_07_FRACTIONS_FOUR_OPS" -> when (rng.nextInt(4)) {
                0 -> {
                    val d = rng.nextInt(3, 9)
                    val n1 = rng.nextInt(1, d)
                    val n2 = rng.nextInt(1, d)
                    val sumN = n1 + n2
                    val correct = fracStr(sumN, d)
                    val ds = listOf("${n1 + n2}/${d + d}", "$sumN/$d", "${n1 * n2}/$d", "${abs(n1 - n2)}/$d")
                    mk(
                        "Tính: $n1/$d + $n2/$d = ?",
                        correct, generate4Options(correct, ds),
                        hint = "Cộng hai tử số với nhau và giữ nguyên mẫu số."
                    )
                }
                1 -> {
                    val d = rng.nextInt(3, 9)
                    val n1 = rng.nextInt(2, d)
                    val n2 = rng.nextInt(1, n1)
                    val diffN = n1 - n2
                    val correct = fracStr(diffN, d)
                    val ds = listOf("${n1 * n2}/$d", "$diffN/$d", "${n1 + n2}/$d")
                    mk(
                        "Tính: $n1/$d - $n2/$d = ?",
                        correct, generate4Options(correct, ds),
                        hint = "Trừ tử số của phân số thứ nhất cho tử số của phân số thứ hai và giữ nguyên mẫu số."
                    )
                }
                2 -> {
                    val n1 = rng.nextInt(1, 5)
                    val d1 = rng.nextInt(2, 6)
                    val n2 = rng.nextInt(1, 5)
                    val d2 = rng.nextInt(2, 6)
                    val correct = fracStr(n1 * n2, d1 * d2)
                    val ds = listOf("${n1 + n2}/${d1 + d2}", "${n1 * d2}/${d1 * n2}", "${n1 * n2}/${d1 + d2}")
                    mk(
                        "Tính: $n1/$d1 × $n2/$d2 = ?",
                        correct, generate4Options(correct, ds),
                        hint = "Lấy tử số nhân với tử số, mẫu số nhân với mẫu số."
                    )
                }
                else -> {
                    val n1 = rng.nextInt(1, 5)
                    val d1 = rng.nextInt(2, 6)
                    val n2 = rng.nextInt(1, 5)
                    val d2 = rng.nextInt(2, 6)
                    val correct = fracStr(n1 * d2, d1 * n2)
                    val ds = listOf("${n1 * n2}/${d1 * d2}", "${n1 + n2}/${d1 + d2}", "${n1 * d2}/${d1 * d2}")
                    mk(
                        "Tính: $n1/$d1 : $n2/$d2 = ?",
                        correct, generate4Options(correct, ds),
                        hint = "Lấy phân số thứ nhất nhân với phân số thứ hai đảo ngược ($d2/$n2)."
                    )
                }
            }

            "G4_08_ANGLES_PARALLEL_SHAPES" -> when (rng.nextInt(3)) {
                0 -> {
                    val (q, ans) = choose(
                        listOf(
                            "Góc bé hơn góc vuông gọi là góc gì?" to "Góc nhọn",
                            "Góc lớn hơn góc vuông và bé hơn góc bẹt gọi là góc gì?" to "Góc tù",
                            "Góc bằng 2 lần góc vuông gọi là góc gì?" to "Góc bẹt"
                        )
                    )
                    val all = listOf("Góc nhọn", "Góc vuông", "Góc tù", "Góc bẹt")
                    mk(q, ans, generate4Options(ans, all.filter { it != ans }))
                }
                1 -> {
                    val a = rng.nextInt(4, 15)
                    val h = rng.nextInt(3, 10)
                    val area = a * h
                    mk(
                        "Tính diện tích hình bình hành có độ dài đáy $a cm và chiều cao $h cm:",
                        "$area cm²", intOptions(area, listOf((a + h) * 2, area + 2, area - 2, a + h), " cm²", 1),
                        bp = VisualBlueprintType.GEOMETRY_GRID,
                        hint = "Diện tích hình bình hành bằng độ dài đáy nhân với chiều cao (cùng đơn vị đo): S = a × h."
                    )
                }
                else -> {
                    val m = rng.nextInt(4, 12)
                    val n = rng.nextInt(3, 10)
                    val exactM = if ((m * n) % 2 != 0) m + 1 else m
                    val exactArea = (exactM * n) / 2
                    mk(
                        "Tính diện tích hình thoi có độ dài hai đường chéo lần lượt là $exactM cm và $n cm:",
                        "$exactArea cm²", intOptions(exactArea, listOf(exactM * n, (exactM + n) * 2, exactArea + 2), " cm²", 1),
                        bp = VisualBlueprintType.GEOMETRY_GRID,
                        hint = "Diện tích hình thoi bằng tích độ dài hai đường chéo chia cho 2: S = (m × n) : 2."
                    )
                }
            }

            "G4_09_MEASURE_TON_M2_CENTURY" -> when (rng.nextInt(3)) {
                0 -> {
                    val ton = rng.nextInt(2, 15)
                    mk(
                        "Đổi đơn vị đo khối lượng: $ton tấn = ... kg", "${fmtInt(ton * 1000)} kg",
                        intOptions(ton * 1000, listOf(ton * 100, ton * 10, ton * 10000), " kg", 1),
                        hint = "1 tấn = 1000 kg."
                    )
                }
                1 -> {
                    val m2 = rng.nextInt(2, 15)
                    mk(
                        "Đổi đơn vị đo diện tích: $m2 m² = ... dm²", "${fmtInt(m2 * 100)} dm²",
                        intOptions(m2 * 100, listOf(m2 * 10, m2 * 1000, m2 * 10000), " dm²", 1),
                        hint = "1 m² = 100 dm²."
                    )
                }
                else -> {
                    val year = rng.nextInt(1801, 2026)
                    val century = (year - 1) / 100 + 1
                    val romanC = toRoman(century)
                    val ds = listOf(toRoman(century - 1), toRoman(century + 1), toRoman(century - 2)).filter { it.isNotEmpty() }
                    mk(
                        "Năm $year thuộc thế kỷ thứ mấy?",
                        "Thế kỷ $romanC", generate4Options("Thế kỷ $romanC", ds.map { "Thế kỷ $it" }),
                        hint = "Năm từ ${(century - 1) * 100 + 1} đến ${century * 100} thuộc thế kỷ $romanC."
                    )
                }
            }

            "G4_10_BAR_CHART_PROBABILITY" -> when (rng.nextInt(2)) {
                0 -> {
                    val items = listOf("Màu đỏ", "Màu xanh", "Màu vàng", "Màu tím")
                    val vals = generateSequence { rng.nextInt(10, 50) }.distinct().take(4).toList()
                    val maxIdx = vals.indexOf(vals.maxOrNull()!!)
                    val correct = items[maxIdx]
                    val chartInfo = items.indices.joinToString(", ") { "${items[it]}: ${vals[it]}" }
                    mk(
                        "Dựa vào số liệu biểu đồ cột ($chartInfo), mục nào có số lượng nhiều nhất?",
                        correct, generate4Options(correct, items.filter { it != correct }),
                        hint = "Quan sát cột cao nhất trên biểu đồ để tìm mục có số lượng nhiều nhất."
                    )
                }
                else -> {
                    val total = 10
                    val red = rng.nextInt(2, 8)
                    val blue = total - red
                    mk(
                        "Trong túi có $red viên bi đỏ và $blue viên bi xanh. Nam rút ngẫu nhiên 1 viên bi. Khả năng Nam rút được viên bi đỏ là:",
                        "Có thể", generate4Options("Có thể", listOf("Chắc chắn", "Không thể", "Chắc chắn không")),
                        hint = "Trong túi có cả bi đỏ và bi xanh nên việc rút được bi đỏ là 'Có thể'."
                    )
                }
            }

            else -> {
                val a = rng.nextInt(10, 50)
                val b = rng.nextInt(2, 10)
                val sum = a + b
                mk("Tính kết quả: $a + $b = ?", "$sum", intOptions(sum, listOf(sum + 1, sum - 1, sum + 2)), "$a + $b")
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // GRADE 5 (SGK Toán 5 - Kết nối tri thức với cuộc sống)
    // ------------------------------------------------------------------------------------------
    private fun generateGrade5Node(ctx: Ctx): AcademicProblem = with(ctx) {
        when (nodeId) {
            "G5_01_DECIMAL_FRACTIONS_MIXED" -> when (rng.nextInt(2)) {
                0 -> {
                    val den = choose(listOf(10, 100, 1000))
                    val num = rng.nextInt(1, den)
                    val decStr = fmtDecimal(num, if (den == 10) 1 else if (den == 100) 2 else 3, trim = true)
                    mk(
                        "Viết phân số thập phân $num/$den dưới dạng số thập phân ta được:",
                        decStr, generate4Options(decStr, listOf(fmtDecimal(num * 10, 1), fmtDecimal(num, 1), "$num")),
                        hint = "Phân số thập phân có mẫu số 10, 100, 1000... được viết tương ứng thành số thập phân."
                    )
                }
                else -> {
                    val w = rng.nextInt(1, 9)
                    val n = rng.nextInt(1, 5)
                    val d = rng.nextInt(n + 1, 10)
                    val correct = "$w $n/$d"
                    val ds = listOf("$w", "$n/$d", "${w * d + n}/$d", "$w ${n + 1}/$d")
                    mk(
                        "Hỗn số '$w và $n/$d' được viết là:",
                        correct, generate4Options(correct, ds),
                        hint = "Hỗn số gồm phần nguyên đứng trước, phần phân số đứng sau."
                    )
                }
            }

            "G5_02_DECIMALS_PLACE_ROUNDING" -> when (rng.nextInt(3)) {
                0 -> {
                    val v = rng.nextInt(101, 999)
                    val decStr = fmtDecimal(v, 2)
                    val tensDigit = (v / 10) % 10
                    mk(
                        "Trong số thập phân $decStr, chữ số $tensDigit thuộc hàng nào?",
                        "Hàng phần mười",
                        generate4Options("Hàng phần mười", listOf("Hàng phần trăm", "Hàng đơn vị", "Hàng chục")),
                        hint = "Chữ số đầu tiên ngay sau dấu phẩy thuộc hàng phần mười."
                    )
                }
                1 -> {
                    val v = rng.nextInt(123, 899)
                    val decStr = fmtDecimal(v, 2)
                    // v = 709 nghĩa là 7,09 → làm tròn đến hàng đơn vị: nhìn chữ số phần mười (0) → 7
                    val units = roundTo(v, 100) / 100
                    val roundedStr = units.toString()
                    mk(
                        "Làm tròn số thập phân $decStr đến hàng đơn vị ta được:",
                        roundedStr, intOptions(units, listOf(units + 1, (units - 1).coerceAtLeast(0), v / 10), min = 0),
                        hint = "So sánh chữ số hàng phần mười với 5: từ 5 trở lên thì làm tròn lên, bé hơn 5 làm tròn xuống."
                    )
                }
                else -> {
                    val a = rng.nextInt(10, 99)
                    val b = a + rng.nextInt(1, 10)
                    val sA = fmtDecimal(a, 1)
                    val sB = fmtDecimal(b, 1)
                    val correct = "$sA bé hơn $sB"
                    val ds = listOf("$sA lớn hơn $sB", "$sA bằng $sB")
                    mk(
                        "So sánh hai số thập phân $sA và $sB:",
                        correct, generate4Options(correct, ds),
                        hint = "So sánh phần nguyên trước; nếu phần nguyên bằng nhau thì so sánh hàng phần mười."
                    )
                }
            }

            "G5_03_DECIMALS_FOUR_OPS" -> when (rng.nextInt(4)) {
                0 -> {
                    val a = rng.nextInt(12, 89)
                    val b = rng.nextInt(11, 78)
                    val sum = a + b
                    val sa = fmtDecimal(a, 1)
                    val sb = fmtDecimal(b, 1)
                    val ssum = fmtDecimal(sum, 1)
                    mk(
                        "Đặt tính rồi tính: $sa + $sb = ?",
                        ssum, generate4Options(ssum, listOf(fmtDecimal(sum + 10, 1), fmtDecimal(sum - 1, 1), "$sum")),
                        "$sa + $sb", VisualBlueprintType.VERTICAL_ARITHMETIC,
                        hint = "Đặt các chữ số cùng hàng thẳng cột với nhau rồi cộng từ phải sang trái."
                    )
                }
                1 -> {
                    val a = rng.nextInt(50, 99)
                    val b = rng.nextInt(10, a - 1)
                    val diff = a - b
                    val sa = fmtDecimal(a, 1)
                    val sb = fmtDecimal(b, 1)
                    val sdiff = fmtDecimal(diff, 1)
                    mk(
                        "Đặt tính rồi tính: $sa - $sb = ?",
                        sdiff, generate4Options(sdiff, listOf(fmtDecimal(diff + 10, 1), fmtDecimal(diff - 1, 1), "$diff")),
                        "$sa - $sb", VisualBlueprintType.VERTICAL_ARITHMETIC
                    )
                }
                2 -> {
                    val a = rng.nextInt(12, 45)
                    val b = rng.nextInt(2, 9)
                    val prod = a * b
                    val sa = fmtDecimal(a, 1)
                    val sprod = fmtDecimal(prod, 1)
                    mk(
                        "Tính nhẩm: $sa × $b = ?",
                        sprod, generate4Options(sprod, listOf(fmtDecimal(prod + 10, 1), "$prod", fmtDecimal(prod - 1, 1))),
                        "$sa × $b"
                    )
                }
                else -> {
                    val b = rng.nextInt(2, 8)
                    val q = rng.nextInt(12, 45)
                    val a = b * q
                    val sa = fmtDecimal(a, 1)
                    val sq = fmtDecimal(q, 1)
                    mk(
                        "Đặt tính rồi tính: $sa : $b = ?",
                        sq, generate4Options(sq, listOf(fmtDecimal(q + 10, 1), "$q", fmtDecimal(q - 1, 1))),
                        "$sa : $b"
                    )
                }
            }

            "G5_04_SUM_DIFF_RATIO_PROBLEMS" -> when (rng.nextInt(2)) {
                0 -> {
                    val n1 = rng.nextInt(1, 4)
                    val n2 = rng.nextInt(n1 + 1, 5)
                    val unit = rng.nextInt(5, 20)
                    val sum = (n1 + n2) * unit
                    val v1 = n1 * unit
                    val v2 = n2 * unit
                    val askBig = rng.nextBoolean()
                    val ans = if (askBig) v2 else v1
                    val label = if (askBig) "Số lớn" else "Số bé"
                    mk(
                        "Hai số có tổng bằng $sum và tỉ số là $n1/$n2. $label là:",
                        "$ans", intOptions(ans, listOf(if (askBig) v1 else v2, sum, unit, ans + 5), min = 1),
                        bp = VisualBlueprintType.SINGAPORE_BAR_MODEL,
                        hint = "Bước 1: Tìm tổng số phần bằng nhau ($n1 + $n2 = ${n1 + n2}). Bước 2: Giá trị 1 phần = $sum : ${n1 + n2}."
                    )
                }
                else -> {
                    val n1 = rng.nextInt(1, 4)
                    val n2 = rng.nextInt(n1 + 1, 5)
                    val unit = rng.nextInt(5, 20)
                    val diff = (n2 - n1) * unit
                    val v1 = n1 * unit
                    val v2 = n2 * unit
                    val askBig = rng.nextBoolean()
                    val ans = if (askBig) v2 else v1
                    val label = if (askBig) "Số lớn" else "Số bé"
                    mk(
                        "Hai số có hiệu bằng $diff và tỉ số là $n1/$n2. $label là:",
                        "$ans", intOptions(ans, listOf(if (askBig) v1 else v2, diff, unit, ans + 5), min = 1),
                        bp = VisualBlueprintType.SINGAPORE_BAR_MODEL,
                        hint = "Bước 1: Tìm hiệu số phần bằng nhau ($n2 - $n1 = ${n2 - n1}). Bước 2: Giá trị 1 phần = $diff : ${n2 - n1}."
                    )
                }
            }

            "G5_05_PERCENTAGE_MAP_SCALE" -> when (rng.nextInt(2)) {
                0 -> {
                    val p = choose(listOf(10, 20, 25, 50))
                    val total = rng.nextInt(2, 20) * 100
                    val valP = (total * p) / 100
                    mk(
                        "Tìm $p% của $total:",
                        "$valP", intOptions(valP, listOf(total, p, valP + 10, (valP - 10).coerceAtLeast(1)), min = 1),
                        hint = "Muốn tìm $p% của $total, ta lấy $total : 100 × $p."
                    )
                }
                else -> {
                    val scale = choose(listOf(1000, 10000, 100000))
                    val mapCm = rng.nextInt(2, 10)
                    val realM = (mapCm * scale) / 100
                    mk(
                        "Trên bản đồ tỉ lệ 1 : $scale, khoảng cách giữa hai điểm đo được là $mapCm cm. Khoảng cách thực tế giữa hai điểm đó là bao nhiêu mét?",
                        "$realM m", intOptions(realM, listOf(mapCm * scale, realM * 10, realM / 10, mapCm), " m", 1),
                        hint = "Bước 1: Khoảng cách thực tế = $mapCm × $scale (cm). Bước 2: Đổi cm ra m (chia cho 100)."
                    )
                }
            }

            "G5_06_TRIANGLE_TRAPEZOID_CIRCLE" -> when (rng.nextInt(3)) {
                0 -> {
                    val a = rng.nextInt(4, 20)
                    val h = rng.nextInt(3, 15)
                    val areaStr = fmtDecimal((a * h * 5), 1, trim = true)
                    mk(
                        "Tính diện tích hình tam giác có độ dài đáy $a cm và chiều cao $h cm:",
                        "$areaStr cm²", generate4Options("$areaStr cm²", listOf("${a * h} cm²", "${a + h} cm²", "${(a + h) * 2} cm²")),
                        bp = VisualBlueprintType.GEOMETRY_GRID,
                        hint = "Công thức diện tích hình tam giác: S = (a × h) : 2."
                    )
                }
                1 -> {
                    val a = rng.nextInt(5, 15)
                    val b = rng.nextInt(3, 12)
                    val h = rng.nextInt(2, 10)
                    val areaStr = fmtDecimal(((a + b) * h * 5), 1, trim = true)
                    mk(
                        "Tính diện tích hình thang có độ dài hai đáy lần lượt là $a cm và $b cm, chiều cao $h cm:",
                        "$areaStr cm²", generate4Options("$areaStr cm²", listOf("${(a + b) * h} cm²", "${a + b + h} cm²")),
                        bp = VisualBlueprintType.GEOMETRY_GRID,
                        hint = "Công thức diện tích hình thang: S = (a + b) × h : 2."
                    )
                }
                else -> {
                    val r = rng.nextInt(2, 10)
                    val areaStr = fmtDecimal((r * r * 314), 2, trim = true)
                    mk(
                        "Tính diện tích hình tròn có bán kính $r cm (lấy π = 3,14):",
                        "$areaStr cm²", generate4Options("$areaStr cm²", listOf("${r * 2 * 3.14} cm²", "${r * 3.14} cm²")),
                        bp = VisualBlueprintType.GEOMETRY_GRID,
                        hint = "Công thức diện tích hình tròn: S = r × r × 3,14."
                    )
                }
            }

            "G5_07_SURFACE_AREA_VOLUME_3D" -> when (rng.nextInt(2)) {
                0 -> {
                    val a = rng.nextInt(2, 10)
                    val b = rng.nextInt(2, 10)
                    val c = rng.nextInt(2, 10)
                    val vol = a * b * c
                    mk(
                        "Tính thể tích hình hộp chữ nhật có chiều dài $a cm, chiều rộng $b cm và chiều cao $c cm:",
                        "$vol cm³", intOptions(vol, listOf((a + b + c) * 2, a * b + c, vol + 10), " cm³", 1),
                        hint = "Thể tích hình hộp chữ nhật = chiều dài × chiều rộng × chiều cao: V = a × b × c."
                    )
                }
                else -> {
                    val a = rng.nextInt(2, 10)
                    val vol = a * a * a
                    mk(
                        "Tính thể tích hình lập phương có cạnh $a cm:",
                        "$vol cm³", intOptions(vol, listOf(a * a * 6, a * 12, a * 4), " cm³", 1),
                        hint = "Thể tích hình lập phương = cạnh × cạnh × cạnh: V = a × a × a."
                    )
                }
            }

            "G5_08_TIME_OPS_AND_MOTION" -> when (rng.nextInt(2)) {
                0 -> {
                    val v = rng.nextInt(30, 60)
                    val t = rng.nextInt(2, 5)
                    val s = v * t
                    mk(
                        "Một ô tô đi với vận tốc $v km/giờ trong $t giờ. Quãng đường ô tô đi được là bao nhiêu ki-lô-mét?",
                        "$s km", intOptions(s, listOf(v + t, v, s + 10, s - 10), " km", 1),
                        hint = "Muốn tính quãng đường, ta lấy vận tốc nhân với thời gian: s = v × t."
                    )
                }
                else -> {
                    val t = choose(listOf(2, 3, 4, 5))
                    val v = rng.nextInt(20, 50)
                    val exactS = v * t
                    mk(
                        "Một người đi xe máy đi được quãng đường $exactS km trong $t giờ. Vận tốc của người đi xe máy là bao nhiêu km/giờ?",
                        "$v km/giờ", intOptions(v, listOf(exactS * t, exactS + t, v + 5, v - 5), " km/giờ", 1),
                        hint = "Muốn tính vận tốc, ta lấy quãng đường chia cho thời gian: v = s : t."
                    )
                }
            }

            "G5_09_PIE_CHART_PROBABILITY_RATIO" -> {
                val perc = threePercents()
                val a = perc[0]
                val b = perc[1]
                val c = perc[2]
                val items = listOf("Cơm", "Phở", "Bún")
                val correct = items[0]
                mk(
                    "Biểu đồ hình quạt tròn thể hiện tỉ lệ chọn món ăn: Cơm $a%, Phở $b%, Bún $c%. Món ăn nào được chọn nhiều nhất?",
                    correct, generate4Options(correct, items.filter { it != correct }),
                    hint = "Món ăn nào có tỉ lệ phần trăm lớn nhất thì được chọn nhiều nhất."
                )
            }

            "G5_10_COMPREHENSIVE_HEURISTICS" -> {
                val legs = choose(listOf(36, 44, 52, 100))
                val totalAnimals = 36
                val dogs = (legs - 72) / 2
                val chickens = totalAnimals - dogs
                val exactLegs = chickens * 2 + dogs * 4
                val exactChickens = (4 * 36 - exactLegs) / 2
                val exactDogs = 36 - exactChickens
                mk(
                    "Vừa gà vừa chó có 36 con, đếm được tất cả $exactLegs cái chân. Hỏi có bao nhiêu con chó?",
                    "$exactDogs con", intOptions(exactDogs, listOf(exactChickens, 18, exactDogs + 2), " con", 1),
                    hint = "Phương pháp giả thiết tạm: Giả sử tất cả 36 con đều là gà, tính tổng số chân rồi tìm số con chó."
                )
            }

            else -> {
                val a = rng.nextInt(10, 50)
                val b = rng.nextInt(2, 10)
                val sum = a + b
                mk("Tính kết quả: $a + $b = ?", "$sum", intOptions(sum, listOf(sum + 1, sum - 1, sum + 2)), "$a + $b")
            }
        }
    }

    private fun getFallbackProblem(
        nodeId: String,
        grade: Int,
        scaffoldingLevel: ScaffoldingLevel,
        academicTier: AcademicTier
    ): AcademicProblem {
        val a = rng.nextInt(5, 20)
        val b = rng.nextInt(1, 10)
        val sum = a + b
        val correct = "$sum"
        val options = generate4Options(correct, listOf("${sum + 1}", "${sum - 1}", "${sum + 2}"))
        return AcademicProblem(
            problemId = "${nodeId}_fallback_${rng.nextInt(10000)}",
            nodeId = nodeId,
            scaffoldingLevel = scaffoldingLevel,
            academicTier = academicTier,
            statementVi = "Tính kết quả phép tính: $a + $b = ?",
            blueprintSpec = VisualBlueprintSpec(
                type = VisualBlueprintType.NONE,
                primaryLabel = "$a + $b",
                paramValue1 = 0.0,
                paramValue2 = 20.0,
                paramValue3 = sum.toDouble()
            ),
            steps = listOf(
                SolutionStep(
                    stepIndex = 1,
                    promptVi = "Chọn phương án đúng:",
                    mathExpression = "$a + $b",
                    expectedAnswer = correct,
                    answerMode = AnswerMode.MULTIPLE_CHOICE_4,
                    options = options,
                    correctOptionIndex = options.indexOf(correct),
                    bugRules = emptyMap()
                )
            )
        )
    }
}


