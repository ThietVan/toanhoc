package com.example.toanhoc.ui.canvas

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/* ==========================================================================================
 * BỘ PHÂN TÍCH SƠ ĐỒ (thuần Kotlin, không phụ thuộc Compose → chạy được trong unit test)
 *
 * Đọc ĐỀ BÀI + biểu thức (primaryLabel) + các đáp án để dựng mô hình sơ đồ đúng với câu hỏi.
 * Nguyên tắc:
 *   1. Sơ đồ chỉ thể hiện DỮ KIỆN đề cho. Ẩn số luôn là "?" – không bao giờ in đáp án.
 *   2. Không nhận ra dạng bài → trả về null → KHÔNG vẽ (tốt hơn vẽ sai).
 * ======================================================================================== */

sealed interface Diagram

/* ---------------------------------- Tia số ---------------------------------- */

enum class Mark { GIVEN, UNKNOWN, PLAIN }

data class NLPoint(val v: Double, val mark: Mark)

data class NLJump(
    val from: Double,
    val to: Double,
    val label: String,
    val subtract: Boolean = false,
    val unknown: Boolean = false
)

/** Ngoặc dưới trục: độ dài đoạn [from, to]. */
data class NLSpan(val from: Double, val to: Double, val label: String, val unknown: Boolean)

data class NumberLineDiagram(
    val min: Double,
    val max: Double,
    val step: Double,
    val labelEvery: Int,
    val points: List<NLPoint> = emptyList(),
    val jumps: List<NLJump> = emptyList(),
    val spans: List<NLSpan> = emptyList(),
    val unit: String = "",
    val ruler: Boolean = false
) : Diagram {
    val tickCount: Int get() = ((max - min) / step).roundToLong().toInt()

    /**
     * Nhãn số dưới trục. Vạch trùng điểm "?" hoặc điểm trung gian (PLAIN) bị ẩn để không lộ đáp án.
     * Điểm GIVEN nằm ngoài vạch chia được thêm nhãn riêng.
     */
    fun axisLabels(): List<Pair<Double, String>> {
        val hidden = points.filter { it.mark != Mark.GIVEN }.map { it.v }
        val given = points.filter { it.mark == Mark.GIVEN }.map { it.v }
        val out = mutableListOf<Pair<Double, String>>()
        for (i in 0..tickCount) {
            val v = min + i * step
            val isGiven = given.any { abs(it - v) < step * 0.01 }
            if (hidden.any { abs(it - v) < step * 0.5 } && !isGiven) continue
            if (!isGiven && i % labelEvery != 0 && i != tickCount) continue
            // nhường chỗ cho nhãn điểm GIVEN nằm sát bên
            if (!isGiven && given.any { abs(it - v) in (step * 0.01)..(step * labelEvery * 0.6) }) continue
            out += v to fmtNum(v)
        }
        given.filter { g -> out.none { abs(it.first - g) < step * 0.01 } }.forEach { out += it to fmtNum(it) }
        return out.sortedBy { it.first }
    }
}

/* ------------------------------- Sơ đồ đoạn thẳng ------------------------------- */

data class BarSeg(
    val units: Double,
    val label: String? = null,
    val unknown: Boolean = false,
    val removed: Boolean = false,
    val faint: Boolean = false
)

data class BarRow(val name: String, val segs: List<BarSeg>, val askMark: Boolean = false) {
    val totalUnits: Double get() = segs.sumOf { it.units }
}

/** Ngoặc trên một hàng, từ đầu hàng tới hết hàng (tổng của hàng đó). */
data class RowBrace(val row: Int, val label: String, val unknown: Boolean)

/** Ngoặc dọc bên phải gộp nhiều hàng (tổng cả hai). */
data class TotalBrace(val label: String, val unknown: Boolean)

/** Phần chênh lệch giữa cuối hàng ngắn và cuối hàng dài. */
data class GapMark(val label: String, val unknown: Boolean)

data class BarDiagram(
    val rows: List<BarRow>,
    val rowBrace: RowBrace? = null,
    val totalBrace: TotalBrace? = null,
    val gap: GapMark? = null
) : Diagram

/* ---------------------------------- Đặt tính ---------------------------------- */

