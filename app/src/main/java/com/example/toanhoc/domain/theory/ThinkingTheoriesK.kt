package com.example.toanhoc.domain.theory

import com.example.toanhoc.domain.theory.Hue.BLUE
import com.example.toanhoc.domain.theory.Hue.GREEN
import com.example.toanhoc.domain.theory.Hue.INK
import com.example.toanhoc.domain.theory.Hue.MUTED
import com.example.toanhoc.domain.theory.Hue.ORANGE
import com.example.toanhoc.domain.thinking.ThinkingWords

/**
 * Lời giảng các dạng Toán tư duy lớp 1, 2, 4, 5 (dạng mới). Cùng chuẩn với ThinkingTheories:
 * cách ở trường (steps) + cách nhanh (quick, chỉ khi có mẹo thật) + giải thích từng đáp án sai.
 */
internal object ThinkingTheoriesK {
    /** Lớp của đề đang dựng (ThinkingTheories đặt trước khi gọi). */
    @Volatile var grade = 3

    private fun T(s: String, h: Hue = INK) = Tok.T(s, h)
    private fun P(s: Any, h: Hue) = Tok.P(s.toString(), h)
    private fun step(title: String, vararg line: Tok, note: String? = null, visual: Visual? = null) = TheoryStep(title, line.toList(), note, visual)
    private fun num(o: String) = o.substringBefore(' ').removeSuffix("%").toLongOrNull()
    private fun dec(h: Long) = ThinkingWords.dec(h)
    private fun lc(x: String) = x.replaceFirstChar { it.lowercase() }

    fun build(kind: String, s: String, v: List<Long>): Built? = when (kind) {
        "cmp" -> cmp(s, v)
        "qsum" -> qsum(s, v)
        "sumdiff" -> sumDiff(s, v)
        "ratio" -> ratio(s, v)
        "avg" -> if (v[0] == 0L) avgList(s, v) else avgMissing(s, v)
        "digits" -> digits(s, v)
        "dec" -> dec(s, v)
        "pct" -> pct(s, v)
        "motion" -> motion(s, v)
        "fcmp" -> fcmp(s, v)
        "fpair" -> fpair(s, v)
        "fgeo" -> fgeo(s, v)
        "fof" -> fof(s, v)
        "frem" -> frem(s, v)
        "fwhole" -> fwhole(s, v)
        else -> null
    }


    /* -------------------------------- Tính nhanh: tổng, hiệu nhiều số (qsum) -------------------------------- */
    private fun qsum(s: String, v: List<Long>): Built {
        val form = v[0]
        val g1 = grade <= 1
        fun roundWord(n: Long) = if (n % 100 == 0L) "tròn trăm" else "tròn chục"
        return when (form) {
            0L -> {
                val n = v[1].toInt(); val terms = v.subList(2, 2 + n)
                val pc = v[2 + n].toInt(); val idx = v.subList(3 + n, 3 + n + 2 * pc).map { it.toInt() }
                val pairs = (0 until pc).map { terms[idx[2 * it]] to terms[idx[2 * it + 1]] }
                val used = idx.toSet(); val lefts = terms.filterIndexed { i, _ -> i !in used }
                val sums = pairs.map { it.first + it.second }
                val ans = sums.sum() + lefts.sum()
                val unit = if (sums.any { it % 100 == 0L }) 100L else 10L
                val pairText = pairs.joinToString(", ") { "${it.first} + ${it.second} = ${it.first + it.second}" }
                val th = KidTheory("Tính nhanh: ghép cặp số tròn",
                    listOf(listOf(T("Tìm hai số cộng lại được "), P(if (unit == 100L) "số tròn trăm" else "số tròn chục", BLUE), T(".")),
                        listOf(T("Ghép chúng trước, "), P(if (g1) "kết quả không đổi" else "tổng không đổi", GREEN), T("."))),
                    listOf(Tile("7 + 3 = 10", "hàng đơn vị: 7 + 3", BLUE), Tile("64 + 36 = 100", "4 + 6 = 10, 6 + 3 + 1 = 10", GREEN)), s,
                    listOf(
                        step("Tìm các cặp số tròn", *pairs.map { P("${it.first} + ${it.second} = ${it.first + it.second}", BLUE) }.toTypedArray(),
                            note = "Nhìn chữ số hàng đơn vị: cộng lại được 10 là ghép được."),
                        step("Cộng các số tròn${if (lefts.isNotEmpty()) " với số còn lại" else ""}", T((sums + lefts).joinToString(" + ") + " = "), P(ans, GREEN),
                            note = if (lefts.isNotEmpty()) "Số ${lefts.joinToString(", ")} không có cặp, cộng ở cuối." else null)
                    ), "Vậy kết quả là $ans")
                th to { o -> val x = o.toLongOrNull() ?: 0
                    when {
                        x == ans + unit -> "Con cộng thừa $unit. Kiểm tra lại từng cặp: $pairText."
                        x == ans - unit -> "Con cộng thiếu $unit. Kiểm tra lại từng cặp: $pairText."
                        lefts.isNotEmpty() && x == ans - lefts.first() -> "Con quên cộng số ${lefts.first()} không có cặp."
                        else -> "Ghép $pairText, rồi cộng: kết quả là $ans."
                    } }
            }
            1L -> { val a = v[1]; val b = v[2]; val c = v[3]; val R = b + c; val ans = a - R
                val th = KidTheory("Tính nhanh: trừ liên tiếp",
                    listOf(listOf(T("Trừ $b rồi trừ $c cũng là "), P("trừ một lần ${b} + $c", GREEN), T(".")),
                        listOf(T("Thấy $b + $c "), P(roundWord(R), BLUE), T(" thì gộp lại trước."))),
                    listOf(Tile("90 − 7 − 3", "= 90 − 10 = 80", BLUE)), s,
                    listOf(
                        step("Gộp hai số bị trừ đi", T("$b + $c = "), P(R, BLUE)),
                        step("Trừ một lần", T("$a − $R = "), P(ans, GREEN))
                    ), "Vậy kết quả là $ans")
                th to { o -> val x = o.toLongOrNull() ?: 0
                    when (x) {
                        a - b + c -> "Con cộng $c, nhưng đề là trừ $c. Trừ cả hai: $a − ($b + $c)."
                        else -> "Gộp $b + $c = $R, rồi $a − $R = $ans."
                    } }
            }
            2L -> { val a = v[1]; val b = v[2]; val c = v[3]; val R = b - c; val ans = a + R
                val th = KidTheory("Tính nhanh: thêm rồi bớt",
                    listOf(listOf(T("Thêm $b rồi bớt $c cũng là "), P("thêm $R", GREEN), T(".")),
                        listOf(T("Thấy $b − $c "), P(roundWord(R), BLUE), T(" thì tính trước."))),
                    listOf(Tile("45 + 38 − 8", "= 45 + 30 = 75", BLUE)), s,
                    listOf(
                        step("Tính $b − $c trước", T("$b − $c = "), P(R, BLUE)),
                        step("Cộng với $a", T("$a + $R = "), P(ans, GREEN))
                    ), "Vậy kết quả là $ans")
                th to { o -> val x = o.toLongOrNull() ?: 0
                    when (x) {
                        a + b + c -> "Con cộng $c, nhưng đề là trừ $c."
                        a - R -> "Con trừ $R. Thêm $b bớt $c là THÊM $R."
                        else -> "Tính $b − $c = $R trước, rồi $a + $R = $ans."
                    } }
            }
            3L -> { val a = v[1]; val b = v[2]; val R = v[3]; val k = v[4]; val ans = a + b
                val th = KidTheory("Tính nhanh: làm tròn rồi bù",
                    listOf(listOf(T("$b gần $R. Cộng "), P("$R", BLUE), T(" cho dễ,")), listOf(T("rồi "), P("bớt lại $k", ORANGE), T(" vì đã cộng thừa."))),
                    listOf(Tile("98 + 25", "= 100 + 25 − 2 = 123", BLUE)), s,
                    listOf(
                        step("Làm tròn $b thành $R", T("$b = "), P("$R − $k", BLUE)),
                        step("Cộng số tròn", T("$R + $a = "), P(R + a, BLUE)),
                        step("Bớt lại $k", T("${R + a} − $k = "), P(ans, GREEN))
                    ), "Vậy kết quả là $ans")
                th to { o -> val x = o.toLongOrNull() ?: 0
                    when (x) {
                        a + R + k -> "Con thêm $k. Nhưng $b kém $R là $k, nên phải bớt $k."
                        a + R -> "Con quên bớt lại $k sau khi làm tròn."
                        else -> "$R + $a = ${R + a}, bớt $k được $ans."
                    } }
            }
            else -> { val a = v[1]; val b = v[2]; val R = v[3]; val k = v[4]; val ans = a - b
                val th = KidTheory("Tính nhanh: trừ số tròn rồi bù",
                    listOf(listOf(T("$b gần $R. Trừ "), P("$R", BLUE), T(" cho dễ,")), listOf(T("rồi "), P("cộng lại $k", ORANGE), T(" vì đã trừ thừa."))),
                    listOf(Tile("145 − 98", "= 145 − 100 + 2 = 47", BLUE)), s,
                    listOf(
                        step("Làm tròn $b thành $R", T("$b = "), P("$R − $k", BLUE)),
                        step("Trừ số tròn", T("$a − $R = "), P(a - R, BLUE)),
                        step("Cộng lại $k", T("${a - R} + $k = "), P(ans, GREEN))
                    ), "Vậy kết quả là $ans")
                th to { o -> val x = o.toLongOrNull() ?: 0
                    when (x) {
                        a - R - k -> "Con bớt thêm $k. Trừ $R là trừ thừa $k, phải cộng lại $k."
                        a - R -> "Con quên cộng lại $k sau khi trừ $R."
                        else -> "$a − $R = ${a - R}, cộng lại $k được $ans."
                    } }
            }
        }
    }

