package com.example.toanhoc.domain.theory

import com.example.toanhoc.domain.model.AcademicProblem
import com.example.toanhoc.domain.theory.Hue.BLUE
import com.example.toanhoc.domain.theory.Hue.GREEN
import com.example.toanhoc.domain.theory.Hue.INK
import com.example.toanhoc.domain.theory.Hue.ORANGE
import com.example.toanhoc.domain.theory.Hue.PURPLE

/**
 * Lời giảng cho các dạng Toán tư duy (problemId "T3_xx:kind:p1:p2:…:rand" do ThinkingEngine sinh).
 * Mỗi bài: CÁCH Ở TRƯỜNG (steps – trình bày chuẩn sư phạm) + CÁCH NHANH (quick – chỉ khi có mẹo thật)
 * + giải thích riêng cho TỪNG đáp án sai (errorNotes). Mọi kết quả được tính lại và đối chiếu đáp án.
 */
/** Lời giảng + hàm giải thích cho một đáp án sai. */
internal typealias Built = Pair<KidTheory, (String) -> String>

internal object ThinkingTheories {

    private fun T(s: String, h: Hue = INK) = Tok.T(s, h)
    private fun P(s: Any, h: Hue) = Tok.P(s.toString(), h)
    private fun step(title: String, vararg line: Tok, note: String? = null, visual: Visual? = null) = TheoryStep(title, line.toList(), note, visual)

    /** Lớp của câu đang giảng (để chọn từ ngữ con đã học). */
    private var grade = 3

    fun build(p: AcademicProblem): KidTheory? {
        grade = p.nodeId.getOrNull(1)?.digitToIntOrNull() ?: 3
        ThinkingTheoriesK.grade = grade
        val parts = p.problemId.split(":")
        if (parts.size < 3) return null
        val kind = parts[1]
        val v = parts.drop(2).dropLast(1).map { it.toLong() }
        val ans = p.steps.firstOrNull()?.expectedAnswer ?: return null
        val opts = p.steps.first().options
        val t = when (kind) {
            "pair" -> pair(p.statementVi, v)
            "mul" -> mul(p.statementVi, v)
            "dist" -> dist(p.statementVi, v)
            "gauss" -> gauss(p.statementVi, v)
            "arith" -> arith(p.statementVi, v)
            "grow" -> grow(p.statementVi, v)
            "geo" -> geo(p.statementVi, v)
            "fib" -> fib(p.statementVi, v)
            "nth" -> nth(p.statementVi, v)
            "count" -> count(p.statementVi, v)
            "diff" -> diff(p.statementVi, v)
            "sum" -> sumAge(p.statementVi, v)
            "when" -> whenAge(p.statementVi, v)
            "times" -> timesAge(p.statementVi, v)
            "x1" -> x1(p.statementVi, v)
            "x2" -> x2(p.statementVi, v)
            "think" -> think(p.statementVi, v)
            else -> ThinkingTheoriesK.build(kind, p.statementVi, v)
        } ?: return null
        // Đối chiếu: kết luận phải chứa đúng đáp án
        if (!t.first.conclusion.contains(ans)) return null
        val raw = opts.filter { it != ans }.associateWith { o -> t.second(o) }
        // Hai đáp án sai dùng chung một lời giải thích → thêm một câu riêng cho từng đáp án (lệch bao nhiêu)
        val dup = raw.values.groupingBy { it }.eachCount()
        val notes = raw.mapValues { (o, n) -> if ((dup[n] ?: 0) > 1) offsetNote(o, ans)?.let { "$it $n" } ?: n else n }
        return t.first.copy(errorNotes = notes)
    }

    private fun num(o: String): Double? = o.substringBefore(' ').removeSuffix("%").replace(',', '.').toDoubleOrNull()

    private fun offsetNote(o: String, ans: String): String? {
        val a = num(ans) ?: return null; val x = num(o) ?: return null
        val d = kotlin.math.abs(x - a); if (d == 0.0) return null
        val ds = if (d % 1.0 == 0.0) d.toLong().toString() else com.example.toanhoc.domain.thinking.ThinkingWords.dec(kotlin.math.round(d * 100).toLong())
        return "Con ra $o, ${if (x > a) "thừa" else "thiếu"} $ds so với kết quả đúng."
    }

    /* ------------------------------- Tính nhanh ------------------------------- */

