package com.example.toanhoc.domain.thinking

import com.example.toanhoc.domain.thinking.ThinkingEngine.Raw
import kotlin.random.Random

/** Từ ngữ dùng chung cho đề và lời giảng (tham số đề chỉ lưu CHỈ SỐ vào problemId). */
internal object ThinkingWords {
    val KIDS = listOf("Lan", "Minh", "Mai", "Nam", "Hoa", "Tùng", "An", "Bình")
    /** (tên đồ vật đầy đủ, đơn vị đếm). */
    val THINGS = listOf("quả táo" to "quả", "viên bi" to "viên", "bông hoa" to "bông", "quyển vở" to "quyển", "cái kẹo" to "cái", "con tem" to "con")

    /** Số thập phân lưu theo phần trăm (hundredths): 370 → "3,7". */
    fun dec(h: Long): String {
        val neg = h < 0; val a = kotlin.math.abs(h)
        val i = a / 100; val f = a % 100
        val s = when {
            f == 0L -> "$i"
            f % 10 == 0L -> "$i,${f / 10}"
            else -> "$i,${f.toString().padStart(2, '0')}"
        }
        return if (neg) "-$s" else s
    }
}

/**
 * Bộ sinh đề Toán tư duy cho lớp 1, 2, 4, 5 (lớp 3 ở ThinkingEngine).
 * Mỗi node có 3 mức (t = 0 cơ bản, 1 nâng cao, 2 thử thách); số liệu theo đúng phạm vi đã học của lớp.
 */
internal object ThinkingBank {
    private fun Random.pick(vararg xs: Int) = xs[nextInt(xs.size)]
    private fun L(vararg xs: Int) = xs.map { it.toLong() }
    private val W = ThinkingWords

    val NODES = listOf(
        "T1_01", "T1_02", "T1_03", "T1_04",
        "T2_01", "T2_02", "T2_03", "T2_04",
        "T4_01", "T4_02", "T4_03", "T4_04", "T4_05", "T4_06", "T4_07",
        "T5_01", "T5_02", "T5_03", "T5_04"
    )

    fun gen(node: String, t: Int, rng: Random): Raw = when (node) {
        // ---------------- LỚP 1 ----------------
        "T1_01" -> qsum(rng, 1, t)
        "T1_02" -> when (t) {
            0 -> arith(rng.nextInt(1, 40), rng.nextInt(1, 6), true)
            1 -> arith(rng.nextInt(10, 60), rng.nextInt(2, 11), rng.nextInt(3) != 0)
            else -> grow(rng.nextInt(1, 40), rng.nextInt(1, 4))
        }
        "T1_03" -> when (t) {
            0 -> box(rng.nextInt(2), rng.nextInt(1, 10), rng.nextInt(1, 10), 20)
            1 -> box(rng.nextInt(2), rng.nextInt(10, 60), rng.nextInt(2, 30), 99)
            else -> thinkAddSub(rng, 30)
        }
        "T1_04" -> cmp(rng, if (t == 0) rng.nextInt(2) else if (t == 1) 2 + rng.nextInt(2) else rng.nextInt(4), if (t == 0) 20 else if (t == 1) 50 else 99)

        // ---------------- LỚP 2 ----------------
        "T2_01" -> when (t) {
            0 -> qsum(rng, 2, 0)
            1 -> qsum(rng, 2, 1)
            else -> { val first = rng.nextInt(1, 21); val step = rng.nextInt(1, 6); val n = rng.nextInt(4, 11)
                val last = first + (n - 1) * step; if ((first + last) % 10 != 0 || (first + last) * n / 2 > 200) error("retry"); gauss(first, step, n) }
        }
        "T2_02" -> when (t) {
            0 -> arith(rng.nextInt(5, 60), rng.pick(2, 3, 4, 5, 10), rng.nextInt(3) != 0)
            1 -> if (rng.nextBoolean()) grow(rng.nextInt(1, 30), rng.nextInt(1, 4)) else arith(rng.pick(100, 200, 300, 150, 250), rng.pick(10, 50, 100), true)
            else -> { val d = rng.pick(2, 5); val a = rng.nextInt(1, 21); val n = rng.nextInt(8, 21); if (a + (n - 1) * d > 100) error("retry"); countSeq(a, d, a + (n - 1) * d) }
        }
        "T2_03" -> when (t) {
            0 -> box(rng.nextInt(2), rng.nextInt(12, 70), rng.nextInt(8, 29), 100)
            1 -> { val b = rng.pick(2, 5); if (rng.nextBoolean()) box(2, rng.nextInt(2, 11), b, 100) else box(3, rng.nextInt(2, 11) * b, b, 100) }
            else -> if (rng.nextBoolean()) thinkAddSub(rng, 100) else thinkMulAdd(rng, rng.pick(2, 5), 10, 30)
        }
        "T2_04" -> when (t) {
            0 -> cmp(rng, rng.nextInt(2), 100)
            1 -> cmp(rng, 2 + rng.nextInt(2), 100)
            else -> ageDiffWhen(rng)
        }

        // ---------------- LỚP 4 ----------------
        "T4_01" -> when (t) {
            0 -> qsum(rng, 4, 1)
            1 -> if (rng.nextBoolean()) { val m = rng.nextInt(3, 10); mul(25, m, 4) } else distRaw(rng.nextInt(2, 10), rng.nextInt(11, 90), 100, rng.nextBoolean())
            else -> if (rng.nextBoolean()) { val first = rng.nextInt(1, 30); val step = rng.nextInt(1, 6); val n = rng.nextInt(10, 31)
                val last = first + (n - 1) * step; if ((first + last) * n / 2 > 999) error("retry"); gauss(first, step, n) }
                else distRaw(rng.nextInt(11, 100), rng.nextInt(2, 9), 10, rng.nextBoolean())
        }
        "T4_02" -> when (t) {
            0 -> sumDiff(rng, 0)
            1 -> sumDiff(rng, 1 + rng.nextInt(2))
            else -> sumDiff(rng, if (rng.nextBoolean()) 3 else 2)
        }
        "T4_03" -> ratio(rng, mode = if (t == 1) 1 else if (t == 0) 0 else rng.nextInt(2), frac = false, ctx = if (t == 2) 1 else 0)
        "T4_04" -> when (t) {
            0 -> avgList(rng, 3, 0)
            1 -> if (rng.nextBoolean()) avgList(rng, rng.nextInt(4, 6), 0) else avgList(rng, 3, 2)
            else -> avgMissing(rng)
        }
        "T4_05" -> when (t) {
            0 -> if (rng.nextBoolean()) { val a = rng.nextInt(1, 20); val d = rng.nextInt(2, 10); val n = rng.nextInt(20, 61); nth(a, d, n) }
                 else { val a = rng.nextInt(1, 20); val d = rng.nextInt(2, 10); val n = rng.nextInt(20, 81); countSeq(a, d, a + (n - 1) * d) }
            1 -> digits(rng.nextInt(20, 100), 0)
            else -> digits(rng.nextInt(100, 300), rng.nextInt(2))
        }

        "T4_06" -> when (t) {
            0 -> fracCompare(rng, rng.nextInt(2))
            1 -> fracCompare(rng, 2 + rng.nextInt(2))
            else -> if (rng.nextBoolean()) fracPairs(rng) else fracHalves(rng)
        }
        "T4_07" -> when (t) {
            0 -> fracOf(rng)
            1 -> fracRemain(rng)
            else -> fracWhole(rng)
        }

        // ---------------- LỚP 5 ----------------
        "T5_01" -> when (t) {
            0 -> if (rng.nextBoolean()) decQuarter(rng) else decHalf(rng)
            1 -> decDist(rng)
            else -> decNine(rng)
        }
        "T5_02" -> when (t) {
            0 -> pctOf(rng)
            1 -> if (rng.nextBoolean()) pctRatio(rng) else pctWhole(rng)
            else -> pctSale(rng)
        }
        "T5_03" -> ratio(rng, mode = if (t == 1) 1 else if (t == 0) 0 else rng.nextInt(2), frac = true, ctx = if (t == 2) 1 else 0)
        "T5_04" -> when (t) {
            0 -> motionDist(rng)
            1 -> motionTime(rng)
            else -> if (rng.nextBoolean()) motionMeet(rng) else motionChase(rng)
        }
        else -> error("node $node")
    }