    /* -------------------------------- Hơn – kém -------------------------------- */
    private fun cmp(s: String, v: List<Long>): Built {
        val form = v[0].toInt(); val a = v[1]; val b = v[2]
        val (_, unit) = ThinkingWords.THINGS[v[3].toInt()]
        val A = ThinkingWords.KIDS[v[4].toInt()]; val B = ThinkingWords.KIDS[v[5].toInt()]
        val bMore = form == 0 || form == 3
        val ans = if (bMore) a + b else a - b
        val op = if (bMore) "+" else "−"
        val bars = Visual.Bars(listOf(
            BarRow(A, listOf(Seg(a.toFloat(), BLUE, "$a"))),
            BarRow(B, if (bMore) listOf(Seg(a.toFloat(), BLUE), Seg(b.toFloat(), ORANGE, "$b")) else listOf(Seg(ans.toFloat(), BLUE), Seg(b.toFloat(), MUTED, "$b")))
        ))
        val flipped = form >= 2
        val relText = when (form) { 0 -> "$B nhiều hơn $A $b $unit"; 1 -> "$B ít hơn $A $b $unit"; 2 -> "$A nhiều hơn $B $b $unit"; else -> "$A ít hơn $B $b $unit" }
        val meaning = if (bMore) "$B có NHIỀU hơn $A" else "$B có ÍT hơn $A"
        val steps = listOfNotNull(
            step("Tóm tắt", T("$A: $a $unit; $relText"), visual = bars),
            if (flipped) step("Đọc kĩ: ai nhiều hơn?", P(meaning, ORANGE), note = "Câu này nói về $A trước. Đổi lại cho dễ hiểu.") else null,
            step(if (bMore) "Tìm số lớn hơn: làm phép cộng" else "Tìm số bé hơn: làm phép trừ", T("$a $op $b = "), P("$ans $unit", GREEN))
        )
        val th = KidTheory("Bài toán nhiều hơn – ít hơn",
            listOf(listOf(T("Hỏi người có "), P("nhiều hơn", GREEN), T(" → làm phép cộng. Hỏi người có "), P("ít hơn", ORANGE), T(" → làm phép trừ."))),
            listOf(Tile("A nhiều hơn B 3", "= B ít hơn A 3", ORANGE)), s, steps, "Vậy $B có $ans $unit",
            quick = if (flipped) listOf(
                step("Đổi câu cho dễ hiểu", T("\"$relText\" = "), P("${if (bMore) "$B nhiều hơn $A" else "$B ít hơn $A"} $b $unit", ORANGE)),
                step("Tính ngay", T("$a $op $b = "), P("$ans $unit", GREEN))
            ) else emptyList(),
            quickRule = if (flipped) "Đổi câu so sánh để tên người được hỏi đứng trước." else null)
        return th to { o -> val x = num(o) ?: 0
            when (x) {
                (if (bMore) a - b else a + b) -> if (flipped) "Đề có chữ \"${if (form == 2) "nhiều hơn" else "ít hơn"}\" nhưng nói về $A. Thật ra $meaning, nên phải ${if (bMore) "cộng" else "trừ"}: $a $op $b = $ans."
                    else "$meaning nên phải ${if (bMore) "cộng" else "trừ"}: $a $op $b = $ans."
                b -> "$b chỉ là phần ${if (bMore) "hơn" else "kém"}. Còn phải ${if (bMore) "cộng" else "trừ"} với số $unit của $A: $a $op $b = $ans."
                else -> "Tính lại: $a $op $b = $ans."
            } }
    }

    /* -------------------------------- Tổng – hiệu -------------------------------- */
    private fun sumDiff(s: String, v: List<Long>): Built {
        val ctx = v[0].toInt(); val big = v[1]; val small = v[2]; val askBig = v[3] == 1L
        val T0 = big + small; val H = big - small
        val (bigN, smallN, unit) = when (ctx) { 1 -> Triple("Lớp 4A", "Lớp 4B", " cây"); 2 -> Triple("Mẹ", "Con", " tuổi"); else -> Triple("Số lớn", "Số bé", "") }
        val ans = if (askBig) big else small
        val bars = Visual.Bars(listOf(
            BarRow(bigN, listOf(Seg(small.toFloat(), BLUE), Seg(H.toFloat(), ORANGE, "$H"))),
            BarRow(smallN, listOf(Seg(small.toFloat(), BLUE)))
        ), "Tổng $T0")
        val steps = mutableListOf<TheoryStep>()
        if (ctx == 3) steps += step("Tìm tổng (dữ kiện ẩn)", T("Số lớn nhất có hai chữ số là "), P(99, BLUE))
        steps += step("Vẽ sơ đồ", note = "$bigN hơn ${lc(smallN)} đúng bằng hiệu $H.", visual = bars)
        if (askBig) {
            steps += step("Hai lần ${bigN.let(::lc)}", T("$T0 + $H = "), P(T0 + H, BLUE), note = "Thêm hiệu vào tổng thì hai phần bằng nhau, mỗi phần bằng ${bigN.let(::lc)}.")
            steps += step(bigN, T("${T0 + H} : 2 = "), P("$big$unit", GREEN))
        } else {
            steps += step("Hai lần ${smallN.let(::lc)}", T("$T0 − $H = "), P(T0 - H, BLUE), note = "Bớt phần hơn đi thì hai phần bằng nhau, mỗi phần bằng ${smallN.let(::lc)}.")
            steps += step(smallN, T("${T0 - H} : 2 = "), P("$small$unit", GREEN))
        }
        val concl = when (ctx) {
            1 -> "Vậy lớp ${if (askBig) "4A" else "4B"} trồng được $ans cây"
            2 -> "Vậy ${if (askBig) "mẹ" else "con"} $ans tuổi"
            else -> "Vậy số ${if (askBig) "lớn" else "bé"} là $ans"
        }
        val quick = if (T0 % 2 == 0L && H % 2 == 0L) listOf(
            step("Chia đôi tổng và hiệu", T("$T0 : 2 = "), P(T0 / 2, BLUE), T(" ;  $H : 2 = "), P(H / 2, ORANGE)),
            step(if (askBig) "Lớn = nửa tổng + nửa hiệu" else "Bé = nửa tổng − nửa hiệu", T("${T0 / 2} ${if (askBig) "+" else "−"} ${H / 2} = "), P("$ans$unit", GREEN))
        ) else emptyList()
        val th = KidTheory("Tìm hai số khi biết tổng và hiệu",
            listOf(listOf(P("Số lớn = (Tổng + Hiệu) : 2", GREEN)), listOf(P("Số bé = (Tổng − Hiệu) : 2", BLUE))),
            listOf(Tile("Tổng 10, hiệu 2", "lớn 6, bé 4", BLUE)), s, steps, concl,
            quick = quick, quickRule = if (quick.isEmpty()) null else "Nửa tổng là \"điểm giữa\": số lớn hơn nó nửa hiệu, số bé kém nó nửa hiệu.")
        return th to { o -> val x = num(o) ?: 0
            when (x) {
                T0 + H -> "${T0 + H} mới là HAI lần ${bigN.let(::lc)}. Còn phải chia 2: ${T0 + H} : 2 = $big."
                T0 - H -> "${T0 - H} là HAI lần ${smallN.let(::lc)}${if (askBig) ", đề lại hỏi ${bigN.let(::lc)}" else ". Còn phải chia 2"}."
                (if (askBig) small else big) -> "Đó là ${(if (askBig) smallN else bigN).let(::lc)}. Đề hỏi ${(if (askBig) bigN else smallN).let(::lc)}."
                else -> "${if (askBig) "$bigN = ($T0 + $H) : 2" else "$smallN = ($T0 − $H) : 2"} = $ans."
            } }
    }