    private fun pair(s: String, v: List<Long>): Built {
        val (a, b, a2, b2) = v
        val ta = a + a2; val tb = b + b2; val r = ta + tb
        val hundred = ta % 100 == 0L
        val round = if (hundred) "tròn trăm" else "tròn chục"
        val unit = if (hundred) 100L else 10L
        val rule = if (grade <= 2) listOf(T("Ghép hai số cộng lại "), P(round, BLUE), T(", rồi cộng."))
            else listOf(T("Ghép các số có tổng "), P(round, BLUE), T(", rồi cộng."))
        val keep = when {
            grade <= 1 -> listOf(T("Đổi chỗ các số, "), P("kết quả không đổi", GREEN), T("."))
            else -> listOf(T("Đổi chỗ các số hạng, "), P("tổng không đổi", GREEN), T("."))
        }
        val th = KidTheory("Tính nhanh: ghép cặp $round",
            listOf(rule, keep),
            if (grade >= 4) listOf(Tile("a + b = b + a", "tính chất giao hoán", BLUE), Tile("(a + b) + c = a + (b + c)", "tính chất kết hợp", GREEN))
            else listOf(Tile("6 + 4 = 4 + 6", "đổi chỗ, kết quả không đổi", BLUE), Tile("7 + 3 = 10", "ghép thành 10", GREEN)), s,
            listOf(
                step("Đổi chỗ, nhóm các số", T("$a + $b + $a2 + $b2 = "), P("($a + $a2) + ($b + $b2)", BLUE),
                    note = if (grade >= 4) "Dùng tính chất giao hoán và kết hợp." else "Đổi chỗ, cộng từng cặp trước: kết quả không đổi."),
                step("Tính trong ngoặc", T("= "), P("$ta + $tb", BLUE)),
                step("Cộng hai số $round", T("= "), P(r, GREEN))
            ),
            "Vậy kết quả là $r",
            quick = listOf(
                step("Tìm các cặp $round", P("$a + $a2 = $ta", BLUE), P("$b + $b2 = $tb", BLUE),
                    note = if (hundred) "Hàng đơn vị cộng được 10, hàng chục cộng được 9." else "Nhìn hàng đơn vị: cộng được 10 là ghép được."),
                step("Cộng các số $round", T("$ta + $tb = "), P(r, GREEN))
            ), quickRule = if (grade <= 1) "Tìm hai số cộng lại tròn chục." else "Tìm cặp số có tổng $round.")
        return th to { o -> val x = o.toLongOrNull() ?: 0
            when (x) {
                r + unit -> "Con cộng thừa $unit ở một cặp. Kiểm tra: $a + $a2 = $ta."
                r - unit -> "Con cộng thiếu $unit ở một cặp. Kiểm tra: $b + $b2 = $tb."
                else -> "Ghép $a với $a2, ghép $b với $b2, rồi cộng: $ta + $tb = $r."
            } }
    }

    private fun mul(s: String, v: List<Long>): Built {
        val (p, m, q) = v; val pq = p * q; val r = pq * m
        val th = KidTheory("Tính nhanh: nhân để được số tròn",
            listOf(listOf(T("Nhóm hai số nhân được "), P("10, 100", BLUE), T(" để nhân trước.")), listOf(T("Đổi chỗ các thừa số, "), P("tích không đổi", GREEN), T("."))),
            listOf(Tile("5 × 2", "= 10", BLUE), Tile("25 × 4", "= 100", BLUE)), s,
            listOf(
                step("Đổi chỗ các thừa số", T("$p × $m × $q = "), P("($p × $q) × $m", BLUE),
                    note = if (grade >= 4) "Tính chất giao hoán và kết hợp của phép nhân." else "Đổi chỗ các thừa số, tích không đổi."),
                step("Nhân trong ngoặc", T("= "), P("$pq × $m", BLUE)),
                step("Nhân với $pq", T("= "), P(r, GREEN))
            ), "Vậy kết quả là $r",
            quick = listOf(
                step("Thấy $p × $q = $pq", P("$p × $q = $pq", BLUE)),
                step("Nhân với $pq: viết thêm ${if (pq == 10L) "1 chữ số 0" else "2 chữ số 0"}", T("$m × $pq = "), P(r, GREEN))
            ), quickRule = "Thấy 5 và 2, 25 và 4 thì nhân với nhau trước.")
        return th to { o -> val x = o.toLongOrNull() ?: 0
            when (x) {
                p * m -> "Con mới tính $p × $m, còn quên nhân thêm $q."
                p * (m + q) -> "Con đã cộng $m với $q, nhưng giữa các số là dấu nhân."
                r + p * q -> "Con nhân thừa: chỉ cần $m × $pq = $r."
                else -> "Nhân $p × $q = $pq trước, rồi $m × $pq = $r."
            } }
    }