    /* ============================== TÍNH NHANH: tổng, hiệu nhiều số (dạng "qsum") ==============================
     * Thay cho dạng cũ a + b + (100 − a) + (100 − b) luôn ra 200. Có 5 kiểu, số liệu và đáp số đổi liên tục:
     *   0 ghép cặp tròn chục/tròn trăm (3–5 số hạng, có thể có số lẻ ra, thứ tự xáo trộn)
     *   1 a − b − c  với b + c tròn          2 a + b − c  với b − c tròn
     *   3 a + b với b gần số tròn (298…)     4 a − b với b gần số tròn
     * ------------------------------------------------------------------------------------------------ */
    private fun nonRound(x: Int) = x % 10 != 0

    internal fun qsum(rng: Random, g: Int, t: Int): Raw {
        val form = when {
            g <= 1 -> 0
            g == 2 -> if (t == 0) 0 else listOf(0, 0, 1, 3)[rng.nextInt(4)]
            t == 0 -> if (rng.nextInt(3) == 0) 1 else 0
            else -> listOf(0, 1, 2, 3, 4)[rng.nextInt(5)]
        }
        return when (form) {
            0 -> qsumPairs(rng, g, t)
            1 -> qsumMinus(rng, g, t)
            2 -> qsumPlusMinus(rng, g, t)
            3 -> qsumNear(rng, g, false)
            else -> qsumNear(rng, g, true)
        }
    }

    private fun qsumPairs(rng: Random, g: Int, t: Int): Raw {
        // các tổng của cặp + các số lẻ ra
        val sums: List<Int>; val lefts: List<Int>
        when {
            g <= 1 -> when (t) {
                0 -> { sums = listOf(10); lefts = listOf(rng.nextInt(1, 10)) }
                1 -> { sums = listOf(20, 10); lefts = if (rng.nextBoolean()) listOf(rng.nextInt(1, 10)) else emptyList() }
                else -> { sums = listOf(10, 10); lefts = listOf(rng.nextInt(1, 10)) }
            }
            g == 2 && t == 0 -> { sums = listOf(10 * rng.nextInt(3, 10), 10 * rng.nextInt(3, 10)); lefts = if (rng.nextBoolean()) listOf(rng.nextInt(11, 60)) else emptyList() }
            g == 2 -> { sums = listOf(100 * rng.nextInt(1, 4), 100 * rng.nextInt(1, 3)); lefts = listOf(rng.nextInt(11, 99)) }
            t == 0 -> { sums = listOf(10 * rng.nextInt(3, 10), 10 * rng.nextInt(3, 10)); lefts = if (rng.nextBoolean()) listOf(rng.nextInt(11, 60)) else emptyList() }
            else -> { sums = listOf(100 * rng.nextInt(1, 5), 100 * rng.nextInt(1, 4)); lefts = listOf(rng.nextInt(11, 99)) }
        }
        val pairs = sums.map { S ->
            var got: Pair<Int, Int>? = null
            repeat(40) {
                if (got != null) return@repeat
                val x = when {
                    S == 20 && g <= 1 -> rng.nextInt(11, 20)
                    S == 10 -> rng.nextInt(1, 10)
                    else -> rng.nextInt(11, S - 10)
                }
                val y = S - x
                if (y > 0 && x != y && nonRound(x) && nonRound(y) && (S == 10 || S == 20 || y > 10)) got = x to y
            }
            got ?: error("retry")
        }
        if (lefts.any { !nonRound(it) }) error("retry")
        val ans = sums.sum() + lefts.sum()
        if (ans > 999 || (g <= 1 && ans > 60)) error("retry")
        // xáo thứ tự, tránh để hai số của một cặp đứng cạnh nhau
        val items = pairs.flatMapIndexed { pi, (x, y) -> listOf(x to pi, y to pi) } + lefts.map { it to -1 }
        var order = items.shuffled(rng)
        repeat(30) {
            if (order.zipWithNext().none { (u, w) -> u.second >= 0 && u.second == w.second }) return@repeat
            order = items.shuffled(rng)
        }
        val terms = order.map { it.first }
        if (terms.toSet().size != terms.size) error("retry")
        val idx = pairs.indices.flatMap { pi -> order.withIndex().filter { it.value.second == pi }.map { it.index } }
        val unit = if (sums.any { it % 100 == 0 }) 100 else 10
        val traps = mutableListOf(ans + unit, ans - unit)
        traps += if (lefts.isNotEmpty()) ans - lefts.first() else ans + 1
        return Raw("qsum", listOf(0L, terms.size.toLong()) + terms.map { it.toLong() } + listOf(pairs.size.toLong()) + idx.map { it.toLong() },
            "Tính nhanh: ${terms.joinToString(" + ")} = ?", "$ans", traps.filter { it > 0 }.map { "$it" })
    }