    /* -------------------------------- Tổng – tỉ, hiệu – tỉ -------------------------------- */
    private fun ratio(s: String, v: List<Long>): Built {
        val mode = v[0]; val p = v[1]; val q = v[2]; val u = v[3]; val askBig = v[4] == 1L; val ctx = v[5]; val frac = v[6] == 1L
        val small = p * u; val big = q * u
        val total = if (mode == 0L) small + big else big - small
        val parts = if (mode == 0L) p + q else q - p
        val (bigN, smallN) = if (ctx == 0L) "Số lớn" to "Số bé" else "Thùng thứ nhất" to "Thùng thứ hai"
        val unit = if (ctx == 0L) "" else " lít"
        val ans = if (askBig) big else small
        val k = if (askBig) q else p
        val bars = Visual.Bars(listOf(
            BarRow(bigN, List(q.toInt()) { i -> Seg(1f, if (mode == 1L && i >= p) ORANGE else BLUE, if (mode == 1L && i == p.toInt()) "hiệu $total" else null) }),
            BarRow(smallN, List(p.toInt()) { Seg(1f, BLUE) })
        ), if (mode == 0L) "Tổng $total" else null)
        val note = if (frac) "Coi ${bigN.let(::lc)} là $q phần bằng nhau thì ${smallN.let(::lc)} là $p phần như thế." else "${smallN} là 1 phần thì ${bigN.let(::lc)} là $q phần như thế."
        val steps = mutableListOf(
            step("Vẽ sơ đồ", note = note, visual = bars),
            step(if (mode == 0L) "Tổng số phần bằng nhau" else "Hiệu số phần bằng nhau", T(if (mode == 0L) "$p + $q = " else "$q − $p = "), P(parts, ORANGE)),
            step("Giá trị một phần", T("$total : $parts = "), P(u, BLUE))
        )
        if (k == 1L) steps += step("$smallN là 1 phần", T("$smallN = "), P("$ans$unit", GREEN))
        else steps += step(if (askBig) bigN else smallN, T("$u × $k = "), P("$ans$unit", GREEN))
        val concl = if (ctx == 0L) "Vậy số ${if (askBig) "lớn" else "bé"} là $ans" else "Vậy thùng thứ ${if (askBig) "nhất" else "hai"} có $ans lít dầu"
        val th = KidTheory(if (mode == 0L) "Tìm hai số khi biết tổng và tỉ" else "Tìm hai số khi biết hiệu và tỉ",
            listOf(listOf(T("Vẽ sơ đồ, tìm "), P(if (mode == 0L) "tổng số phần" else "hiệu số phần", ORANGE), T(", rồi tìm "), P("giá trị một phần", BLUE), T("."))),
            emptyList(), s, steps, concl,
            quick = listOf(step("Một phần rồi nhân số phần", T("$total : $parts × $k = "), P("$ans$unit", GREEN), note = "${if (mode == 0L) "Tổng" else "Hiệu"} ứng với $parts phần; ${if (askBig) bigN.let(::lc) else smallN.let(::lc)} là $k phần.")),
            quickRule = "Một phần = ${if (mode == 0L) "tổng" else "hiệu"} : số phần.")
        return th to { o -> val x = num(o) ?: 0
            when {
                x == (if (askBig) small else big) -> "Đó là ${(if (askBig) smallN else bigN).let(::lc)}. Đề hỏi ${(if (askBig) bigN else smallN).let(::lc)}."
                x == u && k != 1L -> "$u mới là giá trị MỘT phần. ${(if (askBig) bigN else smallN)} có $k phần: $u × $k = $ans."
                else -> "${if (mode == 0L) "Tổng" else "Hiệu"} $total ứng với ${if (mode == 0L) "$p + $q" else "$q − $p"} = $parts phần (không phải ${if (mode == 0L) q else p + q} phần). Một phần là $u."
            } }
    }

    /* -------------------------------- Trung bình cộng -------------------------------- */
    private fun avgList(s: String, v: List<Long>): Built {
        val ctx = v[1]; val n = v[2]; val xs = v.drop(3); val sum = xs.sum(); val avg = sum / n
        val unit = if (ctx == 0L) "" else " kg"
        val base = xs.min() / 10 * 10
        val devs = xs.map { it - base }; val dsum = devs.sum()
        fun sg(d: Long) = if (d >= 0) "+$d" else "−${-d}"
        val th = KidTheory("Trung bình cộng",
            listOf(listOf(P("Trung bình cộng", GREEN), T(" = "), P("tổng các số", BLUE), T(" : "), P("số các số hạng", ORANGE), T("."))),
            listOf(Tile("2, 4, 6", "(2 + 4 + 6) : 3 = 4", BLUE)), s,
            listOf(
                step("Tính tổng", T("${xs.joinToString(" + ")} = "), P(sum, BLUE)),
                step("Chia cho số các số hạng ($n)", T("$sum : $n = "), P("$avg$unit", GREEN))
            ), if (ctx == 0L) "Vậy trung bình cộng là $avg" else "Vậy trung bình mỗi ngày bán được $avg kg gạo",
            quick = listOf(
                step("Chọn mốc $base, ghi phần chênh", T(devs.joinToString("  ") { sg(it) }), note = "Mỗi số so với $base hơn (+) hay kém (−) bao nhiêu."),
                step("Chia đều phần chênh", T("${sg(dsum)} : $n = ${sg(dsum / n)}  →  $base ${if (dsum >= 0) "+" else "−"} ${kotlin.math.abs(dsum / n)} = "), P("$avg$unit", GREEN))
            ), quickRule = "Chọn một số tròn làm mốc, chỉ cộng phần chênh lệch (số nhỏ, dễ nhẩm).")
        return th to { o -> val x = num(o) ?: 0
            when (x) {
                sum -> "$sum mới là tổng. Còn phải chia cho $n (có $n số hạng): $sum : $n = $avg."
                else -> "Có $n số hạng nên lấy tổng chia cho $n: $sum : $n = $avg."
            } }
    }

