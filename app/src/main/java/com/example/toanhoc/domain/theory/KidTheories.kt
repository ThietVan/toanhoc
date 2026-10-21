package com.example.toanhoc.domain.theory

import com.example.toanhoc.domain.model.AcademicProblem
import java.math.BigDecimal
import java.math.RoundingMode
import com.example.toanhoc.domain.theory.Hue.BLUE
import com.example.toanhoc.domain.theory.Hue.GREEN
import com.example.toanhoc.domain.theory.Hue.INK
import com.example.toanhoc.domain.theory.Hue.MUTED
import com.example.toanhoc.domain.theory.Hue.ORANGE
import com.example.toanhoc.domain.theory.Hue.PURPLE

/**
 * Dựng lý thuyết cho trẻ từ MỘT bài ví dụ (đã sinh sẵn, cùng dạng với câu con đang làm).
 * Mỗi bộ dựng tự giải lại ví dụ và chỉ trả về khi kết quả KHỚP đáp án của đề → không bao giờ dạy sai;
 * dạng chưa có bộ dựng (hoặc không khớp) trả về null → app dùng lý thuyết cũ.
 */
object KidTheories {

    fun build(problem: AcademicProblem): KidTheory? {
        val s = problem.statementVi.trim()
        val ans = problem.steps.firstOrNull()?.expectedAnswer?.trim() ?: return null
        // Lớp của dạng đề (G1_… → 1): để lời giảng đúng với kiến thức con đã học (lớp 1 chưa học nhân, chia…)
        GRADE.set(problem.nodeId.getOrNull(1)?.digitToIntOrNull() ?: 3)
        if (com.example.toanhoc.domain.thinking.ThinkingEngine.supports(problem.nodeId)) return runCatching { ThinkingTheories.build(problem) }.getOrNull()
        for (b in BUILDERS) {
            val r = runCatching { b(s, ans) }.getOrNull()
            if (r != null) return r
        }
        return null
    }

    private val GRADE = ThreadLocal.withInitial { 3 }
    private fun grade(): Int = GRADE.get()

    /* ======================================== tiện ích ======================================== */

    private fun T(s: String, h: Hue = INK) = Tok.T(s, h)
    private fun P(s: Any, h: Hue) = Tok.P(s.toString(), h)
    private fun same(a: String, b: String) = a.replace(" ", "").replace(" ", "").lowercase() == b.replace(" ", "").replace(" ", "").lowercase()

    /** Số kiểu Việt Nam: "37 440" → 37440. */
    private fun num(x: String): Long = x.replace(" ", "").replace(" ", "").toLong()
    private fun dec(x: String): BigDecimal = BigDecimal(x.replace(" ", "").replace(',', '.'))

    /** Viết số như bộ sinh đề: từ 10 000 trở lên tách nhóm 3 chữ số. */
    private fun f(n: Long): String {
        if (kotlin.math.abs(n) < 10000) return n.toString()
        val s = kotlin.math.abs(n).toString()
        val sb = StringBuilder()
        s.forEachIndexed { i, c -> if (i > 0 && (s.length - i) % 3 == 0) sb.append(' '); sb.append(c) }
        return (if (n < 0) "-" else "") + sb
    }
    private fun fd(b: BigDecimal): String {
        val st = b.stripTrailingZeros()
        val plain = if (st.scale() < 0) st.setScale(0).toPlainString() else st.toPlainString()
        return if (!plain.contains('.')) f(plain.toLong()) else plain.replace('.', ',')
    }
    private fun gcd(a: Long, b: Long): Long = if (b == 0L) kotlin.math.abs(a) else gcd(b, a % b)

    private val PLACES = listOf("đơn vị", "chục", "trăm", "nghìn", "chục nghìn", "trăm nghìn", "triệu", "chục triệu", "trăm triệu")
    private val NUMRE = Regex("""(?<![\p{L}\d])(?:\d{1,3}(?: \d{3})+|\d+(?:,\d+)?)(?![\p{L}\d])""")
    private fun nums(s: String) = NUMRE.findAll(s).map { it.value }.toList()

    private val BUILDERS: List<(String, String) -> KidTheory?> by lazy {
        listOf(
            ::roman, ::century, ::rounding, ::placeCompose, ::placeSplit, ::digitPlace, ::millionClass,
            ::unitConvert, ::fracSimplify, ::fracOp, ::fracOf, ::percentOf,
            ::geometry, ::motion, ::sumDiff, ::sumRatio, ::average, ::unitRate, ::twoStep, ::probability, ::angleFacts, ::mixedNumber, ::mapScale, ::divisibility, ::chickenDog,
            ::findUnknown, ::timesMoreLess, ::orderOps, ::letterExpr, ::commutative, ::distributive,
            ::midpoint, ::clock, ::remainderMax, ::divRemainder, ::facts, ::calendar, ::termNames, ::ruler,
            ::shapeFacts, ::shapePattern, ::leftMiddleRight, ::equalGroups, ::circleRadius, ::squareFromPerimeter, ::increase, ::decimalOps,
            ::wordNumber, ::sequence, ::tensToUnits, ::fillBlank, ::sortNums, ::compareTwo, ::unitAddSub, ::threeTerms, ::takeAway,
            ::make10, ::smallAddSub, ::mentalAddSub, ::tableMulDiv, ::columnAddSub, ::columnMul, ::longDivision,
            ::splitCombine, ::nextPrev, ::compareMax, ::statsMax, ::moreLess, ::combineTotal
        )
    }

    /* ==================================== số La Mã, thế kỷ ==================================== */