    /** a − b − c, b + c tròn. */
    private fun qsumMinus(rng: Random, g: Int, t: Int): Raw {
        val R = if (g <= 2 || t == 0) 10 * rng.nextInt(3, 10) else 100 * rng.nextInt(1, 5)
        val b = rng.nextInt(11, R - 10); val c = R - b
        if (!nonRound(b) || !nonRound(c) || b == c) error("retry")
        val a = R + rng.nextInt(20, if (g <= 2) 200 else 500)
        val ans = a - R
        return Raw("qsum", listOf(1L, a.toLong(), b.toLong(), c.toLong()), "Tính nhanh: $a − $b − $c = ?", "$ans",
            listOf("${a - b + c}", "${ans + 10}", "${ans - 10}".takeIf { ans > 10 } ?: "${ans + 20}"))
    }

    /** a + b − c, b − c tròn. */
    private fun qsumPlusMinus(rng: Random, g: Int, t: Int): Raw {
        val R = if (t <= 1) 10 * rng.nextInt(2, 10) else 100 * rng.nextInt(1, 4)
        val c = rng.nextInt(11, 99); val b = c + R
        if (!nonRound(c) || b > 999) error("retry")
        val a = rng.nextInt(101, 600).let { if (it % 10 == 0) it + 3 else it }
        val ans = a + R
        if (a + b + c > 999 || ans > 999) error("retry")
        return Raw("qsum", listOf(2L, a.toLong(), b.toLong(), c.toLong()), "Tính nhanh: $a + $b − $c = ?", "$ans",
            listOf("${a + b + c}", "${a - R}".takeIf { a > R } ?: "${ans + 100}", "${ans - 10}"))
    }

    /** a + b (hoặc a − b) với b = R − k gần số tròn. */
    private fun qsumNear(rng: Random, g: Int, minus: Boolean): Raw {
        val R = if (g <= 2) 10 * rng.nextInt(2, 10) else rng.pick(100, 200, 300, 400, 500, 600)
        val k = rng.nextInt(1, if (R >= 100) 4 else 3); val b = R - k
        return if (!minus) {
            val a = rng.nextInt(if (g <= 2) 11 else 101, if (g <= 2) 90 else 400).let { if (it % 10 == 0) it + 7 else it }
            val ans = a + b; if (ans > 999) error("retry")
            Raw("qsum", listOf(3L, a.toLong(), b.toLong(), R.toLong(), k.toLong()), "Tính nhanh: $b + $a = ?", "$ans",
                listOf("${a + R + k}", "${a + R}", "${ans + 10}"))
        } else {
            val a = rng.nextInt(R + 20, if (g <= 2) R + 90 else 999).let { if (it % 10 == 0) it + 7 else it }
            val ans = a - b
            Raw("qsum", listOf(4L, a.toLong(), b.toLong(), R.toLong(), k.toLong()), "Tính nhanh: $a − $b = ?", "$ans",
                listOf("${a - R - k}", "${a - R}", "${ans + 10}"))
        }
    }

    /* ============================== Dạng dùng chung với lớp 3 (cùng bố cục tham số) ============================== */

    private fun pair(a: Int, b: Int, a2: Int, b2: Int): Raw {
        val r = a + b + a2 + b2; val unit = if ((a + a2) % 100 == 0) 100 else 10
        return Raw("pair", L(a, b, a2, b2), "Tính nhanh: $a + $b + $a2 + $b2 = ?", "$r", listOf("${r + unit}", "${r - unit}", "${r + 1}"))
    }

    private fun mul(p: Int, m: Int, q: Int): Raw { val r = p * q * m
        return Raw("mul", L(p, m, q), "Tính nhanh: $p × $m × $q = ?", "$r", listOf("${p * m}", "${p * (m + q)}", "${r + p * q}")) }

    private fun distRaw(a: Int, x: Int, tt: Int, plus: Boolean): Raw =
        if (plus) { val b = x.coerceAtMost(tt - 1); val c = tt - b; if (c <= 0 || b == c) error("retry")
            Raw("dist", L(a, b, c, 1, tt), "Tính nhanh: $a × $b + $a × $c = ?", "${a * tt}", listOf("${a * b + c}", "${a * tt + a}", "${a * (tt - 1)}"))
        } else { val c = x; val b = c + tt
            Raw("dist", L(a, b, c, 0, tt), "Tính nhanh: $a × $b − $a × $c = ?", "${a * tt}", listOf("${a * b - c}".takeIf { a * b - c < 1000 } ?: "${a * tt + tt}", "${a * (tt - 1)}", "${a * 2 * tt}".takeIf { a * 2 * tt < 1000 } ?: "${a * tt - a}")) }

    private fun gauss(first: Int, step: Int, n: Int): Raw {
        val last = first + (n - 1) * step
        if ((first + last) * n % 2 != 0) error("retry")
        val sum = (first + last) * n / 2
        return Raw("gauss", L(first, step, last), "Tính nhanh tổng: $first + ${first + step} + ${first + 2 * step} + … + $last = ?", "$sum",
            listOf("${(first + last) * n}".takeIf { (first + last) * n < 1000 } ?: "${sum + step}", "${sum - last}", "${sum + first}"))
    }