data class VerticalDiagram(val a: String, val op: Char, val b: String) : Diagram

/* ---------------------------------- Hình học ---------------------------------- */

enum class GeoKind {
    POLYLINE, TRIANGLE_SIDES, QUAD_SIDES, RECTANGLE, SQUARE, CIRCLE_RADIUS, CIRCLE_DIAMETER,
    SEGMENT_MIDPOINT, RECT_AND_SQUARE, PARALLELOGRAM, RHOMBUS, TRIANGLE_HEIGHT, TRAPEZOID
}

/** [labels]: nhãn kích thước theo thứ tự quy ước của từng loại hình. "?" = cần tìm. */
data class GeometryDiagram(
    val kind: GeoKind,
    val labels: List<String> = emptyList(),
    val caption: String? = null
) : Diagram

/* ---------------------------------- Dãy hình ---------------------------------- */

data class ShapesDiagram(
    val shapes: List<String>,
    val showNames: Boolean,
    val showPositions: Boolean
) : Diagram

/* ==========================================================================================
 *                                       PHÂN TÍCH
 * ======================================================================================== */

object BlueprintInterpreter {

    private const val N = """\d+(?:[  ]\d{3})*(?:,\d+)?"""
    private val NUM = Regex(N)

    fun interpret(
        type: String,
        primaryLabel: String,
        secondaryLabel: String,
        statement: String,
        options: List<String>
    ): Diagram? {
        val st = statement.trim().ifEmpty { primaryLabel }
        val expr = primaryLabel.trim().takeIf { it.isNotEmpty() && it != st }
        return runCatching {
            when (type) {
                "NUMBER_LINE" -> numberLine(st, expr)
                "SINGAPORE_BAR_MODEL" -> barModel(st, expr)
                "VERTICAL_ARITHMETIC" -> vertical(st, expr)
                "GEOMETRY_GRID" -> geometry(st)
                "SPATIAL_SHAPES" -> shapes(st, options, primaryLabel, secondaryLabel)
                else -> null
            }
        }.getOrNull()
    }

    /* ================================ TIA SỐ ================================ */