    private fun avgMissing(s: String, v: List<Long>): Built {
        val A = v[3]; val b = v[4]; val c = v[5]; val x3 = 3 * A - b - c
        fun sg(d: Long) = if (d >= 0) "+$d" else "−${-d}"
        val db = b - A; val dc = c - A
        val th = KidTheory("Tìm số chưa biết khi biết trung bình cộng",
            listOf(listOf(P("Tổng các số", BLUE), T(" = "), P("trung bình cộng × số các số hạng", GREEN), T("."))),
            emptyList(), s,
            listOf(
                step("Tổng của ba số", T("$A × 3 = "), P(3 * A, BLUE)),
                step("Tổng hai số đã biết", T("$b + $c = "), P(b + c, BLUE)),
                step("Số thứ ba", T("${3 * A} − ${b + c} = "), P(x3, GREEN))
            ), "Vậy số thứ ba là $x3",
            quick = listOf(
                step("So với trung bình $A", T("$b là ${sg(db)},  $c là ${sg(dc)}"), note = "Tổng phần chênh của cả ba số phải bằng 0."),
                step("Số thứ ba bù lại", T("$A ${if (db + dc >= 0) "−" else "+"} ${kotlin.math.abs(db + dc)} = "), P(x3, GREEN))
            ), quickRule = "Các số hơn – kém trung bình bù trừ cho nhau vừa hết.")
        return th to { o -> val x = num(o) ?: 0
            when (x) {
                2 * A - b - c -> "Ba số nên tổng là $A × 3 (không phải × 2) = ${3 * A}."
                3 * A -> "${3 * A} là tổng cả ba số. Còn phải trừ hai số đã biết: ${3 * A} − ${b + c} = $x3."
                else -> "Tổng ba số là $A × 3 = ${3 * A}, nên số thứ ba là ${3 * A} − $b − $c = $x3."
            } }
    }

    /* -------------------------------- Đếm chữ số -------------------------------- */
    private fun digits(s: String, v: List<Long>): Built {
        val n = v[0]; val ctx = v[1]
        val c2 = if (n >= 10) minOf(n, 99) - 9 else 0; val c3 = if (n >= 100) n - 99 else 0
        val d = 9 + c2 * 2 + c3 * 3
        val word = if (ctx == 0L) "số" else "trang"
        val steps = mutableListOf(step("Từ 1 đến 9: 9 $word có 1 chữ số", T("9 × 1 = "), P(9, BLUE)))
        if (c2 > 0) steps += step("Từ 10 đến ${minOf(n, 99)}: $c2 $word có 2 chữ số", T("$c2 × 2 = "), P(c2 * 2, BLUE), note = if (c2 == 90L) "Từ 10 đến 99 có (99 − 10) + 1 = 90 số." else "(${minOf(n, 99)} − 10) + 1 = $c2.")
        if (c3 > 0) steps += step("Từ 100 đến $n: $c3 $word có 3 chữ số", T("$c3 × 3 = "), P(c3 * 3, BLUE), note = "($n − 100) + 1 = $c3.")
        steps += step("Cộng lại", T(listOfNotNull("9", if (c2 > 0) "${c2 * 2}" else null, if (c3 > 0) "${c3 * 3}" else null).joinToString(" + ") + " = "), P(d, GREEN))
        val big = if (n >= 100) 3 else 2
        val missing = if (n >= 100) 9 * 2 + 90 else 9
        val th = KidTheory("Đếm chữ số",
            listOf(listOf(T("Chia thành nhóm số có "), P("1, 2, 3 chữ số", BLUE), T(", đếm số lượng mỗi nhóm rồi nhân với số chữ số."))),
            listOf(Tile("1 → 9", "9 chữ số", BLUE), Tile("10 → 99", "180 chữ số", BLUE)), s, steps, "Vậy cần $d chữ số",
            quick = listOf(
                step("Coi mọi $word đều có $big chữ số", T("$n × $big = "), P(n * big, BLUE)),
                step("Trừ phần đếm thừa", T("${n * big} − $missing = "), P(d, GREEN), note = if (n >= 100) "Số 1 chữ số đếm thừa 18, số 2 chữ số đếm thừa 90." else "9 số có 1 chữ số, mỗi số đếm thừa 1.")
            ), quickRule = "Đếm như thể mọi số đều dài bằng nhau, rồi trừ phần đếm thừa.")
        return th to { o -> val x = num(o) ?: 0
            when (x) {
                n -> "$n là số lượng ${word}. Đề hỏi số CHỮ SỐ: mỗi $word có thể có 1, 2 hoặc 3 chữ số."
                d - 9 -> "Con quên nhóm từ 1 đến 9 (9 chữ số)."
                else -> "Không phải $word nào cũng có $big chữ số. Đếm theo nhóm: kết quả là $d."
            } }
    }

    /* -------------------------------- Tính nhanh số thập phân -------------------------------- */
    private fun dec(s: String, v: List<Long>): Built {
        val form = v[0]
        return when (form) {
            0L -> { val m = v[1]
                val th = KidTheory("Tính nhanh: 0,25 × 4 = 1",
                    listOf(listOf(T("Đổi chỗ, nhóm hai thừa số có tích là "), P("1, 10, 100", GREEN), T(" để nhân trước."))),
                    listOf(Tile("0,25 × 4", "= 1", GREEN), Tile("0,5 × 2", "= 1", GREEN)), s,
                    listOf(
                        step("Đổi chỗ, nhóm thừa số", T("0,25 × $m × 4 = "), P("(0,25 × 4) × $m", BLUE), note = "Tính chất giao hoán và kết hợp của phép nhân."),
                        step("Nhân trong ngoặc", T("= "), P("1 × $m", BLUE)),
                        step("Nhân với 1", T("= "), P(m, GREEN))
                    ), "Vậy kết quả là $m",
                    quick = listOf(step("Thấy 0,25 × 4 = 1", T("Kết quả chính là "), P(m, GREEN))), quickRule = "Thừa số nhân với 1 thì giữ nguyên.")
                th to { o -> when (o) {
                    "${m * 4}" -> "Con quên nhân với 0,25. Nhóm 0,25 × 4 = 1 trước."
                    dec(m * 25) -> "Con quên nhân với 4. 0,25 × 4 = 1 nên kết quả là $m."
                    else -> "0,25 × 4 = 1, nên 0,25 × $m × 4 = $m." } }
            }
            3L -> { val a = v[1]; val A = dec(a * 10); val ans = dec(a * 20)
                val th = KidTheory("Chia cho 0,5",
                    listOf(listOf(T("Chia một số cho "), P("0,5", BLUE), T(" chính là "), P("nhân số đó với 2", GREEN), T("."))),
                    listOf(Tile("3 : 0,5", "= 3 × 2 = 6", GREEN)), s,
                    listOf(
                        step("Chuyển dấu phẩy sang phải 1 chữ số ở cả hai số", T("$A : 0,5 = "), P("${dec(a * 100)} : 5", BLUE)),
                        step("Thực hiện phép chia", T("${dec(a * 100)} : 5 = "), P(ans, GREEN))
                    ), "Vậy kết quả là $ans",
                    quick = listOf(step("Chia 0,5 = nhân 2", T("$A × 2 = "), P(ans, GREEN), note = "Trong 1 có 2 lần 0,5.")), quickRule = "Chia cho 0,5 là gấp lên 2 lần; chia cho 0,25 là gấp lên 4 lần.")
                th to { o -> when (o) {
                    dec(a * 5) -> "Con đã chia cho 2. Chia cho 0,5 làm số đó LỚN lên gấp 2: $A × 2 = $ans."
                    dec(a * 50) -> "Con đã nhân với 0,5. Đề là phép CHIA cho 0,5: $A × 2 = $ans."
                    else -> "Chia cho 0,5 là nhân với 2: $A × 2 = $ans." } }
            }
            1L -> { val a = v[1]; val b = v[2]; val c = v[3]; val A = dec(a * 10); val ans = dec(a * 1000)
                val th = KidTheory("Tính nhanh: thừa số chung",
                    listOf(listOf(P("a × b + a × c", BLUE), T(" = "), P("a × (b + c)", GREEN), T("."))), emptyList(), s,
                    listOf(
                        step("Đưa thừa số chung $A ra ngoài", T("$A × $b + $A × $c = "), P("$A × ($b + $c)", BLUE)),
                        step("Tính trong ngoặc", T("= "), P("$A × 100", BLUE)),
                        step("Nhân với 100: dời dấu phẩy sang phải 2 chữ số", T("= "), P(ans, GREEN))
                    ), "Vậy kết quả là $ans",
                    quick = listOf(step("Thấy $b + $c = 100", T("$A × 100 = "), P(ans, GREEN))), quickRule = "Cùng nhân với một số: gộp các số còn lại.")
                th to { o -> when (o) {
                    dec(a * 100) -> "Nhân với 100 phải dời dấu phẩy sang phải HAI chữ số: $A × 100 = $ans."
                    else -> "$b + $c = 100, nên kết quả là $A × 100 = $ans." } }
            }
            else -> { val a = v[1]; val A = dec(a); val ans = dec(a * 100)
                val th = KidTheory("Tính nhanh: thêm 1 lần nữa",
                    listOf(listOf(T("Một số cũng là "), P("số đó × 1", BLUE), T(". Gộp lại: "), P("a × 99 + a × 1 = a × 100", GREEN), T("."))), emptyList(), s,
                    listOf(
                        step("Viết $A thành $A × 1", T("$A × 99 + $A = "), P("$A × 99 + $A × 1", BLUE)),
                        step("Đưa thừa số chung ra ngoài", T("= $A × (99 + 1) = "), P("$A × 100", BLUE)),
                        step("Dời dấu phẩy sang phải 2 chữ số", T("= "), P(ans, GREEN))
                    ), "Vậy kết quả là $ans",
                    quick = listOf(step("99 lần $A thêm 1 lần $A", T("= 100 lần $A = "), P(ans, GREEN))), quickRule = "99 lần thêm 1 lần là 100 lần.")
                th to { o -> when (o) {
                    dec(a * 99) -> "Con quên cộng thêm $A (chính là $A × 1)."
                    dec(a * 10) -> "Nhân với 100 phải dời dấu phẩy sang phải HAI chữ số."
                    else -> "$A × 99 + $A = $A × 100 = $ans." } }
            }
        }
    }