    private fun arith(a: Int, d: Int, up: Boolean): Raw {
        val xs = (0 until 4).map { if (up) a + it * d else a + (3 - it) * d + 4 * d }.let { if (up) it else it.sortedDescending() }
        val next = if (up) xs.last() + d else xs.last() - d
        if (next <= 0) error("retry")
        return Raw("arith", L(xs[0], if (up) d else -d), "Tìm số tiếp theo của dãy: ${xs.joinToString(", ")}, …", "$next",
            listOf("${if (up) next + 1 else next - 1}", "${if (up) xs.last() + 2 * d else xs.last() - 2 * d}".takeIf { !it.startsWith("-") && it != "0" } ?: "${next + 2}", "${if (up) next - 1 else next + 1}"))
    }

    private fun grow(a: Int, k: Int): Raw {
        val xs = mutableListOf(a); repeat(4) { xs += xs.last() + k + it }
        val next = xs.last() + k + 4
        return Raw("grow", L(a, k), "Tìm số tiếp theo của dãy: ${xs.joinToString(", ")}, …", "$next",
            listOf("${xs.last() + k + 3}", "${next + 1}", "${xs.last() * 2}"))
    }

    private fun countSeq(a: Int, d: Int, last: Int): Raw {
        val n = (last - a) / d + 1
        return Raw("count", L(a, d, last), "Dãy số $a, ${a + d}, ${a + 2 * d}, …, $last có tất cả bao nhiêu số?", "$n",
            listOf("${n - 1}", "${n + 1}", "${last / d}"))
    }

    private fun nth(a: Int, d: Int, n: Int): Raw {
        val v = a + (n - 1) * d; if (v > 999) error("retry")
        return Raw("nth", L(a, d, n), "Cho dãy số: $a, ${a + d}, ${a + 2 * d}, ${a + 3 * d}, … Số thứ $n của dãy là số nào?", "$v",
            listOf("${a + n * d}", "${n * d}", "${v - d}"))
    }

    /** Ô trống thay cho x (lớp 1, 2): op 0 □ + b, 1 □ − b, 2 □ × b, 3 □ : b. Tham số thứ 4 = 1 (kiểu ô trống). */
    private fun box(op: Int, x: Int, b: Int, max: Int): Raw = when (op) {
        0 -> { if (x + b > max) error("retry"); Raw("x1", L(0, x, b, 1), "Số thích hợp điền vào ô trống: □ + $b = ${x + b}", "$x", listOf("${x + 2 * b}".takeIf { x + 2 * b <= max } ?: "${x + 2}", "${x + 1}", "${x + b}")) }
        1 -> { val bb = b.coerceAtMost(x - 1); if (bb < 1) error("retry"); Raw("x1", L(1, x, bb, 1), "Số thích hợp điền vào ô trống: □ − $bb = ${x - bb}", "$x", listOf("${x - 2 * bb}".takeIf { x - 2 * bb > 0 } ?: "${x + 2}", "${x - bb}", "${x + 1}")) }
        2 -> Raw("x1", L(2, x, b, 1), "Số thích hợp điền vào ô trống: □ × $b = ${x * b}", "$x", listOf("${x * b - b}", "${x + 1}", "${x * b + b}"))
        else -> { val q = x / b; Raw("x1", L(3, x, b, 1), "Số thích hợp điền vào ô trống: □ : $b = $q", "$x", listOf("${q + b}", "$q".takeIf { q != x } ?: "${x + 1}", "${x + b}")) }
    }

    /** Nghĩ một số: thêm a rồi bớt b (form 2). */
    private fun thinkAddSub(rng: Random, max: Int): Raw {
        val name = W.KIDS[rng.nextInt(W.KIDS.size)]
        val x = rng.nextInt(3, max / 2); val a = rng.nextInt(2, max / 3); val b = rng.nextInt(2, x + a)
        val r = x + a - b; if (r <= 0 || x + a > max || a == b) error("retry")
        return Raw("think", L(2, x, a, b), "$name nghĩ một số. Đem số đó cộng thêm $a rồi bớt đi $b thì được $r. Hỏi $name đã nghĩ số nào?", "$x",
            listOf("${r + a - b}".takeIf { r + a - b > 0 } ?: "${x + 2}", "${r + a + b}", "${r - a + b + 2}".takeIf { r - a + b + 2 > 0 } ?: "${x + 1}"))
    }

    private fun thinkMulAdd(rng: Random, k: Int, xMax: Int, aMax: Int): Raw {
        val name = W.KIDS[rng.nextInt(W.KIDS.size)]
        val x = rng.nextInt(2, xMax + 1); val a = rng.nextInt(3, aMax); val r = x * k + a
        return Raw("think", L(0, x, k, a), "$name nghĩ một số. Đem số đó nhân với $k rồi cộng thêm $a thì được $r. Hỏi $name đã nghĩ số nào?", "$x",
            listOf("${(r + a) / k}".takeIf { (r + a) % k == 0 && (r + a) / k != x } ?: "${x + 3}", "${r - a}", "${x + 1}"))
    }

    /** Hơn – kém. form 0: B nhiều hơn A; 1: B ít hơn A; 2: A nhiều hơn B (hỏi B); 3: A ít hơn B (hỏi B). */
    private fun cmp(rng: Random, form: Int, max: Int): Raw {
        val i = rng.nextInt(W.KIDS.size); var j = rng.nextInt(W.KIDS.size); if (j == i) j = (j + 1) % W.KIDS.size
        val th = rng.nextInt(W.THINGS.size); val (thing, unit) = W.THINGS[th]
        val A = W.KIDS[i]; val B = W.KIDS[j]
        val a = rng.nextInt(if (max <= 20) 5 else 12, max - 3); val b = rng.nextInt(2, if (max <= 20) 6 else 20)
        val ans = when (form) { 0, 3 -> a + b; else -> a - b }
        if (ans <= 0 || ans > max || a == b) error("retry")
        val rel = when (form) {
            0 -> "$B có nhiều hơn $A $b $unit."
            1 -> "$B có ít hơn $A $b $unit."
            2 -> "$A có nhiều hơn $B $b $unit."
            else -> "$A có ít hơn $B $b $unit."
        }
        val wrong = if (ans == a + b) a - b else a + b
        return Raw("cmp", L(form, a, b, th, i, j), "$A có $a $thing. $rel Hỏi $B có bao nhiêu $thing?", "$ans $unit",
            listOf("$wrong $unit", "$b $unit".takeIf { b != ans } ?: "${ans + 1} $unit", "${ans + 1} $unit".takeIf { b != ans + 1 } ?: "${ans + 2} $unit"))
    }