    private fun numberLine(st: String, expr: String?): NumberLineDiagram? {
        // 1. Số liền sau / liền trước
        Regex("""liền (sau|trước) của ($N)""").find(st)?.let { m ->
            val n = num(m.groupValues[2])
            val d = if (m.groupValues[1] == "sau") 1.0 else -1.0
            val lo = max(0.0, n - 5)
            return NumberLineDiagram(
                min = lo, max = lo + 10, step = 1.0, labelEvery = 1,
                points = listOf(NLPoint(n, Mark.GIVEN), NLPoint(n + d, Mark.UNKNOWN)),
                jumps = listOf(NLJump(n, n + d, if (d > 0) "+1" else "−1", subtract = d < 0))
            )
        }

        // 2. Thước kẻ: "vạch số A ... vạch số B"
        val marks = Regex("""vạch số ($N)""").findAll(st).map { num(it.groupValues[1]) }.toList()
        if (marks.size >= 2) {
            val a = min(marks[0], marks[1]); val b = max(marks[0], marks[1])
            val hi = max(10.0, ceil((b + 1) / 5.0) * 5)
            return NumberLineDiagram(
                min = 0.0, max = hi, step = 1.0, labelEvery = if (hi > 12) 2 else 1,
                points = listOf(NLPoint(a, Mark.GIVEN), NLPoint(b, Mark.GIVEN)),
                spans = listOf(NLSpan(a, b, "? cm", unknown = true)),
                unit = "cm", ruler = true
            )
        }

        // 3. Dãy số có quy luật: "...: s0; s1; s2; ..." hoặc "s0; s1; ...; s3"
        if (st.contains("dãy số")) {
            val tail = st.substringAfter(":")
            val tokens = tail.split(";").map { it.trim() }.filter { it.isNotEmpty() }
            val vals = tokens.map { t -> if (t.startsWith("..") || t.startsWith("…")) null else NUM.find(t)?.value?.let(::num) }
            val known = vals.filterNotNull()
            if (known.size >= 2) {
                val holeIdx = vals.indexOfFirst { it == null }
                val steps = vals.zipWithNext().mapNotNull { (x, y) -> if (x != null && y != null) y - x else null }
                val d = steps.firstOrNull()
                val consistent = d != null && steps.all { abs(it - d) < 1e-9 }
                val full = vals.toMutableList()
                if (consistent && holeIdx >= 0) {
                    full[holeIdx] = if (holeIdx > 0 && full[holeIdx - 1] != null) full[holeIdx - 1]!! + d!!
                    else full[holeIdx + 1]!! - d!!
                }
                val pts = full.mapIndexedNotNull { i, v ->
                    v?.let { NLPoint(it, if (i == holeIdx) Mark.UNKNOWN else Mark.GIVEN) }
                }
                val jumps = if (consistent) full.zipWithNext().mapIndexedNotNull { i, (x, y) ->
                    if (x == null || y == null) null
                    else {
                        val toHole = i == holeIdx || i + 1 == holeIdx
                        // chỉ ghi "?" trên bước nhảy đi VÀO ô trống; bước đi ra để trống cho gọn
                        val lbl = when {
                            i + 1 == holeIdx -> "?"
                            i == holeIdx -> ""
                            else -> (if (y >= x) "+" else "−") + fmtNum(abs(y - x))
                        }
                        NLJump(x, y, lbl, subtract = y < x, unknown = toHole)
                    }
                } else emptyList()
                return withRange(pts, jumps)
            }
        }

        // 4. Liệt kê số: lớn nhất / bé nhất / sắp xếp / so sánh
        if (Regex("""lớn nhất|bé nhất|Sắp xếp|so sánh""").containsMatchIn(st)) {
            val src = when {
                st.contains("so sánh") -> st.substringAfter("hai số")
                st.contains("Sắp xếp") -> st.substringAfter("các số").substringBefore("theo")
                else -> st.substringAfter(":")
            }
            val ns = NUM.findAll(src).map { num(it.value) }.distinct().toList()
            if (ns.isNotEmpty()) return withRange(ns.map { NLPoint(it, Mark.GIVEN) }, emptyList())
        }

        // 5. Biểu thức cộng/trừ
        val e = (expr ?: Regex("""(?:Tính|\?)\s*:\s*(.+)$""").find(st)?.groupValues?.get(1) ?: st)
            .replace("cm", "").replace("= ?", "").trim()
        val unit = if ((expr ?: st).contains("cm")) "cm" else ""

        Regex("""^($N)\s*\+\s*\?\s*=\s*($N)$""").find(e)?.let { m ->
            val a = num(m.groupValues[1]); val t = num(m.groupValues[2])
            return withRange(
                listOf(NLPoint(a, Mark.GIVEN), NLPoint(t, Mark.GIVEN)),
                listOf(NLJump(a, t, "+ ?", unknown = true)), unit
            )
        }
        Regex("""^($N)\s*-\s*\?\s*=\s*($N)$""").find(e)?.let { m ->
            val t = num(m.groupValues[1]); val a = num(m.groupValues[2])
            return withRange(
                listOf(NLPoint(t, Mark.GIVEN), NLPoint(a, Mark.GIVEN)),
                listOf(NLJump(t, a, "− ?", subtract = true, unknown = true)), unit
            )
        }
        Regex("""^($N)\s*([+\-])\s*($N)(?:\s*([+\-])\s*($N))?$""").find(e)?.let { m ->
            val a = num(m.groupValues[1])
            val b = num(m.groupValues[3])
            val op1 = m.groupValues[2]
            val mid = if (op1 == "+") a + b else a - b
            if (mid < 0) return null
            val jumps = mutableListOf(NLJump(a, mid, sign(op1) + fmtNum(b), subtract = op1 == "-"))
            val pts = mutableListOf(NLPoint(a, Mark.GIVEN))
            if (m.groupValues[4].isNotEmpty()) {
                val c = num(m.groupValues[5]); val op2 = m.groupValues[4]
                val end = if (op2 == "+") mid + c else mid - c
                if (end < 0) return null
                pts += NLPoint(mid, Mark.PLAIN); pts += NLPoint(end, Mark.UNKNOWN)
                jumps += NLJump(mid, end, sign(op2) + fmtNum(c), subtract = op2 == "-")
            } else {
                pts += NLPoint(mid, Mark.UNKNOWN)
            }
            return withRange(pts, jumps, unit)
        }
        return null
    }