    private fun dist(s: String, v: List<Long>): Built {
        val (a, b, c, plus) = v; val tt = v.getOrElse(4) { 10L }; val r = a * tt; val add = plus == 1L
        val sign = if (add) "+" else "−"; val verb = if (add) "cộng" else "bớt"
        val quick = listOf(
            step("Gộp số lần $a", T("$b lần $a $verb $c lần $a = "), P("$tt lần $a", BLUE), note = if (add) "$b + $c = $tt." else "$b − $c = $tt."),
            step("$tt lần $a", T("$a × $tt = "), P(r, GREEN), note = if (tt == 20L) "$a × 2 = ${a * 2}, viết thêm 1 chữ số 0." else null))
        val th = if (grade <= 3) KidTheory("Tính nhanh: gộp số lần",
            listOf(listOf(T("Cùng nhân với một số thì "), P("gộp số lần", GREEN), T(" lại."))),
            listOf(Tile("3 × 4 + 3 × 6", "10 lần 3 = 30", BLUE)), s,
            listOf(
                step("Tính từng phép nhân", P("$a × $b = ${a * b}", BLUE), P("$a × $c = ${a * c}", BLUE)),
                step("Tính kết quả", T("${a * b} $sign ${a * c} = "), P(r, GREEN))
            ), "Vậy kết quả là $r", quick = quick, quickRule = "Cùng nhân với một số: gộp số lần lại.")
        else KidTheory("Tính nhanh: thừa số chung",
            listOf(listOf(P("a × b $sign a × c", BLUE), T(" = "), P("a × (b $sign c)", GREEN), T("."))),
            listOf(Tile("3 × 4 + 3 × 6", "= 3 × 10 = 30", BLUE)), s,
            listOf(
                step("Đưa thừa số chung $a ra ngoài", T("$a × $b $sign $a × $c = "), P("$a × ($b $sign $c)", BLUE), note = "Tính chất phân phối của phép nhân."),
                step("Tính trong ngoặc", T("= "), P("$a × $tt", BLUE)),
                step("Nhân với $tt", T("= "), P(r, GREEN))
            ), "Vậy kết quả là $r", quick = quick, quickRule = "Cùng nhân với một số: gộp số lần lại.")
        return th to { o -> val x = o.toLongOrNull() ?: 0
            when {
                x == (if (add) a * b + c else a * b - c) -> "Con $verb thẳng $c. Nhưng $c cũng phải nhân với $a."
                x == a * b * c -> "Con nhân cả ba số. Giữa hai phép nhân là dấu $sign."
                x == a * tt + a -> "Con gộp thừa một lần $a. Chỉ có $tt lần $a."
                x == a * (tt - 1) -> "Con gộp thiếu một lần $a. Phải là $tt lần $a."
                x == a * 2 * tt -> "Con gộp thành ${2 * tt} lần. Đúng là $b − $c = $tt lần."
                else -> "${if (add) "$b + $c" else "$b − $c"} = $tt, nên kết quả là $a × $tt = $r."
            } }
    }

    private fun gauss(s: String, v: List<Long>): Built {
        val (first, stepV, last) = v
        val n = (last - first) / stepV + 1; val pairs = n / 2; val sPair = first + last
        val mid = if (n % 2 == 1L) first + pairs * stepV else null
        val sum = sPair * pairs + (mid ?: 0)
        val th = KidTheory("Tính nhanh: ghép đầu – cuối",
            listOf(listOf(T("Ghép "), P("số đầu với số cuối", BLUE), T(". Các cặp có "), P("tổng bằng nhau", GREEN), T("."))),
            listOf(Tile("1 + 2 + … + 9", "(1 + 9) + (2 + 8) + … + 5", BLUE)), s,
            listOf(
                step("Đếm số các số", T("($last − $first) : $stepV + 1 = "), P(n, BLUE)),
                step("Ghép thành các cặp", T("($first + $last) + (${first + stepV} + ${last - stepV}) + …"),
                    note = "Mỗi cặp bằng $sPair. Có $pairs cặp${if (mid != null) ", lẻ một số ở giữa là $mid" else ""}."),
                step("Tính tổng", T("$sPair × $pairs${if (mid != null) " + $mid" else ""} = "), P(sum, GREEN))
            ), "Vậy tổng là $sum",
            quick = listOf(
                step("Mỗi cặp đầu – cuối", T("$first + $last = "), P(sPair, BLUE)),
                step("Số cặp × tổng một cặp", T("$sPair × $pairs${if (mid != null) " + $mid" else ""} = "), P(sum, GREEN), note = "$n số ghép được $pairs cặp${if (mid != null) ", lẻ 1 số" else ""}.")
            ), quickRule = "Tổng = tổng một cặp × số cặp.")
        return th to { o -> val x = o.toLongOrNull() ?: 0
            when (x) {
                sPair * n -> "Con nhân $sPair với $n. Mỗi cặp có 2 số, nên chỉ có $pairs cặp."
                sum - last -> "Con bỏ sót số cuối cùng ($last)."
                sum + first -> "Con cộng thừa số đầu ($first) một lần."
                else -> "Mỗi cặp bằng $sPair: $sPair × $pairs${if (mid != null) " + $mid" else ""} = $sum."
            } }
    }

    /* ------------------------------- Dãy số ------------------------------- */

    private fun seqLine(s: String) = s.substringAfter(":").substringBefore("…").split(",").map { it.trim() }.filter { it.isNotEmpty() }

    private fun arith(s: String, v: List<Long>): Built {
        val xs = seqLine(s).map { it.toLong() }; val d = xs[1] - xs[0]; val next = xs.last() + d
        val up = d > 0; val ad = kotlin.math.abs(d); val verb = if (up) "cộng" else "bớt"
        val th = KidTheory("Dãy số cách đều",
            listOf(listOf(T("Tìm "), P("khoảng cách", ORANGE), T(" giữa hai số liền nhau.")), listOf(T("Rồi "), P(if (up) "cộng thêm" else "bớt đi", GREEN), T(" đúng khoảng cách đó."))),
            emptyList(), s,
            listOf(
                step("Tìm khoảng cách", T(if (up) "${xs[1]} − ${xs[0]} = " else "${xs[0]} − ${xs[1]} = "), P(ad, ORANGE), note = "Thử với các số khác: lần nào cũng ${if (up) "thêm" else "bớt"} $ad."),
                step("Số tiếp theo", T("${xs.last()} ${if (up) "+" else "−"} $ad = "), P(next, GREEN))
            ), "Vậy số tiếp theo là $next")
        return th to { o -> val x = o.toLongOrNull() ?: 0
            val used = if (up) x - xs.last() else xs.last() - x
            when {
                used == 2 * ad -> "Con $verb khoảng cách hai lần. Chỉ cần một lần: ${xs.last()} ${if (up) "+" else "−"} $ad."
                used > 0 -> "Con $verb $used. Khoảng cách của dãy là $ad."
                else -> "Khoảng cách là $ad. Số tiếp theo là ${xs.last()} ${if (up) "+" else "−"} $ad = $next."
            } }
    }