    private fun ageDiffWhen(rng: Random): Raw = if (rng.nextBoolean()) {
        val c = rng.nextInt(5, 10); val gap = rng.nextInt(22, 32); val m = c + gap; val n = rng.nextInt(2, 6); val later = rng.nextBoolean() || n >= c
        Raw("diff", L(m, c, n, if (later) 1 else 0),
            "Năm nay mẹ $m tuổi, con $c tuổi. Hỏi ${if (later) "$n năm nữa" else "$n năm trước"} mẹ hơn con bao nhiêu tuổi?",
            "$gap tuổi", listOf("${if (later) gap + n else gap - n} tuổi", "${if (later) gap + 2 * n else gap + n} tuổi", "${m + c} tuổi".takeIf { m + c <= 100 } ?: "${gap + 1} tuổi"))
    } else {
        val e = rng.nextInt(4, 9); val gap = rng.nextInt(2, 6); val a = e + gap; val target = e + rng.nextInt(2, 7)
        Raw("when", L(a, e, target), "Năm nay anh $a tuổi, em $e tuổi. Hỏi khi em $target tuổi thì anh bao nhiêu tuổi?",
            "${target + gap} tuổi", listOf("${a + target - gap} tuổi".takeIf { a + target - gap != target + gap } ?: "${target + gap + 2} tuổi", "$a tuổi".takeIf { a != target + gap } ?: "${a + 1} tuổi", "${target + gap + 1} tuổi"))
    }

    /* ============================== Lớp 4 ============================== */

    /** Tổng – hiệu. ctx 0 hai số · 1 hai lớp trồng cây · 2 tuổi mẹ con · 3 tổng ẩn (số lớn nhất có hai chữ số). */
    private fun sumDiff(rng: Random, ctx: Int): Raw {
        val askBig = rng.nextBoolean()
        val (big, small) = when (ctx) {
            0 -> { val s = rng.nextInt(10, 80); val h = rng.nextInt(4, 60); (s + h) to s }
            1 -> { val s = rng.nextInt(20, 120); val h = rng.nextInt(4, 30); (s + h) to s }
            2 -> { val c = rng.nextInt(5, 13); val h = rng.nextInt(22, 32); (c + h) to c }
            else -> { val h = rng.pick(11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33); ((99 + h) / 2) to ((99 - h) / 2) }
        }
        val T = big + small; val H = big - small
        val ans = if (askBig) big else small
        val (stm, unit) = when (ctx) {
            0 -> "Tổng của hai số là $T. Hiệu của hai số là $H. Tìm số ${if (askBig) "lớn" else "bé"}." to ""
            1 -> "Hai lớp 4A và 4B trồng được $T cây. Lớp 4A trồng nhiều hơn lớp 4B $H cây. Hỏi lớp ${if (askBig) "4A" else "4B"} trồng được bao nhiêu cây?" to " cây"
            2 -> "Tổng số tuổi của mẹ và con là $T tuổi. Mẹ hơn con $H tuổi. Hỏi ${if (askBig) "mẹ" else "con"} bao nhiêu tuổi?" to " tuổi"
            else -> "Tổng của hai số là số lớn nhất có hai chữ số. Hiệu của hai số là $H. Tìm số ${if (askBig) "lớn" else "bé"}." to ""
        }
        val other = if (askBig) small else big
        return Raw("sumdiff", L(ctx, big, small, if (askBig) 1 else 0), stm, "$ans$unit",
            listOf("${T + H}$unit".takeIf { T + H != ans } ?: "${ans + 2}$unit", "$other$unit", "${if (askBig) T - H else T + H - small}$unit".takeIf { (if (askBig) T - H else T + H - small) != ans } ?: "${ans + 1}$unit"))
    }

    /**
     * Tổng – tỉ / hiệu – tỉ. mode 0 tổng, 1 hiệu. frac=false: "gấp k lần" (p = 1, q = k); frac=true: "bé bằng p/q lớn".
     * ctx 0 hai số · 1 hai thùng dầu (lít).
     */
    private fun ratio(rng: Random, mode: Int, frac: Boolean, ctx: Int): Raw {
        val (p, q) = if (frac) listOf(1 to 2, 2 to 3, 3 to 4, 2 to 5, 3 to 5, 4 to 5, 3 to 7, 5 to 7, 3 to 8, 5 to 8)[rng.nextInt(10)] else 1 to (if (mode == 1) rng.nextInt(3, 7) else rng.nextInt(2, 7))
        val u = rng.nextInt(3, if (frac) 40 else 60)
        val small = p * u; val big = q * u
        if (big > 999) error("retry")
        val askBig = rng.nextBoolean()
        val total = if (mode == 0) small + big else big - small
        val rel = if (!frac) {
            if (ctx == 0) "Số lớn gấp $q lần số bé" else "Thùng thứ nhất có số dầu gấp $q lần thùng thứ hai"
        } else {
            if (ctx == 0) "Số bé bằng $p/$q số lớn" else "Thùng thứ hai có số dầu bằng $p/$q thùng thứ nhất"
        }
        val (stm, unit) = if (ctx == 0) {
            "${if (mode == 0) "Tổng" else "Hiệu"} của hai số là $total. $rel. Tìm số ${if (askBig) "lớn" else "bé"}." to ""
        } else {
            (if (mode == 0) "Hai thùng có tất cả $total lít dầu. $rel. Hỏi thùng thứ ${if (askBig) "nhất" else "hai"} có bao nhiêu lít dầu?"
             else "Thùng thứ nhất có nhiều hơn thùng thứ hai $total lít dầu. $rel. Hỏi thùng thứ ${if (askBig) "nhất" else "hai"} có bao nhiêu lít dầu?") to " lít"
        }
        val ans = if (askBig) big else small
        val parts = if (mode == 0) p + q else q - p
        val wrongParts = if (mode == 0) q else p + q
        val t1 = if (total % wrongParts == 0) total / wrongParts * (if (askBig) q else p) else ans + u
        return Raw("ratio", L(mode, p, q, u, if (askBig) 1 else 0, ctx, if (frac) 1 else 0), stm, "$ans$unit",
            listOf("${if (askBig) small else big}$unit", "$u$unit".takeIf { u != ans } ?: "${ans + 2}$unit", "$t1$unit".takeIf { t1 != ans && t1 > 0 } ?: "${ans + parts}$unit"))
    }