    private fun sign(op: String) = if (op == "+") "+" else "−"

    /** Chọn khoảng & bước chia đẹp bao trọn mọi điểm. */
    private fun withRange(pts: List<NLPoint>, jumps: List<NLJump>, unit: String = ""): NumberLineDiagram? {
        if (pts.isEmpty()) return null
        val lo = pts.minOf { it.v }; val hi = pts.maxOf { it.v }
        if (lo >= 0 && hi <= 10) return NumberLineDiagram(0.0, 10.0, 1.0, 1, pts, jumps, unit = unit)
        if (lo >= 0 && hi <= 20) return NumberLineDiagram(0.0, 20.0, 1.0, 2, pts, jumps, unit = unit)
        val span = max(hi - lo, 1.0)
        val step = niceStep(span / 8.0)
        var a = floor(lo / step) * step - step
        var b = ceil(hi / step) * step + step
        if (lo >= 0) a = max(0.0, a)
        while ((b - a) / step > 14) b -= step
        if (b < hi) b = ceil(hi / step) * step
        val ticks = ((b - a) / step).roundToLong().toInt()
        return NumberLineDiagram(a, b, step, if (ticks > 10) 2 else 1, pts, jumps, unit = unit)
    }

    private fun niceStep(raw: Double): Double {
        val c = listOf(1.0, 2.0, 5.0, 10.0, 20.0, 25.0, 50.0, 100.0, 200.0, 250.0, 500.0,
            1000.0, 2000.0, 5000.0, 10000.0, 20000.0, 50000.0, 100000.0)
        return c.firstOrNull { it >= raw } ?: c.last()
    }

    /* =========================== SƠ ĐỒ ĐOẠN THẲNG =========================== */

