package com.example.toanhoc.domain.thinking

import com.example.toanhoc.domain.model.AcademicProblem
import com.example.toanhoc.domain.model.AcademicTier
import com.example.toanhoc.domain.model.AnswerMode
import com.example.toanhoc.domain.model.ScaffoldingLevel
import com.example.toanhoc.domain.model.SolutionStep
import kotlin.random.Random

/* ------------------------------------------------------------------------------------------
 * TOÁN TƯ DUY – bộ sinh đề theo chuyên đề (thí điểm lớp 3).
 *
 * Mỗi dạng = khuôn đề có ràng buộc (số tự nhiên, số liệu thực tế, đáp án duy nhất) + đáp án nhiễu
 * sinh từ LỖI SAI THƯỜNG GẶP. Tham số của đề được ghi vào problemId ("T3_03:age3:32:8:3:#") để
 * bộ giảng (ThinkingTheories) giải lại đúng bài đó bằng HAI CÁCH và giải thích từng đáp án sai.
 *
 *   T3_01 Tính nhanh          T3_02 Dãy số quy luật
 *   T3_03 Bài toán tính tuổi  T3_04 Tìm x – đi ngược
 * ---------------------------------------------------------------------------------------- */

object ThinkingEngine {

    val NODES = listOf("T3_01", "T3_02", "T3_03", "T3_04") + ThinkingBank.NODES

    fun supports(nodeId: String) = nodeId.take(5) in NODES

    fun generate(nodeId: String, tier: AcademicTier, level: ScaffoldingLevel, rng: Random = Random.Default): AcademicProblem {
        // Dạng 1 = mức 0; Dạng 2 (GIỎI) trộn mức 1 và 2; XUẤT SẮC = mức 2
        val t = when (tier) { AcademicTier.TRUNG_BINH -> 0; AcademicTier.GIOI -> if (rng.nextBoolean()) 1 else 2; else -> 2 }
        repeat(80) {
            val p = runCatching {
                when (nodeId.take(5)) {
                    "T3_01" -> quick(t, rng)
                    "T3_02" -> sequence(t, rng)
                    "T3_03" -> age(t, rng)
                    "T3_04" -> reverse(t, rng)
                    else -> ThinkingBank.gen(nodeId.take(5), t, rng)
                }.toProblem(nodeId.take(5), tier, level, rng)
            }.getOrNull()
            if (p != null) return p
        }
        error("Không sinh được đề cho $nodeId")
    }

    /** Dạng (kind) của một đề Toán tư duy, đọc từ problemId. */
    fun kindOf(problemId: String): String? = problemId.split(":").getOrNull(1)

    /**
     * "CÂU SINH ĐÔI": cùng chuyên đề, cùng mức, cùng dạng – khác số liệu.
     * Dùng ngay sau khi con xem phân tích lỗi để con tự làm lại và sửa được lỗi.
     */
    fun twin(problem: AcademicProblem, rng: Random = Random.Default): AcademicProblem? {
        val kind = kindOf(problem.problemId) ?: return null
        val form = problem.problemId.split(":").getOrNull(2)
        var sameKind: AcademicProblem? = null
        repeat(200) {
            val p = runCatching { generate(problem.nodeId, problem.academicTier, problem.scaffoldingLevel, rng) }.getOrNull() ?: return@repeat
            if (kindOf(p.problemId) != kind || p.statementVi == problem.statementVi) return@repeat
            // ưu tiên cùng "kiểu" (tham số đầu = dạng con) để thật sự là câu sinh đôi
            if (p.problemId.split(":").getOrNull(2) == form) return p
            if (sameKind == null) sameKind = p
        }
        return sameKind
    }

    /** Một đề thô: lời đề, đáp án, đáp án nhiễu (theo lỗi), tham số ghi vào problemId. */
    internal data class Raw(val kind: String, val params: List<Long>, val statement: String, val answer: String, val traps: List<String>)