    /** TBC của n số (ctx 0) hoặc bán gạo n ngày (ctx 2). */
    private fun avgList(rng: Random, n: Int, ctx: Int): Raw {
        val avg = rng.nextInt(20, 90)
        val xs = MutableList(n - 1) { avg + rng.nextInt(-15, 16) }
        val lastV = avg * n - xs.sum(); if (lastV !in 5..150 || (xs + lastV).toSet().size < n) error("retry")
        xs += lastV
        val sum = avg * n
        val stm = if (ctx == 0) "Tìm trung bình cộng của các số: ${xs.joinToString(", ")}."
            else "Một cửa hàng ngày thứ nhất bán được ${xs[0]} kg gạo, ngày thứ hai bán được ${xs[1]} kg, ngày thứ ba bán được ${xs[2]} kg. Hỏi trung bình mỗi ngày cửa hàng bán được bao nhiêu ki-lô-gam gạo?"
        val unit = if (ctx == 0) "" else " kg"
        val t3 = if (sum % (n - 1) == 0) sum / (n - 1) else avg + 2
        return Raw("avg", listOf(0L, ctx.toLong(), n.toLong()) + xs.map { it.toLong() }, stm, "$avg$unit",
            listOf("$sum$unit".takeIf { sum < 1000 } ?: "${avg + 10}$unit", "$t3$unit".takeIf { t3 != avg } ?: "${avg + 3}$unit", "${avg + 1}$unit"))
    }

    /** TBC của 3 số là A, biết hai số → số thứ ba. */
    private fun avgMissing(rng: Random): Raw {
        val A = rng.nextInt(20, 90); val b = A + rng.nextInt(-15, 16); val c = A + rng.nextInt(-15, 16); val x = 3 * A - b - c
        if (x <= 0 || x == b || x == c || b == c) error("retry")
        return Raw("avg", L(1, 0, 3, A, b, c), "Trung bình cộng của ba số là $A. Số thứ nhất là $b, số thứ hai là $c. Tìm số thứ ba.", "$x",
            listOf("${A * 2 - b - c}".takeIf { A * 2 - b - c > 0 && A * 2 - b - c != x } ?: "${x + 3}", "${3 * A}", "${x + A}".takeIf { x + A < 1000 } ?: "${x + 1}"))
    }

    /** Đếm chữ số để viết các số từ 1 đến n. ctx 0 "viết các số", 1 "đánh số trang sách". */
    private fun digits(n: Int, ctx: Int): Raw {
        val d = when { n <= 9 -> n; n <= 99 -> 9 + (n - 9) * 2; else -> 9 + 180 + (n - 99) * 3 }
        val stm = if (ctx == 0) "Để viết các số tự nhiên liên tiếp từ 1 đến $n cần dùng tất cả bao nhiêu chữ số?"
            else "Một quyển sách dày $n trang. Để đánh số trang quyển sách đó (bắt đầu từ trang 1) cần dùng bao nhiêu chữ số?"
        val bad = if (n <= 99) n * 2 else n * 3
        return Raw("digits", L(n, ctx), stm, "$d", listOf("$n", "${d - 9}", "$bad".takeIf { bad < 1000 && bad != d } ?: "${d + 1}"))
    }

    /* ============================== Lớp 4 – Phân số ============================== */

    private fun distinct(rng: Random, from: Int, until: Int, n: Int): List<Int> =
        (from until until).shuffled(rng).take(n).also { if (it.size < n) error("retry") }

    /**
     * Chọn phân số lớn nhất / bé nhất. mode 0 cùng tử · 1 cùng mẫu · 2 so với 1 (hỏi phân số lớn hơn 1) · 3 phần bù (n−1)/n.
     * params: mode, askMax, rồi 4 cặp (tử, mẫu).
     */
    private fun fracCompare(rng: Random, mode: Int): Raw {
        val askMax = if (mode == 2) true else rng.nextBoolean()
        val fr: List<Pair<Int, Int>> = when (mode) {
            0 -> { val a = rng.nextInt(2, 10); distinct(rng, a + 1, a + 10, 4).map { a to it } }
            1 -> { val d = rng.nextInt(5, 13); distinct(rng, 1, d, 4).map { it to d } }
            2 -> {
                val big = rng.nextInt(3, 12).let { d -> (d + rng.nextInt(1, 5)) to d }
                val one = rng.nextInt(3, 10).let { it to it }
                val lt = distinct(rng, 3, 12, 2).map { d -> rng.nextInt(1, d) to d }
                listOf(big, one) + lt
            }
            else -> distinct(rng, 3, 13, 4).map { (it - 1) to it }
        }
        if (fr.map { it.first * 1000 / it.second }.toSet().size < 4 && mode != 2) error("retry")
        if (fr.map { "${it.first}/${it.second}" }.toSet().size < 4) error("retry")
        val value = { f: Pair<Int, Int> -> f.first.toDouble() / f.second }
        val ans = if (mode == 2) fr[0] else if (askMax) fr.maxBy(value) else fr.minBy(value)
        val list = fr.shuffled(rng).joinToString(", ") { "${it.first}/${it.second}" }
        val stm = if (mode == 2) "Trong các phân số $list, phân số nào lớn hơn 1?"
            else "Trong các phân số $list, phân số nào ${if (askMax) "lớn nhất" else "bé nhất"}?"
        return Raw("fcmp", listOf(mode.toLong(), if (askMax) 1L else 0L) + fr.flatMap { listOf(it.first.toLong(), it.second.toLong()) }, stm,
            "${ans.first}/${ans.second}", fr.filter { it != ans }.map { "${it.first}/${it.second}" })
    }