    /* -------------------------------- Tỉ số phần trăm -------------------------------- */
    private fun quickPct(p: Long, N: Long): Pair<String, Long>? = when (p) {
        50L -> "$N : 2" to N / 2
        25L -> "$N : 4" to N / 4
        10L -> "$N : 10" to N / 10
        20L -> "$N : 5" to N / 5
        5L -> "$N : 10 : 2" to N / 20
        75L -> "$N : 4 × 3" to N / 4 * 3
        40L -> "$N : 5 × 2" to N / 5 * 2
        15L -> "$N : 10 + $N : 20" to N / 10 + N / 20
        else -> null
    }

    private fun pct(s: String, v: List<Long>): Built {
        val form = v[0]
        return when (form) {
            0L -> { val p = v[1]; val N = v[2]; val ans = N * p / 100; val qk = quickPct(p, N)?.takeIf { it.second == ans }
                val th = KidTheory("Tìm tỉ số phần trăm của một số",
                    listOf(listOf(T("Tìm "), P("p%", BLUE), T(" của N: lấy "), P("N × p : 100", GREEN), T("."))),
                    listOf(Tile("50%", "= một nửa", BLUE), Tile("25%", "= một phần tư", BLUE)), s,
                    listOf(step("Lấy $N nhân $p rồi chia 100", T("$N × $p : 100 = "), P(ans, GREEN))), "Vậy $p% của $N là $ans",
                    quick = qk?.let { listOf(step("$p% là phần quen thuộc", T("${it.first} = "), P(ans, GREEN))) } ?: emptyList(),
                    quickRule = qk?.let { "50% là nửa, 25% là một phần tư, 10% là một phần mười." })
                th to { o -> val x = num(o) ?: 0
                    when (x) {
                        N - ans -> "Đó là phần còn lại (${100 - p}%). Đề hỏi $p% của $N."
                        else -> "$p% của $N = $N × $p : 100 = $ans." } }
            }
            1L -> { val N = v[1]; val M = v[2]; val P0 = M * 100 / N; val k = 100 / N
                val th = KidTheory("Tỉ số phần trăm của hai số",
                    listOf(listOf(T("Tìm thương "), P("a : b", BLUE), T(", rồi "), P("nhân với 100 và viết thêm %", GREEN), T("."))),
                    emptyList(), s,
                    listOf(
                        step("Tìm thương", T("$M : $N = "), P(dec(P0), BLUE)),
                        step("Nhân với 100, viết thêm %", T("${dec(P0)} × 100 = "), P("$P0%", GREEN))
                    ), "Vậy học sinh nữ chiếm $P0%",
                    quick = listOf(step("Quy về mẫu số 100", T("$M/$N = ${M * k}/100 = "), P("$P0%", GREEN), note = "Nhân cả tử và mẫu với $k.")),
                    quickRule = "Phần trăm là \"phần trên 100\": đưa phân số về mẫu 100.")
                th to { o -> val x = num(o) ?: 0
                    when (x) {
                        M -> "$M là số học sinh nữ, chưa phải tỉ số phần trăm. Phải lấy $M : $N rồi nhân 100."
                        100 - P0 -> "${100 - P0}% là tỉ số của học sinh nam. Đề hỏi học sinh nữ."
                        else -> "$M : $N = ${dec(P0)} = $P0%." } }
            }
            2L -> { val p = v[1]; val A = v[2]; val X = A * 100 / p
                val mult = when (p) { 50L -> "× 2"; 25L -> "× 4"; 10L -> "× 10"; 20L -> "× 5"; 5L -> "× 20"; else -> null }
                val th = KidTheory("Tìm một số khi biết phần trăm của nó",
                    listOf(listOf(T("Biết "), P("p%", BLUE), T(" của một số là A: số đó = "), P("A : p × 100", GREEN), T("."))),
                    emptyList(), s,
                    listOf(step("Tìm 1% của số đó", T("$A : $p = "), P(dec(A * 100 / p), BLUE)), step("Tìm 100% (cả số)", T("${dec(A * 100 / p)} × 100 = "), P(X, GREEN))),
                    "Vậy số đó là $X",
                    quick = mult?.let { listOf(step("$p% là ${100 / p} phần bằng nhau của số đó", T("$A $it = "), P(X, GREEN))) } ?: emptyList(),
                    quickRule = mult?.let { "Số đó gấp ${100 / p} lần $p% của nó." })
                th to { o -> val x = num(o) ?: 0
                    when {
                        x == A * p / 100 && A * p % 100 == 0L -> "Con lại tìm $p% của $A. Đề cho $A là $p% của số cần tìm."
                        else -> "$A ứng với $p%, nên số đó là $A : $p × 100 = $X." } }
            }
            else -> { val p = v[1]; val N = v[2]; val off = N * p / 100; val ans = N - off
                val th = KidTheory("Bài toán giảm giá",
                    listOf(listOf(T("Giá mới = giá cũ − "), P("số tiền được giảm", ORANGE), T(" (tính theo phần trăm của giá cũ)."))),
                    emptyList(), s,
                    listOf(
                        step("Số tiền được giảm", T("$N × $p : 100 = "), P(off, ORANGE)),
                        step("Giá sau khi giảm", T("$N − $off = "), P("$ans nghìn đồng", GREEN))
                    ), "Vậy giá chiếc áo sau khi giảm là $ans nghìn đồng",
                    quick = listOf(step("Giảm $p% thì còn ${100 - p}%", T("$N × ${100 - p} : 100 = "), P("$ans nghìn đồng", GREEN))),
                    quickRule = "Giảm p% thì giá mới bằng (100 − p)% giá cũ.")
                th to { o -> val x = num(o) ?: 0
                    when (x) {
                        off -> "$off nghìn đồng là số tiền được GIẢM, chưa phải giá mới. Còn phải lấy $N − $off."
                        N - p -> "Con trừ thẳng $p, nhưng $p% là phần trăm của giá áo. Phải tính $p% của $N trước."
                        N + off -> "Giảm giá thì giá mới phải bớt đi, không cộng thêm."
                        else -> "Giá mới = $N − $off = $ans nghìn đồng." } }
            }
        }
    }