    internal fun Raw.toProblem(node: String, tier: AcademicTier, level: ScaffoldingLevel, rng: Random): AcademicProblem {
        val opts = LinkedHashSet<String>(); opts += answer
        traps.filter { it != answer && !it.startsWith("-") && !it.startsWith("0 ") && it != "0" }.forEach { if (opts.size < 4) opts += it }
        // Bù đáp án gần đúng nếu thiếu
        val n = answer.substringBefore(' ').toLongOrNull()
        val unit = answer.substringAfter(' ', "").let { if (it.isEmpty()) "" else " $it" }
        var k = 1
        while (opts.size < 4 && n != null && k < 50) {
            listOf(n + k, n - k).filter { it > 0 }.forEach { if (opts.size < 4) opts += "$it$unit" }
            k++
        }
        require(opts.size == 4)
        val options = opts.toList().shuffled(rng)
        return AcademicProblem(
            problemId = "$node:$kind:${params.joinToString(":")}:${rng.nextInt(100000)}",
            nodeId = node,
            scaffoldingLevel = level,
            academicTier = tier,
            statementVi = statement,
            steps = listOf(
                SolutionStep(
                    stepIndex = 1, promptVi = "Chọn phương án đúng:", mathExpression = statement,
                    expectedAnswer = answer, answerMode = AnswerMode.MULTIPLE_CHOICE_4,
                    options = options, correctOptionIndex = options.indexOf(answer)
                )
            )
        )
    }

    private fun Random.pick(vararg xs: Int) = xs[nextInt(xs.size)]

    /* ======================================== T3_01 Tính nhanh ======================================== */

    private fun quick(t: Int, rng: Random): Raw = when (t) {
        0 -> ThinkingBank.qsum(rng, 3, 0)
        1 -> if (rng.nextBoolean()) {
            // Tổng, hiệu nhiều số: ghép tròn trăm, trừ liên tiếp, thêm rồi bớt, làm tròn rồi bù
            ThinkingBank.qsum(rng, 3, 1)
        } else {
            // Nhóm 5 × 2 hoặc 25 × 4 trước
            val (p, q) = if (rng.nextBoolean()) 5 to 2 else 25 to 4
            val m = rng.nextInt(3, if (p == 5) 99 else 10)
            val r = p * q * m
            Raw("mul", listOf(p.toLong(), m.toLong(), q.toLong()), "Tính nhanh: $p × $m × $q = ?", "$r",
                listOf("${p * m}", "${p * (m + q)}", "${r + p * q}"))
        }
        else -> if (rng.nextBoolean()) {
            // Đặt thừa số chung: a × b + a × c với b + c = 10 (hoặc a × b − a × c với b − c = 10)
            val tt = rng.pick(10, 10, 20, 100)
            val a = if (tt == 100) rng.nextInt(2, 10) else rng.nextInt(3, 20)
            if (rng.nextBoolean()) {
                val b = if (tt == 100) rng.nextInt(11, 90).let { if (it % 10 == 0) it + 3 else it } else rng.nextInt(2, tt - 1).let { if (it * 2 == tt) it + 1 else it }
                val c = tt - b
                Raw("dist", listOf(a.toLong(), b.toLong(), c.toLong(), 1, tt.toLong()), "Tính nhanh: $a × $b + $a × $c = ?", "${a * tt}",
                    listOf("${a * b + c}", "${a * tt + a}", "${a * (tt - 1)}"))
            } else {
                val c = if (tt == 100) rng.nextInt(11, 60) else rng.nextInt(2, 10); val b = c + tt
                Raw("dist", listOf(a.toLong(), b.toLong(), c.toLong(), 0, tt.toLong()), "Tính nhanh: $a × $b − $a × $c = ?", "${a * tt}",
                    listOf("${a * b - c}", "${a * (tt - 1)}", "${a * 2 * tt}"))
            }
        } else {
            // Tổng dãy cách đều: ghép số đầu với số cuối
            val step = rng.nextInt(1, 6); val first = rng.nextInt(1, 21)
            val n = rng.nextInt(6, 13)
            val last = first + (n - 1) * step
            val sum = (first + last) * n / 2
            if ((first + last) * n % 2 != 0) error("retry")
            val shown = "$first + ${first + step} + ${first + 2 * step} + … + $last"
            Raw("gauss", listOf(first.toLong(), step.toLong(), last.toLong()), "Tính nhanh tổng: $shown = ?", "$sum",
                listOf("${(first + last) * n}", "${sum - last}", "${sum + first}"))
        }
    }