    /** Tính nhanh: a/m + b/n + (m−a)/m + (n−b)/n = 2 (ghép cặp cùng mẫu thành 1). */
    private fun fracPairs(rng: Random): Raw {
        val (m, n) = distinct(rng, 3, 10, 2)
        val a = rng.nextInt(1, m); val b = rng.nextInt(1, n)
        val naive = "${a + b + (m - a) + (n - b)}/${2 * (m + n)}"
        return Raw("fpair", L(a, m, b, n), "Tính nhanh: $a/$m + $b/$n + ${m - a}/$m + ${n - b}/$n = ?", "2",
            listOf(naive, "1", "${m + n}/${m * n}".takeIf { (m + n) % (m * n) != 0 } ?: "3"))
    }

    /** 1/2 + 1/4 + … + 1/2^k: thiếu đúng phần cuối để bằng 1. */
    private fun fracHalves(rng: Random): Raw {
        val k = rng.nextInt(3, 6); val den = 1 shl k
        val terms = (1..k).map { 1 shl it }
        val naive = "$k/${terms.sum()}"
        return Raw("fgeo", L(k), "Tính: ${terms.joinToString(" + ") { "1/$it" }} = ?", "${den - 1}/$den",
            listOf(naive, "1", "${den - 1}/${den * 2}"))
    }

    /** Tìm a/b của N (đề trần hoặc có lời). params: a, b, N, ctx. */
    private fun fracOf(rng: Random): Raw {
        val b = rng.nextInt(2, 10); val a = rng.nextInt(1, b); if (gcd(a, b) != 1) error("retry")
        val N = b * rng.nextInt(2, 15); val ans = N / b * a; val ctx = rng.nextInt(3)
        val (stm, unit) = when (ctx) {
            0 -> "Tìm $a/$b của $N." to ""
            1 -> "Lớp 4A có $N học sinh, trong đó $a/$b số học sinh là học sinh nữ. Hỏi lớp 4A có bao nhiêu học sinh nữ?" to " học sinh"
            else -> "Một cửa hàng có $N kg gạo, đã bán $a/$b số gạo đó. Hỏi cửa hàng đã bán bao nhiêu ki-lô-gam gạo?" to " kg"
        }
        return Raw("fof", L(a, b, N, ctx), stm, "$ans$unit",
            listOf("${N / b}$unit".takeIf { a != 1 } ?: "${N - ans}$unit", "${N * a}$unit", "${if (N % a == 0) N / a * b else N - ans}$unit".takeIf { (if (N % a == 0) N / a * b else N - ans) != ans } ?: "${ans + N / b}$unit"))
    }

    /** "Cho đi a/b, còn lại bao nhiêu?" – bẫy: trả lời phần đã cho. */
    private fun fracRemain(rng: Random): Raw {
        val b = rng.nextInt(3, 9); val a = rng.nextInt(1, b); if (gcd(a, b) != 1) error("retry")
        val N = b * rng.nextInt(3, 13); val give = N / b * a; val left = N - give
        val name = W.KIDS[rng.nextInt(W.KIDS.size)]; val th = rng.nextInt(W.THINGS.size); val (thing, unit) = W.THINGS[th]
        return Raw("frem", L(a, b, N, th, W.KIDS.indexOf(name)), "$name có $N $thing. $name cho bạn $a/$b số $thing đó. Hỏi $name còn lại bao nhiêu $thing?", "$left $unit",
            listOf("$give $unit", "${N - a} $unit".takeIf { N - a != left } ?: "${left + 1} $unit", "${N / b} $unit".takeIf { N / b != left && N / b != give } ?: "${left + 2} $unit"))
    }

    /** Biết a/b của một số là A → tìm số đó (nâng cao). */
    private fun fracWhole(rng: Random): Raw {
        val b = rng.nextInt(3, 10); val a = rng.nextInt(2, b); if (gcd(a, b) != 1) error("retry")
        val part = rng.nextInt(3, 15); val A = part * a; val X = part * b
        val name = W.KIDS[rng.nextInt(W.KIDS.size)]
        return Raw("fwhole", L(a, b, A, W.KIDS.indexOf(name)), "$a/$b số bi của $name là $A viên. Hỏi $name có tất cả bao nhiêu viên bi?", "$X viên",
            listOf("$part viên", "${A * a} viên".takeIf { A * a != X } ?: "${X + part} viên", "${X - A} viên".takeIf { X - A != part } ?: "${X - part} viên"))
    }

    /* ============================== Lớp 5 ============================== */

    private fun decQuarter(rng: Random): Raw { val m = rng.nextInt(13, 99); if (m % 10 == 0) error("retry")
        return Raw("dec", L(0, m), "Tính nhanh: 0,25 × $m × 4 = ?", "$m", listOf("${m * 4}", W.dec(m * 25L), "${m * 10}")) }

    private fun decHalf(rng: Random): Raw { val a = rng.nextInt(11, 499); if (a % 10 == 0) error("retry")   // a phần mười
        val ans = W.dec(a * 20L)
        return Raw("dec", L(3, a), "Tính nhanh: ${W.dec(a * 10L)} : 0,5 = ?", ans, listOf(W.dec(a * 5L), W.dec(a * 50L), W.dec(a * 10L + 50))) }

    private fun decDist(rng: Random): Raw { val a = rng.nextInt(11, 99); if (a % 10 == 0) error("retry")   // a phần mười, b + c = 100
        val b = rng.nextInt(11, 90); val c = 100 - b; if (b == c || b % 10 == 0) error("retry")
        return Raw("dec", L(1, a, b, c), "Tính nhanh: ${W.dec(a * 10L)} × $b + ${W.dec(a * 10L)} × $c = ?", W.dec(a * 1000L),
            listOf(W.dec(a * 100L), W.dec(a * 10000L).takeIf { a * 100 < 1000 } ?: W.dec(a * 990L), W.dec(a * 990L + 100))) }

    /** a tính theo phần trăm (hundredths): 3,25 → 325. */
    private fun decNine(rng: Random): Raw { val a = rng.nextInt(101, 999); if (a % 10 == 0) error("retry")
        return Raw("dec", L(2, a), "Tính nhanh: ${W.dec(a.toLong())} × 99 + ${W.dec(a.toLong())} = ?", W.dec(a * 100L),
            listOf(W.dec(a * 99L), W.dec(a * 10L), W.dec(a * 100L + 100))) }