    private fun barModel(st: String, expr: String?): BarDiagram? {
        fun r(p: String) = Regex(p).find(st)?.groupValues

        r("""Tách ($N) gồm ($N) và số mấy""")?.let { g ->
            val t = num(g[1]); val p = num(g[2])
            return BarDiagram(
                listOf(BarRow("", listOf(BarSeg(p, fmtNum(p)), BarSeg(t - p, "?", unknown = true)))),
                rowBrace = RowBrace(0, fmtNum(t), false)
            )
        }
        r("""Gộp ($N) và ($N) được""")?.let { g ->
            val a = num(g[1]); val b = num(g[2])
            return BarDiagram(
                listOf(BarRow("", listOf(BarSeg(a, fmtNum(a)), BarSeg(b, fmtNum(b))))),
                rowBrace = RowBrace(0, "?", true)
            )
        }
        r("""Số ($N) gồm mấy và mấy""")?.let { g ->
            val t = num(g[1])
            return BarDiagram(
                listOf(BarRow("", listOf(BarSeg(1.0, "?", unknown = true), BarSeg(1.0, "?", unknown = true)))),
                rowBrace = RowBrace(0, fmtNum(t), false)
            )
        }
        r("""có ($N) .*? chia vào 2 hộp\. Hộp thứ nhất có ($N)""")?.let { g ->
            val t = num(g[1]); val p = num(g[2])
            return BarDiagram(
                listOf(BarRow("", listOf(BarSeg(p, "Hộp 1: ${fmtNum(p)}"), BarSeg(t - p, "Hộp 2: ?", unknown = true)))),
                rowBrace = RowBrace(0, fmtNum(t), false)
            )
        }
        r("""gấp được ($N) .*?, trong đó có ($N)""")?.let { g ->
            val t = num(g[1]); val p = num(g[2])
            return BarDiagram(
                listOf(BarRow("", listOf(BarSeg(p, "Xanh: ${fmtNum(p)}"), BarSeg(t - p, "Đỏ: ?", unknown = true)))),
                rowBrace = RowBrace(0, fmtNum(t), false)
            )
        }
        r("""^(\S+) có ($N) [^,]*, (\S+) cho \S+ thêm ($N)""")?.let { g ->
            val a = num(g[2]); val b = num(g[4])
            return BarDiagram(
                listOf(BarRow(g[1], listOf(BarSeg(a, fmtNum(a)), BarSeg(b, "+${fmtNum(b)}")))),
                rowBrace = RowBrace(0, "?", true)
            )
        }
        r("""^(\S+) có ($N) [^,]*, \S+ cho (\S+) ($N)""")?.let { g ->
            val a = num(g[2]); val give = num(g[4])
            return BarDiagram(
                listOf(BarRow(g[1], listOf(BarSeg(a - give, "?", unknown = true), BarSeg(give, "Cho: ${fmtNum(give)}", removed = true)))),
                rowBrace = RowBrace(0, fmtNum(a), false)
            )
        }
        r("""^(\S+) có ($N) [^,]*, (\S+) có ($N)""")?.let { g ->
            val a = num(g[2]); val b = num(g[4])
            return BarDiagram(
                listOf(BarRow(g[1], listOf(BarSeg(a, fmtNum(a)))), BarRow(g[3], listOf(BarSeg(b, fmtNum(b))))),
                totalBrace = TotalBrace("?", true)
            )
        }
        r("""^(.+?) trồng được ($N) [^.]*\. (.+?) trồng được nhiều hơn .+? là ($N)""")?.let { g ->
            val a = num(g[2]); val d = num(g[4])
            return BarDiagram(
                listOf(
                    BarRow(g[1], listOf(BarSeg(a, fmtNum(a)))),
                    BarRow(g[3], listOf(BarSeg(a, null, faint = true), BarSeg(d, fmtNum(d))), askMark = true)
                )
            )
        }
        r("""^(.+?) trồng được ($N) [^,]*, (.+?) trồng được nhiều hơn .+? ($N)""")?.let { g ->
            val a = num(g[2]); val d = num(g[4])
            return BarDiagram(
                listOf(
                    BarRow(g[1], listOf(BarSeg(a, fmtNum(a)))),
                    BarRow(g[3], listOf(BarSeg(a, null, faint = true), BarSeg(d, fmtNum(d))))
                ),
                totalBrace = TotalBrace("?", true)
            )
        }
        r("""^(\S+) gấp được ($N) [^.]*\. (\S+) gấp được ít hơn \S+ ($N)""")?.let { g ->
            val a = num(g[2]); val d = num(g[4])
            return BarDiagram(
                listOf(
                    BarRow(g[1], listOf(BarSeg(a, fmtNum(a)))),
                    BarRow(g[3], listOf(BarSeg(max(a - d, a * 0.15), "?", unknown = true)))
                ),
                gap = GapMark(fmtNum(d), false)
            )
        }
        r("""^(\S+) có ($N) .+? và nhiều hơn (\S+) ($N)""")?.let { g ->
            val a = num(g[2]); val d = num(g[4])
            return BarDiagram(
                listOf(
                    BarRow(g[1], listOf(BarSeg(a, fmtNum(a)))),
                    BarRow(g[3], listOf(BarSeg(max(a - d, a * 0.15), "?", unknown = true)))
                ),
                gap = GapMark(fmtNum(d), false)
            )
        }
        r("""Bao (.+?) nặng ($N) (\S+), bao (.+?) nặng ($N)""")?.let { g ->
            val b = num(g[2]); val a = num(g[5]); val u = g[3]
            return BarDiagram(
                listOf(
                    BarRow(cap(g[1]), listOf(BarSeg(b, "${fmtNum(b)} $u"))),
                    BarRow(cap(g[4]), listOf(BarSeg(a, "${fmtNum(a)} $u")))
                ),
                gap = GapMark("?", true)
            )
        }
        r("""(Ngăn trên) có ($N) .*? (ngăn dưới) gấp ($N) lần""")?.let { g ->
            val p = num(g[2]); val k = num(g[4]).toInt().coerceIn(1, 10)
            return BarDiagram(
                listOf(
                    BarRow(g[1], listOf(BarSeg(p, fmtNum(p)))),
                    BarRow(cap(g[3]), List(k) { BarSeg(p, null) })
                ),
                totalBrace = TotalBrace("?", true)
            )
        }
        r("""Hai số có tổng bằng ($N) và hiệu bằng ($N)\. (Số lớn|Số bé)""")?.let { g ->
            val s = num(g[1]); val d = num(g[2]); val small = (s - d) / 2
            return BarDiagram(
                listOf(
                    BarRow("Số lớn", listOf(BarSeg(small, null), BarSeg(d, fmtNum(d))), askMark = g[3] == "Số lớn"),
                    BarRow("Số bé", listOf(BarSeg(small, null)), askMark = g[3] == "Số bé")
                ),
                totalBrace = TotalBrace(fmtNum(s), false)
            )
        }
        r("""Hai số có (tổng|hiệu) bằng ($N) và tỉ số là (\d+)/(\d+)\. (Số lớn|Số bé)""")?.let { g ->
            val v = num(g[2])
            val n1 = g[3].toInt(); val n2 = g[4].toInt()
            val lo = min(n1, n2).coerceIn(1, 12); val hi = max(n1, n2).coerceIn(1, 12)
            val big = BarRow("Số lớn", List(hi) { BarSeg(1.0, null) }, askMark = g[5] == "Số lớn")
            val smallRow = BarRow("Số bé", List(lo) { BarSeg(1.0, null) }, askMark = g[5] == "Số bé")
            return if (g[1] == "tổng") BarDiagram(listOf(big, smallRow), totalBrace = TotalBrace(fmtNum(v), false))
            else BarDiagram(listOf(big, smallRow), gap = GapMark(fmtNum(v), false))
        }
        // Dự phòng: biểu thức "a + b" / "a - b" → một thanh
        if (expr != null) {
            Regex("""^($N)\s*\+\s*($N)$""").find(expr)?.let { m ->
                val a = num(m.groupValues[1]); val b = num(m.groupValues[2])
                return BarDiagram(listOf(BarRow("", listOf(BarSeg(a, fmtNum(a)), BarSeg(b, fmtNum(b))))), rowBrace = RowBrace(0, "?", true))
            }
            Regex("""^($N)\s*-\s*($N)$""").find(expr)?.let { m ->
                val a = num(m.groupValues[1]); val b = num(m.groupValues[2])
                if (b >= a) return null
                return BarDiagram(listOf(BarRow("", listOf(BarSeg(b, fmtNum(b)), BarSeg(a - b, "?", unknown = true)))), rowBrace = RowBrace(0, fmtNum(a), false))
            }
        }
        return null
    }