    /* ======================================== T3_02 Dãy số quy luật ======================================== */

    private fun sequence(t: Int, rng: Random): Raw = when (t) {
        0 -> {
            val d = rng.nextInt(2, 10); val a = rng.nextInt(1, 30); val up = rng.nextInt(4) != 0
            val xs = (0 until 4).map { if (up) a + it * d else a + 40 + (3 - it) * d }.let { if (up) it else it.sortedDescending() }
            val next = if (up) xs.last() + d else xs.last() - d
            if (next <= 0) error("retry")
            Raw("arith", listOf(xs[0].toLong(), (if (up) d else -d).toLong()), "Tìm số tiếp theo của dãy: ${xs.joinToString(", ")}, …", "$next",
                listOf("${if (up) next + 1 else next - 1}", "${if (up) xs.last() + 2 * d else xs.last() - 2 * d}", "${if (up) next - 1 else next + 1}"))
        }
        1 -> when (rng.nextInt(3)) { 0 -> {
            // Khoảng cách tăng dần: +k, +(k+1), +(k+2)…
            val a = rng.nextInt(1, 40); val k = rng.nextInt(1, 6)
            val xs = mutableListOf(a); repeat(4) { xs += xs.last() + k + it }
            val next = xs.last() + k + 4
            Raw("grow", listOf(a.toLong(), k.toLong()), "Tìm số tiếp theo của dãy: ${xs.joinToString(", ")}, …", "$next",
                listOf("${xs.last() + k + 3}", "${next + 1}", "${xs.last() * 2}"))
        }
        1 -> {
            // Mỗi số bằng tổng hai số đứng trước
            val a = rng.nextInt(1, 10); val b = rng.nextInt(1, 10)
            val xs = mutableListOf(a, b); repeat(3) { xs += xs[xs.size - 1] + xs[xs.size - 2] }
            val next = xs[xs.size - 1] + xs[xs.size - 2]
            Raw("fib", listOf(a.toLong(), b.toLong()), "Tìm số tiếp theo của dãy: ${xs.joinToString(", ")}, …", "$next",
                listOf("${xs.last() + (xs.last() - xs[xs.size - 2])}", "${xs.last() * 2}", "${next + 1}"))
        }
        else -> {
            // Gấp đôi (gấp 3) mỗi lần
            val m = rng.pick(2, 2, 3); val a = rng.nextInt(1, if (m == 2) 13 else 6)
            val xs = mutableListOf(a); repeat(3) { xs += xs.last() * m }
            val next = xs.last() * m
            Raw("geo", listOf(a.toLong(), m.toLong()), "Tìm số tiếp theo của dãy: ${xs.joinToString(", ")}, …", "$next",
                listOf("${xs.last() + (xs.last() - xs[xs.size - 2])}", "${xs.last() + m}", "${next + m}"))
        }
        }
        else -> if (rng.nextBoolean()) {
            // Số thứ n của dãy cách đều
            val a = rng.nextInt(1, 10); val d = rng.nextInt(2, 7); val n = rng.pick(10, 15, 20, 25, 30)
            val v = a + (n - 1) * d
            Raw("nth", listOf(a.toLong(), d.toLong(), n.toLong()), "Cho dãy số: $a, ${a + d}, ${a + 2 * d}, ${a + 3 * d}, … Số thứ $n của dãy là số nào?", "$v",
                listOf("${a + n * d}", "${n * d}", "${v - d}"))
        } else {
            // Đếm số các số hạng
            val a = rng.nextInt(1, 10); val d = rng.nextInt(2, 6); val n = rng.nextInt(12, 31)
            val last = a + (n - 1) * d
            Raw("count", listOf(a.toLong(), d.toLong(), last.toLong()), "Dãy số $a, ${a + d}, ${a + 2 * d}, …, $last có tất cả bao nhiêu số?", "$n",
                listOf("${n - 1}", "${n + 1}", "${last / d}"))
        }
    }