    private val ROMAN = listOf(1000 to "M", 900 to "CM", 500 to "D", 400 to "CD", 100 to "C", 90 to "XC", 50 to "L", 40 to "XL", 10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I")
    private fun toRoman(n: Int): String { var x = n; val sb = StringBuilder(); for ((v, r) in ROMAN) while (x >= v) { sb.append(r); x -= v }; return sb.toString() }
    private fun romanGroups(r: String): List<Pair<String, Int>>? {
        var i = 0; val out = ArrayList<Pair<String, Int>>()
        while (i < r.length) {
            val g = ROMAN.firstOrNull { (_, sym) -> r.startsWith(sym, i) } ?: return null
            out += g.second to g.first; i += g.second.length
        }
        return out
    }
    private val romanRule = listOf(
        listOf(T("I là 1, V là 5, X là 10.")),
        listOf(T("Chữ nhỏ đứng "), P("trước → trừ", PURPLE), T(", đứng "), P("sau → cộng", GREEN), T("."))
    )
    private val romanTiles = listOf(Tile("IV", "5 − 1 = 4", PURPLE), Tile("VI", "5 + 1 = 6", GREEN), Tile("IX", "10 − 1 = 9", PURPLE), Tile("XI", "10 + 1 = 11", GREEN))

    private fun roman(s: String, ans: String): KidTheory? {
        val read = Regex("""La Mã ([IVXLCDM]+) đọc""").find(s)
        if (read != null) {
            val r = read.groupValues[1]
            val groups = romanGroups(r) ?: return null
            val value = groups.sumOf { it.second }
            if (!same(value.toString(), ans)) return null
            val line = ArrayList<Tok>(); line += T("$r =", BLUE)
            groups.forEachIndexed { i, (sym, v) -> if (i > 0) line += T("+"); line += P("$sym ($v)", if (sym.length == 2) PURPLE else BLUE) }
            val sub = groups.firstOrNull { it.first.length == 2 }
            val note = sub?.let { (sym, v) -> "${sym[0]} đứng trước ${sym[1]} nên $sym = ${v + romanUnit(sym[0])} − ${romanUnit(sym[0])} = $v." }
            return KidTheory(
                "Chữ số La Mã", romanRule, romanTiles, s,
                if (groups.size == 1) listOf(TheoryStep("Đọc giá trị", line, note))
                else listOf(
                    TheoryStep("Tách thành từng cụm", line, note),
                    TheoryStep("Cộng các cụm lại", listOf(T(groups.joinToString(" + ") { it.second.toString() } + " = $value", BLUE)))
                ),
                "Vậy $r = $value"
            )
        }
        val write = Regex("""Số (\d+) được viết bằng chữ số La Mã""").find(s) ?: return null
        val n = write.groupValues[1].toInt()
        val r = toRoman(n)
        if (!same(r, ans)) return null
        val parts = ArrayList<Pair<Int, String>>(); var x = n
        for ((v, sym) in ROMAN) while (x >= v) { parts += v to sym; x -= v }
        val merged = parts.groupBy { it.second }.map { (sym, l) -> l.sumOf { it.first } to sym.repeat(l.size) }
        return KidTheory(
            "Chữ số La Mã", romanRule, romanTiles, s,
            listOf(
                TheoryStep("Tách số thành chục và đơn vị", listOf(T("$n =", BLUE)) + merged.flatMapIndexed { i, (v, _) -> (if (i > 0) listOf(T("+")) else emptyList()) + P(v, BLUE) }),
                TheoryStep("Viết từng phần bằng chữ La Mã", merged.flatMapIndexed { i, (v, sym) -> (if (i > 0) listOf(T("+")) else emptyList()) + P("$v → $sym", if (sym.length == 2 && ROMAN.any { it.second == sym }) PURPLE else GREEN) }),
                TheoryStep("Ghép lại từ trái sang phải", listOf(T(r, BLUE)))
            ),
            "Vậy $n viết là $r"
        )
    }
    private fun romanUnit(c: Char) = when (c) { 'I' -> 1; 'X' -> 10; 'C' -> 100; else -> 0 }

    private fun century(s: String, ans: String): KidTheory? {
        val m = Regex("""Năm (\d+) thuộc thế kỷ""").find(s) ?: return null
        val y = m.groupValues[1].toInt()
        val c = (y - 1) / 100 + 1
        val res = "Thế kỷ ${toRoman(c)}"
        if (!same(res, ans)) return null
        val from = (c - 1) * 100 + 1; val to = c * 100
        return KidTheory(
            "Thế kỷ", listOf(listOf(T("Một thế kỷ là "), P("100 năm", BLUE), T(".")), listOf(T("Năm 1 → năm 100 là thế kỷ I; năm 101 → năm 200 là thế kỷ II…"))),
            listOf(Tile("1801 → 1900", "thế kỷ XIX", BLUE), Tile("1901 → 2000", "thế kỷ XX", GREEN), Tile("2001 → 2100", "thế kỷ XXI", PURPLE)),
            s,
            listOf(
                TheoryStep("Tìm khoảng 100 năm chứa năm $y", listOf(P("$from", BLUE), T("→"), P("$to", BLUE)), "Năm $y nằm trong khoảng này."),
                TheoryStep("Đọc số thế kỷ", listOf(T("thế kỷ thứ $c → "), P(toRoman(c), GREEN)))
            ),
            "Vậy năm $y thuộc thế kỷ ${toRoman(c)}"
        )
    }

    /* ======================================== làm tròn ======================================== */

    private fun rounding(s: String, ans: String): KidTheory? {
        Regex("""Làm tròn số thập phân (\d+),(\d+) đến hàng đơn vị""").find(s)?.let { m ->
            val ip = m.groupValues[1].toLong(); val d = m.groupValues[2][0].digitToInt()
            val res = ip + if (d >= 5) 1 else 0
            if (!same(res.toString(), ans)) return null
            return KidTheory("Làm tròn số thập phân",
                listOf(listOf(T("Làm tròn đến hàng đơn vị: nhìn chữ số "), P("phần mười", ORANGE), T(" (ngay sau dấu phẩy).")), listOf(P("Bé hơn 5", PURPLE), T(" → giữ phần nguyên. "), P("Từ 5 trở lên", GREEN), T(" → thêm 1."))),
                listOf(Tile("3,4 → 3", "4 < 5", PURPLE), Tile("3,6 → 4", "6 ≥ 5", GREEN)), s,
                listOf(
                    TheoryStep("Tìm chữ số phần mười", listOf(T("$ip,"), P(d, ORANGE), T(m.groupValues[2].drop(1)))),
                    TheoryStep("So sánh với 5", listOf(T("$d ${if (d >= 5) "≥" else "<"} 5 → "), P(if (d >= 5) "thêm 1" else "giữ nguyên", if (d >= 5) GREEN else PURPLE))),
                    TheoryStep("Bỏ phần thập phân", listOf(P(res, BLUE)))
                ), "Vậy làm tròn được $res")
        }
        val m = Regex("""Làm tròn số (\d{1,3}(?: \d{3})+|\d+) đến hàng (chục nghìn|trăm nghìn|chục|trăm|nghìn)""").find(s) ?: return null
        val n = num(m.groupValues[1]); val place = m.groupValues[2]
        val k = PLACES.indexOf(place); if (k <= 0) return null
        val pow = Math.pow(10.0, k.toDouble()).toLong()
        val d = (n / (pow / 10)) % 10
        val res = (n / pow + if (d >= 5) 1 else 0) * pow
        if (!same(f(res), ans)) return null
        val right = PLACES[k - 1]
        val digits = f(n); val line = ArrayList<Tok>()
        var idxFromRight = 0
        val marks = digits.reversed().map { c -> if (c == ' ') null else idxFromRight++ }.reversed()
        digits.forEachIndexed { i, c ->
            val pos = marks[i]
            line += when (pos) { k - 1 -> P(c, ORANGE); k -> P(c, BLUE); else -> T(c.toString()) }
        }
        return KidTheory(
            "Làm tròn số",
            listOf(
                listOf(T("Nhìn chữ số "), P("ngay bên phải", ORANGE), T(" hàng cần làm tròn.")),
                listOf(P("Bé hơn 5", PURPLE), T(" → giữ nguyên. "), P("Từ 5 trở lên", GREEN), T(" → thêm 1.")),
                listOf(T("Các chữ số bên phải đều thành 0."))
            ),
            listOf(Tile("43 → 40", "3 < 5", PURPLE), Tile("47 → 50", "7 ≥ 5", GREEN)),
            s,
            listOf(
                TheoryStep("Tìm chữ số hàng $right", line, "Chữ số hàng $right là $d (tô cam), hàng $place tô xanh."),
                TheoryStep("So sánh với 5", listOf(T("$d"), T(if (d >= 5) "≥ 5" else "< 5"), P(if (d >= 5) "thêm 1 vào hàng $place" else "giữ nguyên hàng $place", if (d >= 5) GREEN else PURPLE))),
                TheoryStep("Viết 0 vào các hàng bên phải", listOf(T(f(res), BLUE)))
            ),
            "Vậy làm tròn ${f(n)} đến hàng $place được ${f(res)}"
        )
    }

    /* ===================================== cấu tạo số / hàng ===================================== */

    private val PLACE_RE = Regex("""(\d+) (trăm triệu|chục triệu|triệu|trăm nghìn|chục nghìn|nghìn|trăm|chục|đơn vị)""")

    private fun placeCompose(s: String, ans: String): KidTheory? {
        if (!s.startsWith("Số gồm")) return null
        val parts = PLACE_RE.findAll(s).map { it.groupValues[1].toLong() to PLACES.indexOf(it.groupValues[2]) }.toList()
        if (parts.isEmpty()) return null
        val top = parts.maxOf { it.second }
        val digits = (top downTo 0).map { p -> parts.firstOrNull { it.second == p }?.first ?: 0L }
        if (digits.any { it > 9 }) return null
        val value = digits.fold(0L) { acc, d -> acc * 10 + d }
        if (!same(f(value), ans)) return null
        val hues = listOf(GREEN, BLUE, PURPLE, ORANGE)
        val cells = digits.mapIndexed { i, d -> Tile(d.toString(), PLACES[top - i], hues[(top - i) % hues.size]) }
        return KidTheory(
            "Viết số theo hàng",
            listOf(listOf(T("Viết lần lượt từ "), P("hàng cao nhất", BLUE), T(" đến hàng đơn vị.")), listOf(T("Hàng nào không có thì viết "), P("0", ORANGE), T("."))),
            emptyList(), s,
            listOf(
                TheoryStep("Xếp từng chữ số vào đúng hàng", visual = Visual.Places(cells), note = if (digits.any { it == 0L }) "Hàng không được nhắc tới thì viết 0." else null),
                TheoryStep("Đọc các chữ số từ trái sang phải", listOf(T(f(value), BLUE)))
            ),
            "Vậy số đó là ${f(value)}"
        )
    }

    private fun placeSplit(s: String, ans: String): KidTheory? {
        val m = Regex("""Số (\d+) gồm mấy chục và mấy đơn vị""").find(s) ?: return null
        val n = m.groupValues[1].toInt(); if (n !in 10..99) return null
        val res = "${n / 10} chục và ${n % 10} đơn vị"
        if (!same(res, ans)) return null
        return KidTheory(
            "Chục và đơn vị", listOf(listOf(T("Số có hai chữ số: chữ số bên trái là "), P("chục", BLUE), T(", bên phải là "), P("đơn vị", GREEN), T("."))),
            listOf(Tile("35", "3 chục, 5 đơn vị", BLUE)), s,
            listOf(TheoryStep("Tách số $n theo hàng", visual = Visual.Places(listOf(Tile("${n / 10}", "chục", BLUE), Tile("${n % 10}", "đơn vị", GREEN))))),
            "Vậy $n gồm $res"
        )
    }

    private fun digitPlace(s: String, ans: String): KidTheory? {
        val m = Regex("""Trong số thập phân (\d+),(\d+), chữ số (\d) thuộc hàng nào""").find(s) ?: return null
        val ip = m.groupValues[1]; val fp = m.groupValues[2]; val d = m.groupValues[3][0]
        val decNames = listOf("phần mười", "phần trăm", "phần nghìn")
        val cells = ArrayList<Tile>()
        ip.forEachIndexed { i, c -> cells += Tile(c.toString(), PLACES[ip.length - 1 - i], BLUE) }
        fp.forEachIndexed { i, c -> cells += Tile(c.toString(), decNames.getOrElse(i) { "" }, GREEN) }
        val hit = cells.firstOrNull { it.top == d.toString() && it.hue == GREEN } ?: cells.firstOrNull { it.top == d.toString() } ?: return null
        val res = "Hàng ${hit.bottom}"
        if (!same(res, ans)) return null
        return KidTheory(
            "Hàng của số thập phân",
            listOf(listOf(T("Bên trái dấu phẩy là "), P("phần nguyên", BLUE), T(", bên phải là "), P("phần thập phân", GREEN), T(".")), listOf(T("Sau dấu phẩy lần lượt là hàng phần mười, phần trăm, phần nghìn."))),
            listOf(Tile("2,57", "5: phần mười · 7: phần trăm", GREEN)), s,
            listOf(TheoryStep("Viết từng chữ số vào bảng hàng", visual = Visual.Places(cells), note = "Chữ số $d nằm ở hàng ${hit.bottom}.")),
            "Vậy chữ số $d thuộc hàng ${hit.bottom}"
        )
    }

    private fun millionClass(s: String, ans: String): KidTheory? {
        val m1 = Regex("""Trong số (\d{1,3}(?: \d{3})+), chữ số nào thuộc hàng (trăm triệu|chục triệu|triệu|trăm nghìn|chục nghìn|nghìn|trăm|chục|đơn vị)""").find(s)
        if (m1 != null) {
            val digits = m1.groupValues[1].replace(" ", ""); val place = m1.groupValues[2]
            val k = PLACES.indexOf(place); if (k < 0 || k >= digits.length) return null
            val d = digits[digits.length - 1 - k].toString()
            if (!same(d, ans)) return null
            val cells = digits.mapIndexed { i, c -> val p = digits.length - 1 - i; Tile(c.toString(), PLACES[p], if (p == k) ORANGE else if (p >= 6) PURPLE else if (p >= 3) BLUE else GREEN) }
            return KidTheory(
                "Hàng và lớp",
                listOf(listOf(T("Đếm từ phải sang trái, cứ 3 hàng là một lớp: "), P("lớp đơn vị", GREEN), T(", "), P("lớp nghìn", BLUE), T(", "), P("lớp triệu", PURPLE), T("."))),
                emptyList(), s,
                listOf(TheoryStep("Viết từng chữ số vào bảng hàng", visual = Visual.Places(cells), note = "Chữ số ở hàng $place được tô cam.")),
                "Vậy chữ số hàng $place là $d"
            )
        }
        val m2 = Regex("""Lớp triệu của số (\d{1,3}(?: \d{3}){2})""").find(s) ?: return null
        val million = m2.groupValues[1].split(" ")[0]
        if (!same("$million triệu", ans)) return null
        return KidTheory(
            "Hàng và lớp",
            listOf(listOf(T("Tách số thành từng nhóm 3 chữ số từ phải sang trái. Nhóm thứ ba là "), P("lớp triệu", PURPLE), T("."))),
            emptyList(), s,
            listOf(TheoryStep("Tách thành các lớp", m2.groupValues[1].split(" ").mapIndexed { i, g -> P(g, listOf(PURPLE, BLUE, GREEN)[i]) }, "Lớp triệu (tím) – lớp nghìn (xanh) – lớp đơn vị (xanh lá).")),
            "Vậy lớp triệu có giá trị $million triệu"
        )
    }

    /* ======================================== đổi đơn vị ======================================== */

    private val UNITS: Map<String, Pair<String, BigDecimal>> = mapOf(
        "mm" to ("len" to BigDecimal("1")), "cm" to ("len" to BigDecimal("10")), "dm" to ("len" to BigDecimal("100")),
        "m" to ("len" to BigDecimal("1000")), "km" to ("len" to BigDecimal("1000000")),
        "g" to ("mass" to BigDecimal("1")), "kg" to ("mass" to BigDecimal("1000")), "yến" to ("mass" to BigDecimal("10000")),
        "tạ" to ("mass" to BigDecimal("100000")), "tấn" to ("mass" to BigDecimal("1000000")),
        "mm²" to ("area" to BigDecimal("1")), "cm²" to ("area" to BigDecimal("100")), "dm²" to ("area" to BigDecimal("10000")),
        "m²" to ("area" to BigDecimal("1000000")), "ha" to ("area" to BigDecimal("10000000000")), "km²" to ("area" to BigDecimal("1000000000000")),
        "cm³" to ("vol" to BigDecimal("1")), "dm³" to ("vol" to BigDecimal("1000")), "m³" to ("vol" to BigDecimal("1000000")),
        "ml" to ("cap" to BigDecimal("1")), "l" to ("cap" to BigDecimal("1000"))
    )

    private fun unitConvert(s: String, ans: String): KidTheory? {
        val m = Regex("""(\d[\d ]*(?:,\d+)?)\s*(mm²|cm²|dm²|m²|km²|ha|cm³|dm³|m³|mm|cm|dm|km|m|kg|g|yến|tạ|tấn|ml|l)\s*=\s*\.\.\.\s*(mm²|cm²|dm²|m²|km²|ha|cm³|dm³|m³|mm|cm|dm|km|m|kg|g|yến|tạ|tấn|ml|l)(?![\p{L}\d²³])""").find(s) ?: return null
        val a = dec(m.groupValues[1]); val u1 = m.groupValues[2]; val u2 = m.groupValues[3]
        val (k1, v1) = UNITS[u1] ?: return null; val (k2, v2) = UNITS[u2] ?: return null
        if (k1 != k2) return null
        val bigToSmall = v1 >= v2
        val factor = if (bigToSmall) v1.divide(v2) else v2.divide(v1)
        val res = if (bigToSmall) a.multiply(factor) else a.divide(factor, 6, RoundingMode.HALF_UP)
        if (!same("${fd(res)} $u2", ans) && !same(fd(res), ans)) return null
        val big = if (bigToSmall) u1 else u2; val small = if (bigToSmall) u2 else u1
        return KidTheory(
            "Đổi đơn vị đo",
            listOf(
                listOf(T("Đổi từ đơn vị "), P("lớn → bé", GREEN), T(": nhân."), T(" Đổi từ "), P("bé → lớn", PURPLE), T(": chia.")),
                listOf(T("Nhớ: 1 $big = ${fd(factor)} $small."))
            ),
            listOf(Tile("1 $big", "= ${fd(factor)} $small", BLUE)), s,
            listOf(
                TheoryStep("Nhớ mối liên hệ", listOf(P("1 $big = ${fd(factor)} $small", BLUE))),
                TheoryStep(if (bigToSmall) "Lớn đổi ra bé → nhân" else "Bé đổi ra lớn → chia",
                    listOf(T("${fd(a)} ${if (bigToSmall) "×" else ":"} ${fd(factor)} = "), P(fd(res), if (bigToSmall) GREEN else PURPLE)))
            ),
            "Vậy ${fd(a)} $u1 = ${fd(res)} $u2"
        )
    }

    /* ======================================== phân số ======================================== */

    private data class Fr(val n: Long, val d: Long) { override fun toString() = if (d == 1L) "$n" else "$n/$d"; fun red(): Fr { val g = gcd(n, d); return Fr(n / g, d / g) } }

    private fun fracSimplify(s: String, ans: String): KidTheory? {
        val m = Regex("""Rút gọn phân số (\d+)/(\d+)""").find(s) ?: return null
        val a = m.groupValues[1].toLong(); val b = m.groupValues[2].toLong()
        val g = gcd(a, b); val r = Fr(a / g, b / g)
        if (!same(r.toString(), ans)) return null
        return KidTheory(
            "Rút gọn phân số",
            listOf(listOf(T("Chia cả "), P("tử số", BLUE), T(" và "), P("mẫu số", GREEN), T(" cho cùng một số lớn nhất có thể."))),
            listOf(Tile("4/6", "= 2/3 (chia 2)", BLUE), Tile("5/10", "= 1/2 (chia 5)", GREEN)), s,
            listOf(
                TheoryStep("Tìm số chia hết cả tử và mẫu", listOf(T("$a và $b cùng chia hết cho "), P(g, ORANGE))),
                TheoryStep("Chia cả tử và mẫu cho $g", listOf(P("$a : $g = ${a / g}", BLUE), P("$b : $g = ${b / g}", GREEN)))
            ),
            "Vậy $a/$b = $r"
        )
    }

    private fun fracOp(s: String, ans: String): KidTheory? {
        val m = Regex("""(\d+)/(\d+)\s*([+\-×:])\s*(\d+)/(\d+)\s*=\s*\?""").find(s) ?: return null
        val a = Fr(m.groupValues[1].toLong(), m.groupValues[2].toLong()); val op = m.groupValues[3]
        val b = Fr(m.groupValues[4].toLong(), m.groupValues[5].toLong())
        val steps = ArrayList<TheoryStep>(); val rule: List<List<Tok>>; val tiles: List<Tile>; var raw: Fr
        when (op) {
            "+", "-" -> {
                val sign = if (op == "+") "+" else "−"
                if (a.d == b.d) {
                    raw = Fr(if (op == "+") a.n + b.n else a.n - b.n, a.d)
                    steps += TheoryStep("Cùng mẫu: giữ mẫu, ${if (op == "+") "cộng" else "trừ"} tử", listOf(T("$a $sign $b = "), P("(${a.n} $sign ${b.n})/${a.d}", BLUE), T(" = $raw")))
                } else {
                    val l = a.d / gcd(a.d, b.d) * b.d
                    val a2 = Fr(a.n * (l / a.d), l); val b2 = Fr(b.n * (l / b.d), l)
                    steps += TheoryStep("Quy đồng mẫu số (mẫu chung $l)", listOf(P("$a = $a2", BLUE), P("$b = $b2", GREEN)), "Nhân cả tử và mẫu với cùng một số để được mẫu $l.")
                    raw = Fr(if (op == "+") a2.n + b2.n else a2.n - b2.n, l)
                    steps += TheoryStep("${if (op == "+") "Cộng" else "Trừ"} hai tử, giữ mẫu", listOf(T("$a2 $sign $b2 = "), P(raw, ORANGE)))
                }
                rule = listOf(listOf(T("Hai phân số "), P("cùng mẫu", BLUE), T(": giữ mẫu, cộng/trừ tử.")), listOf(T("Khác mẫu: "), P("quy đồng", ORANGE), T(" trước rồi mới cộng/trừ.")))
                tiles = listOf(Tile("1/5 + 2/5", "= 3/5", BLUE), Tile("1/2 + 1/4", "= 2/4 + 1/4", ORANGE))
            }
            "×" -> {
                raw = Fr(a.n * b.n, a.d * b.d)
                steps += TheoryStep("Tử nhân tử, mẫu nhân mẫu", listOf(T("$a × $b = "), P("(${a.n} × ${b.n})/(${a.d} × ${b.d})", BLUE), T(" = $raw")))
                rule = listOf(listOf(P("Tử nhân tử", BLUE), T(", "), P("mẫu nhân mẫu", GREEN), T(".")))
                tiles = listOf(Tile("2/3 × 1/5", "= 2/15", BLUE))
            }
            else -> {
                val inv = Fr(b.d, b.n)
                raw = Fr(a.n * inv.n, a.d * inv.d)
                steps += TheoryStep("Đảo ngược phân số thứ hai", listOf(P("$b → $inv", ORANGE)))
                steps += TheoryStep("Nhân với phân số đảo ngược", listOf(T("$a × $inv = "), P(raw, BLUE)))
                rule = listOf(listOf(T("Chia cho một phân số = "), P("nhân với phân số đảo ngược", ORANGE), T(".")))
                tiles = listOf(Tile("1/2 : 1/3", "= 1/2 × 3/1", ORANGE))
            }
        }
        val red = raw.red()
        val final = when {
            same(raw.toString(), ans) -> raw
            same(red.toString(), ans) -> { if (red != raw) steps += TheoryStep("Rút gọn", listOf(T("$raw = "), P(red, GREEN))); red }
            else -> return null
        }
        return KidTheory("Tính với phân số", rule, tiles, s, steps, "Vậy kết quả là $final")
    }

    private fun fracOf(s: String, ans: String): KidTheory? {
        val m = Regex("""Tìm (\d+)/(\d+) của (\d+)""").find(s)
            ?: Regex("""(\d+)/(\d+) số [^\d]*?trong (\d+)""").find(s) ?: return null
        val a = m.groupValues[1].toLong(); val b = m.groupValues[2].toLong(); val n = m.groupValues[3].toLong()
        if (n % b != 0L) return null
        val one = n / b; val res = one * a
        if (!same(res.toString(), ans.substringBefore(" "))) return null
        return KidTheory(
            "Tìm phân số của một số",
            listOf(listOf(T("Muốn tìm "), P("a/b", BLUE), T(" của một số: lấy số đó "), P("chia b", PURPLE), T(" rồi "), P("nhân a", GREEN), T("."))),
            listOf(Tile("1/3 của 12", "12 : 3 = 4", PURPLE), Tile("2/3 của 12", "4 × 2 = 8", GREEN)), s,
            listOf(
                TheoryStep("Chia thành $b phần bằng nhau", listOf(T("$n : $b = "), P(one, PURPLE)), "Mỗi phần là $one."),
                TheoryStep("Lấy $a phần", listOf(T("$one × $a = "), P(res, GREEN)))
            ),
            "Vậy $a/$b của $n là $res"
        )
    }

    private fun percentOf(s: String, ans: String): KidTheory? {
        val m = Regex("""Tìm (\d+(?:,\d+)?)% của (\d[\d ]*(?:,\d+)?)""").find(s) ?: return null
        val p = dec(m.groupValues[1]); val n = dec(m.groupValues[2])
        val one = n.divide(BigDecimal(100), 6, RoundingMode.HALF_UP); val res = one.multiply(p)
        if (!same(fd(res), ans.substringBefore(" ")) && !same(fd(res), ans)) return null
        return KidTheory(
            "Tìm tỉ số phần trăm của một số",
            listOf(listOf(T("Muốn tìm "), P("p%", BLUE), T(" của một số: lấy số đó "), P("chia 100", PURPLE), T(" rồi "), P("nhân p", GREEN), T("."))),
            listOf(Tile("10% của 200", "200 : 100 × 10 = 20", BLUE)), s,
            listOf(
                TheoryStep("Tìm 1%", listOf(T("${fd(n)} : 100 = "), P(fd(one), PURPLE))),
                TheoryStep("Tìm ${fd(p)}%", listOf(T("${fd(one)} × ${fd(p)} = "), P(fd(res), GREEN)))
            ),
            "Vậy ${fd(p)}% của ${fd(n)} là ${fd(res)}"
        )
    }

    /* ======================================== hình học ======================================== */

    private class Formula(val key: Regex, val name: String, val formula: String, val unitPow: Int, val compute: (List<BigDecimal>) -> Pair<String, BigDecimal>?)

    private val FORMULAS = listOf(
        Formula(Regex("diện tích hình bình hành"), "Diện tích hình bình hành", "đáy × chiều cao", 2) { v -> if (v.size < 2) null else "${fd(v[0])} × ${fd(v[1])}" to v[0] * v[1] },
        Formula(Regex("diện tích hình tam giác"), "Diện tích hình tam giác", "đáy × chiều cao : 2", 2) { v -> if (v.size < 2) null else "${fd(v[0])} × ${fd(v[1])} : 2" to (v[0] * v[1]).divide(BigDecimal(2)) },
        Formula(Regex("diện tích hình thang"), "Diện tích hình thang", "(đáy lớn + đáy bé) × chiều cao : 2", 2) { v -> if (v.size < 3) null else "(${fd(v[0])} + ${fd(v[1])}) × ${fd(v[2])} : 2" to ((v[0] + v[1]) * v[2]).divide(BigDecimal(2)) },
        Formula(Regex("diện tích hình thoi"), "Diện tích hình thoi", "đường chéo × đường chéo : 2", 2) { v -> if (v.size < 2) null else "${fd(v[0])} × ${fd(v[1])} : 2" to (v[0] * v[1]).divide(BigDecimal(2)) },
        Formula(Regex("diện tích hình chữ nhật"), "Diện tích hình chữ nhật", "chiều dài × chiều rộng", 2) { v -> if (v.size < 2) null else "${fd(v[0])} × ${fd(v[1])}" to v[0] * v[1] },
        Formula(Regex("diện tích hình vuông có độ dài cạnh"), "Diện tích hình vuông", "cạnh × cạnh", 2) { v -> if (v.isEmpty()) null else "${fd(v[0])} × ${fd(v[0])}" to v[0] * v[0] },
        Formula(Regex("chu vi hình vuông"), "Chu vi hình vuông", "cạnh × 4", 1) { v -> if (v.isEmpty()) null else "${fd(v[0])} × 4" to v[0] * BigDecimal(4) },
        Formula(Regex("chu vi hình chữ nhật"), "Chu vi hình chữ nhật", "(chiều dài + chiều rộng) × 2", 1) { v -> if (v.size < 2) null else "(${fd(v[0])} + ${fd(v[1])}) × 2" to (v[0] + v[1]) * BigDecimal(2) },
        Formula(Regex("thể tích hình hộp chữ nhật"), "Thể tích hình hộp chữ nhật", "dài × rộng × cao", 3) { v -> if (v.size < 3) null else "${fd(v[0])} × ${fd(v[1])} × ${fd(v[2])}" to v[0] * v[1] * v[2] },
        Formula(Regex("thể tích hình lập phương"), "Thể tích hình lập phương", "cạnh × cạnh × cạnh", 3) { v -> if (v.isEmpty()) null else "${fd(v[0])} × ${fd(v[0])} × ${fd(v[0])}" to v[0] * v[0] * v[0] },
        Formula(Regex("chu vi hình tứ giác"), "Chu vi hình tứ giác", "tổng độ dài 4 cạnh", 1) { v -> if (v.size < 4) null else v.joinToString(" + ") { fd(it) } to v.fold(BigDecimal.ZERO) { x, y -> x + y } },
        Formula(Regex("chu vi hình tam giác"), "Chu vi hình tam giác", "tổng độ dài 3 cạnh", 1) { v -> if (v.size < 3) null else v.joinToString(" + ") { fd(it) } to v.fold(BigDecimal.ZERO) { x, y -> x + y } },
        Formula(Regex("đường gấp khúc"), "Độ dài đường gấp khúc", "tổng độ dài các đoạn thẳng", 1) { v -> if (v.size < 3) null else v.drop(1).joinToString(" + ") { fd(it) } to v.drop(1).fold(BigDecimal.ZERO) { x, y -> x + y } }
    )

    private fun geometry(s: String, ans: String): KidTheory? {
        val low = s.lowercase()
        val fm = FORMULAS.firstOrNull { it.key.containsMatchIn(low) } ?: return null
        val found = Regex("""(\d[\d ]*(?:,\d+)?)\s*(mm|cm|dm|km|m)\b""").findAll(s).map { dec(it.groupValues[1]) to it.groupValues[2] }.toList()
        val units = found.map { it.second }.toSet()
        val unit = units.minByOrNull { UNITS[it]?.second ?: BigDecimal.ONE } ?: "cm"
        val pre = ArrayList<TheoryStep>()
        val values = found.map { (v, u) ->
            if (u == unit) v else {
                val k = (UNITS[u]?.second ?: return null).divide(UNITS[unit]?.second ?: return null)
                pre += TheoryStep("Đổi về cùng đơn vị $unit", listOf(T("${fd(v)} $u = "), P("${fd(v * k)} $unit", ORANGE)), "Phải cùng đơn vị mới tính được.")
                v * k
            }
        }
        val all = if (fm.name.startsWith("Độ dài đường")) listOf(BigDecimal.ZERO) + values else values
        val (expr, res) = fm.compute(all) ?: return null
        val u = unit + when (fm.unitPow) { 2 -> "²"; 3 -> "³"; else -> "" }
        if (!same("${fd(res)} $u", ans)) return null
        return KidTheory(
            fm.name,
            listOf(listOf(T("${fm.name} = "), P(fm.formula, BLUE)), listOf(T("Nhớ ghi đơn vị: "), P(u, ORANGE), T("."))),
            emptyList(), s,
            pre + listOf(
                TheoryStep("Nhớ công thức", listOf(P(fm.formula, BLUE))),
                TheoryStep("Thay số vào", listOf(T(expr, BLUE))),
                TheoryStep("Tính và ghi đơn vị", listOf(T("$expr = "), P("${fd(res)} $u", GREEN)))
            ),
            "Vậy ${fm.name.replaceFirstChar { it.lowercase() }} là ${fd(res)} $u"
        )
    }

    private fun motion(s: String, ans: String): KidTheory? {
        val rule = listOf(
            listOf(P("Quãng đường = vận tốc × thời gian", BLUE)),
            listOf(P("Vận tốc = quãng đường : thời gian", GREEN)),
            listOf(P("Thời gian = quãng đường : vận tốc", PURPLE))
        )
        Regex("""quãng đường (\d[\d ]*(?:,\d+)?) km trong (\d+(?:,\d+)?) giờ\. Vận tốc""").find(s)?.let { m ->
            val d = dec(m.groupValues[1]); val t = dec(m.groupValues[2]); val v = d.divide(t, 6, RoundingMode.HALF_UP)
            if (!same("${fd(v)} km/giờ", ans)) return null
            return KidTheory("Vận tốc", rule, emptyList(), s, listOf(
                TheoryStep("Chọn công thức", listOf(P("vận tốc = quãng đường : thời gian", GREEN))),
                TheoryStep("Thay số và tính", listOf(T("${fd(d)} : ${fd(t)} = "), P("${fd(v)} km/giờ", GREEN)))
            ), "Vậy vận tốc là ${fd(v)} km/giờ")
        }
        Regex("""vận tốc (\d+(?:,\d+)?) km/giờ trong (\d+(?:,\d+)?) giờ\. Quãng đường""").find(s)?.let { m ->
            val v = dec(m.groupValues[1]); val t = dec(m.groupValues[2]); val d = v * t
            if (!same("${fd(d)} km", ans)) return null
            return KidTheory("Quãng đường", rule, emptyList(), s, listOf(
                TheoryStep("Chọn công thức", listOf(P("quãng đường = vận tốc × thời gian", BLUE))),
                TheoryStep("Thay số và tính", listOf(T("${fd(v)} × ${fd(t)} = "), P("${fd(d)} km", BLUE)))
            ), "Vậy quãng đường là ${fd(d)} km")
        }
        return null
    }

    /* ===================================== toán có lời văn ===================================== */

    private fun sumDiff(s: String, ans: String): KidTheory? {
        val m = Regex("""tổng bằng (\d+) và hiệu bằng (\d+)\. Số (lớn|bé) là""").find(s) ?: return null
        val t = m.groupValues[1].toLong(); val h = m.groupValues[2].toLong(); val big = m.groupValues[3] == "lớn"
        val res = if (big) (t + h) / 2 else (t - h) / 2
        if ((t + h) % 2 != 0L || !same(res.toString(), ans)) return null
        val small = (t - h) / 2
        return KidTheory(
            "Tìm hai số khi biết Tổng và Hiệu",
            listOf(listOf(P("Tổng", BLUE), T(" là cả hai số cộng lại. "), P("Hiệu", ORANGE), T(" là phần số lớn hơn số bé."))),
            listOf(Tile("Số lớn", "(Tổng + Hiệu) : 2", BLUE), Tile("Số bé", "(Tổng − Hiệu) : 2", GREEN)), s,
            listOf(
                TheoryStep("Vẽ sơ đồ đoạn thẳng", visual = Visual.Bars(listOf(
                    BarRow("Số lớn", listOf(Seg(small.toFloat(), BLUE), Seg(h.toFloat(), ORANGE, "$h"))),
                    BarRow("Số bé", listOf(Seg(small.toFloat(), BLUE)))
                ), "$t"), note = "Phần cam là hiệu: số lớn hơn số bé $h."),
                TheoryStep("Dùng công thức số ${m.groupValues[3]}", listOf(T("("), P(t, BLUE), T(if (big) "+" else "−"), P(h, ORANGE), T(") : 2 = "), P(res, GREEN))),
                TheoryStep("Thử lại cho chắc", note = "${small + h} + $small = $t ✓  ·  ${small + h} − $small = $h ✓")
            ),
            "Vậy số ${m.groupValues[3]} là $res"
        )
    }

    private fun sumRatio(s: String, ans: String): KidTheory? {
        val m = Regex("""có (tổng|hiệu) bằng (\d+) và tỉ số là (\d+)/(\d+)\. Số (lớn|bé) là""").find(s) ?: return null
        val isSum = m.groupValues[1] == "tổng"; val total = m.groupValues[2].toLong()
        var a = m.groupValues[3].toLong(); var b = m.groupValues[4].toLong(); val big = m.groupValues[5] == "lớn"
        if (a > b) { val x = a; a = b; b = x }
        val parts = if (isSum) a + b else b - a
        if (parts <= 0 || total % parts != 0L) return null
        val one = total / parts
        val res = one * (if (big) b else a)
        if (!same(res.toString(), ans)) return null
        return KidTheory(
            if (isSum) "Tìm hai số khi biết Tổng và Tỉ số" else "Tìm hai số khi biết Hiệu và Tỉ số",
            listOf(listOf(T("Vẽ số bé "), P("$a phần", BLUE), T(", số lớn "), P("$b phần", GREEN), T(" bằng nhau.")), listOf(T(if (isSum) "Tìm tổng số phần → giá trị 1 phần." else "Tìm hiệu số phần → giá trị 1 phần."))),
            emptyList(), s,
            listOf(
                TheoryStep("Vẽ sơ đồ", visual = Visual.Bars(listOf(
                    BarRow("Số bé", List(a.toInt()) { Seg(1f, BLUE) }),
                    BarRow("Số lớn", List(b.toInt()) { Seg(1f, GREEN) })
                ), if (isSum) "$total" else null), note = if (isSum) null else "Số lớn hơn số bé ${b - a} phần, ứng với $total."),
                TheoryStep(if (isSum) "Tổng số phần bằng nhau" else "Hiệu số phần bằng nhau", listOf(T(if (isSum) "$a + $b = " else "$b − $a = "), P(parts, ORANGE))),
                TheoryStep("Giá trị 1 phần", listOf(T("$total : $parts = "), P(one, ORANGE))),
                TheoryStep("Số ${m.groupValues[5]}", listOf(T("$one × ${if (big) b else a} = "), P(res, GREEN)))
            ),
            "Vậy số ${m.groupValues[5]} là $res"
        )
    }

    private fun average(s: String, ans: String): KidTheory? {
        val m = Regex("""trung bình cộng của các số: ([\d, ]+)""").find(s) ?: return null
        val xs = m.groupValues[1].split(",").map { it.trim() }.filter { it.isNotEmpty() }.map { it.toLong() }
        if (xs.size < 2) return null
        val sum = xs.sum(); if (sum % xs.size != 0L) return null
        val res = sum / xs.size
        if (!same(res.toString(), ans)) return null
        return KidTheory(
            "Số trung bình cộng",
            listOf(listOf(T("Muốn tìm trung bình cộng: "), P("cộng tất cả các số", BLUE), T(" rồi "), P("chia cho số các số hạng", GREEN), T("."))),
            listOf(Tile("TBC của 2 và 4", "(2 + 4) : 2 = 3", BLUE)), s,
            listOf(
                TheoryStep("Cộng tất cả các số", listOf(T(xs.joinToString(" + ") + " = "), P(sum, BLUE))),
                TheoryStep("Chia cho ${xs.size} (có ${xs.size} số)", listOf(T("$sum : ${xs.size} = "), P(res, GREEN)))
            ),
            "Vậy trung bình cộng là $res"
        )
    }

    private fun unitRate(s: String, ans: String): KidTheory? {
        val m = Regex("""Mua (\d+) (.+?) hết (\d[\d ]*) đồng\. Hỏi mua (\d+) """).find(s) ?: return null
        val n1 = m.groupValues[1].toLong(); val item = m.groupValues[2]; val total = num(m.groupValues[3]); val n2 = m.groupValues[4].toLong()
        if (total % n1 != 0L) return null
        val one = total / n1; val res = one * n2
        if (!same("${f(res)} đồng", ans)) return null
        return KidTheory(
            "Bài toán rút về đơn vị",
            listOf(listOf(T("Bước 1: tìm giá trị "), P("1 phần", ORANGE), T(" (chia).")), listOf(T("Bước 2: tìm giá trị "), P("nhiều phần", GREEN), T(" (nhân)."))),
            emptyList(), s,
            listOf(
                TheoryStep("Tìm giá 1 $item", listOf(T("${f(total)} : $n1 = "), P("${f(one)} đồng", ORANGE))),
                TheoryStep("Tìm giá $n2 $item", listOf(T("${f(one)} × $n2 = "), P("${f(res)} đồng", GREEN)))
            ),
            "Vậy mua $n2 $item hết ${f(res)} đồng"
        )
    }

    private fun moreLess(s: String, ans: String): KidTheory? {
        val ns = nums(s).filter { !it.contains(',') }.map { num(it) }
        if (ns.size != 2 || !Regex("hơn").containsMatchIn(s)) return null
        val (a, b) = ns
        val ansN = Regex("""\d[\d ]*""").find(ans)?.value?.let { num(it.trim()) } ?: return null
        val add = a + b == ansN; val sub = a - b == ansN
        if (!add && !sub) return null
        val unit = ans.substringAfter(Regex("""\d[\d ]*""").find(ans)!!.value, "").trim()
        // Câu hỏi (phần sau "Hỏi") có "hơn/kém … bao nhiêu" → dạng tìm phần hơn
        val question = s.substringAfterLast("Hỏi", s)
        val compare = sub && (question.contains("hơn") || question.contains("kém"))
        val title: String; val why: String
        when {
            compare -> { title = "Tìm phần hơn: lấy số lớn trừ số bé"; why = "Hỏi \"hơn bao nhiêu\" → lấy số lớn trừ số bé." }
            add -> { title = "Nhiều hơn → làm phép cộng"; why = "Số cần tìm nhiều hơn $a là $b." }
            s.contains("ít hơn") || s.contains("kém") -> { title = "Ít hơn → làm phép trừ"; why = "Số cần tìm ít hơn $a là $b." }
            else -> { title = "Ít hơn → làm phép trừ"; why = "$a nhiều hơn số cần tìm $b, nghĩa là số cần tìm ít hơn $a là $b." }
        }
        val res = if (add) a + b else a - b
        val bigV = maxOf(a, res); val smallV = minOf(a, res)
        return KidTheory(
            "Bài toán nhiều hơn, ít hơn",
            listOf(listOf(P("Nhiều hơn", GREEN), T(" → cộng. "), P("Ít hơn", PURPLE), T(" → trừ.")), listOf(T("Hỏi "), P("hơn bao nhiêu", ORANGE), T(" → lấy số lớn trừ số bé."))),
            emptyList(), s,
            listOf(
                TheoryStep("Vẽ sơ đồ", visual = Visual.Bars(listOf(
                    BarRow("", listOf(Seg(smallV.toFloat(), BLUE), Seg((bigV - smallV).toFloat(), ORANGE, "${bigV - smallV}"))),
                    BarRow("", listOf(Seg(smallV.toFloat(), BLUE)))
                )), note = why),
                TheoryStep(title, listOf(T(if (add) "$a + $b = " else "$a − $b = "), P("$res${if (unit.isNotEmpty()) " $unit" else ""}", if (add) GREEN else PURPLE)))
            ),
            "Vậy đáp số là $res${if (unit.isNotEmpty()) " $unit" else ""}"
        )
    }

    private fun combineTotal(s: String, ans: String): KidTheory? {
        if (!Regex("Cả hai|tất cả").containsMatchIn(s)) return null
        val ns = nums(s).filter { !it.contains(',') }.map { num(it) }
        if (ns.size != 2) return null
        val ansN = Regex("""\d[\d ]*""").find(ans)?.value?.let { num(it.trim()) } ?: return null
        val res = ns[0] + ns[1]; if (res != ansN) return null
        return KidTheory(
            "Bài toán tìm tổng",
            listOf(listOf(T("Hỏi "), P("cả hai / tất cả", GREEN), T(" → gộp lại → làm phép cộng."))),
            emptyList(), s,
            listOf(
                TheoryStep("Vẽ sơ đồ", visual = Visual.Bars(listOf(BarRow("", listOf(Seg(ns[0].toFloat(), BLUE, "${ns[0]}"), Seg(ns[1].toFloat(), GREEN, "${ns[1]}")))), "?")),
                TheoryStep("Gộp lại", listOf(T("${f(ns[0])} + ${f(ns[1])} = "), P(ans, GREEN)))
            ),
            "Vậy đáp số là $ans"
        )
    }

    private fun splitCombine(s: String, ans: String): KidTheory? {
        Regex("""Gộp (\d+) và (\d+) được""").find(s)?.let { m ->
            val a = m.groupValues[1].toInt(); val b = m.groupValues[2].toInt()
            if (!same((a + b).toString(), ans) || a + b > 10) return null
            return KidTheory("Gộp số", listOf(listOf(P("Gộp", GREEN), T(" là cộng lại: đếm tất cả."))), emptyList(), s,
                listOf(TheoryStep("Gộp hai nhóm chấm", visual = Visual.Dots(listOf(DotGroup(a, BLUE), DotGroup(b, GREEN)))), TheoryStep("Đếm tất cả", listOf(T("$a + $b = "), P(a + b, GREEN)))),
                "Vậy gộp $a và $b được ${a + b}")
        }
        Regex("""Số (\d+) gồm mấy và mấy""").find(s)?.let { g ->
            val n = g.groupValues[1].toInt()
            val mm = Regex("""^(\d+) và (\d+)$""").find(ans.trim()) ?: return null
            val x = mm.groupValues[1].toInt(); val y = mm.groupValues[2].toInt()
            if (x + y != n || n > 10) return null
            return KidTheory("Tách số", listOf(listOf(T("Một số có thể "), P("tách thành hai phần", PURPLE), T(": hai phần gộp lại bằng số đó."))), emptyList(), s,
                listOf(
                    TheoryStep("Chia $n chấm thành hai nhóm", visual = Visual.Dots(listOf(DotGroup(x, BLUE), DotGroup(y, GREEN)))),
                    TheoryStep("Kiểm tra bằng phép cộng", listOf(T("$x + $y = "), P(n, GREEN)))
                ), "Vậy $n gồm $x và $y")
        }
        val m = Regex("""Tách (\d+) gồm (\d+) và số mấy""").find(s)
            ?: Regex("""có (\d+) .*? chia vào 2 hộp\. Hộp thứ nhất có (\d+)""").find(s) ?: return null
        val n = m.groupValues[1].toInt(); val a = m.groupValues[2].toInt(); val r = n - a
        if (r < 0 || n > 10 || !same(r.toString(), ans)) return null
        return KidTheory("Tách số", listOf(listOf(P("Tách", PURPLE), T(" là chia thành hai phần: biết một phần thì "), P("lấy tổng trừ đi", PURPLE), T("."))), emptyList(), s,
            listOf(
                TheoryStep("Có $n chấm, lấy ra $a chấm", visual = Visual.Dots(listOf(DotGroup(r, BLUE), DotGroup(a, PURPLE, crossed = true)))),
                TheoryStep("Đếm phần còn lại", listOf(T("$n − $a = "), P(r, BLUE)))
            ), if (s.startsWith("Tách")) "Vậy $n gồm $a và $r" else "Vậy hộp thứ hai có $r")
    }

    private fun nextPrev(s: String, ans: String): KidTheory? {
        val m = Regex("""[Ss]ố liền (sau|trước) của (\d+)""").find(s) ?: return null
        val n = m.groupValues[2].toLong(); val after = m.groupValues[1] == "sau"
        val res = if (after) n + 1 else n - 1
        if (!same(res.toString(), ans)) return null
        return KidTheory("Số liền trước, số liền sau",
            listOf(listOf(P("Liền sau", GREEN), T(": thêm 1. "), P("Liền trước", PURPLE), T(": bớt 1."))),
            listOf(Tile("4", "liền trước 3 · liền sau 5", BLUE)), s,
            listOf(TheoryStep(if (after) "Thêm 1" else "Bớt 1", listOf(T(if (after) "$n + 1 = " else "$n − 1 = "), P(res, if (after) GREEN else PURPLE)))),
            "Vậy số liền ${m.groupValues[1]} của $n là $res")
    }

    /* ======================================== so sánh, thống kê ======================================== */

    private fun compareMax(s: String, ans: String): KidTheory? {
        val m1 = Regex("""(lớn nhất|bé nhất)[^:]*: ([\d, ]+)\?""").find(s)
        val m2 = Regex("""Trong các số ([\d, ]+), số nào (lớn nhất|bé nhất)""").find(s)
        val word: String; val list: String
        when {
            m1 != null -> { word = m1.groupValues[1]; list = m1.groupValues[2] }
            m2 != null -> { word = m2.groupValues[2]; list = m2.groupValues[1] }
            else -> return null
        }
        val big = word == "lớn nhất"
        val xs = list.split(",").map { it.trim() }.filter { it.isNotEmpty() }.map { it.toLong() }
        if (xs.size < 2) return null
        val res = if (big) xs.max() else xs.min()
        if (!same(res.toString(), ans)) return null
        return KidTheory("So sánh các số",
            listOf(listOf(T("Số nào "), P("nhiều chữ số hơn", BLUE), T(" thì lớn hơn.")), listOf(T("Cùng số chữ số: so sánh từng hàng "), P("từ trái sang phải", GREEN), T("."))),
            listOf(Tile("452 và 427", "hàng chục 5 > 2", GREEN)), s,
            listOf(
                TheoryStep("So sánh từ hàng cao nhất", xs.map { if (it == res) P(it, GREEN) else T(it.toString(), MUTED) }),
                TheoryStep("Chọn số $word", listOf(P(res, GREEN)))
            ), "Vậy số $word là $res")
    }

    private fun statsMax(s: String, ans: String): KidTheory? {
        val pairs = Regex("""([A-Za-zÀ-ỹ0-9][A-Za-zÀ-ỹ0-9 ]{0,14}?): (\d+)(?![\dA-Za-zÀ-ỹ])""").findAll(s).map { it.groupValues[1].trim() to it.groupValues[2].toLong() }.toList()
        if (pairs.size < 3) return null
        val wantMax = Regex("""nhiều(?: \p{L}+)? nhất|cao nhất|lớn nhất""").containsMatchIn(s)
        val wantMin = Regex("""ít(?: \p{L}+)? nhất|thấp nhất|bé nhất""").containsMatchIn(s)
        if (wantMax == wantMin) return null
        val pick = if (wantMax) pairs.maxBy { it.second } else pairs.minBy { it.second }
        if (!same(pick.first, ans)) return null
        return KidTheory("Đọc bảng số liệu",
            listOf(listOf(T("Đọc số của từng mục, rồi "), P(if (wantMax) "chọn số lớn nhất" else "chọn số bé nhất", GREEN), T("."))),
            emptyList(), s,
            listOf(
                TheoryStep("Ghi lại số liệu", pairs.map { (n, v) -> if (n == pick.first) P("$n: $v", GREEN) else T("$n: $v", MUTED) }),
                TheoryStep("So sánh", listOf(T("Số ${if (wantMax) "lớn" else "bé"} nhất là "), P(pick.second, GREEN)))
            ), "Vậy đáp án là ${pick.first}")
    }

    /* ==================================== tìm x, gấp/giảm, biểu thức ==================================== */

    private fun findUnknown(s: String, ans: String): KidTheory? {
        val m = Regex("""(\?|\d+)\s*([+\-×:])\s*(\?|\d+)\s*=\s*(\d+)""").find(s) ?: return null
        val l = m.groupValues[1]; val op = m.groupValues[2]; val r = m.groupValues[3]; val c = m.groupValues[4].toLong()
        if ((l == "?") == (r == "?")) return null
        val known = (if (l == "?") r else l).toLong()
        val (res, title, how) = when (op) {
            "+" -> Triple(c - known, "Số hạng = Tổng − số hạng kia", "$c − $known")
            "-" -> if (l == "?") Triple(c + known, "Số bị trừ = Hiệu + Số trừ", "$c + $known") else Triple(known - c, "Số trừ = Số bị trừ − Hiệu", "$known − $c")
            "×" -> if (c % known != 0L) return null else Triple(c / known, "Thừa số = Tích : thừa số kia", "$c : $known")
            else -> if (l == "?") Triple(c * known, "Số bị chia = Thương × Số chia", "$c × $known") else if (c == 0L || known % c != 0L) return null else Triple(known / c, "Số chia = Số bị chia : Thương", "$known : $c")
        }
        if (!same(res.toString(), ans)) return null
        val check = m.value.replace("?", res.toString()).replace(" - ", " − ")
        val young = grade() <= 1
        val addSubOnly = op == "+" || op == "-"
        return KidTheory("Tìm thành phần chưa biết",
            if (addSubOnly && grade() <= 2) listOf(listOf(T("Dùng "), P("phép tính ngược", ORANGE), T(": biết tổng và một số → "), P("lấy tổng trừ đi số đã biết", PURPLE), T(".")))
            else listOf(listOf(T("Dùng "), P("phép tính ngược", ORANGE), T(": cộng ↔ trừ, nhân ↔ chia."))),
            if (addSubOnly && grade() <= 2) listOf(Tile("3 + ? = 8", "8 − 3 = 5", GREEN))
            else listOf(Tile("? + 3 = 8", "8 − 3 = 5", GREEN), Tile("? × 4 = 20", "20 : 4 = 5", BLUE)), s,
            listOf(
                TheoryStep(if (young) (if (op == "+") "Lấy kết quả trừ đi số đã biết" else title) else title, listOf(T("${how.replace(" - ", " − ")} = "), P(res, GREEN))),
                TheoryStep("Thử lại", listOf(T(check, BLUE), T("✓")))
            ), "Vậy số cần tìm là $res")
    }

    private fun timesMoreLess(s: String, ans: String): KidTheory? {
        Regex("""Gấp (\d+) lên (\d+) lần""").find(s)?.let { m ->
            val a = m.groupValues[1].toLong(); val k = m.groupValues[2].toLong(); val r = a * k
            if (!same(r.toString(), ans)) return null
            return KidTheory("Gấp một số lên nhiều lần", listOf(listOf(P("Gấp lên", GREEN), T(" mấy lần → "), P("nhân", GREEN), T(". "), P("Giảm đi", PURPLE), T(" mấy lần → "), P("chia", PURPLE), T("."))), emptyList(), s,
                listOf(TheoryStep("Gấp lên → nhân", listOf(T("$a × $k = "), P(r, GREEN)))), "Vậy gấp $a lên $k lần được $r")
        }
        Regex("""Giảm (\d+) đi (\d+) lần""").find(s)?.let { m ->
            val a = m.groupValues[1].toLong(); val k = m.groupValues[2].toLong(); if (a % k != 0L) return null; val r = a / k
            if (!same(r.toString(), ans)) return null
            return KidTheory("Giảm một số đi nhiều lần", listOf(listOf(P("Gấp lên", GREEN), T(" mấy lần → "), P("nhân", GREEN), T(". "), P("Giảm đi", PURPLE), T(" mấy lần → "), P("chia", PURPLE), T("."))), emptyList(), s,
                listOf(TheoryStep("Giảm đi → chia", listOf(T("$a : $k = "), P(r, PURPLE)))), "Vậy giảm $a đi $k lần được $r")
        }
        val m = Regex("""dài (\d+) cm, đoạn thẳng \w+ dài (\d+) cm\..*gấp mấy lần""").find(s) ?: return null
        val a = m.groupValues[1].toLong(); val b = m.groupValues[2].toLong(); if (a % b != 0L) return null
        if (!same("${a / b} lần", ans)) return null
        return KidTheory("Số lớn gấp mấy lần số bé", listOf(listOf(T("Muốn biết số lớn gấp mấy lần số bé: lấy "), P("số lớn chia số bé", BLUE), T("."))), emptyList(), s,
            listOf(TheoryStep("Lấy số lớn chia số bé", listOf(T("$a : $b = "), P("${a / b} lần", BLUE)))), "Vậy gấp ${a / b} lần")
    }

    private fun evalOp(a: Long, op: String, b: Long): Long? = when (op) { "+" -> a + b; "-", "−" -> a - b; "×" -> a * b; ":" -> if (b != 0L && a % b == 0L) a / b else null; else -> null }

    private val opsRule = listOf(
        listOf(T("Có ngoặc: tính "), P("trong ngoặc", ORANGE), T(" trước.")),
        listOf(P("Nhân, chia", BLUE), T(" trước; "), P("cộng, trừ", GREEN), T(" sau.")),
        listOf(T("Chỉ có cộng trừ (hoặc nhân chia): tính từ trái sang phải."))
    )

    private fun orderOps(s: String, ans: String): KidTheory? {
        if (!s.contains("biểu thức")) return null
        val body = s.substringAfter(":").substringBefore("=").trim()
        return solveExpr(body, ans, s, "Tính giá trị biểu thức", emptyList())
    }

    private fun solveExpr(body: String, ans: String, s: String, topic: String, pre: List<TheoryStep>): KidTheory? {
        val paren = Regex("""^\((\d+) ([+\-×:]) (\d+)\) ([+\-×:]) (\d+)$""").find(body)
        val plain = Regex("""^(\d+) ([+\-×:]) (\d+) ([+\-×:]) (\d+)$""").find(body)
        val steps = ArrayList(pre)
        val res: Long
        if (paren != null) {
            val (a, o1, b, o2, c) = paren.destructured
            val inner = evalOp(a.toLong(), o1, b.toLong()) ?: return null
            res = evalOp(inner, o2, c.toLong()) ?: return null
            steps += TheoryStep("Tính trong ngoặc trước", listOf(T("$a $o1 $b = "), P(inner, ORANGE)))
            steps += TheoryStep("Tính tiếp", listOf(P(inner, ORANGE), T(" $o2 $c = "), P(res, GREEN)))
        } else if (plain != null) {
            val (a, o1, b, o2, c) = plain.destructured
            val hi = setOf("×", ":")
            if (o2 in hi && o1 !in hi) {
                val second = evalOp(b.toLong(), o2, c.toLong()) ?: return null
                res = evalOp(a.toLong(), o1, second) ?: return null
                steps += TheoryStep("Nhân, chia trước", listOf(T("$b $o2 $c = "), P(second, BLUE)))
                steps += TheoryStep("Cộng, trừ sau", listOf(T("$a $o1 "), P(second, BLUE), T(" = "), P(res, GREEN)))
            } else {
                val first = evalOp(a.toLong(), o1, b.toLong()) ?: return null
                res = evalOp(first, o2, c.toLong()) ?: return null
                steps += TheoryStep(if (o1 in hi && o2 !in hi) "Nhân, chia trước" else "Tính từ trái sang phải", listOf(T("$a $o1 $b = "), P(first, BLUE)))
                steps += TheoryStep("Tính tiếp", listOf(P(first, BLUE), T(" $o2 $c = "), P(res, GREEN)))
            }
        } else return null
        if (!same(res.toString(), ans)) return null
        val pretty = steps.map { st -> st.copy(line = st.line.map { t -> if (t is Tok.T) t.copy(s = t.s.replace(" - ", " − ").replace(Regex("^- "), "− ")) else t }) }
        steps.clear(); steps.addAll(pretty)
        val onlyAddSub = !body.contains("×") && !body.contains(":") && !body.contains("(")
        return if (onlyAddSub)
            KidTheory(topic, listOf(listOf(T("Chỉ có cộng, trừ: tính lần lượt "), P("từ trái sang phải", BLUE), T("."))), emptyList(), s, steps, "Vậy giá trị biểu thức là $res")
        else KidTheory(topic, opsRule, listOf(Tile("2 + 3 × 4", "= 2 + 12 = 14", BLUE), Tile("(2 + 3) × 4", "= 5 × 4 = 20", ORANGE)), s, steps, "Vậy giá trị biểu thức là $res")
    }

    private fun letterExpr(s: String, ans: String): KidTheory? {
        val m = Regex("""biểu thức (.+?) với (a = \d+(?:, [b-z] = \d+)+)""").find(s) ?: return null
        var expr = m.groupValues[1]
        val vals = Regex("""([a-z]) = (\d+)""").findAll(m.groupValues[2]).associate { it.groupValues[1] to it.groupValues[2] }
        vals.forEach { (k, v) -> expr = expr.replace(Regex("""\b$k\b"""), v) }
        val pre = listOf(TheoryStep("Thay chữ bằng số", listOf(T(m.groupValues[1] + " → "), P(expr, ORANGE))))
        return solveExpr(expr.trim(), ans, s, "Biểu thức có chứa chữ", pre)
    }

    private fun commutative(s: String, ans: String): KidTheory? {
        val m = Regex("""giao hoán của phép (cộng|nhân): (\d+) ([+×]) (\d+) = (\d+) [+×] \.\.\.""").find(s) ?: return null
        val a = m.groupValues[2]; val b = m.groupValues[4]
        val res = if (m.groupValues[5] == b) a else b
        if (!same(res, ans)) return null
        val op = m.groupValues[3]
        return KidTheory("Tính chất giao hoán", listOf(listOf(T("Đổi chỗ các số trong một "), P(if (op == "+") "tổng" else "tích", BLUE), T(" thì kết quả "), P("không thay đổi", GREEN), T("."))),
            listOf(Tile("3 + 5", "= 5 + 3", GREEN)), s,
            listOf(TheoryStep("Đổi chỗ hai số", listOf(P(a, BLUE), T(op), P(b, GREEN), T("="), P(b, GREEN), T(op), P(a, BLUE)))),
            "Vậy số cần điền là $res")
    }

    private fun distributive(s: String, ans: String): KidTheory? {
        val m = Regex("""Tính nhanh: (\d+) × (\d+) \+ (\d+) × (\d+)""").find(s) ?: return null
        val a = m.groupValues[1].toLong(); val b = m.groupValues[2].toLong(); val a2 = m.groupValues[3].toLong(); val c = m.groupValues[4].toLong()
        if (a != a2) return null
        val res = a * (b + c); if (!same(res.toString(), ans)) return null
        return KidTheory("Nhân một số với một tổng", listOf(listOf(P("a × b + a × c", BLUE), T(" = "), P("a × (b + c)", GREEN))),
            emptyList(), s,
            listOf(
                TheoryStep("Gom thừa số chung $a", listOf(T("$a × $b + $a × $c = "), P("$a × ($b + $c)", GREEN))),
                TheoryStep("Tính trong ngoặc rồi nhân", listOf(T("$a × ${b + c} = "), P(res, GREEN)))
            ), "Vậy kết quả là $res")
    }

    private fun midpoint(s: String, ans: String): KidTheory? {
        val m = Regex("""trung điểm của đoạn thẳng (\w+) dài (\d+) cm""").find(s) ?: return null
        val l = m.groupValues[2].toLong(); if (l % 2 != 0L) return null
        if (!same("${l / 2} cm", ans)) return null
        return KidTheory("Trung điểm của đoạn thẳng", listOf(listOf(T("Trung điểm chia đoạn thẳng thành "), P("hai phần bằng nhau", BLUE), T("."))), emptyList(), s,
            listOf(
                TheoryStep("Vẽ đoạn thẳng", visual = Visual.Bars(listOf(BarRow(m.groupValues[1], listOf(Seg(1f, BLUE, "${l / 2} cm"), Seg(1f, GREEN, "${l / 2} cm")))), "$l cm")),
                TheoryStep("Chia đôi độ dài", listOf(T("$l : 2 = "), P("${l / 2} cm", BLUE)))
            ), "Vậy độ dài cần tìm là ${l / 2} cm")
    }

    private fun clock(s: String, ans: String): KidTheory? {
        Regex("""đồng hồ chỉ (\d+) giờ (\d+) phút, kim dài""").find(s)?.let { m ->
            val mi = m.groupValues[2].toInt(); val r = if (mi == 0) 12 else mi / 5
            if (mi % 5 != 0 || !same(r.toString(), ans)) return null
            return KidTheory("Xem đồng hồ", listOf(listOf(T("Kim dài chỉ "), P("phút", BLUE), T(". Mỗi số trên mặt đồng hồ là "), P("5 phút", ORANGE), T("."))),
                listOf(Tile("số 3", "15 phút", BLUE), Tile("số 6", "30 phút", GREEN), Tile("số 12", "đúng giờ", PURPLE)), s,
                listOf(TheoryStep("Đổi số phút ra số trên đồng hồ", listOf(T("$mi : 5 = "), P(r, BLUE)))), "Vậy kim dài chỉ số $r")
        }
        Regex("""kim ngắn chỉ số (\d+), kim dài chỉ số 12\. Đồng hồ chỉ mấy giờ""").find(s)?.let { m ->
            val r = "${m.groupValues[1]} giờ"; if (!same(r, ans)) return null
            return KidTheory("Xem đồng hồ", listOf(listOf(P("Kim ngắn", BLUE), T(" chỉ giờ, "), P("kim dài", GREEN), T(" chỉ phút. Kim dài chỉ số 12 là đúng giờ."))), emptyList(), s,
                listOf(TheoryStep("Đọc kim ngắn", listOf(T("kim ngắn chỉ số ${m.groupValues[1]} → "), P(r, BLUE)), "Kim dài ở số 12 nên là giờ đúng.")), "Vậy đồng hồ chỉ $r")
        }
        Regex("""(?:đồng hồ chỉ |Lúc )(\d+) giờ đúng, kim dài""").find(s)?.let { m ->
            if (!same("12", ans)) return null
            return KidTheory("Xem đồng hồ", listOf(listOf(T("Đúng giờ thì "), P("kim dài chỉ số 12", BLUE), T("."))), emptyList(), s,
                listOf(TheoryStep("Đúng giờ", listOf(T("${m.groupValues[1]} giờ đúng → "), P("12", BLUE)))), "Vậy kim dài chỉ số 12")
        }
        val m = Regex("""Đồng hồ chỉ (\d+) giờ 30 phút hay còn gọi là""").find(s) ?: return null
        val r = "${m.groupValues[1]} giờ rưỡi"; if (!same(r, ans)) return null
        return KidTheory("Xem đồng hồ", listOf(listOf(P("30 phút", BLUE), T(" còn gọi là "), P("rưỡi", GREEN), T("."))), emptyList(), s,
            listOf(TheoryStep("30 phút = rưỡi", listOf(T("${m.groupValues[1]} giờ 30 phút = "), P(r, GREEN)))), "Vậy đó là $r")
    }

    /* ======================================== phép chia có dư ======================================== */

    private fun remainderMax(s: String, ans: String): KidTheory? {
        val m = Regex("""số chia là (\d+), số dư lớn nhất""").find(s) ?: return null
        val b = m.groupValues[1].toLong(); if (!same((b - 1).toString(), ans)) return null
        return KidTheory("Phép chia có dư", listOf(listOf(T("Số dư luôn "), P("bé hơn số chia", ORANGE), T("."))), listOf(Tile("chia 5", "dư 1, 2, 3 hoặc 4", ORANGE)), s,
            listOf(TheoryStep("Số dư lớn nhất = số chia − 1", listOf(T("$b − 1 = "), P(b - 1, ORANGE)))), "Vậy số dư lớn nhất là ${b - 1}")
    }

    private fun divRemainder(s: String, ans: String): KidTheory? {
        if (!s.contains("có dư")) return null
        val m = Regex("""(\d+) : (\d+) = \?""").find(s) ?: return null
        val a = m.groupValues[1].toLong(); val b = m.groupValues[2].toLong()
        val q = a / b; val r = a % b
        if (!same("$q (dư $r)", ans)) return null
        return KidTheory("Phép chia có dư",
            listOf(listOf(T("Tìm số lớn nhất trong "), P("bảng nhân $b", BLUE), T(" mà không quá $a.")), listOf(T("Phần còn thừa là "), P("số dư", ORANGE), T(" – luôn bé hơn số chia."))),
            emptyList(), s,
            listOf(
                TheoryStep("Nhẩm bảng nhân $b", listOf(P("$b × $q = ${b * q}", BLUE), T("≤ $a <"), T("$b × ${q + 1} = ${b * (q + 1)}", MUTED))),
                TheoryStep("Tìm số dư", listOf(T("$a − ${b * q} = "), P(r, ORANGE))),
                TheoryStep("Kiểm tra", note = "Số dư $r bé hơn số chia $b ✓")
            ), "Vậy $a : $b = $q (dư $r)")
    }

    /* ======================================== cộng trừ ======================================== */

    private val ADDSUB = Regex("""(?<![\d,])(\d{1,3}(?: \d{3})+|\d+)\s*([+\-−])\s*(\d{1,3}(?: \d{3})+|\d+)(?:\s*=\s*\?|\s*$)""")

    private fun make10(s: String, ans: String): KidTheory? {
        if (!s.contains("qua 10")) return null
        val m = ADDSUB.find(s) ?: return null
        val a = num(m.groupValues[1]); val b = num(m.groupValues[3]); val plus = m.groupValues[2] == "+"
        if (plus) {
            val need = 10 - a; val rest = b - need
            if (need <= 0 || rest < 0 || !same((a + b).toString(), ans)) return null
            return KidTheory("Cộng qua 10",
                listOf(listOf(T("Tách số sau để "), P("làm tròn 10", BLUE), T(", rồi cộng "), P("phần còn lại", GREEN), T("."))),
                listOf(Tile("9 + 1", "= 10", BLUE), Tile("8 + 2", "= 10", BLUE), Tile("7 + 3", "= 10", BLUE), Tile("6 + 4", "= 10", BLUE)), s,
                listOf(
                    TheoryStep("Tách $b", listOf(T("$b ="), P(need, BLUE), T("+"), P(rest, GREEN)), "$a cần thêm $need nữa là đủ 10."),
                    TheoryStep("Gộp cho đủ 10", listOf(T("$a + "), P(need, BLUE), T(" = 10"))),
                    TheoryStep("Cộng phần còn lại", listOf(T("10 + "), P(rest, GREEN), T(" = ${a + b}", BLUE)))
                ), "Vậy $a + $b = ${a + b}")
        }
        val first = a - 10; val rest = b - first
        if (first <= 0 || rest < 0 || !same((a - b).toString(), ans)) return null
        return KidTheory("Trừ qua 10",
            listOf(listOf(T("Trừ cho "), P("về 10", BLUE), T(" trước, rồi trừ tiếp "), P("phần còn lại", PURPLE), T("."))),
            emptyList(), s,
            listOf(
                TheoryStep("Tách $b", listOf(T("$b ="), P(first, BLUE), T("+"), P(rest, PURPLE))),
                TheoryStep("Trừ về 10", listOf(T("$a − "), P(first, BLUE), T(" = 10"))),
                TheoryStep("Trừ tiếp phần còn lại", listOf(T("10 − "), P(rest, PURPLE), T(" = ${a - b}", BLUE)))
            ), "Vậy $a − $b = ${a - b}")
    }

    private fun smallAddSub(s: String, ans: String): KidTheory? {
        val m = ADDSUB.find(s) ?: return null
        val a = num(m.groupValues[1]); val b = num(m.groupValues[3]); val plus = m.groupValues[2] == "+"
        val r = if (plus) a + b else a - b
        if (a > 10 || b > 10 || r > 10 || r < 0 || !same(r.toString(), ans)) return null
        return KidTheory(if (plus) "Phép cộng" else "Phép trừ",
            listOf(listOf(T("Phép cộng là "), P("gộp thêm", GREEN), T(", phép trừ là "), P("bớt đi", PURPLE), T(".")), listOf(T("Đếm số chấm để tìm kết quả."))),
            listOf(Tile("3 + 2", "= 5", GREEN), Tile("5 − 2", "= 3", PURPLE)), s,
            if (plus) listOf(
                TheoryStep("Có $a chấm, thêm $b chấm", visual = Visual.Dots(listOf(DotGroup(a.toInt(), BLUE), DotGroup(b.toInt(), GREEN)))),
                TheoryStep("Đếm tất cả", listOf(T("$a + $b = "), P(r, GREEN)))
            ) else listOf(
                TheoryStep("Có $a chấm, bớt đi $b chấm", visual = Visual.Dots(listOf(DotGroup(r.toInt(), BLUE), DotGroup(b.toInt(), PURPLE, crossed = true)))),
                TheoryStep("Đếm số chấm còn lại", listOf(T("$a − $b = "), P(r, BLUE)))
            ),
            "Vậy $a ${if (plus) "+" else "−"} $b = $r")
    }

    /**
     * Cách nhẩm bảng nhân theo nguyên tắc "đưa về số tròn chục / phép tính quen":
     * ×10 viết thêm 0 · ×9 = ×10 bớt 1 lần · ×8 = ×10 bớt 2 lần · thừa số nhỏ (≤ 3) cộng lặp ·
     * ×5 đếm thêm 5 · ×4 gấp đôi hai lần · ×6, ×7 = ×5 thêm 1–2 lần.
     */
    private fun mulStrategy(a: Long, b: Long): Pair<List<Tok>, List<TheoryStep>> {
        val r = a * b
        fun other(k: Long) = if (a == k) b else a
        // Khi thừa số "dễ nhẩm" đứng trước thì đổi chỗ cho đúng nghĩa (đổi chỗ các thừa số, tích không đổi)
        fun swap(first: Boolean): List<TheoryStep> =
            if (first && a != b) listOf(TheoryStep("Đổi chỗ hai thừa số", listOf(T("$a × $b = "), P("$b × $a", ORANGE)), "Đổi chỗ các thừa số thì tích không thay đổi.")) else emptyList()
        return when {
            a == 0L || b == 0L -> listOf(T("Số nào nhân với "), P("0", ORANGE), T(" cũng bằng 0.")) to
                listOf(TheoryStep("Nhân với 0", listOf(T("$a × $b = "), P(0, BLUE))))
            a == 1L || b == 1L -> listOf(T("Số nào nhân với "), P("1", ORANGE), T(" cũng bằng chính số đó.")) to
                listOf(TheoryStep("Nhân với 1", listOf(T("$a × $b = "), P(r, BLUE))))
            a == 10L || b == 10L -> { val o = other(10)
                listOf(T("Nhân với 10: "), P("viết thêm chữ số 0", ORANGE), T(" vào bên phải.")) to
                    swap(a == 10L) + listOf(TheoryStep("Viết thêm 0 vào sau $o", listOf(T("$o × 10 = "), P(r, BLUE)))) }
            a == 9L || b == 9L -> { val o = other(9)
                listOf(T("Nhân với 9: nhân với "), P("10", BLUE), T(" cho tròn chục, rồi "), P("bớt đi 1 lần", PURPLE), T(".")) to swap(a == 9L) + listOf(
                    TheoryStep("Nhân với 10 cho tròn chục", listOf(T("$o × 10 = "), P(o * 10, BLUE))),
                    TheoryStep("Bớt đi một lần $o", listOf(T("${o * 10} − $o = "), P(r, GREEN)), "Vì $o × 9 ít hơn $o × 10 đúng một lần $o.")
                ) }
            minOf(a, b) <= 3L -> { val k = minOf(a, b); val o = maxOf(a, b)
                listOf(T("Thừa số nhỏ: "), P("cộng lặp", GREEN), T(" cho nhanh.")) to listOf(
                    TheoryStep(if (k == b) "Cộng $o liên tiếp $k lần" else "Đổi chỗ: $a × $b = $o × $k, cộng $o liên tiếp $k lần",
                        listOf(T(List(k.toInt()) { "$o" }.joinToString(" + ") + " = "), P(r, BLUE)))
                ) }
            a == 5L || b == 5L -> { val o = other(5)
                listOf(T("Nhân với 5: "), P("đếm thêm 5", GREEN), T(".")) to swap(b == 5L) + listOf(
                    TheoryStep("Đếm thêm 5, đếm $o lần", listOf(T(if (o > 6) "5, 10, 15, …, ${r - 5}, " else (1 until o).joinToString(", ") { "${it * 5}" } + ", "), P(r, BLUE)))
                ) }
            a == 8L || b == 8L -> { val o = other(8)
                listOf(T("Nhân với 8: nhân với "), P("10", BLUE), T(" rồi "), P("bớt đi 2 lần", PURPLE), T(".")) to swap(a == 8L) + listOf(
                    TheoryStep("Nhân với 10 cho tròn chục", listOf(T("$o × 10 = "), P(o * 10, BLUE))),
                    TheoryStep("Bớt đi hai lần $o", listOf(T("${o * 10} − ${o * 2} = "), P(r, GREEN)), "Hai lần $o là $o × 2 = ${o * 2}.")
                ) }
            a == 4L || b == 4L -> { val o = other(4)
                listOf(T("Nhân với 4: "), P("gấp đôi", GREEN), T(" rồi gấp đôi thêm lần nữa.")) to swap(a == 4L) + listOf(
                    TheoryStep("Gấp đôi $o", listOf(T("$o × 2 = "), P(o * 2, BLUE))),
                    TheoryStep("Gấp đôi thêm lần nữa", listOf(T("${o * 2} × 2 = "), P(r, GREEN)))
                ) }
            else -> { // còn 6 × 6, 6 × 7, 7 × 7
                val k = minOf(a, b); val o = maxOf(a, b); val extra = k - 5
                listOf(T("Nhân với $k: lấy "), P("$o × 5", BLUE), T(" (nửa của $o × 10) rồi thêm "), P("$extra lần $o", GREEN), T(".")) to swap(a == k) + listOf(
                    TheoryStep("Nhân với 5", listOf(T("$o × 5 = "), P(o * 5, BLUE)), "$o × 5 là một nửa của $o × 10 = ${o * 10}."),
                    TheoryStep("Thêm $extra lần $o", listOf(T("${o * 5} + ${o * extra} = "), P(r, GREEN)))
                )
            }
        }
    }

    /**
     * "Tính nhẩm" cộng, trừ – KHÔNG đặt tính. Nguyên tắc: đưa về số tròn chục.
     *  · Hai số tròn chục/tròn trăm: đếm theo chục (trăm): 4 chục − 2 chục = 2 chục.
     *  · Cộng không nhớ: chục với chục, đơn vị với đơn vị.
     *  · Cộng có nhớ: tách số sau để số trước thành tròn chục: 47 + 35 = 47 + 3 + 32 = 50 + 32.
     *  · Trừ không nhớ: chục trừ chục, đơn vị trừ đơn vị.
     *  · Trừ có nhớ: trừ số tròn chục gần nhất rồi cộng trả phần trừ thừa: 52 − 17 = 52 − 20 + 3.
     */
    private fun mentalAddSub(s: String, ans: String): KidTheory? {
        if (!s.contains("Tính nhẩm") || s.contains("qua 10") || s.contains("/") || Regex("""\d,\d""").containsMatchIn(s)) return null
        val m = ADDSUB.find(s) ?: return null
        val a = num(m.groupValues[1]); val b = num(m.groupValues[3]); val plus = m.groupValues[2] == "+"
        val r = if (plus) a + b else a - b
        if (r < 0 || a > 100_000 || !same(f(r), ans)) return null
        val sign = if (plus) "+" else "−"
        fun unitOf(x: Long): Long = listOf(10_000L, 1000L, 100L, 10L).firstOrNull { x > 0 && x % it == 0L } ?: 1L
        val unitName = mapOf(10L to "chục", 100L to "trăm", 1000L to "nghìn", 10_000L to "chục nghìn")
        val roundRule = listOf(T("Tính nhẩm: "), P("đưa về số tròn chục", BLUE), T(" để tính cho nhanh – không cần đặt tính."))
        val steps = ArrayList<TheoryStep>()
        val rules = ArrayList<List<Tok>>(); rules += roundRule
        val tiles: List<Tile>

        val u = minOf(unitOf(a), if (b == 0L) unitOf(a) else unitOf(b))
        if (u >= 10 && (b == 0L || b % u == 0L) && a % u == 0L) {
            // Hai số tròn chục / tròn trăm: đếm theo chục / trăm
            val name = unitName[u]!!
            val x = a / u; val y = b / u
            rules += listOf(T("Số tròn $name: tính theo "), P(name, BLUE), T(" như tính số bình thường."))
            steps += TheoryStep("Đổi ra số $name", listOf(T("${f(a)} là "), P("$x $name", BLUE), T(", ${f(b)} là "), P("$y $name", BLUE)))
            steps += TheoryStep("Tính theo $name", listOf(T("$x $name $sign $y $name = "), P("${x + (if (plus) y else -y)} $name", GREEN)))
            steps += TheoryStep("Viết thành số", listOf(T("${r / u} $name = "), P(f(r), GREEN)))
            tiles = listOf(Tile("30 + 20", "3 chục + 2 chục = 5 chục", BLUE), Tile("700 − 400", "7 trăm − 4 trăm = 3 trăm", BLUE))
        } else if (plus) {
            val au = a % 10; val bu = b % 10
            if (au + bu < 10) {
                // Không nhớ: chục với chục, đơn vị với đơn vị
                rules += listOf(T("Cộng "), P("chục với chục", BLUE), T(", "), P("đơn vị với đơn vị", GREEN), T("."))
                val at = a - au; val bt = b - bu
                if (bt == 0L || at == 0L) {
                    val (big, small) = if (bt == 0L) a to b else b to a
                    steps += TheoryStep("Giữ nguyên số chục, cộng hàng đơn vị", listOf(T("${big % 10} + ${small} = "), P(big % 10 + small, GREEN)))
                    steps += TheoryStep("Ghép lại", listOf(T("${f(big - big % 10)} + ${big % 10 + small} = "), P(f(r), GREEN)))
                } else {
                    steps += TheoryStep("Cộng các chục", listOf(T("${f(at)} + ${f(bt)} = "), P(f(at + bt), BLUE)))
                    steps += TheoryStep("Cộng các đơn vị", listOf(T("$au + $bu = "), P(au + bu, GREEN)))
                    steps += TheoryStep("Gộp lại", listOf(T("${f(at + bt)} + ${au + bu} = "), P(f(r), GREEN)))
                }
                tiles = listOf(Tile("34 + 25", "30 + 20 = 50 · 4 + 5 = 9 → 59", BLUE))
            } else {
                // Có nhớ: lấy bớt ở số sau cho số trước tròn chục
                val need = 10 - au; val rest = b - need
                val round = a + need
                rules += listOf(T("Thiếu bao nhiêu cho "), P("tròn chục", BLUE), T(" thì "), P("tách ra từ số kia", GREEN), T("."))
                steps += TheoryStep("Tách ${f(b)} cho ${f(a)} thành tròn chục", listOf(T("${f(b)} = "), P(need, BLUE), T(" + "), P(f(rest), GREEN)), "${f(a)} cần thêm $need nữa là tròn chục.")
                steps += TheoryStep("Cộng cho tròn chục", listOf(T("${f(a)} + $need = "), P(f(round), BLUE)))
                steps += TheoryStep("Cộng phần còn lại", listOf(T("${f(round)} + ${f(rest)} = "), P(f(r), GREEN)))
                tiles = listOf(Tile("38 + 5", "38 + 2 + 3 = 40 + 3 = 43", BLUE))
            }
        } else {
            val au = a % 10; val bu = b % 10
            if (au >= bu) {
                rules += listOf(T("Trừ "), P("chục cho chục", BLUE), T(", "), P("đơn vị cho đơn vị", PURPLE), T("."))
                val at = a - au; val bt = b - bu
                if (bt == 0L) {
                    steps += TheoryStep("Giữ nguyên số chục, trừ hàng đơn vị", listOf(T("$au − $b = "), P(au - b, PURPLE)))
                    steps += TheoryStep("Ghép lại", listOf(T("${f(at)} + ${au - b} = "), P(f(r), GREEN)))
                } else {
                    steps += TheoryStep("Trừ các chục", listOf(T("${f(at)} − ${f(bt)} = "), P(f(at - bt), BLUE)))
                    steps += TheoryStep("Trừ các đơn vị", listOf(T("$au − $bu = "), P(au - bu, PURPLE)))
                    steps += TheoryStep("Gộp lại", listOf(T("${f(at - bt)} + ${au - bu} = "), P(f(r), GREEN)))
                }
                tiles = listOf(Tile("78 − 35", "70 − 30 = 40 · 8 − 5 = 3 → 43", BLUE))
            } else {
                // Có nhớ: trừ số tròn chục gần nhất, trừ thừa bao nhiêu cộng trả lại
                val round = b - bu + 10; val over = round - b
                rules += listOf(T("Trừ số "), P("tròn chục", BLUE), T(" trước; "), P("trừ thừa bao nhiêu thì cộng trả lại", GREEN), T("."))
                steps += TheoryStep("Làm tròn số trừ", listOf(T("${f(b)} → "), P(f(round), BLUE)), "${f(round)} nhiều hơn ${f(b)} là $over.")
                steps += TheoryStep("Trừ số tròn chục", listOf(T("${f(a)} − ${f(round)} = "), P(f(a - round), BLUE)))
                steps += TheoryStep("Trừ thừa $over nên cộng trả lại", listOf(T("${f(a - round)} + $over = "), P(f(r), GREEN)))
                tiles = listOf(Tile("52 − 19", "52 − 20 + 1 = 33", BLUE))
            }
        }
        return KidTheory("Tính nhẩm", rules, tiles, s, steps, "Vậy ${f(a)} $sign ${f(b)} = ${f(r)}")
    }

    private fun tableMulDiv(s: String, ans: String): KidTheory? {
        val m = Regex("""(\d+) ([×:]) (\d+) = \?""").find(s) ?: return null
        if (s.contains("Đặt tính") || s.contains("có dư")) return null
        val a = m.groupValues[1].toLong(); val b = m.groupValues[3].toLong()
        if (m.groupValues[2] == "×") {
            if (a > 10 || b > 10) return null
            val r = a * b; if (!same(r.toString(), ans)) return null
            val (tip, steps) = mulStrategy(a, b)
            return KidTheory("Phép nhân",
                listOf(listOf(P("$a × $b", BLUE), T(" nghĩa là "), P("$a được lấy $b lần", GREEN), T(".")), tip),
                listOf(Tile("9 × 7", "70 − 7 = 63", BLUE), Tile("8 × 6", "60 − 12 = 48", BLUE)), s,
                steps, "Vậy $a × $b = $r")
        }
        if (b == 0L || a % b != 0L || a / b > 10 || b > 10) return null
        val q = a / b; if (!same(q.toString(), ans)) return null
        return KidTheory("Phép chia", listOf(listOf(T("Chia là "), P("ngược của nhân", ORANGE), T(": tìm số nhân với $b được $a."))),
            listOf(Tile("12 : 3", "vì 3 × 4 = 12", ORANGE)), s,
            listOf(TheoryStep("Nhẩm bảng nhân $b", listOf(T("$b × "), P(q, GREEN), T(" = $a")))),
            "Vậy $a : $b = $q")
    }

    private fun columnAddSub(s: String, ans: String): KidTheory? {
        if (s.contains("/") || Regex("""\d,\d""").containsMatchIn(s)) return null
        val m = ADDSUB.find(s) ?: return null
        val a = num(m.groupValues[1]); val b = num(m.groupValues[3]); val plus = m.groupValues[2] == "+"
        val r = if (plus) a + b else a - b
        if (r < 0 || (a < 10 && b < 10) || !same(f(r), ans)) return null
        val ad = a.toString().reversed(); val bd = b.toString().reversed()
        val width = maxOf(ad.length, bd.length)
        val steps = ArrayList<TheoryStep>()
        steps += TheoryStep("Đặt tính thẳng cột", visual = Visual.Column(listOf(a.toString(), b.toString()), if (plus) "+" else "-", null),
            note = "Hàng đơn vị thẳng hàng đơn vị, chục thẳng chục…")
        val carries = MutableList(width + 1) { "" }
        var carry = 0
        for (i in 0 until width) {
            val x = ad.getOrNull(i)?.digitToInt() ?: 0; val y = bd.getOrNull(i)?.digitToInt() ?: 0
            val place = PLACES[i]
            if (i >= 3 && width > 4) continue
            if (plus) {
                val sum = x + y + carry
                val line = mutableListOf<Tok>(T("$x + $y"))
                if (carry > 0) line += P("+ 1", ORANGE)
                line += T("= $sum")
                val note = if (sum >= 10) "Viết ${sum % 10}, nhớ 1 sang hàng ${PLACES[i + 1]}." else "Viết $sum."
                steps += TheoryStep("Cộng hàng $place", line, note)
                if (sum >= 10) carries[i + 1] = "1"
                carry = sum / 10
            } else {
                // Cách của SGK: số trừ ở hàng này "thêm 1" (số nhớ); không trừ được thì lấy thêm 10 rồi nhớ 1
                val y2 = y + carry
                val borrow = x < y2
                val top = if (borrow) x + 10 else x
                val d = top - y2
                val line = mutableListOf<Tok>()
                line += if (borrow) P("$top", ORANGE) else T("$x")
                line += T(" − ")
                line += if (carry > 0) P("$y2", ORANGE) else T("$y")
                line += T(" = $d")
                val pre = if (carry > 0) "$y thêm 1 (số nhớ) bằng $y2. " else ""
                val note = pre + if (borrow) "$x không trừ được $y2, lấy $top trừ $y2 bằng $d, viết $d, nhớ 1." else "$x trừ $y2 bằng $d, viết $d."
                steps += TheoryStep("Trừ hàng $place", line, note)
                if (borrow) carries[i + 1] = "1"
                carry = if (borrow) 1 else 0
            }
        }
        if (width > 4) steps += TheoryStep("Làm tương tự với các hàng còn lại", note = if (plus) "Nhớ cộng số nhớ (tô cam) vào hàng bên trái." else "Nhớ cộng số nhớ (tô cam) vào số trừ ở hàng bên trái.")
        steps += TheoryStep("Kết quả", visual = Visual.Column(listOf(a.toString(), b.toString()), if (plus) "+" else "-", r.toString(), carries))
        val anyCarry = carries.any { it.isNotEmpty() }
        return KidTheory(if (plus) (if (anyCarry) "Phép cộng có nhớ" else "Phép cộng đặt tính") else (if (anyCarry) "Phép trừ có nhớ" else "Phép trừ đặt tính"),
            if (plus) listOf(
                listOf(T("Viết thẳng cột: "), P("chục", BLUE), T(" dưới chục, "), P("đơn vị", GREEN), T(" dưới đơn vị.")),
                listOf(T("Cộng từ "), P("phải sang trái", INK), T(".")),
                listOf(T("Được từ 10 trở lên: viết số đơn vị, "), P("nhớ 1", ORANGE), T(" sang hàng bên trái."))
            ) else listOf(
                listOf(T("Viết thẳng cột: "), P("chục", BLUE), T(" dưới chục, "), P("đơn vị", GREEN), T(" dưới đơn vị.")),
                listOf(T("Trừ từ "), P("phải sang trái", INK), T(".")),
                listOf(T("Không trừ được: "), P("lấy thêm 10", ORANGE), T(" để trừ, viết kết quả và "), P("nhớ 1", ORANGE), T(" – cộng 1 vào số trừ ở hàng bên trái."))
            ),
            if (plus) listOf(Tile("7 + 5 = 12", "viết 2", GREEN), Tile("nhớ 1", "sang hàng chục", ORANGE))
            else listOf(Tile("2 − 7", "không trừ được", PURPLE), Tile("12 − 7 = 5", "viết 5, nhớ 1", ORANGE)),
            s, steps, "Vậy ${f(a)} ${if (plus) "+" else "−"} ${f(b)} = ${f(r)}")
    }

    private fun columnMul(s: String, ans: String): KidTheory? {
        if (Regex("""\d,\d""").containsMatchIn(s)) return null
        val m = Regex("""(\d+) × (\d+) = \?""").find(s) ?: return null
        val a = m.groupValues[1].toLong(); val b = m.groupValues[2].toLong()
        if (a < 10 && b < 10) return null
        val r = a * b; if (!same(f(r), ans)) return null
        val steps = ArrayList<TheoryStep>()
        steps += TheoryStep("Đặt tính thẳng cột", visual = Visual.Column(listOf(a.toString(), b.toString()), "×", null))
        if (b < 10) {
            val ad = a.toString().reversed(); var carry = 0L
            val carries = MutableList(ad.length + 1) { "" }
            ad.forEachIndexed { i, c ->
                if (i >= 4) return@forEachIndexed
                val x = c.digitToInt(); val p = x * b + carry
                val line = mutableListOf<Tok>(T("$x × $b")); if (carry > 0) line += P("+ $carry", ORANGE); line += T("= $p")
                steps += TheoryStep("Nhân hàng ${PLACES[i]}", line, if (p >= 10 && i < ad.length - 1) "Viết ${p % 10}, nhớ ${p / 10}." else "Viết $p.")
                if (p >= 10 && i < ad.length - 1) carries[i + 1] = "${p / 10}"
                carry = if (i < ad.length - 1) p / 10 else 0
            }
            steps += TheoryStep("Kết quả", visual = Visual.Column(listOf(a.toString(), b.toString()), "×", r.toString(), carries))
        } else if (b < 100) {
            val u = b % 10; val t = b / 10
            steps += TheoryStep("Nhân với chữ số hàng đơn vị", listOf(T("$a × $u = "), P(a * u, BLUE)))
            steps += TheoryStep("Nhân với chữ số hàng chục", listOf(T("$a × $t = "), P(a * t, GREEN)), "Viết lùi sang trái 1 cột (vì là hàng chục).")
            steps += TheoryStep("Cộng hai tích riêng", listOf(T("${a * u} + ${a * t}0 = "), P(r, ORANGE)),
                visual = Visual.Column(listOf(a.toString(), b.toString()), "×", r.toString(), partials = listOf((a * u).toString(), (a * t).toString())))
        } else return null
        return KidTheory("Phép nhân đặt tính",
            listOf(listOf(T("Nhân lần lượt "), P("từ phải sang trái", INK), T(".")), listOf(T("Được từ 10 trở lên: viết đơn vị, "), P("nhớ", ORANGE), T(" sang hàng bên trái rồi cộng vào lần nhân sau."))),
            listOf(Tile("4 × 3 = 12", "viết 2, nhớ 1", ORANGE)), s, steps, "Vậy ${f(a)} × ${f(b)} = ${f(r)}")
    }

    private fun longDivision(s: String, ans: String): KidTheory? {
        if (Regex("""\d,\d""").containsMatchIn(s) || s.contains("có dư")) return null
        val m = Regex("""(\d+) : (\d+) = \?""").find(s) ?: return null
        val a = m.groupValues[1].toLong(); val b = m.groupValues[2].toLong()
        if (b == 0L || a % b != 0L || a < 10) return null
        val q = a / b; if (!same(f(q), ans)) return null
        val digits = a.toString(); val steps = ArrayList<TheoryStep>()
        var cur = 0L; var started = false; var count = 0
        for ((i, c) in digits.withIndex()) {
            cur = cur * 10 + c.digitToInt()
            if (!started && cur < b && i < digits.length - 1) continue
            started = true
            val d = cur / b; val rem = cur - d * b
            if (count < 4) steps += TheoryStep(if (count == 0) "Lấy $cur chia $b" else "Hạ chữ số tiếp → $cur chia $b",
                listOf(T("$cur : $b = "), P(d, BLUE)), "Nhân lại: $d × $b = ${d * b}. Trừ: $cur − ${d * b} = $rem.")
            count++; cur = rem
        }
        if (count > 4) steps += TheoryStep("Làm tương tự đến hết", note = "Mỗi lượt: chia – nhân – trừ – hạ.")
        return KidTheory("Phép chia đặt tính",
            listOf(listOf(T("Chia lần lượt "), P("từ trái sang phải", INK), T(".")), listOf(T("Mỗi lượt: "), P("chia", BLUE), T(" – nhân – "), P("trừ", ORANGE), T(" – hạ chữ số tiếp theo."))),
            emptyList(), s, steps, "Vậy ${f(a)} : ${f(b)} = ${f(q)}")
    }

    /* ==================================== bổ sung lớp 1–2 ==================================== */

    private fun facts(s: String, ans: String): KidTheory? {
        val table = listOf(
            Regex("Một giờ có bao nhiêu phút") to ("60 phút" to "1 giờ = 60 phút"),
            Regex("Một ngày có tất cả bao nhiêu giờ|Một ngày có bao nhiêu giờ") to ("24 giờ" to "1 ngày = 24 giờ"),
            Regex("Một tuần lễ có tất cả bao nhiêu ngày|Một tuần có bao nhiêu ngày") to ("7 ngày" to "1 tuần = 7 ngày"),
            Regex("Một năm có bao nhiêu tháng") to ("12 tháng" to "1 năm = 12 tháng"),
            Regex("Một phút có bao nhiêu giây") to ("60 giây" to "1 phút = 60 giây")
        )
        val hit = table.firstOrNull { it.first.containsMatchIn(s) }?.second ?: return null
        if (!same(hit.first, ans)) return null
        return KidTheory("Đơn vị thời gian", listOf(listOf(T("Ghi nhớ: "), P(hit.second, BLUE))),
            listOf(Tile("1 giờ", "60 phút", BLUE), Tile("1 ngày", "24 giờ", GREEN), Tile("1 tuần", "7 ngày", PURPLE)), s,
            listOf(TheoryStep("Nhớ quy đổi", listOf(P(hit.second, BLUE)))), "Vậy đáp án là ${hit.first}")
    }

    private val DAYS = listOf("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ nhật")
    private fun calendar(s: String, ans: String): KidTheory? {
        val rule = listOf(listOf(P("Ngày mai", GREEN), T(" = hôm nay thêm 1 ngày. "), P("Hôm qua", PURPLE), T(" = hôm nay bớt 1 ngày.")))
        val tiles = listOf(Tile("Thứ Hai → Thứ Ba", "ngày mai", GREEN), Tile("Thứ Bảy → Chủ nhật", "ngày mai", GREEN))
        Regex("""ngày (\d+)\. Hỏi (ngày mai|hôm qua) là ngày mấy""").find(s)?.let { m ->
            val d = m.groupValues[1].toInt(); val next = m.groupValues[2] == "ngày mai"
            val r = "Ngày ${if (next) d + 1 else d - 1}"; if (!same(r, ans)) return null
            return KidTheory("Xem lịch", rule, tiles, s, listOf(TheoryStep(if (next) "Thêm 1 ngày" else "Bớt 1 ngày", listOf(T(if (next) "$d + 1 = " else "$d − 1 = "), P(r, if (next) GREEN else PURPLE)))), "Vậy đáp án là $r")
        }
        val m = Regex("""Hôm nay là (Thứ \S+|Chủ nhật)\. (Ngày mai|Hôm qua) là thứ mấy""").find(s) ?: return null
        val i = DAYS.indexOfFirst { it.equals(m.groupValues[1], ignoreCase = true) }; if (i < 0) return null
        val next = m.groupValues[2] == "Ngày mai"
        val r = DAYS[(i + if (next) 1 else 6) % 7]; if (!same(r, ans)) return null
        return KidTheory("Các ngày trong tuần", rule, tiles, s,
            listOf(TheoryStep("Đọc thứ tự các ngày trong tuần", DAYS.map { if (it == r) P(it, if (next) GREEN else PURPLE) else if (it == DAYS[i]) P(it, BLUE) else T(it, MUTED) })),
            "Vậy ${m.groupValues[2].lowercase()} là $r")
    }

    private fun termNames(s: String, ans: String): KidTheory? {
        Regex("""Biết số bị trừ là (\d+), số trừ là (\d+)\. Hiệu""").find(s)?.let { m ->
            val a = m.groupValues[1].toLong(); val b = m.groupValues[2].toLong(); if (!same((a - b).toString(), ans)) return null
            return KidTheory("Tên gọi trong phép trừ", listOf(listOf(P("Số bị trừ", BLUE), T(" − "), P("Số trừ", PURPLE), T(" = "), P("Hiệu", GREEN))), emptyList(), s,
                listOf(TheoryStep("Hiệu = Số bị trừ − Số trừ", listOf(T("$a − $b = "), P(a - b, GREEN)))), "Vậy hiệu là ${a - b}")
        }
        Regex("""hai số hạng lần lượt là (\d+) và (\d+)\. Tổng""").find(s)?.let { m ->
            val a = m.groupValues[1].toLong(); val b = m.groupValues[2].toLong(); if (!same((a + b).toString(), ans)) return null
            return KidTheory("Tên gọi trong phép cộng", listOf(listOf(P("Số hạng", BLUE), T(" + "), P("Số hạng", BLUE), T(" = "), P("Tổng", GREEN))), emptyList(), s,
                listOf(TheoryStep("Tổng = Số hạng + Số hạng", listOf(T("$a + $b = "), P(a + b, GREEN)))), "Vậy tổng là ${a + b}")
        }
        val m = Regex("""phép (?:tính|nhân|chia|cộng|trừ) (\d+) ([+\-×:]) (\d+) = (\d+), số (\d+) được gọi là gì""").find(s) ?: return null
        val (a, op, b, c, x) = m.destructured
        val names = when (op) { "+" -> listOf("Số hạng", "Số hạng", "Tổng"); "-" -> listOf("Số bị trừ", "Số trừ", "Hiệu"); "×" -> listOf("Thừa số", "Thừa số", "Tích"); else -> listOf("Số bị chia", "Số chia", "Thương") }
        val res = when (x) { c -> names[2]; a -> names[0]; b -> names[1]; else -> return null }
        if (!same(res, ans)) return null
        val opShow = if (op == "-") "−" else op
        return KidTheory("Tên gọi các thành phần", listOf(listOf(P(names[0], BLUE), T(" $opShow "), P(names[1], PURPLE), T(" = "), P(names[2], GREEN))), emptyList(), s,
            listOf(TheoryStep("Ghi tên dưới từng số", listOf(P("$a: ${names[0]}", BLUE), P("$b: ${names[1]}", PURPLE), P("$c: ${names[2]}", GREEN)))),
            "Vậy số $x là ${res.lowercase()}")
    }

    private fun ruler(s: String, ans: String): KidTheory? {
        val m = Regex("""vạch số (\d+)[^0-9]+vạch số (\d+)""").find(s) ?: return null
        val a = m.groupValues[1].toLong(); val b = m.groupValues[2].toLong(); val d = kotlin.math.abs(b - a)
        if (!same("$d cm", ans)) return null
        return KidTheory("Đo độ dài bằng thước", listOf(listOf(T("Độ dài = "), P("vạch cuối − vạch đầu", BLUE), T(". Đặt vạch 0 ở đầu vật thì đọc luôn số ở đầu kia."))), emptyList(), s,
            listOf(TheoryStep("Lấy vạch lớn trừ vạch bé", listOf(T("${maxOf(a, b)} − ${minOf(a, b)} = "), P("$d cm", BLUE)))), "Vậy độ dài là $d cm")
    }

    private val UNIT_WORD = mapOf("không" to 0, "một" to 1, "mốt" to 1, "hai" to 2, "ba" to 3, "bốn" to 4, "tư" to 4, "năm" to 5, "lăm" to 5, "sáu" to 6, "bảy" to 7, "tám" to 8, "chín" to 9)
    private fun wordNumber(s: String, ans: String): KidTheory? {
        val m = Regex("""Số '([^']+)' được viết là""").find(s) ?: return null
        val w = m.groupValues[1].trim().split(" ")
        val (t, u) = when {
            w.size >= 2 && w[0] == "mười" -> 1 to (UNIT_WORD[w[1]] ?: return null)
            w.size == 1 && w[0] == "mười" -> 1 to 0
            w.size >= 2 && w[1] == "mươi" -> (UNIT_WORD[w[0]] ?: return null) to (if (w.size > 2) UNIT_WORD[w[2]] ?: return null else 0)
            else -> return null
        }
        val n = t * 10 + u; if (!same(n.toString(), ans)) return null
        return KidTheory("Đọc, viết số", listOf(listOf(T("Phần đầu là "), P("số chục", BLUE), T(", phần sau là "), P("số đơn vị", GREEN), T(".")), listOf(T("\"mốt\" = 1, \"tư\" = 4, \"lăm\" = 5."))),
            emptyList(), s,
            listOf(TheoryStep("Tách lời đọc", listOf(P("${w.take(if (w[0] == "mười") 1 else 2).joinToString(" ")} → $t chục", BLUE)) + (if (u > 0) listOf(P("${w.last()} → $u đơn vị", GREEN)) else emptyList())),
                TheoryStep("Viết số", visual = Visual.Places(listOf(Tile("$t", "chục", BLUE), Tile("$u", "đơn vị", GREEN))))),
            "Vậy số đó viết là $n")
    }

    private fun sequence(s: String, ans: String): KidTheory? {
        if (!s.contains("dãy số")) return null
        val body = s.substringAfter(":").trim()
        val parts = body.split(";").map { it.trim().trimEnd('.', '?') }
        if (parts.size < 3) return null
        val gap = parts.indexOfFirst { it.startsWith("...") || it.isEmpty() }
        val nums = parts.map { it.toLongOrNull() }
        val known = nums.withIndex().filter { it.value != null }
        if (known.size < 2) return null
        val step = (known[1].value!! - known[0].value!!) / (known[1].index - known[0].index)
        val idx = if (gap >= 0) gap else return null
        val res = known[0].value!! + step * (idx - known[0].index)
        if (!same(res.toString(), ans)) return null
        return KidTheory("Dãy số theo quy luật", listOf(listOf(T("Tìm "), P("khoảng cách", ORANGE), T(" giữa hai số liền nhau, rồi cộng (hoặc trừ) tiếp."))), emptyList(), s,
            listOf(
                run {
                    val x = known[0].value!!; val y = known[1].value!!
                    val apart = known[1].index - known[0].index
                    val diff = kotlin.math.abs(y - x); val per = kotlin.math.abs(step)
                    val calc = if (y >= x) "$y − $x = $diff" else "$x − $y = $diff"
                    TheoryStep(
                        if (step >= 0) "Dãy tăng dần: mỗi số sau thêm $per" else "Dãy giảm dần: mỗi số sau bớt $per",
                        listOf(T(calc + if (apart > 1) " (cách $apart số → mỗi bước $per)" else ""), T(" → "), P(if (step >= 0) "thêm $per" else "bớt $per", ORANGE))
                    )
                },
                TheoryStep("Điền số còn thiếu", parts.mapIndexed { i, x -> if (i == idx) P(res, GREEN) else T(x, MUTED) })
            ), "Vậy số cần điền là $res")
    }

    private fun tensToUnits(s: String, ans: String): KidTheory? {
        val m = Regex("""(\d+) chục bằng bao nhiêu đơn vị""").find(s) ?: return null
        val n = m.groupValues[1].toLong(); if (!same((n * 10).toString(), ans)) return null
        return KidTheory("Chục và đơn vị", listOf(listOf(P("1 chục", BLUE), T(" = "), P("10 đơn vị", GREEN), T("."))), emptyList(), s,
            if (n in 2..10) listOf(
                TheoryStep("Đếm theo chục", listOf(T((1..n).joinToString(", ") { "${it * 10}" }.let { if (n > 6) (1..3).joinToString(", ") { "${it * 10}" } + ", …, " + (n * 10) else it }))),
                TheoryStep("$n chục là", listOf(P("${n * 10} đơn vị", GREEN)), "Mỗi chục là 10 đơn vị, đếm thêm 10 cho mỗi chục.")
            ) else listOf(TheoryStep("Viết thêm chữ số 0 vào sau số chục", listOf(T("$n chục = "), P("${n * 10} đơn vị", GREEN)))),
            "Vậy $n chục = ${n * 10} đơn vị")
    }

    private fun fillBlank(s: String, ans: String): KidTheory? {
        val m = Regex("""(\d+) = (\d+) \+ \.\.\.""").find(s) ?: return null
        val a = m.groupValues[1].toLong(); val b = m.groupValues[2].toLong(); if (!same((a - b).toString(), ans)) return null
        return KidTheory("Tách số theo chục và đơn vị", listOf(listOf(T("Số có hai chữ số = "), P("số chục tròn", BLUE), T(" + "), P("số đơn vị", GREEN), T("."))), emptyList(), s,
            listOf(TheoryStep("Lấy $a bớt đi $b", listOf(T("$a − $b = "), P(a - b, GREEN)))), "Vậy số cần điền là ${a - b}")
    }

    private fun sortNums(s: String, ans: String): KidTheory? {
        val m = Regex("""Sắp xếp các số ([\d, ]+) theo thứ tự từ (lớn đến bé|bé đến lớn)""").find(s) ?: return null
        val xs = m.groupValues[1].split(",").map { it.trim().toLong() }
        val desc = m.groupValues[2] == "lớn đến bé"
        val sorted = if (desc) xs.sortedDescending() else xs.sorted()
        val res = sorted.joinToString("; "); if (!same(res, ans)) return null
        return KidTheory("Sắp xếp các số", listOf(listOf(T("Tìm số "), P(if (desc) "lớn nhất" else "bé nhất", GREEN), T(" viết trước, rồi tìm tiếp trong các số còn lại."))), emptyList(), s,
            listOf(TheoryStep("Xếp lần lượt", sorted.mapIndexed { i, x -> P(x, if (i == 0) GREEN else BLUE) })), "Vậy thứ tự là $res")
    }

    private fun compareTwo(s: String, ans: String): KidTheory? {
        val m = Regex("""so sánh hai số (\d+) và (\d+), nhận xét nào đúng""").find(s) ?: return null
        val a = m.groupValues[1].toLong(); val b = m.groupValues[2].toLong()
        val res = when { a > b -> "$a lớn hơn $b"; a < b -> "$a bé hơn $b"; else -> "$a bằng $b" }
        if (!same(res, ans)) return null
        return KidTheory("So sánh hai số", listOf(listOf(T("Số đếm sau thì "), P("lớn hơn", GREEN), T(". Dấu > mở về phía số lớn."))), listOf(Tile("5 > 3", "5 lớn hơn 3", GREEN), Tile("2 < 7", "2 bé hơn 7", PURPLE)), s,
            listOf(TheoryStep("So sánh", listOf(P(a, BLUE), T(when { a > b -> ">"; a < b -> "<"; else -> "=" }), P(b, BLUE)))), "Vậy $res")
    }

    private fun unitAddSub(s: String, ans: String): KidTheory? {
        val m = Regex("""(\d+) (cm|dm|m|km|kg|g|l|ml) ([+\-]) (\d+) \2 = \?""").find(s) ?: return null
        val a = m.groupValues[1].toLong(); val u = m.groupValues[2]; val plus = m.groupValues[3] == "+"; val b = m.groupValues[4].toLong()
        val r = if (plus) a + b else a - b; if (!same("$r $u", ans)) return null
        return KidTheory("Tính với số đo", listOf(listOf(T("Tính như số bình thường, rồi "), P("giữ nguyên đơn vị $u", ORANGE), T("."))), emptyList(), s,
            listOf(TheoryStep("Tính phần số", listOf(T(if (plus) "$a + $b = " else "$a − $b = "), P(r, BLUE))), TheoryStep("Viết thêm đơn vị", listOf(P("$r $u", ORANGE)))),
            "Vậy kết quả là $r $u")
    }

    private fun threeTerms(s: String, ans: String): KidTheory? {
        val m = Regex("""Tính: (\d+ [+\-×:] \d+ [+\-×:] \d+) = \?""").find(s) ?: return null
        return solveExpr(m.groupValues[1], ans, s, "Tính dãy phép tính", emptyList())
    }

    private fun takeAway(s: String, ans: String): KidTheory? {
        if (!Regex("còn lại|cắt đi|bớt đi|trả lại|cho .* Hỏi").containsMatchIn(s)) return null
        var ns = nums(s).filter { !it.contains(',') }.map { num(it) }
        if (ns.size != 2) return null
        if (ns[0] < ns[1]) ns = listOf(ns[1], ns[0])
        val r = ns[0] - ns[1]
        val unit = ans.substringAfter(Regex("""\d+""").find(ans)?.value ?: return null, "").trim()
        if (!same("$r${if (unit.isNotEmpty()) " $unit" else ""}", ans)) return null
        return KidTheory("Bài toán bớt đi", listOf(listOf(T("Cho đi, cắt đi, bớt đi, hỏi "), P("còn lại", PURPLE), T(" → làm phép trừ."))), emptyList(), s,
            listOf(
                TheoryStep("Vẽ sơ đồ", visual = Visual.Bars(listOf(BarRow("", listOf(Seg(r.toFloat(), BLUE, "?"), Seg(ns[1].toFloat(), PURPLE, "${ns[1]}")))), "${ns[0]}"), note = "Phần tím là phần bớt đi."),
                TheoryStep("Lấy tổng trừ phần bớt đi", listOf(T("${ns[0]} − ${ns[1]} = "), P("$r${if (unit.isNotEmpty()) " $unit" else ""}", PURPLE)))
            ), "Vậy đáp số là $r${if (unit.isNotEmpty()) " $unit" else ""}")
    }

    /* ==================================== hình, nhóm đều, thập phân ==================================== */

    private val SHAPE_INFO = linkedMapOf(
        "Hình tam giác" to "3 cạnh, 3 góc",
        "Hình vuông" to "4 cạnh bằng nhau, 4 góc vuông",
        "Hình chữ nhật" to "2 cạnh dài bằng nhau, 2 cạnh ngắn bằng nhau",
        "Hình tròn" to "đường cong khép kín, không có góc",
        "Khối lập phương" to "6 mặt đều là hình vuông (như con xúc xắc)",
        "Khối hộp chữ nhật" to "6 mặt là hình chữ nhật (như hộp sữa, viên gạch)",
        "Khối cầu" to "tròn đều mọi phía (như quả bóng)",
        "Khối trụ" to "hai đáy tròn, thân thẳng (như lon nước)"
    )
    /* ============================ khả năng xảy ra, góc, hai bước tính, hỗn số, tỉ lệ ============================ */

    private fun probability(s: String, ans: String): KidTheory? {
        val kinds = listOf("Chắc chắn", "Có thể", "Không thể")
        val pick = kinds.firstOrNull { same(it, ans) } ?: return null
        if (!s.contains("Khả năng")) return null
        val want = Regex("""được (?:quả bóng|viên bi) (?:màu )?([\p{L}]+)""").find(s)?.groupValues?.get(1) ?: return null
        val only = Regex("""chỉ có (\d+) (?:quả bóng|viên bi) (?:màu )?([\p{L}]+)""").find(s)
        val mix = Regex("""có (\d+) (?:quả bóng|viên bi) (?:màu )?([\p{L}]+) và (\d+) (?:quả bóng|viên bi) (?:màu )?([\p{L}]+)""").find(s)
        val inside: List<String> = when {
            only != null -> listOf("${only.groupValues[1]} ${only.groupValues[2]}")
            mix != null -> listOf("${mix.groupValues[1]} ${mix.groupValues[2]}", "${mix.groupValues[3]} ${mix.groupValues[4]}")
            else -> return null
        }
        val colors = if (only != null) listOf(only.groupValues[2]) else listOf(mix!!.groupValues[2], mix.groupValues[4])
        val expect = when { want !in colors -> "Không thể"; colors.size == 1 -> "Chắc chắn"; else -> "Có thể" }
        if (expect != pick) return null
        val why = when (expect) {
            "Chắc chắn" -> "Trong hộp chỉ có màu $want, lấy quả nào cũng là màu $want."
            "Không thể" -> "Trong hộp không có màu $want nào."
            else -> "Trong hộp có cả màu $want và màu khác, nên có thể lấy được hoặc không."
        }
        return KidTheory("Khả năng xảy ra",
            listOf(
                listOf(P("Chắc chắn", GREEN), T(": chỉ có loại đó.")),
                listOf(P("Có thể", BLUE), T(": có loại đó và cả loại khác.")),
                listOf(P("Không thể", PURPLE), T(": không có loại đó."))
            ), emptyList(), s,
            listOf(
                TheoryStep("Xem trong hộp có gì", inside.map { P(it, BLUE) }),
                TheoryStep("Có màu $want không?", listOf(P(expect, if (expect == "Chắc chắn") GREEN else if (expect == "Có thể") BLUE else PURPLE)), why)
            ), "Vậy khả năng là: $expect")
    }

    private val ANGLES = linkedMapOf(
        "Góc nhọn" to "bé hơn góc vuông",
        "Góc vuông" to "như góc của tờ giấy, 90°",
        "Góc tù" to "lớn hơn góc vuông, bé hơn góc bẹt",
        "Góc bẹt" to "bằng 2 góc vuông, 180°"
    )

    private fun angleFacts(s: String, ans: String): KidTheory? {
        if (!s.contains("góc")) return null
        val tiles = ANGLES.map { (k, v) -> Tile(k, v, BLUE) }
        val rule = listOf(listOf(T("So sánh với "), P("góc vuông", ORANGE), T(": bé hơn là góc nhọn, lớn hơn là góc tù, bằng 2 góc vuông là góc bẹt.")))
        val key = ANGLES.keys.firstOrNull { same(it, ans) }
        if (key != null) return KidTheory("Các loại góc", rule, tiles.map { if (it.top == key) it.copy(hue = GREEN) else it }, s,
            listOf(TheoryStep("So với góc vuông", listOf(P(key, GREEN)), "$key: ${ANGLES[key]}.")), "Vậy đáp án là $key")
        val m = Regex("""(\d+) góc vuông""").find(ans) ?: return null
        if (!Regex("hình chữ nhật|hình vuông", RegexOption.IGNORE_CASE).containsMatchIn(s) || m.groupValues[1] != "4") return null
        return KidTheory("Góc vuông", rule, tiles, s,
            listOf(TheoryStep("Đếm các góc", listOf(T("Hình chữ nhật, hình vuông có 4 góc, "), P("cả 4 góc đều vuông", GREEN)))), "Vậy đáp án là $ans")
    }

    /** Bài toán hai bước tính: nhân rồi trừ/cộng, hoặc tìm số thứ hai rồi tìm tổng. */
    private fun twoStep(s: String, ans: String): KidTheory? {
        if (!s.contains("Hỏi")) return null
        val ns = nums(s).filter { !it.contains(',') }.map { num(it) }
        if (ns.size != 3 && !(ns.size == 2 && (s.contains("cả hai") || s.contains("Cả hai")))) return null
        val ansN = Regex("""\d[\d ]*""").find(ans)?.value?.trim()?.let { num(it) } ?: return null
        val unit = ans.substringAfter(Regex("""\d[\d ]*""").find(ans)!!.value, "").trim().let { if (it.isEmpty()) "" else " $it" }
        val rule = listOf(listOf(P("Bước 1", BLUE), T(": tìm số chưa biết đề cần trước.")), listOf(P("Bước 2", GREEN), T(": dùng số vừa tìm để trả lời câu hỏi.")))
        if (ns.size == 2) {
            val (a, d) = ns
            val more = s.contains("nhiều hơn"); val less = s.contains("ít hơn"); val times = Regex("""gấp \d+ lần""").containsMatchIn(s)
            val second = if (times) a * d else if (more) a + d else if (less) a - d else return null
            if (a + second != ansN) return null
            val opS = if (times) "×" else if (more) "+" else "−"
            return KidTheory("Bài toán giải bằng hai bước tính", rule, emptyList(), s,
                listOf(
                    TheoryStep("Bước 1: tìm số của bên thứ hai", listOf(T("${f(a)} $opS ${f(d)} = "), P(f(second), BLUE)), if (times) "Gấp lên mấy lần → nhân." else if (more) "Nhiều hơn → cộng." else "Ít hơn → trừ."),
                    TheoryStep("Bước 2: tìm cả hai", listOf(T("${f(a)} + ${f(second)} = "), P(f(ansN) + unit, GREEN)))
                ), "Vậy đáp số là ${f(ansN)}$unit")
        }
        if (!s.contains("mỗi")) return null
        val (x, y, z) = ns
        val p = x * y
        val (title2, line2) = when {
            p - z == ansN && p >= z -> "Bước 2: bớt đi phần đã dùng" to "${f(p)} − ${f(z)} = "
            z - p == ansN -> "Bước 2: lấy số tiền đưa trừ số tiền phải trả" to "${f(z)} − ${f(p)} = "
            p + z == ansN -> "Bước 2: cộng thêm" to "${f(p)} + ${f(z)} = "
            else -> return null
        }
        return KidTheory("Bài toán giải bằng hai bước tính", rule, emptyList(), s,
            listOf(
                TheoryStep("Bước 1: tìm tất cả (nhiều nhóm bằng nhau → nhân)", listOf(T("${f(x)} × ${f(y)} = "), P(f(p), BLUE))),
                TheoryStep(title2, listOf(T(line2), P(f(ansN) + unit, GREEN)))
            ), "Vậy đáp số là ${f(ansN)}$unit")
    }

    private fun mixedNumber(s: String, ans: String): KidTheory? {
        val m = Regex("""Hỗn số '(\d+) và (\d+)/(\d+)'""").find(s) ?: return null
        val w = m.groupValues[1]; val fr = "${m.groupValues[2]}/${m.groupValues[3]}"
        if (!same("$w $fr", ans)) return null
        return KidTheory("Hỗn số", listOf(listOf(T("Hỗn số gồm "), P("phần nguyên", BLUE), T(" và "), P("phần phân số", GREEN), T(" (bé hơn 1). Viết phần nguyên trước, phân số sau."))),
            listOf(Tile("2 1/2", "đọc: hai và một phần hai", BLUE)), s,
            listOf(TheoryStep("Viết phần nguyên rồi đến phân số", listOf(P(w, BLUE), P(fr, GREEN)))), "Vậy hỗn số viết là $ans")
    }

    private fun mapScale(s: String, ans: String): KidTheory? {
        val m = Regex("""tỉ lệ 1 : ([\d ]+), .*? là (\d+) cm""").find(s) ?: return null
        val k = num(m.groupValues[1].trim()); val d = m.groupValues[2].toLong()
        val cm = d * k
        val (v, u) = when {
            ans.endsWith("km") -> (cm / 100_000) to "km"
            ans.endsWith(" m") || ans.endsWith("m") && !ans.endsWith("cm") -> (cm / 100) to "m"
            else -> cm to "cm"
        }
        if (!same("${f(v)} $u", ans)) return null
        return KidTheory("Tỉ lệ bản đồ", listOf(listOf(T("Tỉ lệ 1 : ${f(k)} nghĩa là "), P("1 cm trên bản đồ", BLUE), T(" ứng với "), P("${f(k)} cm ngoài thực tế", GREEN), T("."))), emptyList(), s,
            listOf(
                TheoryStep("Nhân với tỉ lệ", listOf(T("$d × ${f(k)} = "), P("${f(cm)} cm", BLUE))),
                TheoryStep("Đổi đơn vị", listOf(T("${f(cm)} cm = "), P("${f(v)} $u", GREEN)), if (u == "m") "100 cm = 1 m." else if (u == "km") "100 000 cm = 1 km." else null)
            ), "Vậy khoảng cách thực tế là ${f(v)} $u")
    }

    private fun shapeFacts(s: String, ans: String): KidTheory? {
        if (!Regex("có dạng hình|Hình phẳng nào|khối hình học|hình khối gì").containsMatchIn(s)) return null
        val key = SHAPE_INFO.keys.firstOrNull { same(it, ans) } ?: return null
        val isBlock = key.startsWith("Khối")
        val tiles = SHAPE_INFO.entries.filter { it.key.startsWith("Khối") == isBlock }.map { Tile(it.key, it.value, if (it.key == key) GREEN else BLUE) }
        return KidTheory(if (isBlock) "Các khối hình" else "Các hình phẳng",
            listOf(listOf(T("Nhận dạng hình bằng cách "), P("đếm cạnh, góc", BLUE), T(" hoặc xem "), P("mặt cong hay phẳng", GREEN), T("."))),
            tiles, s,
            listOf(TheoryStep("So với đặc điểm của từng hình", listOf(P(key, GREEN)), "$key: ${SHAPE_INFO[key]}.")),
            "Vậy đáp án là $key")
    }

    private fun shapePattern(s: String, ans: String): KidTheory? {
        if (!s.contains("theo quy luật")) return null
        val items = s.substringAfter(":").split(",").map { it.trim().trimEnd('.', '?') }.filter { it.isNotEmpty() && !it.startsWith("...") && !it.startsWith("Hình nào") }
        if (items.size < 3) return null
        val p = (1 until items.size).firstOrNull { per -> items.indices.all { items[it] == items[it % per] } } ?: return null
        val next = items[items.size % p]
        if (!same(next, ans)) return null
        val unit = items.take(p)
        return KidTheory("Tìm quy luật", listOf(listOf(T("Tìm "), P("nhóm hình lặp lại", ORANGE), T(", rồi đếm tiếp theo nhóm đó."))), emptyList(), s,
            listOf(
                TheoryStep("Tìm nhóm lặp lại", unit.map { P(it, ORANGE) }, "Cứ ${unit.size} hình lại lặp lại một lần."),
                TheoryStep("Hình tiếp theo", listOf(P(next, GREEN)))
            ), "Vậy hình tiếp theo là $next")
    }

    private fun leftMiddleRight(s: String, ans: String): KidTheory? {
        val m = Regex("""từ trái sang phải: ([^.]+)\. Hình nào ở (?:vị trí )?(?:ở )?(bên trái|giữa|bên phải)""").find(s) ?: return null
        val items = m.groupValues[1].split(",").map { it.trim() }; if (items.size != 3) return null
        val pick = when (m.groupValues[2]) { "bên trái" -> items[0]; "giữa" -> items[1]; else -> items[2] }
        if (!same(pick, ans)) return null
        return KidTheory("Vị trí: trái – giữa – phải", listOf(listOf(T("Đọc từ trái sang phải: hình đầu "), P("bên trái", BLUE), T(", hình thứ hai "), P("ở giữa", GREEN), T(", hình cuối "), P("bên phải", PURPLE), T("."))), emptyList(), s,
            listOf(TheoryStep("Ghi vị trí từng hình", listOf(P("trái: ${items[0]}", BLUE), P("giữa: ${items[1]}", GREEN), P("phải: ${items[2]}", PURPLE)))),
            "Vậy hình ở ${m.groupValues[2]} là $pick")
    }

    private fun equalGroups(s: String, ans: String): KidTheory? {
        val ansN = Regex("""\d+""").find(ans)?.value?.toLongOrNull() ?: return null
        val ns = nums(s).filter { !it.contains(',') }.map { num(it) }
        if (ns.size != 2) return null
        val (a, b) = ns
        if (s.contains("Mỗi") && a * b == ansN) return KidTheory("Nhiều nhóm bằng nhau → phép nhân",
            listOf(listOf(T("Có nhiều nhóm, "), P("mỗi nhóm bằng nhau", BLUE), T(" → lấy số trong một nhóm "), P("nhân", GREEN), T(" số nhóm."))), emptyList(), s,
            listOf(TheoryStep("Một nhóm có $a, có $b nhóm", visual = Visual.Bars(List(minOf(b, 6).toInt()) { BarRow("", listOf(Seg(1f, BLUE, "$a"))) })),
                TheoryStep("Nhân", listOf(T("$a × $b = "), P(ans, GREEN)))), "Vậy đáp số là $ans")
        if (s.contains("chia đều") && b != 0L && a % b == 0L && a / b == ansN && !s.contains("nhiều nhất")) return KidTheory("Chia đều → phép chia",
            listOf(listOf(P("Chia đều", PURPLE), T(" một số cho nhiều phần bằng nhau → làm phép chia."))), emptyList(), s,
            listOf(TheoryStep("Chia đều", listOf(T("$a : $b = "), P(ans, PURPLE)))), "Vậy đáp số là $ans")
        if (s.contains("nhiều nhất") && b != 0L && a / b == ansN) return KidTheory("Chia có dư trong thực tế",
            listOf(listOf(T("Chia lấy phần nguyên; phần dư "), P("không đủ một nhóm", ORANGE), T(" thì không tính."))), emptyList(), s,
            listOf(TheoryStep("Chia", listOf(T("$a : $b = ${a / b} (dư ${a % b})"))), TheoryStep("Chỉ lấy số nhóm đầy đủ", listOf(P(ans, GREEN)))), "Vậy đáp số là $ans")
        // "Cần ít nhất mấy chuyến/hộp": phần dư vẫn cần thêm một chuyến nữa
        if (s.contains("nhiều nhất") && b != 0L && a % b != 0L && a / b + 1 == ansN) return KidTheory("Chia có dư trong thực tế",
            listOf(listOf(T("Phần dư vẫn cần chở/đựng, nên "), P("thêm 1", ORANGE), T(" vào thương."))), emptyList(), s,
            listOf(
                TheoryStep("Chia", listOf(T("$a : $b = ${a / b} (dư ${a % b})"))),
                TheoryStep("Còn dư ${a % b} nên cần thêm 1", listOf(T("${a / b} + 1 = "), P(ans, GREEN)), "${a / b} lần chỉ chở hết ${a / b * b}, còn ${a % b} chưa có chỗ.")
            ), "Vậy đáp số là $ans")
        return null
    }

    /** Dấu hiệu chia hết cho 2, 3, 5, 9. */
    private fun divisibility(s: String, ans: String): KidTheory? {
        val m = Regex("""Nhận xét nào đúng về số (\d+)""").find(s) ?: return null
        val n = m.groupValues[1].toLong()
        val digits = n.toString().map { it.digitToInt() }; val sum = digits.sum(); val last = digits.last()
        val by3 = sum % 3 == 0; val by5 = last == 0 || last == 5
        val expect = when { by3 && by5 -> "Chia hết cho cả 3 và 5"; by3 -> "Chia hết cho 3"; by5 -> "Chia hết cho 5"; else -> "Không chia hết cho 3 và 5" }
        if (!same(expect, ans)) return null
        return KidTheory("Dấu hiệu chia hết",
            listOf(
                listOf(T("Chia hết cho 5: chữ số tận cùng là "), P("0 hoặc 5", BLUE), T(".")),
                listOf(T("Chia hết cho 3: "), P("tổng các chữ số", GREEN), T(" chia hết cho 3."))
            ), emptyList(), s,
            listOf(
                TheoryStep("Xét chữ số tận cùng", listOf(T("Chữ số tận cùng là $last → "), P(if (by5) "chia hết cho 5" else "không chia hết cho 5", if (by5) GREEN else PURPLE))),
                TheoryStep("Xét tổng các chữ số", listOf(T(digits.joinToString(" + ") + " = $sum → "), P(if (by3) "chia hết cho 3" else "không chia hết cho 3", if (by3) GREEN else PURPLE)))
            ), "Vậy đáp án là: $expect")
    }

    /** Gà – chó (giả thiết tạm): giả sử tất cả là gà. */
    private fun chickenDog(s: String, ans: String): KidTheory? {
        val m = Regex("""Vừa gà vừa chó có (\d+) con, đếm được tất cả (\d+) cái chân\. Hỏi có bao nhiêu con (chó|gà)""").find(s) ?: return null
        val heads = m.groupValues[1].toLong(); val legs = m.groupValues[2].toLong(); val askDog = m.groupValues[3] == "chó"
        val allChicken = heads * 2; val missing = legs - allChicken
        if (missing < 0 || missing % 2 != 0L) return null
        val dogs = missing / 2; val chickens = heads - dogs
        val res = if (askDog) dogs else chickens
        if (!same("$res con", ans)) return null
        return KidTheory("Giả thiết tạm (gà – chó)",
            listOf(listOf(T("Giả sử "), P("tất cả đều là gà", BLUE), T(", tính số chân thiếu; mỗi con chó hơn con gà "), P("2 chân", ORANGE), T(".")))
            , emptyList(), s,
            listOf(
                TheoryStep("Giả sử cả $heads con đều là gà", listOf(T("$heads × 2 = "), P("$allChicken chân", BLUE))),
                TheoryStep("Số chân còn thiếu", listOf(T("$legs − $allChicken = "), P("$missing chân", ORANGE))),
                TheoryStep("Mỗi con chó hơn con gà 2 chân", listOf(T("$missing : 2 = "), P("$dogs con chó", GREEN))),
            ) + (if (!askDog) listOf(TheoryStep("Số con gà", listOf(T("$heads − $dogs = "), P("$chickens con", GREEN)))) else emptyList()),
            "Vậy có $res con ${m.groupValues[3]}")
    }

    private fun circleRadius(s: String, ans: String): KidTheory? {
        val m = Regex("""có (đường kính|bán kính) (\d+) cm\. (Bán kính|Đường kính)""").find(s) ?: return null
        val v = m.groupValues[2].toLong(); val toR = m.groupValues[1] == "đường kính"
        if (toR && v % 2 != 0L) return null
        val res = if (toR) v / 2 else v * 2
        if (!same("$res cm", ans)) return null
        return KidTheory("Bán kính và đường kính", listOf(listOf(P("Đường kính", BLUE), T(" = "), P("bán kính × 2", GREEN), T("."))), emptyList(), s,
            listOf(TheoryStep(if (toR) "Bán kính = đường kính : 2" else "Đường kính = bán kính × 2", listOf(T(if (toR) "$v : 2 = " else "$v × 2 = "), P("$res cm", GREEN)))), "Vậy đáp số là $res cm")
    }

    private fun squareFromPerimeter(s: String, ans: String): KidTheory? {
        val m = Regex("""hình vuông có chu vi là (\d+) cm\. Diện tích""").find(s) ?: return null
        val pr = m.groupValues[1].toLong(); if (pr % 4 != 0L) return null
        val a = pr / 4; if (!same("${a * a} cm²", ans)) return null
        return KidTheory("Từ chu vi tìm diện tích hình vuông", listOf(listOf(T("Cạnh = "), P("chu vi : 4", BLUE), T(". Diện tích = "), P("cạnh × cạnh", GREEN), T("."))), emptyList(), s,
            listOf(TheoryStep("Tìm cạnh", listOf(T("$pr : 4 = "), P("$a cm", BLUE))), TheoryStep("Tính diện tích", listOf(T("$a × $a = "), P("${a * a} cm²", GREEN)))), "Vậy diện tích là ${a * a} cm²")
    }

    private fun increase(s: String, ans: String): KidTheory? {
        if (!Regex("tăng thêm|thêm|mua thêm|được thêm").containsMatchIn(s)) return null
        val ns = Regex("""(?<![\p{L}\d])(\d+)(?:°C)?""").findAll(s).map { it.groupValues[1].toLong() }.toList()
        if (ns.size != 2) return null
        val r = ns[0] + ns[1]
        val unit = ans.substringAfter(r.toString(), "").trim()
        if (!ans.startsWith(r.toString())) return null
        return KidTheory("Thêm vào → phép cộng", listOf(listOf(P("Thêm, tăng thêm", GREEN), T(" → làm phép cộng."))), emptyList(), s,
            listOf(TheoryStep("Cộng phần thêm vào", listOf(T("${ns[0]} + ${ns[1]} = "), P(ans, GREEN)))), "Vậy đáp số là $ans")
    }

    private fun decimalOps(s: String, ans: String): KidTheory? {
        val m = Regex("""(\d+(?:,\d+)?) ([+\-×:]) (\d+(?:,\d+)?) = \?""").find(s) ?: return null
        if (!m.groupValues[1].contains(',') && !m.groupValues[3].contains(',')) return null
        val a = dec(m.groupValues[1]); val op = m.groupValues[2]; val b = dec(m.groupValues[3])
        val res = when (op) { "+" -> a + b; "-" -> a - b; "×" -> a * b; else -> if (b.signum() == 0) return null else a.divide(b, 6, RoundingMode.HALF_UP) }
        val ansD = runCatching { dec(ans) }.getOrNull() ?: return null
        if (res.compareTo(ansD) != 0) return null
        val sa = m.groupValues[1]; val sb = m.groupValues[3]
        val opS = if (op == "-") "−" else op
        val steps = when (op) {
            "+", "-" -> {
                // Thêm chữ số 0 cho cùng số chữ số sau dấu phẩy rồi mới tính như số tự nhiên
                val k = maxOf(a.scale(), b.scale())
                val ia = a.setScale(k).unscaledValue(); val ib = b.setScale(k).unscaledValue()
                val pb = b.setScale(k).toPlainString().replace('.', ',')
                listOf(
                    TheoryStep("Đặt tính: dấu phẩy thẳng cột", listOf(P(a.setScale(k).toPlainString().replace('.', ','), BLUE), T(" $opS "), P(pb, BLUE)),
                        if (a.scale() != b.scale()) "Viết thêm chữ số 0 cho hai số có cùng số chữ số sau dấu phẩy." else "Phần nguyên thẳng phần nguyên, phần thập phân thẳng phần thập phân."),
                    TheoryStep("${if (op == "+") "Cộng" else "Trừ"} như số tự nhiên", listOf(T("$ia $opS $ib = ${if (op == "+") ia + ib else ia - ib}"))),
                    TheoryStep("Đặt dấu phẩy thẳng cột ở kết quả", listOf(P(ans, GREEN)), "Kết quả có $k chữ số sau dấu phẩy.")
                )
            }
            "×" -> {
                val k = (if (sa.contains(',')) sa.substringAfter(',').length else 0) + (if (sb.contains(',')) sb.substringAfter(',').length else 0)
                val decX = if (sa.contains(',')) a else b; val n = if (sa.contains(',')) b else a
                val whole = decX.setScale(0, RoundingMode.HALF_UP)
                val gap = whole - decX
                val mental = s.contains("Tính nhẩm") && n.scale() == 0 && decX.scale() == 1 && gap.abs() <= BigDecimal("0.2") && gap.signum() != 0
                if (mental) {
                    // Tính nhẩm: làm tròn số thập phân thành số tự nhiên, nhân, rồi bù phần chênh
                    val wn = whole * n; val gn = gap.abs() * n
                    listOf(
                        TheoryStep("Làm tròn cho dễ nhân", listOf(T("${fd(decX)} → "), P(fd(whole), BLUE)), "${fd(decX)} ${if (gap.signum() > 0) "kém" else "hơn"} ${fd(whole)} là ${fd(gap.abs())}."),
                        TheoryStep("Nhân số tròn", listOf(T("${fd(whole)} × ${fd(n)} = "), P(fd(wn), BLUE))),
                        TheoryStep(if (gap.signum() > 0) "Bớt phần đã thêm" else "Thêm phần đã bớt", listOf(T("${fd(wn)} ${if (gap.signum() > 0) "−" else "+"} ${fd(gap.abs())} × ${fd(n)} = ${fd(wn)} ${if (gap.signum() > 0) "−" else "+"} ${fd(gn)} = "), P(ans, GREEN)))
                    )
                } else listOf(
                    TheoryStep("Nhân như số tự nhiên", listOf(T("${sa.replace(",", "")} × ${sb.replace(",", "")} = ${res.movePointRight(k).toBigInteger()}"))),
                    TheoryStep("Đếm chữ số phần thập phân", listOf(T("Có "), P("$k chữ số", ORANGE), T(" sau dấu phẩy")), "Tách $k chữ số từ phải sang trái."),
                    TheoryStep("Đặt dấu phẩy", listOf(P(ans, GREEN)))
                )
            }
            else -> {
                if (b.scale() > 0 && b.stripTrailingZeros().scale() > 0) return null
                val bi = b.toBigInteger(); val ai = a.toBigInteger()
                val q0 = ai / bi; val r0 = ai % bi
                listOf(
                    TheoryStep("Chia phần nguyên", listOf(T("$ai : $bi = "), P(q0, BLUE), T(" (dư $r0)"))),
                    TheoryStep("Viết dấu phẩy vào thương, chia tiếp", listOf(T("Hạ chữ số sau dấu phẩy (hoặc thêm 0) xuống cạnh số dư $r0")), "Chia tiếp như chia số tự nhiên."),
                    TheoryStep("Kết quả", listOf(P(ans, GREEN)))
                )
            }
        }
        return KidTheory("Tính với số thập phân",
            listOf(
                listOf(T("Cộng, trừ: "), P("dấu phẩy thẳng cột", ORANGE), T(".")),
                listOf(T("Nhân: nhân như số tự nhiên, rồi "), P("đếm chữ số sau dấu phẩy", ORANGE), T(" để đặt dấu phẩy."))
            ),
            listOf(Tile("1,2 + 3,5", "= 4,7", BLUE), Tile("1,2 × 3", "12 × 3 = 36 → 3,6", ORANGE)), s, steps, "Vậy kết quả là $ans")
    }
}