    /* -------------------------------- Phân số (lớp 4) -------------------------------- */

    /** Một thanh = một cái bánh chia [den] phần bằng nhau, tô [num] phần. */
    private fun fracRow(num: Long, den: Long, hue: Hue = BLUE) = BarRow("$num/$den",
        List(maxOf(num, den).toInt()) { i -> Seg(1f / den, if (i < num) hue else MUTED) })

    private fun fcmp(s: String, v: List<Long>): Built {
        val mode = v[0]; val askMax = v[1] == 1L
        val fr = (0 until 4).map { v[2 + 2 * it] to v[3 + 2 * it] }
        fun str(f: Pair<Long, Long>) = "${f.first}/${f.second}"
        fun value(f: Pair<Long, Long>) = f.first.toDouble() / f.second
        val ans = if (mode == 2L) fr[0] else if (askMax) fr.maxBy(::value) else fr.minBy(::value)
        val word = if (askMax) "lớn nhất" else "bé nhất"
        val bars = Visual.Bars(fr.sortedByDescending(::value).map { fracRow(it.first, it.second, if (it == ans) GREEN else BLUE) })
        return when (mode) {
            0L -> { val a = fr[0].first; val d = ans.second
                val th = KidTheory("So sánh phân số cùng tử số",
                    listOf(listOf(T("Cùng tử số: phân số nào có "), P("mẫu số bé hơn", GREEN), T(" thì "), P("lớn hơn", GREEN), T("."))),
                    listOf(Tile("1/2 > 1/3", "chia 2 phần thì mỗi phần to hơn", BLUE)), s,
                    listOf(
                        step("Đọc đề: các phân số đều có tử số $a", T(fr.joinToString(";  ") { str(it) }), note = "Mỗi phân số đều lấy $a phần, chỉ khác là bánh chia thành bao nhiêu phần."),
                        step("Nhìn hình: chia càng ít phần, mỗi phần càng to", visual = bars),
                        step("So sánh mẫu số", T(fr.map { it.second }.sorted().joinToString(" < ")), note = "Mẫu số $word ngược lại: phân số $word có mẫu số ${if (askMax) "bé nhất" else "lớn nhất"}."),
                        step("Chọn phân số $word", P(str(ans), GREEN))
                    ), "Vậy phân số $word là ${str(ans)}")
                th to { o -> val f = fr.first { str(it) == o }
                    if (f == (if (askMax) fr.minBy(::value) else fr.maxBy(::value))) "Con chọn phân số có mẫu số ${if (askMax) "lớn nhất" else "bé nhất"}. Cùng tử số thì mẫu càng lớn, phân số càng bé."
                    else "$o có mẫu số ${f.second}. Phân số $word là $a/$d." }
            }
            1L -> { val d = fr[0].second
                val th = KidTheory("So sánh phân số cùng mẫu số",
                    listOf(listOf(T("Cùng mẫu số: phân số nào có "), P("tử số lớn hơn", GREEN), T(" thì "), P("lớn hơn", GREEN), T("."))),
                    listOf(Tile("3/7 < 5/7", "lấy 3 phần ít hơn lấy 5 phần", BLUE)), s,
                    listOf(
                        step("Đọc đề: các phân số đều có mẫu số $d", T(fr.joinToString(";  ") { str(it) }), note = "Bánh nào cũng chia $d phần bằng nhau; chỉ khác số phần được lấy."),
                        step("Nhìn hình: lấy càng nhiều phần càng nhiều bánh", visual = bars),
                        step("So sánh tử số", T(fr.map { it.first }.sorted().joinToString(" < "))),
                        step("Chọn phân số $word", P(str(ans), GREEN))
                    ), "Vậy phân số $word là ${str(ans)}")
                th to { o -> val f = fr.first { str(it) == o }
                    "$o lấy ${f.first} phần. Cùng mẫu số $d, phân số $word là ${str(ans)}." }
            }
            2L -> {
                val th = KidTheory("So sánh phân số với 1",
                    listOf(listOf(P("Tử > mẫu", GREEN), T(": lớn hơn 1. "), P("Tử = mẫu", BLUE), T(": bằng 1. "), P("Tử < mẫu", ORANGE), T(": bé hơn 1."))),
                    listOf(Tile("5/4 > 1", "nhiều hơn một cái bánh", GREEN), Tile("4/4 = 1", "vừa đúng một cái bánh", BLUE)), s,
                    listOf(
                        step("So sánh tử số với mẫu số từng phân số", T(fr.joinToString("   ") { "${str(it)}: ${it.first} ${if (it.first > it.second) ">" else if (it.first == it.second) "=" else "<"} ${it.second}" })),
                        step("Nhìn hình: phân số nào nhiều hơn một cái bánh?", visual = bars),
                        step("Chọn phân số có tử số lớn hơn mẫu số", P(str(ans), GREEN))
                    ), "Vậy phân số lớn hơn 1 là ${str(ans)}")
                th to { o -> val f = fr.first { str(it) == o }
                    if (f.first == f.second) "$o có tử số bằng mẫu số nên $o = 1, không lớn hơn 1."
                    else "$o có tử số bé hơn mẫu số (${f.first} < ${f.second}) nên bé hơn 1." }
            }
            else -> {
                val gaps = fr.map { "1/${it.second}" }
                val th = KidTheory("So sánh bằng phần bù",
                    listOf(listOf(T("Phân số nào "), P("thiếu ít hơn", GREEN), T(" để bằng 1 thì "), P("lớn hơn", GREEN), T("."))),
                    listOf(Tile("2/3 thiếu 1/3", "3/4 thiếu 1/4 → 3/4 lớn hơn", BLUE)), s,
                    listOf(
                        step("Mỗi phân số thiếu bao nhiêu để bằng 1?", T(fr.joinToString("   ") { "${str(it)} thiếu 1/${it.second}" }), note = "Mỗi phân số chỉ thiếu đúng 1 phần."),
                        step("Nhìn hình: phần thiếu (màu xám)", visual = bars),
                        step("So sánh phần thiếu", T(fr.sortedBy { it.second }.joinToString(" > ") { "1/${it.second}" }), note = "Cùng tử số 1: mẫu càng lớn, phần thiếu càng nhỏ."),
                        step("Thiếu ${if (askMax) "ít nhất" else "nhiều nhất"} thì ${if (askMax) "lớn nhất" else "bé nhất"}", P(str(ans), GREEN))
                    ), "Vậy phân số $word là ${str(ans)}",
                    quick = listOf(step("Nhìn nhanh mẫu số", T("Dạng (n − 1)/n: mẫu số ${if (askMax) "lớn nhất" else "bé nhất"} → "), P(str(ans), GREEN))),
                    quickRule = "Với các phân số chỉ thiếu 1 phần để bằng 1: mẫu số càng lớn, phân số càng gần 1.")
                th to { o -> val f = fr.first { str(it) == o }
                    if (f == (if (askMax) fr.minBy(::value) else fr.maxBy(::value))) "$o thiếu 1/${f.second} để bằng 1 – phần thiếu ${if (askMax) "lớn nhất" else "bé nhất"}, nên $o ${if (askMax) "bé nhất" else "lớn nhất"}."
                    else "$o thiếu 1/${f.second}. ${str(ans)} thiếu 1/${ans.second}, ${if (askMax) "ít" else "nhiều"} nhất." }
            }
        }
    }