    /* =============================== ĐẶT TÍNH =============================== */

    private fun vertical(st: String, expr: String?): VerticalDiagram? {
        val re = Regex("""($N)\s*([+\-×x:])\s*($N)""")
        val m = (expr?.let { re.find(it) }) ?: re.find(st.substringAfter(":")) ?: re.find(st) ?: return null
        val op = when (m.groupValues[2]) { "x" -> '×'; else -> m.groupValues[2][0] }
        return VerticalDiagram(compact(m.groupValues[1]), op, compact(m.groupValues[3]))
    }

    /* =============================== HÌNH HỌC =============================== */

    private fun geometry(st: String): GeometryDiagram? {
        val g = geometryShape(st) ?: return null
        if (g.caption != null) return g
        val s = st.lowercase()
        val cap = when {
            s.contains("diện tích") && !s.contains("chu vi là") -> "Diện tích: ?"
            s.contains("chu vi") -> "Chu vi: ?"
            else -> null
        }
        return g.copy(caption = cap)
    }

    private fun geometryShape(st: String): GeometryDiagram? {
        val s = st.lowercase()
        fun cms(): List<String> = Regex("""($N) ?(cm|dm|m)\b""").findAll(st).map { "${it.groupValues[1]} ${it.groupValues[2]}" }.toList()

        return when {
            s.contains("đường gấp khúc") -> {
                val segs = cms()
                if (segs.size < 2) null else GeometryDiagram(GeoKind.POLYLINE, segs, caption = "Độ dài: ?")
            }
            s.contains("hình bình hành") -> cms().takeIf { it.size >= 2 }?.let { GeometryDiagram(GeoKind.PARALLELOGRAM, it.take(2)) }
            s.contains("hình thoi") -> cms().takeIf { it.size >= 2 }?.let { GeometryDiagram(GeoKind.RHOMBUS, it.take(2)) }
            s.contains("hình thang") -> cms().takeIf { it.size >= 3 }?.let { GeometryDiagram(GeoKind.TRAPEZOID, it.take(3)) }
            s.contains("tam giác") && s.contains("chiều cao") ->
                cms().takeIf { it.size >= 2 }?.let { GeometryDiagram(GeoKind.TRIANGLE_HEIGHT, it.take(2)) }
            s.contains("tam giác") -> cms().takeIf { it.size >= 3 }?.let { GeometryDiagram(GeoKind.TRIANGLE_SIDES, it.take(3)) }
            s.contains("tứ giác") -> cms().takeIf { it.size >= 4 }?.let { GeometryDiagram(GeoKind.QUAD_SIDES, it.take(4)) }
            s.contains("trung điểm") -> cms().firstOrNull()?.let { GeometryDiagram(GeoKind.SEGMENT_MIDPOINT, listOf(it, "?")) }
            s.contains("hình tròn") && s.contains("đường kính") && s.indexOf("đường kính") < s.indexOf("bán kính") ->
                cms().firstOrNull()?.let { GeometryDiagram(GeoKind.CIRCLE_DIAMETER, listOf(it, "?")) }
            s.contains("hình tròn") && s.contains("bán kính") -> cms().firstOrNull()?.let {
                GeometryDiagram(GeoKind.CIRCLE_RADIUS, listOf(it, if (s.contains("đường kính")) "?" else ""))
            }
            s.contains("hình chữ nhật") && s.contains("hình vuông") && s.contains("góc vuông") ->
                GeometryDiagram(GeoKind.RECT_AND_SQUARE)
            s.contains("hình chữ nhật") -> cms().takeIf { it.size >= 2 }?.let { GeometryDiagram(GeoKind.RECTANGLE, it.take(2)) }
            s.contains("hình vuông") && s.contains("chu vi là") ->
                cms().firstOrNull()?.let { GeometryDiagram(GeoKind.SQUARE, listOf("?"), caption = "Chu vi: $it") }
            s.contains("hình vuông") -> cms().firstOrNull()?.let { GeometryDiagram(GeoKind.SQUARE, listOf(it)) }
            else -> null
        }
    }