    private fun grow(s: String, v: List<Long>): Built {
        val xs = seqLine(s).map { it.toLong() }; val gaps = xs.zipWithNext { a, b -> b - a }; val nextGap = gaps.last() + 1; val next = xs.last() + nextGap
        val th = KidTheory("Dãy số có khoảng cách tăng dần",
            listOf(listOf(T("Viết "), P("các khoảng cách", ORANGE), T(" ra.")), listOf(T("Khoảng cách không đều thì tìm "), P("quy luật của khoảng cách", BLUE), T("."))),
            emptyList(), s,
            listOf(
                step("Viết các khoảng cách", *gaps.map { P("+$it", ORANGE) }.toTypedArray(), note = "Mỗi lần khoảng cách tăng thêm 1."),
                step("Khoảng cách tiếp theo", P("+$nextGap", ORANGE)),
                step("Số tiếp theo", T("${xs.last()} + $nextGap = "), P(next, GREEN))
            ), "Vậy số tiếp theo là $next")
        return th to { o -> val x = o.toLongOrNull() ?: 0
            when (x) {
                xs.last() + gaps.last() -> "Con dùng lại +${gaps.last()}. Khoảng cách phải tăng thêm 1."
                next + 1 -> "Khoảng cách tiếp theo là +$nextGap, không phải +${nextGap + 1}."
                else -> "Hãy viết khoảng cách ra: ${gaps.joinToString(", ") { "+$it" }}. Lần sau là +$nextGap."
            } }
    }

    private fun fib(s: String, v: List<Long>): Built {
        val xs = seqLine(s).map { it.toLong() }; val n = xs.size
        val next = xs[n - 1] + xs[n - 2]
        val th = KidTheory("Dãy số: cộng hai số đứng trước",
            listOf(listOf(T("Khoảng cách không đều?")), listOf(T("Thử xem mỗi số có bằng "), P("tổng hai số đứng trước", BLUE), T(" không."))),
            listOf(Tile("1, 1, 2, 3, 5, 8", "1 + 1 = 2, 1 + 2 = 3…", BLUE)), s,
            listOf(
                step("Thử với ba số đầu", T("${xs[0]} + ${xs[1]} = "), P(xs[2], BLUE), note = "Thử tiếp: ${xs[1]} + ${xs[2]} = ${xs[3]} ✓"),
                step("Cộng hai số cuối", T("${xs[n - 2]} + ${xs[n - 1]} = "), P(next, GREEN))
            ), "Vậy số tiếp theo là $next")
        return th to { o -> val x = o.toLongOrNull() ?: 0
            when (x) {
                xs[n - 1] + (xs[n - 1] - xs[n - 2]) -> "Dãy này không cách đều. Mỗi số bằng tổng hai số đứng trước."
                xs[n - 1] * 2 -> "Dãy này không gấp đôi. Cộng hai số cuối: ${xs[n - 2]} + ${xs[n - 1]} = $next."
                else -> "Con cộng nhầm hai số cuối: ${xs[n - 2]} + ${xs[n - 1]} = $next."
            } }
    }

    private fun geo(s: String, v: List<Long>): Built {
        val xs = seqLine(s).map { it.toLong() }; val m = xs[1] / xs[0]; val next = xs.last() * m
        val th = KidTheory("Dãy số gấp lên",
            listOf(listOf(T("Khoảng cách tăng nhanh thì thử xem số sau "), P("gấp mấy lần", BLUE), T(" số trước."))),
            emptyList(), s,
            listOf(
                step("Số sau gấp số trước mấy lần?", T("${xs[1]} : ${xs[0]} = "), P(m, BLUE), note = "Thử với số khác: ${xs[2]} : ${xs[1]} = $m."),
                step("Số tiếp theo", T("${xs.last()} × $m = "), P(next, GREEN))
            ), "Vậy số tiếp theo là $next")
        return th to { o -> val x = o.toLongOrNull() ?: 0
            when (x) {
                xs.last() + (xs.last() - xs[xs.size - 2]) -> "Con cộng thêm một số không đổi. Dãy này gấp $m lần mỗi bước."
                xs.last() + m -> "Dãy này gấp $m lần, không phải cộng thêm $m."
                next + m -> "Con nhân đúng rồi lại cộng thêm $m. Chỉ cần ${xs.last()} × $m."
                else -> "Mỗi số gấp $m lần số trước: ${xs.last()} × $m = $next."
            } }
    }