    /* ======================================== T3_03 Tính tuổi ======================================== */

    private val ADULT = listOf("mẹ", "bố")

    private fun age(t: Int, rng: Random): Raw = when (t) {
        0 -> {
            val who = ADULT[rng.nextInt(2)]; val c = rng.nextInt(5, 12); val gap = rng.nextInt(22, 32); val m = c + gap
            val n = rng.nextInt(3, 9); val later = rng.nextBoolean() || n >= c
            Raw("diff", listOf(m.toLong(), c.toLong(), n.toLong(), if (later) 1 else 0),
                "Năm nay $who $m tuổi, con $c tuổi. Hỏi ${if (later) "$n năm nữa" else "$n năm trước"} $who hơn con bao nhiêu tuổi?",
                "$gap tuổi", listOf("${if (later) gap + n else gap - n} tuổi", "${if (later) gap + 2 * n else gap + n} tuổi", "${m + c} tuổi"))
        }
        1 -> if (rng.nextBoolean()) {
            val s = rng.nextInt(30, 55); val n = rng.nextInt(2, 8)
            Raw("sum", listOf(s.toLong(), n.toLong()),
                "Năm nay tổng số tuổi của bố và con là $s tuổi. Hỏi $n năm nữa tổng số tuổi của hai bố con là bao nhiêu?",
                "${s + 2 * n} tuổi", listOf("${s + n} tuổi", "${s + 4 * n} tuổi", "${s - 2 * n} tuổi"))
        } else {
            val e = rng.nextInt(4, 9); val gap = rng.nextInt(3, 7); val a = e + gap; val target = e + rng.nextInt(3, 8)
            Raw("when", listOf(a.toLong(), e.toLong(), target.toLong()),
                "Năm nay anh $a tuổi, em $e tuổi. Hỏi khi em $target tuổi thì anh bao nhiêu tuổi?",
                "${target + gap} tuổi", listOf("${a + target - gap} tuổi".takeIf { a + target - gap != target + gap } ?: "${target + gap + 2} tuổi", "$a tuổi".takeIf { a != target + gap } ?: "${a + 1} tuổi", "${target + (target - e)} tuổi"))
        }
        else -> {
            // Sau n năm tuổi mẹ gấp k lần tuổi con: hiệu không đổi
            val who = ADULT[rng.nextInt(2)]; val k = rng.pick(3, 4, 5, 6); val cAt = rng.nextInt(5, 17)
            val gap = (k - 1) * cAt; val c = rng.nextInt(3, cAt); val m = c + gap; val n = cAt - c
            if (m !in 24..55 || gap < 20 || n < 1) error("retry")
            Raw("times", listOf(m.toLong(), c.toLong(), k.toLong()),
                "Năm nay $who $m tuổi, con $c tuổi. Hỏi sau mấy năm nữa thì tuổi $who gấp $k lần tuổi con?",
                "$n năm", listOf("$cAt năm", "${gap / k} năm".takeIf { gap / k != n && gap % k == 0 } ?: "${n + 2} năm", "${n + 1} năm"))
        }
    }

    /* ======================================== T3_04 Tìm x – đi ngược ======================================== */