    private fun pctOf(rng: Random): Raw { val p = rng.pick(10, 20, 25, 40, 50, 75, 5, 15); val base = 100 / gcd(p, 100); val N = base * rng.nextInt(1, 999 / base + 1)
        if (N < 20 || N > 900) error("retry"); val ans = N * p / 100
        return Raw("pct", L(0, p, N), "Tìm $p% của $N.", "$ans", listOf("${N - ans}".takeIf { N - ans != ans } ?: "${ans + 5}", "${N * p / 10}".takeIf { N * p / 10 < 10000 && N * p % 10 == 0 && N * p / 10 != ans } ?: "${ans * 2}", "${ans + p}")) }

    private fun pctRatio(rng: Random): Raw { val N = rng.pick(20, 25, 40, 50); val unitM = N / gcd(N, 100); val M = unitM * rng.nextInt(1, N / unitM)
        val P = M * 100 / N; if (P == 50 || M <= 0) error("retry")
        return Raw("pct", L(1, N, M), "Một lớp học có $N học sinh, trong đó có $M học sinh nữ. Hỏi số học sinh nữ chiếm bao nhiêu phần trăm số học sinh cả lớp?", "$P%",
            listOf("$M%".takeIf { M != P } ?: "${P + 10}%", "${100 - P}%", "${P + 5}%".takeIf { P + 5 != 100 - P } ?: "${P + 15}%")) }

    private fun pctWhole(rng: Random): Raw { val p = rng.pick(10, 20, 25, 40, 50, 5); val X = (100 / gcd(p, 100)) * rng.nextInt(2, 40); if (X > 900) error("retry"); val A = X * p / 100
        return Raw("pct", L(2, p, A), "$p% của một số là $A. Tìm số đó.", "$X", listOf("${A * p / 100}".takeIf { A * p % 100 == 0 && A * p / 100 > 0 } ?: "${X + p}", "${A * p}".takeIf { A * p < 10000 } ?: "${X * 2}", "${X - A}")) }

    private fun pctSale(rng: Random): Raw { val p = rng.pick(10, 20, 25, 30, 40, 15); val base = 100 / gcd(p, 100); val N = base * rng.nextInt(1, 600 / base)
        if (N < 60) error("retry"); val off = N * p / 100; val ans = N - off
        return Raw("pct", L(3, p, N), "Một chiếc áo giá $N nghìn đồng. Cửa hàng giảm giá $p%. Hỏi giá chiếc áo sau khi giảm là bao nhiêu nghìn đồng?", "$ans nghìn đồng",
            listOf("$off nghìn đồng", "${N - p} nghìn đồng".takeIf { N - p != ans } ?: "${ans + 5} nghìn đồng", "${N + off} nghìn đồng")) }

    private fun motionDist(rng: Random): Raw { val v = if (rng.nextInt(4) == 0) rng.nextInt(10, 19) else rng.nextInt(30, 66); val t = rng.nextInt(2, 7); val s = v * t
        return Raw("motion", L(0, v, t), "Một ${if (v < 20) "xe đạp" else "ô tô"} đi với vận tốc $v km/giờ. Hỏi trong $t giờ ${if (v < 20) "xe đạp" else "ô tô"} đi được bao nhiêu ki-lô-mét?", "$s km",
            listOf("${v + t} km", "${v * (t + 1)} km", "${s - v} km")) }

    /** Thời gian = quãng đường : vận tốc; có thể ra nửa giờ (2,5 giờ). Thời gian lưu theo phần mười giờ. */
    private fun motionTime(rng: Random): Raw { val v = rng.pick(30, 36, 40, 42, 44, 48, 50, 60); val t10 = rng.pick(15, 20, 25, 30, 35, 40)
        if (v * t10 % 10 != 0) error("retry"); val s = v * t10 / 10
        val ans = "${W.dec(t10 * 10L)} giờ"
        return Raw("motion", L(1, v, t10, s), "Quãng đường AB dài $s km. Một xe máy đi từ A đến B với vận tốc $v km/giờ. Hỏi xe máy đi hết quãng đường AB trong bao nhiêu giờ?", ans,
            listOf("${W.dec((t10 + 5) * 10L)} giờ", "${W.dec((t10 - 5) * 10L)} giờ", "${W.dec(t10 * 10L + 30)} giờ")) }

    private fun motionMeet(rng: Random): Raw { val v1 = rng.nextInt(40, 66); val v2 = rng.nextInt(30, 51); val t = rng.nextInt(2, 6); val s = (v1 + v2) * t
        if (s > 999 || v1 == v2) error("retry")
        return Raw("motion", L(2, v1, v2, t), "Quãng đường AB dài $s km. Cùng một lúc, một ô tô đi từ A đến B với vận tốc $v1 km/giờ và một xe máy đi từ B đến A với vận tốc $v2 km/giờ. Hỏi sau mấy giờ hai xe gặp nhau?", "$t giờ",
            listOf("${t + 1} giờ", "${t * 2} giờ".takeIf { t * 2 != t + 1 } ?: "${t + 2} giờ", "${if (s % v1 == 0) s / v1 else t + 3} giờ".takeIf { s % v1 != 0 || s / v1 !in setOf(t, t + 1, t * 2) } ?: "${t + 3} giờ")) }

    private fun motionChase(rng: Random): Raw { val v1 = rng.nextInt(42, 66); val v2 = rng.nextInt(25, 41); val t = rng.nextInt(2, 6); val d = (v1 - v2) * t
        if (v1 <= v2 || d < 20) error("retry")
        return Raw("motion", L(3, v1, v2, t), "Một xe máy đi trước ô tô $d km, hai xe cùng đi theo một chiều. Vận tốc của ô tô là $v1 km/giờ, vận tốc của xe máy là $v2 km/giờ. Hỏi sau mấy giờ ô tô đuổi kịp xe máy?", "$t giờ",
            listOf("${t + 1} giờ", "${if (d % (v1 + v2) == 0) d / (v1 + v2) else t + 2} giờ".takeIf { d % (v1 + v2) != 0 || d / (v1 + v2) !in setOf(t, t + 1) } ?: "${t + 2} giờ", "${t + 3} giờ")) }

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
}