    private fun fpair(s: String, v: List<Long>): Built {
        val a = v[0]; val m = v[1]; val b = v[2]; val n = v[3]
        val th = KidTheory("Tính nhanh: ghép thành 1",
            listOf(listOf(T("Tìm các phân số "), P("cùng mẫu số", BLUE), T(" cộng lại được "), P("1", GREEN), T(" rồi ghép chúng với nhau."))),
            listOf(Tile("2/5 + 3/5", "= 5/5 = 1", GREEN)), s,
            listOf(
                step("Đổi chỗ, nhóm các phân số cùng mẫu", T("= "), P("($a/$m + ${m - a}/$m) + ($b/$n + ${n - b}/$n)", BLUE), note = "Đổi chỗ các số hạng thì tổng không đổi."),
                step("Cộng hai phân số cùng mẫu: cộng tử số, giữ mẫu số", T("= $m/$m + $n/$n")),
                step("Mỗi phân số bằng 1", T("= 1 + 1 = "), P(2, GREEN))
            ), "Vậy kết quả là 2",
            quick = listOf(step("Thấy ngay hai cặp bằng 1", P("$a/$m + ${m - a}/$m = 1", GREEN), P("$b/$n + ${n - b}/$n = 1", GREEN))),
            quickRule = "Hai phân số cùng mẫu, tử số cộng lại bằng mẫu số → tổng bằng 1.")
        return th to { o -> when (o) {
            "1" -> "Con mới ghép được một cặp. Có hai cặp, mỗi cặp bằng 1: 1 + 1 = 2."
            else -> if (o.contains("/") && o.substringAfter("/").toLongOrNull() == 2 * (m + n)) "Con đã cộng tử số với tử số, mẫu số với mẫu số. Cộng phân số thì GIỮ mẫu số (khi cùng mẫu)."
                    else "Ghép $a/$m với ${m - a}/$m và $b/$n với ${n - b}/$n: mỗi cặp bằng 1, tổng là 2." } }
    }

    private fun fgeo(s: String, v: List<Long>): Built {
        val k = v[0].toInt(); val den = 1L shl k
        val terms = (1..k).map { 1L shl it }
        val bars = Visual.Bars(listOf(BarRow("1", terms.map { Seg((den / it).toFloat(), BLUE, "1/$it") } + Seg(1f, MUTED, "thiếu"))))
        val th = KidTheory("Tính nhanh: tô dần một nửa",
            listOf(listOf(T("Mỗi phân số bằng "), P("một nửa", BLUE), T(" phân số trước. Cộng hết lại thì chỉ "), P("thiếu đúng phần cuối", ORANGE), T(" để bằng 1."))),
            emptyList(), s,
            listOf(
                step("Quy đồng mẫu số $den", T(terms.joinToString(" + ") { "${den / it}/$den" })),
                step("Cộng các tử số", T("${terms.joinToString(" + ") { "${den / it}" }} = "), P(den - 1, BLUE)),
                step("Kết quả", P("${den - 1}/$den", GREEN))
            ), "Vậy kết quả là ${den - 1}/$den",
            quick = listOf(
                step("Nhìn hình: tô dần một nửa phần còn lại", visual = bars),
                step("Chỉ thiếu phần cuối", T("1 − 1/$den = "), P("${den - 1}/$den", GREEN))
            ), quickRule = "1/2 + 1/4 + … + 1/n = 1 − 1/n.")
        return th to { o -> when {
            o == "1" -> "Gần bằng 1 nhưng chưa đủ: còn thiếu đúng 1/$den."
            o.substringBefore("/") == "$k" -> "Con đã cộng tử với tử, mẫu với mẫu. Phải quy đồng mẫu số $den rồi cộng tử số."
            else -> "Quy đồng mẫu số $den rồi cộng tử số: kết quả là ${den - 1}/$den." } }
    }

    private fun fof(s: String, v: List<Long>): Built {
        val a = v[0]; val b = v[1]; val N = v[2]; val ctx = v[3]
        val part = N / b; val ans = part * a
        val unit = when (ctx) { 1L -> " học sinh"; 2L -> " kg"; else -> "" }
        val bars = Visual.Bars(listOf(BarRow("$N$unit", List(b.toInt()) { i -> Seg(1f, if (i < a) GREEN else MUTED, "$part") })))
        val concl = when (ctx) { 1L -> "Vậy lớp 4A có $ans học sinh nữ"; 2L -> "Vậy cửa hàng đã bán $ans kg gạo"; else -> "Vậy $a/$b của $N là $ans" }
        val th = KidTheory("Tìm phân số của một số",
            listOf(listOf(T("Muốn tìm "), P("$a/$b", BLUE), T(" của một số: lấy số đó "), P("chia cho $b", ORANGE), T(" rồi "), P("nhân với $a", GREEN), T("."))),
            listOf(Tile("3/4 của 20", "20 : 4 × 3 = 15", BLUE)), s,
            listOf(
                step("Chia $N thành $b phần bằng nhau, lấy $a phần", visual = bars),
                step(if (a == 1L) "Lấy một phần" else "Một phần là", T("$N : $b = "), P(if (a == 1L) "$ans$unit" else "$part", if (a == 1L) GREEN else ORANGE)),
            ) + (if (a == 1L) emptyList() else listOf(step("$a phần là", T("$part × $a = "), P("$ans$unit", GREEN)))), concl,
            quick = if (a == 1L) emptyList() else listOf(step("Viết gọn trong một dòng", T("$N × $a : $b = "), P("$ans$unit", GREEN))),
            quickRule = if (a == 1L) null else "Nhân trước hay chia trước đều được: $N : $b × $a = $N × $a : $b.")
        return th to { o -> val x = num(o) ?: 0
            when (x) {
                part -> "$part mới là MỘT phần. $a/$b là $a phần như thế: $part × $a = $ans."
                N -> "$N là cả số. $a/$b của $N chỉ là $a trong $b phần bằng nhau: $N : $b${if (a == 1L) "" else " × $a"} = $ans."
                N * a -> "Con nhân với $a mà quên chia cho $b."
                N * b -> "Con nhân $N với $b. Tìm $a/$b của một số thì phải CHIA cho $b."
                N - ans -> "Đó là phần còn lại. Đề hỏi $a/$b của $N."
                else -> "Lấy $N chia $b được $part, rồi nhân $a: kết quả là $ans." } }
    }

    private fun frem(s: String, v: List<Long>): Built {
        val a = v[0]; val b = v[1]; val N = v[2]; val (_, unit) = ThingsOf(v[3]); val name = ThinkingWords.KIDS[v[4].toInt()]
        val part = N / b; val give = part * a; val left = N - give
        val bars = Visual.Bars(listOf(BarRow("$N $unit", List(b.toInt()) { i -> Seg(1f, if (i < a) ORANGE else GREEN, "$part") })))
        val th = KidTheory("Phân số: phần còn lại",
            listOf(listOf(T("Đọc kĩ đề hỏi "), P("phần cho đi", ORANGE), T(" hay "), P("phần còn lại", GREEN), T("."))),
            emptyList(), s,
            listOf(
                step("Vẽ: chia $N thành $b phần bằng nhau, cho đi $a phần", visual = bars),
                step("Số $unit đã cho", T("$N : $b × $a = "), P(give, ORANGE)),
                step("Số $unit còn lại", T("$N − $give = "), P("$left $unit", GREEN))
            ), "Vậy $name còn lại $left $unit",
            quick = listOf(step("Cho đi $a/$b thì còn ${b - a}/$b", T("$N : $b × ${b - a} = "), P("$left $unit", GREEN))),
            quickRule = "Còn lại = 1 − phần đã cho = ${b - a}/$b.")
        return th to { o -> val x = num(o) ?: 0
            when (x) {
                give -> "$give $unit là số $name đã CHO đi. Đề hỏi số còn lại: $N − $give = $left."
                N - a -> "Con trừ thẳng $a, nhưng $a/$b là phân số của $N, phải tính $N : $b × $a trước."
                part -> "$part mới là một phần. Còn lại ${b - a} phần: $part × ${b - a} = $left."
                else -> "Còn lại ${b - a}/$b số $unit: $N : $b × ${b - a} = $left." } }
    }