    private fun nth(s: String, v: List<Long>): Built {
        val (a, d, n) = v; val gaps = n - 1; val value = a + gaps * d
        val th = KidTheory("Tìm số thứ n của dãy cách đều",
            listOf(listOf(T("Từ số thứ 1 đến số thứ $n có "), P("$n − 1 = $gaps khoảng cách", ORANGE), T(".")),
                listOf(T("Giống trồng cây: số cây nhiều hơn số khoảng cách 1."))),
            emptyList(), s,
            listOf(
                step("Đếm số khoảng cách", T("$n − 1 = "), P(gaps, ORANGE)),
                step("Mỗi khoảng cách là $d", T("$gaps × $d = "), P(gaps * d, BLUE)),
                step("Cộng với số đầu tiên", T("$a + ${gaps * d} = "), P(value, GREEN))
            ), "Vậy số thứ $n là $value",
            quick = listOf(step("Số đầu + (n − 1) × khoảng cách", T("$a + ($n − 1) × $d = "), P(value, GREEN))),
            quickRule = "Số thứ n = số đầu + (n − 1) × khoảng cách.")
        return th to { o -> val x = o.toLongOrNull() ?: 0
            when (x) {
                a + n * d -> "Con tính $n khoảng cách. Từ số thứ 1 đến số thứ $n chỉ có $gaps khoảng cách."
                n * d -> "Con quên cộng số đầu tiên ($a)."
                value - d -> "Con tính thiếu một khoảng cách. Phải là $gaps × $d."
                else -> "Có $gaps khoảng cách, mỗi khoảng $d: $a + $gaps × $d = $value."
            } }
    }

    private fun count(s: String, v: List<Long>): Built {
        val (a, d, last) = v; val span = last - a; val gaps = span / d; val n = gaps + 1
        val th = KidTheory("Đếm số các số của dãy cách đều",
            listOf(listOf(P("Số các số", GREEN), T(" = "), P("số khoảng cách + 1", ORANGE), T("."))),
            listOf(Tile("2, 4, 6, 8", "3 khoảng cách → 4 số", BLUE)), s,
            listOf(
                step("Số cuối cách số đầu", T("$last − $a = "), P(span, BLUE)),
                step("Số khoảng cách", T("$span : $d = "), P(gaps, ORANGE)),
                step("Số các số", T("$gaps + 1 = "), P(n, GREEN))
            ), "Vậy dãy có $n số",
            quick = listOf(step("(cuối − đầu) : khoảng cách + 1", T("($last − $a) : $d + 1 = "), P(n, GREEN))),
            quickRule = "Số các số = (số cuối − số đầu) : khoảng cách + 1.")
        return th to { o -> val x = o.toLongOrNull() ?: 0
            when (x) {
                n - 1 -> "Con mới đếm số khoảng cách ($gaps). Số các số nhiều hơn 1."
                n + 1 -> "Con cộng thêm 1 hai lần. Có $gaps khoảng cách nên có $n số."
                last / d -> "Phải lấy số cuối trừ số đầu trước: ($last − $a) : $d."
                else -> "($last − $a) : $d = $gaps khoảng cách, nên có $n số."
            } }
    }

    /* ------------------------------- Tính tuổi ------------------------------- */

    private fun ageBars(big: String, small: String, k: Int, gapLabel: String) = Visual.Bars(
        listOf(BarRow(small, listOf(Seg(1f, BLUE, "1 phần"))), BarRow(big, listOf(Seg(1f, BLUE)) + List(k - 1) { Seg(1f, ORANGE, if (it == 0) gapLabel else null) })), null)

    private fun diff(s: String, v: List<Long>): Built {
        val (m, c, n, later) = v; val gap = m - c; val who = if (s.contains("bố")) "bố" else "mẹ"
        val m2 = if (later == 1L) m + n else m - n; val c2 = if (later == 1L) c + n else c - n
        val word = if (grade <= 2) "khoảng cách tuổi" else "hiệu số tuổi"
        val bars = Visual.Bars(listOf(
            BarRow(who, listOf(Seg(c2.toFloat(), BLUE), Seg(gap.toFloat(), ORANGE, "hơn $gap"))),
            BarRow("con", listOf(Seg(c2.toFloat(), BLUE)))))
        val th = KidTheory("Hơn kém bao nhiêu tuổi không đổi",
            listOf(listOf(T("Mỗi năm "), P("cả hai người", BLUE), T(" đều thêm 1 tuổi.")), listOf(T("Nên "), P("$word không đổi", GREEN), T("."))),
            listOf(Tile("Năm nay 30 và 6", "hơn 24 tuổi", BLUE), Tile("5 năm nữa 35 và 11", "vẫn hơn 24 tuổi", BLUE)), s,
            listOf(
                step("Tuổi hai người ${if (later == 1L) "$n năm nữa" else "$n năm trước"}", P("$who: $m2 tuổi", BLUE), P("con: $c2 tuổi", BLUE), visual = bars),
                step("Lấy tuổi $who trừ tuổi con", T("$m2 − $c2 = "), P("$gap tuổi", GREEN))
            ), "Vậy $who hơn con $gap tuổi",
            quick = listOf(step("Tính ngay năm nay", T("$m − $c = "), P("$gap tuổi", GREEN), note = "Không cần tính tuổi ${if (later == 1L) "$n năm nữa" else "$n năm trước"}.")),
            quickRule = "${word.replaceFirstChar { it.uppercase() }} không đổi theo thời gian.")
        return th to { o -> val x = o.substringBefore(' ').toLongOrNull() ?: 0
            when {
                x == m + c -> "Con đã cộng hai số tuổi. Hỏi \"hơn bao nhiêu\" thì làm phép trừ."
                x > gap -> "Con cộng thêm ${x - gap} tuổi. Hai người cùng ${if (later == 1L) "thêm" else "bớt"} $n tuổi, nên vẫn hơn $gap."
                else -> "Con bớt đi ${gap - x} tuổi. Hai người cùng ${if (later == 1L) "thêm" else "bớt"} $n tuổi, nên vẫn hơn $gap."
            } }
    }