    /* ================================ DÃY HÌNH ================================ */

    private val SHAPE_NAMES = listOf(
        "Khối hộp chữ nhật", "Khối lập phương", "Hình chữ nhật", "Hình vuông", "Hình tam giác", "Hình tròn"
    )

    private fun shapes(st: String, options: List<String>, primary: String, secondary: String): ShapesDiagram? {
        if (st.contains("từ trái sang phải")) {
            val list = st.substringAfter(":").substringBefore(".")
            val found = list.split(",").map { it.trim() }.mapNotNull { t -> SHAPE_NAMES.firstOrNull { it.equals(t, true) } }
            if (found.size >= 2) return ShapesDiagram(found, showNames = false, showPositions = true)
        }
        val opts = options.mapNotNull { o -> SHAPE_NAMES.firstOrNull { it.equals(o.trim(), true) } }
        if (opts.size >= 2) return ShapesDiagram(opts, showNames = true, showPositions = false)
        val fallback = listOf(primary, secondary).mapNotNull { o -> SHAPE_NAMES.firstOrNull { it.equals(o.trim(), true) } }
        return if (fallback.isNotEmpty()) ShapesDiagram(fallback, showNames = true, showPositions = false) else null
    }

    /* ================================ TIỆN ÍCH ================================ */

    private fun cap(s: String) = s.replaceFirstChar { it.uppercase() }

    private fun compact(s: String) = s.replace(" ", "").replace(" ", "")

    fun num(s: String): Double = s.replace(" ", "").replace(" ", "").replace(",", ".").toDouble()
}

/** 12 → "12", 2.5 → "2,5", 12345 → "12 345". */
fun fmtNum(v: Double): String {
    if (abs(v - v.roundToLong()) < 1e-9) {
        val s = abs(v.roundToLong()).toString()
        val body = if (s.length >= 5) s.reversed().chunked(3).joinToString(" ").reversed() else s
        return (if (v < 0) "-" else "") + body
    }
    return ("%.2f".format(java.util.Locale.US, v)).trimEnd('0').trimEnd('.').replace(".", ",")
}