    private fun ThingsOf(i: Long) = ThinkingWords.THINGS[i.toInt()]

    private fun fwhole(s: String, v: List<Long>): Built {
        val a = v[0]; val b = v[1]; val A = v[2]; val name = ThinkingWords.KIDS[v[3].toInt()]
        val part = A / a; val X = part * b
        val bars = Visual.Bars(listOf(BarRow("Số bi", List(b.toInt()) { i -> Seg(1f, if (i < a) BLUE else MUTED, if (i < a) "$part" else "?") })), "$a phần = $A viên")
        val th = KidTheory("Tìm một số khi biết phân số của nó",
            listOf(listOf(P("$a phần", BLUE), T(" là $A → tìm "), P("1 phần", ORANGE), T(" → tìm "), P("cả $b phần", GREEN), T("."))),
            emptyList(), s,
            listOf(
                step("Vẽ: số bi chia $b phần bằng nhau, $a phần là $A viên", visual = bars),
                step("Một phần là", T("$A : $a = "), P(part, ORANGE)),
                step("Cả $b phần là", T("$part × $b = "), P("$X viên", GREEN)),
                step("Thử lại", T("$a/$b của $X = $X : $b × $a = $A ✓"))
            ), "Vậy $name có tất cả $X viên bi",
            quick = listOf(step("Một dòng", T("$A : $a × $b = "), P("$X viên", GREEN))),
            quickRule = "Biết $a phần → chia $a được 1 phần → nhân $b được cả số.")
        return th to { o -> val x = num(o) ?: 0
            when (x) {
                part -> "$part mới là MỘT phần. Số bi có $b phần: $part × $b = $X."
                A * a -> "Con nhân với $a. $A đã là $a phần rồi, phải CHIA cho $a để được 1 phần."
                X - A -> "Đó là số bi của $b − $a = ${b - a} phần còn lại. Đề hỏi tất cả số bi."
                A -> "$A viên mới là $a/$b số bi, chưa phải tất cả. Tìm 1 phần rồi nhân $b."
                else -> "$A : $a = $part (1 phần), $part × $b = $X." } }
    }

    /* -------------------------------- Chuyển động đều -------------------------------- */
    private fun motion(s: String, v: List<Long>): Built {
        val form = v[0]
        return when (form) {
            0L -> { val vv = v[1]; val t = v[2]; val d = vv * t
                val who = if (s.contains("xe đạp")) "xe đạp" else "ô tô"
                val th = KidTheory("Tính quãng đường",
                    listOf(listOf(P("Quãng đường", GREEN), T(" = "), P("vận tốc", BLUE), T(" × "), P("thời gian", ORANGE), T("."))),
                    listOf(Tile("s = v × t", "", GREEN)), s,
                    listOf(step("Quãng đường = vận tốc × thời gian", T("$vv × $t = "), P("$d km", GREEN), note = "Mỗi giờ đi $vv km, $t giờ đi $t lần như thế.")),
                    "Vậy $who đi được $d km")
                th to { o -> val x = num(o) ?: 0
                    when (x) {
                        vv + t -> "Con đã cộng. Quãng đường = vận tốc × thời gian = $vv × $t = $d km."
                        else -> "Mỗi giờ đi $vv km, nên $t giờ đi $vv × $t = $d km." } }
            }
            1L -> { val vv = v[1]; val t10 = v[2]; val d = v[3]; val ans = "${dec(t10 * 10)} giờ"
                val half = t10 % 10 == 5L; val whole = t10 / 10
                val th = KidTheory("Tính thời gian",
                    listOf(listOf(P("Thời gian", ORANGE), T(" = "), P("quãng đường", GREEN), T(" : "), P("vận tốc", BLUE), T("."))),
                    listOf(Tile("t = s : v", "", ORANGE)), s,
                    listOf(step("Thời gian = quãng đường : vận tốc", T("$d : $vv = "), P(ans, GREEN))) + (if (half) listOf(step("Đổi ra giờ và phút", T("${dec(t10 * 10)} giờ = "), P("$whole giờ 30 phút", BLUE))) else emptyList()),
                    "Vậy xe máy đi hết quãng đường AB trong $ans",
                    quick = if (half) listOf(
                        step("$whole giờ đi được", T("$vv × $whole = "), P("${vv * whole} km", BLUE)),
                        step("Còn lại ${d - vv * whole} km = nửa của $vv km", T("thêm "), P("0,5 giờ", ORANGE), T(" → "), P(ans, GREEN))
                    ) else emptyList(), quickRule = if (half) "Đếm theo từng giờ, phần lẻ là nửa giờ." else null)
                th to { _ -> "Thời gian = quãng đường : vận tốc = $d : $vv = $ans." }
            }
            2L -> { val v1 = v[1]; val v2 = v[2]; val t = v[3]; val d = (v1 + v2) * t
                val th = KidTheory("Hai xe đi ngược chiều gặp nhau",
                    listOf(listOf(T("Mỗi giờ hai xe "), P("gần nhau thêm v1 + v2", BLUE), T(". Thời gian gặp nhau = "), P("quãng đường : tổng vận tốc", GREEN), T("."))),
                    emptyList(), s,
                    listOf(
                        step("Mỗi giờ hai xe gần nhau thêm", T("$v1 + $v2 = "), P("${v1 + v2} km", BLUE)),
                        step("Thời gian để gặp nhau", T("$d : ${v1 + v2} = "), P("$t giờ", GREEN))
                    ), "Vậy sau $t giờ hai xe gặp nhau",
                    quick = listOf(step("Một dòng", T("$d : ($v1 + $v2) = "), P("$t giờ", GREEN))),
                    quickRule = "Ngược chiều: cộng hai vận tốc.")
                th to { o -> val x = num(o) ?: 0
                    when {
                        d % v1 == 0L && x == d / v1 -> "Con chỉ dùng vận tốc ô tô. Hai xe cùng đi về phía nhau nên phải cộng hai vận tốc."
                        else -> "Mỗi giờ hai xe gần nhau $v1 + $v2 = ${v1 + v2} km, nên cần $d : ${v1 + v2} = $t giờ." } }
            }
            else -> { val v1 = v[1]; val v2 = v[2]; val t = v[3]; val d = (v1 - v2) * t
                val th = KidTheory("Ô tô đuổi kịp xe máy",
                    listOf(listOf(T("Cùng chiều: mỗi giờ khoảng cách "), P("ngắn lại v1 − v2", BLUE), T(". Thời gian đuổi kịp = "), P("khoảng cách : hiệu vận tốc", GREEN), T("."))),
                    emptyList(), s,
                    listOf(
                        step("Mỗi giờ ô tô gần xe máy thêm", T("$v1 − $v2 = "), P("${v1 - v2} km", BLUE)),
                        step("Thời gian để đuổi kịp", T("$d : ${v1 - v2} = "), P("$t giờ", GREEN))
                    ), "Vậy sau $t giờ ô tô đuổi kịp xe máy",
                    quick = listOf(step("Một dòng", T("$d : ($v1 − $v2) = "), P("$t giờ", GREEN))),
                    quickRule = "Cùng chiều: trừ hai vận tốc.")
                th to { o -> val x = num(o) ?: 0
                    when {
                        d % (v1 + v2) == 0L && x == d / (v1 + v2) -> "Hai xe đi CÙNG chiều nên lấy hiệu vận tốc ($v1 − $v2), không phải tổng."
                        else -> "Mỗi giờ ô tô gần thêm $v1 − $v2 = ${v1 - v2} km, nên cần $d : ${v1 - v2} = $t giờ." } }
            }
        }
    }
}