    private fun sumAge(s: String, v: List<Long>): Built {
        val (sum, n) = v; val r = sum + 2 * n
        val bars = Visual.Bars(listOf(
            BarRow("bố", listOf(Seg(3f, BLUE), Seg(1f, ORANGE, "+$n"))),
            BarRow("con", listOf(Seg(1.2f, BLUE), Seg(1f, ORANGE, "+$n")))), "tăng ${2 * n}")
        val th = KidTheory("Tổng số tuổi thay đổi thế nào?",
            listOf(listOf(T("Sau mỗi năm, "), P("mỗi người", BLUE), T(" thêm 1 tuổi.")), listOf(T("Có 2 người nên tổng tăng "), P("2 tuổi mỗi năm", GREEN), T("."))),
            emptyList(), s,
            listOf(
                step("Sau $n năm, mỗi người thêm $n tuổi", T("bố thêm "), P("$n", BLUE), T(", con thêm "), P("$n", BLUE), visual = bars),
                step("Tổng tăng thêm", T("$n + $n = "), P(2 * n, ORANGE)),
                step("Tổng số tuổi khi đó", T("$sum + ${2 * n} = "), P("$r tuổi", GREEN))
            ), "Vậy tổng số tuổi là $r tuổi",
            quick = listOf(step("Tổng tăng 2 × số năm", T("$sum + 2 × $n = "), P("$r tuổi", GREEN))),
            quickRule = "Hai người thì tổng tuổi tăng 2 mỗi năm.")
        return th to { o -> val x = o.substringBefore(' ').toLongOrNull() ?: 0
            when (x) {
                sum + n -> "Con chỉ cộng $n một lần. Có HAI người cùng thêm $n tuổi."
                sum - 2 * n -> "Đề hỏi $n năm NỮA nên phải cộng thêm, không trừ."
                sum + 4 * n -> "Con cộng thừa. Mỗi người chỉ thêm $n tuổi."
                else -> "Hai người thêm ${2 * n} tuổi: $sum + ${2 * n} = $r."
            } }
    }

    private fun whenAge(s: String, v: List<Long>): Built {
        val (a, e, target) = v; val gap = a - e; val r = target + gap
        val bars = Visual.Bars(listOf(
            BarRow("anh", listOf(Seg(e.toFloat(), BLUE), Seg(gap.toFloat(), ORANGE, "hơn $gap"))),
            BarRow("em", listOf(Seg(e.toFloat(), BLUE)))))
        val th = KidTheory("Anh luôn hơn em bấy nhiêu tuổi",
            listOf(listOf(T("Anh luôn hơn em "), P("đúng một số tuổi", GREEN), T(".")), listOf(T("Năm nào cũng vậy."))),
            emptyList(), s,
            listOf(
                step("Anh hơn em", T("$a − $e = "), P("$gap tuổi", ORANGE), visual = bars),
                step("Khi em $target tuổi, anh vẫn hơn $gap tuổi", T("$target + $gap = "), P("$r tuổi", GREEN))
            ), "Vậy khi đó anh $r tuổi",
            quick = listOf(step("Em thêm mấy tuổi, anh thêm mấy tuổi", T("$a + ($target − $e) = "), P("$r tuổi", GREEN))),
            quickRule = "Em thêm mấy tuổi, anh cũng thêm mấy tuổi.")
        return th to { o -> val x = o.substringBefore(' ').toLongOrNull() ?: 0
            when (x) {
                a -> "Con giữ nguyên tuổi anh. Em lớn thêm thì anh cũng lớn thêm."
                e + target -> "Con cộng tuổi em hai lần. Anh hơn em $gap tuổi: $target + $gap."
                else -> "Anh hơn em đúng $gap tuổi, không phải ${x - target} tuổi."
            } }
    }