    private fun reverse(t: Int, rng: Random): Raw = when (t) {
        0 -> {
            when (rng.nextInt(4)) {
                0 -> { val x = rng.nextInt(12, 90); val b = rng.nextInt(12, 90); Raw("x1", listOf(0, x.toLong(), b.toLong(), 1), "Số thích hợp điền vào ô trống: □ + $b = ${x + b}", "$x", listOf("${x + 2 * b}", "${x + 10}", "${b}")) }
                1 -> { val x = rng.nextInt(30, 99); val b = rng.nextInt(11, x - 5); Raw("x1", listOf(1, x.toLong(), b.toLong(), 1), "Số thích hợp điền vào ô trống: □ − $b = ${x - b}", "$x", listOf("${x - 2 * b}", "${x - 10}", "${x + 10}")) }
                2 -> { val x = rng.nextInt(3, 10); val b = rng.nextInt(3, 10); Raw("x1", listOf(2, x.toLong(), b.toLong(), 1), "Số thích hợp điền vào ô trống: □ × $b = ${x * b}", "$x", listOf("${x * b - b}", "${x + 1}", "${x * b * b}")) }
                else -> { val q = rng.nextInt(3, 10); val b = rng.nextInt(3, 10); Raw("x1", listOf(3, (q * b).toLong(), b.toLong(), 1), "Số thích hợp điền vào ô trống: □ : $b = $q", "${q * b}", listOf("${q + b}", "${q * b + b}", "${q}")) }
            }
        }
        1 -> {
            val x = rng.nextInt(2, 12)
            when (rng.nextInt(3)) {
                0 -> { val a = rng.nextInt(2, 9); val k = rng.nextInt(2, 7); val r = (x + a) * k
                    Raw("x2", listOf(0, x.toLong(), a.toLong(), k.toLong(), 1), "Số thích hợp điền vào ô trống: (□ + $a) × $k = $r", "$x", listOf("${r / k + a}", "${r - a}".takeIf { r - a != x } ?: "${x + 3}", "${(r - a) / k}".takeIf { (r - a) % k == 0 && (r - a) / k != x } ?: "${x + 2}")) }
                1 -> { val k = rng.nextInt(2, 9); val a = rng.nextInt(5, 40); val r = x * k + a
                    Raw("x2", listOf(1, x.toLong(), k.toLong(), a.toLong(), 1), "Số thích hợp điền vào ô trống: □ × $k + $a = $r", "$x", listOf("${(r + a) / k}".takeIf { (r + a) % k == 0 } ?: "${x + 4}", "${r - a}", "${x + 1}")) }
                else -> { val a = rng.nextInt(2, 9); val k = rng.nextInt(2, 6); val y = x - a; if (y <= 0) error("retry"); val r = y * k
                    Raw("x2", listOf(2, x.toLong(), a.toLong(), k.toLong(), 1), "Số thích hợp điền vào ô trống: (□ − $a) × $k = $r", "$x", listOf("${r / k - a}", "${r + a}", "${r / k}")) }
            }
        }
        else -> {
            // Bài toán nghĩ số: đi ngược từ kết quả
            val name = listOf("Lan", "Minh", "Mai", "Nam").let { it[rng.nextInt(it.size)] }
            val x = rng.nextInt(5, 30); val k = rng.nextInt(2, 6); val a = rng.nextInt(5, 30)
            if (rng.nextBoolean()) {
                val r = x * k + a
                Raw("think", listOf(0, x.toLong(), k.toLong(), a.toLong()), "$name nghĩ một số. Đem số đó nhân với $k rồi cộng thêm $a thì được $r. Hỏi $name đã nghĩ số nào?", "$x",
                    listOf("${(r + a) / k}".takeIf { (r + a) % k == 0 && (r + a) / k != x } ?: "${x + 5}", "${r / k - a}".takeIf { r / k - a > 0 && r / k - a != x } ?: "${x + 2}", "${r - a}"))
            } else {
                val r = (x - a).let { if (it <= 0) error("retry") else it } * k
                Raw("think", listOf(1, x.toLong(), a.toLong(), k.toLong()), "$name nghĩ một số. Đem số đó bớt đi $a rồi nhân với $k thì được $r. Hỏi $name đã nghĩ số nào?", "$x",
                    listOf("${r / k - a}".takeIf { r / k - a > 0 && r / k - a != x } ?: "${x + 3}", "${r - a}", "${(r + a) / k}".takeIf { (r + a) % k == 0 && (r + a) / k != x } ?: "${x + 1}"))
            }
        }
    }
}