    private fun timesAge(s: String, v: List<Long>): Built {
        val (m, c, k) = v; val gap = m - c; val parts = k - 1; val cAt = gap / parts; val n = cAt - c
        val who = if (s.contains("bố")) "bố" else "mẹ"
        val th = KidTheory("Tuổi gấp lần – hiệu không đổi",
            listOf(
                listOf(T("Hiệu số tuổi "), P("không đổi", GREEN), T(".")),
                listOf(T("Khi tuổi $who gấp $k lần tuổi con: con "), P("1 phần", BLUE), T(", $who "), P("$k phần", BLUE), T(", hiệu là "), P("$parts phần", ORANGE), T("."))
            ), emptyList(), s,
            listOf(
                step("Hiệu số tuổi (luôn như vậy)", T("$m − $c = "), P("$gap tuổi", ORANGE)),
                step("Vẽ sơ đồ lúc $who gấp $k lần con", visual = ageBars(who, "con", k.toInt(), "hiệu $gap"), note = "Hiệu $gap tuổi ứng với $k − 1 = $parts phần."),
                step("Tuổi con lúc đó", T("$gap : $parts = "), P("$cAt tuổi", BLUE)),
                step("Số năm cần tìm", T("$cAt − $c = "), P("$n năm", GREEN)),
                step("Thử lại", T("con ${c + n}, $who ${m + n}: ${m + n} : ${c + n} = $k ✓"))
            ), "Vậy sau $n năm nữa",
            quick = listOf(
                step("Hiệu ứng với ($k − 1) phần", T("$gap : $parts = "), P("$cAt", BLUE), note = "Đó là tuổi con lúc $who gấp $k lần."),
                step("Trừ tuổi con bây giờ", T("$cAt − $c = "), P("$n năm", GREEN))
            ), quickRule = "Tuổi con lúc đó = hiệu : (số lần − 1).")
        return th to { o -> val x = o.substringBefore(' ').toLongOrNull() ?: 0
            when (x) {
                cAt -> "$cAt là tuổi của con lúc đó, chưa phải số năm. Còn phải trừ tuổi con bây giờ: $cAt − $c = $n."
                gap / k -> "Hiệu $gap tuổi ứng với $k − 1 = $parts phần (không phải $k phần)."
                else -> "Thử lại với $x năm: con ${c + x}, $who ${m + x}. Chưa gấp $k lần."
            } }
    }

    /* ------------------------------- Tìm x, đi ngược ------------------------------- */

    private fun x1(s: String, v: List<Long>): Built {
        val (op, x, b) = v
        val box = v.getOrNull(3) == 1L
        val V = if (box) "□" else "x"
        val g1 = grade <= 1
        val expr = s.substringAfter(": ")
        val (rule, line, inv) = when (op) {
            0L -> Triple(if (g1) "Ô trống cộng $b được ${x + b}. Lấy ${x + b} bớt đi $b." else "Muốn tìm số hạng chưa biết, lấy tổng trừ số hạng kia.", "$V = ${x + b} − $b", "−")
            1L -> Triple(if (g1) "Ô trống bớt $b còn ${x - b}. Lấy ${x - b} thêm $b." else "Muốn tìm số bị trừ, lấy hiệu cộng số trừ.", "$V = ${x - b} + $b", "+")
            2L -> Triple("Muốn tìm thừa số chưa biết, lấy tích chia thừa số kia.", "$V = ${x * b} : $b", ":")
            else -> Triple("Muốn tìm số bị chia, lấy thương nhân số chia.", "$V = ${x / b} × $b", "×")
        }
        fun apply(y: Long) = when (op) { 0L -> y + b; 1L -> y - b; 2L -> y * b; else -> if (y % b == 0L) y / b else -1 }
        val target = apply(x)
        val sym = when (op) { 0L -> "+"; 1L -> "−"; 2L -> "×"; else -> ":" }
        val th = KidTheory("Tìm thành phần chưa biết",
            listOf(listOf(T(rule))), emptyList(), s,
            listOf(
                step(if (g1) "Đọc phép tính" else "Gọi tên thành phần", T(expr), note = rule),
                step("Làm phép tính ngược", T("$line = "), P(x, GREEN)),
                step("Thử lại", T(expr.replace(V, "$x") + " ✓"))
            ), if (box) "Vậy số cần điền là $x" else "Vậy x = $x",
            quick = listOf(step("Đi ngược: dùng phép tính ngược ($inv)", T("$line = "), P(x, GREEN))),
            quickRule = if (grade <= 1) "Cộng thì làm ngược là trừ." else "Cộng ↔ trừ, nhân ↔ chia.")
        return th to { o -> val y = o.toLongOrNull() ?: 0
            val got = apply(y)
            val check = when { got >= 0 -> "Thử thay $y: $y $sym $b = $got, không phải $target."; op == 1L -> "Thử thay $y: $y không bớt được $b."; else -> "Thử thay $y: $y không chia hết cho $b." }
            when {
                op == 0L && y == x + b -> "Con chép lại ${if (g1) "số sau dấu bằng" else "tổng"}. $check"
                op == 1L && y == x - b -> "Con chép lại ${if (g1) "số sau dấu bằng" else "hiệu"}. $check"
                op == 3L && y == x / b -> "Con chép lại thương. Phải nhân: ${x / b} × $b = $x."
                else -> check
            } }
    }

    private fun x2(s: String, v: List<Long>): Built {
        val (form, x) = v
        val V = if (v.getOrNull(4) == 1L) "□" else "x"
        val school: List<TheoryStep>; val quickLine: String; val r: Long; val f: (Long) -> Long?
        when (form) {
            0L -> { val a = v[2]; val k = v[3]; r = (x + a) * k; val y = x + a; f = { (it + a) * k }
                school = listOf(
                    step("Coi ($V + $a) là thừa số chưa biết", T("$V + $a = $r : $k"), note = "Muốn tìm thừa số chưa biết, lấy tích chia thừa số kia."),
                    step("Tính", T("$V + $a = "), P(y, BLUE)),
                    step("Tìm số hạng chưa biết", T("$V = $y − $a = "), P(x, GREEN)))
                quickLine = "$r : $k = $y → $y − $a = $x" }
            1L -> { val k = v[2]; val a = v[3]; r = x * k + a; val y = x * k; f = { it * k + a }
                school = listOf(
                    step("Coi $V × $k là số hạng chưa biết", T("$V × $k = $r − $a"), note = "Muốn tìm số hạng chưa biết, lấy tổng trừ số hạng kia."),
                    step("Tính", T("$V × $k = "), P(y, BLUE)),
                    step("Tìm thừa số chưa biết", T("$V = $y : $k = "), P(x, GREEN)))
                quickLine = "$r − $a = $y → $y : $k = $x" }
            else -> { val a = v[2]; val k = v[3]; r = (x - a) * k; val y = x - a; f = { if (it >= a) (it - a) * k else null }
                school = listOf(
                    step("Coi ($V − $a) là thừa số chưa biết", T("$V − $a = $r : $k"), note = "Muốn tìm thừa số chưa biết, lấy tích chia thừa số kia."),
                    step("Tính", T("$V − $a = "), P(y, BLUE)),
                    step("Tìm số bị trừ", T("$V = $y + $a = "), P(x, GREEN)))
                quickLine = "$r : $k = $y → $y + $a = $x" }
        }
        val expr = s.substringAfter(": ")
        val th = KidTheory("Tìm số – bóc từng lớp",
            listOf(listOf(T("Coi phần chứa ${if (V == "x") "x" else "ô trống"} là "), P("một số chưa biết", BLUE), T(".")),
                listOf(T("Bỏ "), P("lớp ngoài cùng", ORANGE), T(" trước, làm phép tính ngược."))),
            emptyList(), s,
            school + step("Thử lại", T(expr.replace(V, "$x") + " ✓")),
            if (V == "x") "Vậy x = $x" else "Vậy số cần điền là $x",
            quick = listOf(step("Đi ngược, bóc lớp ngoài trước", T(quickLine), note = "Mỗi bước dùng phép tính ngược.")),
            quickRule = "Đi ngược: phép tính làm sau cùng thì bỏ trước.")
        return th to { o -> val y = o.toLongOrNull() ?: 0
            val got = f(y)
            if (got != null) "Thử thay $y: ${expr.substringBefore(" =").replace(V, "$y")} = $got, không phải $r." else "Thử thay $y: phép trừ không làm được."
        }
    }

    private fun think(s: String, v: List<Long>): Built {
        val (form, x) = v
        val name = s.substringBefore(" nghĩ")
        val chain: String; val back: List<TheoryStep>; val r: Long; val quickLine: String; val fwd: (Long) -> String
        if (form == 2L) { val a = v[2]; val b = v[3]; r = x + a - b
            chain = "? → + $a → − $b → $r"
            back = listOf(step("Bỏ phép − $b: cộng thêm $b", T("$r + $b = "), P(r + b, BLUE)), step("Bỏ phép + $a: trừ đi $a", T("${r + b} − $a = "), P(x, GREEN)))
            quickLine = "$r + $b − $a = $x"; fwd = { y -> "$y + $a − $b = ${y + a - b}" }
        } else if (form == 0L) { val k = v[2]; val a = v[3]; r = x * k + a
            chain = "? → × $k → + $a → $r"
            back = listOf(step("Bỏ phép + $a: trừ đi $a", T("$r − $a = "), P(r - a, BLUE)), step("Bỏ phép × $k: chia cho $k", T("${r - a} : $k = "), P(x, GREEN)))
            quickLine = "($r − $a) : $k = $x"; fwd = { y -> "$y × $k + $a = ${y * k + a}" }
        } else { val a = v[2]; val k = v[3]; r = (x - a) * k
            chain = "? → − $a → × $k → $r"
            back = listOf(step("Bỏ phép × $k: chia cho $k", T("$r : $k = "), P(r / k, BLUE)), step("Bỏ phép − $a: cộng thêm $a", T("${r / k} + $a = "), P(x, GREEN)))
            quickLine = "$r : $k + $a = $x"; fwd = { y -> if (y >= a) "($y − $a) × $k = ${(y - a) * k}" else "$y không bớt được $a" }
        }
        val th = KidTheory("Đi ngược từ kết quả",
            listOf(listOf(T("Viết "), P("sơ đồ các bước", BLUE), T(".")), listOf(T("Rồi đi ngược từ kết quả, dùng "), P("phép tính ngược", ORANGE), T("."))),
            if (grade <= 1) listOf(Tile("+ ↔ −", "phép ngược", ORANGE)) else listOf(Tile("+ ↔ −", "phép ngược", ORANGE), Tile("× ↔ :", "phép ngược", ORANGE)), s,
            listOf(step("Viết sơ đồ $name đã làm", P(chain, BLUE))) + back + step("Thử lại", T(fwd(x) + " ✓")),
            "Vậy $name đã nghĩ số $x",
            quick = listOf(step("Đi ngược trong một dòng", T(quickLine))),
            quickRule = "Làm ngược lại, bắt đầu từ bước cuối.")
        return th to { o -> val y = o.toLongOrNull() ?: 0
            "Thử với $y: ${fwd(y)}, không phải $r. Hãy đi ngược từ bước cuối." }
    }

}
