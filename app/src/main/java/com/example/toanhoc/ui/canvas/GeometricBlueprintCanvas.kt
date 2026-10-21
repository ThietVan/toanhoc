package com.example.toanhoc.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Hình học phẳng đúng với đề: tam giác/tứ giác theo độ dài cạnh, hình chữ nhật theo tỉ lệ dài–rộng,
 * hình tròn (bán kính/đường kính), trung điểm, đường gấp khúc, hình bình hành, thoi, thang, tam giác có đường cao.
 * Nét đứt cam = đại lượng cần tìm hoặc đường phụ.
 */
@Composable
fun GeometryView(d: GeometryDiagram, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer()
    val h = if (d.kind == GeoKind.SEGMENT_MIDPOINT || d.kind == GeoKind.POLYLINE) 150.dp else 196.dp
    Canvas(modifier.fillMaxWidth().height(h + if (d.caption != null) 30.dp else 0.dp)) {
        val area = Size(size.width, h.toPx())
        when (d.kind) {
            GeoKind.POLYLINE -> polyline(m, d.labels, area)
            GeoKind.TRIANGLE_SIDES -> triangleSides(m, d.labels, area)
            GeoKind.QUAD_SIDES -> quadSides(m, d.labels, area)
            GeoKind.RECTANGLE -> rectangle(m, d.labels, area, square = false)
            GeoKind.SQUARE -> rectangle(m, listOf(d.labels.firstOrNull() ?: "?"), area, square = true)
            GeoKind.CIRCLE_RADIUS -> circle(m, radius = d.labels[0], diameter = d.labels.getOrNull(1).orEmpty(), area = area, givenIsRadius = true)
            GeoKind.CIRCLE_DIAMETER -> circle(m, radius = d.labels.getOrNull(1).orEmpty(), diameter = d.labels[0], area = area, givenIsRadius = false)
            GeoKind.SEGMENT_MIDPOINT -> midpoint(m, d.labels, area)
            GeoKind.RECT_AND_SQUARE -> rectAndSquare(m, area)
            GeoKind.PARALLELOGRAM -> parallelogram(m, d.labels, area)
            GeoKind.RHOMBUS -> rhombus(m, d.labels, area)
            GeoKind.TRIANGLE_HEIGHT -> triangleHeight(m, d.labels, area)
            GeoKind.TRAPEZOID -> trapezoid(m, d.labels, area)
        }
        d.caption?.let { cap ->
            val unknown = cap.contains("?")
            pill(m, cap, Offset(size.width / 2f, h.toPx() + 14.dp.toPx()), if (unknown) Dg.Unknown else Dg.Given, unknown, fs = 13f)
        }
    }
}

/* ------------------------------------ tiện ích ------------------------------------ */

private fun value(label: String): Double? =
    Regex("""(\d+(?:,\d+)?)\s*(cm|dm|m)?""").find(label)?.let {
        val v = it.groupValues[1].replace(",", ".").toDouble()
        when (it.groupValues[2]) { "dm" -> v * 10; "m" -> v * 100; else -> v }
    }

private fun DrawScope.shape(points: List<Offset>, closed: Boolean = true) {
    val p = Path().apply {
        moveTo(points[0].x, points[0].y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
        if (closed) close()
    }
    if (closed) drawPath(p, Dg.ShapeFill)
    drawPath(p, Dg.Ink, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.vertex(m: TextMeasurer, name: String, at: Offset, center: Offset) {
    drawCircle(Dg.Ink, 3.dp.toPx(), at)
    var dx = at.x - center.x; var dy = at.y - center.y
    val l = sqrt(dx * dx + dy * dy).coerceAtLeast(1f); dx /= l; dy /= l
    textAt(m, name, Offset(at.x + dx * 13.dp.toPx(), at.y + dy * 13.dp.toPx()), Dg.Muted, 12f, FontWeight.SemiBold)
}

private fun centroid(ps: List<Offset>) = Offset(ps.sumOf { it.x.toDouble() }.toFloat() / ps.size, ps.sumOf { it.y.toDouble() }.toFloat() / ps.size)

/** Khớp một đa giác (toạ độ tùy ý) vào khung giữa, chừa lề cho nhãn. */
private fun fit(ps: List<Offset>, area: Size, margin: Float): List<Offset> {
    val minX = ps.minOf { it.x }; val maxX = ps.maxOf { it.x }
    val minY = ps.minOf { it.y }; val maxY = ps.maxOf { it.y }
    val w = (maxX - minX).coerceAtLeast(1f); val h = (maxY - minY).coerceAtLeast(1f)
    val s = min((area.width - 2 * margin) / w, (area.height - 2 * margin) / h).coerceAtMost(area.width * 0.6f / w)
    val ox = (area.width - w * s) / 2f; val oy = (area.height - h * s) / 2f
    return ps.map { Offset(ox + (it.x - minX) * s, oy + (it.y - minY) * s) }
}

private fun DrawScope.rightAngle(at: Offset, dir1: Offset, dir2: Offset) {
    val k = 9.dp.toPx()
    val a = Offset(at.x + dir1.x * k, at.y + dir1.y * k)
    val b = Offset(a.x + dir2.x * k, a.y + dir2.y * k)
    val c = Offset(at.x + dir2.x * k, at.y + dir2.y * k)
    drawPath(Path().apply { moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y) }, Dg.Muted, style = Stroke(1.3.dp.toPx()))
}

/* -------------------------------------- hình -------------------------------------- */

private fun DrawScope.polyline(m: TextMeasurer, labels: List<String>, area: Size) {
    val raw = mutableListOf(Offset(0f, 0f))
    labels.forEachIndexed { i, l ->
        val len = (value(l) ?: 5.0).toFloat()
        val dy = if (i % 2 == 0) -0.55f else 0.55f
        val dx = sqrt(1f - dy * dy)
        val last = raw.last()
        raw += Offset(last.x + dx * len, last.y + dy * len)
    }
    val ps = fit(raw, area, 28.dp.toPx())
    shape(ps, closed = false)
    labels.forEachIndexed { i, l ->
        // đoạn đi lên: nhãn phía trên-trái; đoạn đi xuống: nhãn phía trên-phải
        edgeLabel(m, l, ps[i], ps[i + 1], Offset((ps[i].x + ps[i + 1].x) / 2f, area.height * 2))
    }
    val names = "ABCDEFGHIK"
    ps.forEachIndexed { i, p ->
        val above = i % 2 == 1 // đỉnh nhô lên
        drawCircle(Dg.Ink, 3.dp.toPx(), p)
        textAt(m, names[i].toString(), Offset(p.x, p.y + (if (above) -14 else 14).dp.toPx()), Dg.Muted, 12f, FontWeight.SemiBold)
    }
}

private fun DrawScope.triangleSides(m: TextMeasurer, labels: List<String>, area: Size) {
    // AB = l0 (đáy), BC = l1, CA = l2
    val a = value(labels[0]) ?: 5.0; val b = value(labels[1]) ?: 5.0; val c = value(labels[2]) ?: 5.0
    val valid = a + b > c && a + c > b && b + c > a
    val raw = if (valid) {
        val cx = (a * a + c * c - b * b) / (2 * a)
        val cy = sqrt(max(0.0, c * c - cx * cx))
        listOf(Offset(0f, 0f), Offset(a.toFloat(), 0f), Offset(cx.toFloat(), -cy.toFloat()))
    } else listOf(Offset(0f, 0f), Offset(10f, 0f), Offset(4f, -7f))
    val ps = fit(raw, area, 32.dp.toPx())
    shape(ps)
    val ctr = centroid(ps)
    edgeLabel(m, labels[0], ps[0], ps[1], ctr)
    edgeLabel(m, labels[1], ps[1], ps[2], ctr)
    edgeLabel(m, labels[2], ps[2], ps[0], ctr)
    listOf("A", "B", "C").forEachIndexed { i, s -> vertex(m, s, ps[i], ctr) }
}

private fun DrawScope.quadSides(m: TextMeasurer, labels: List<String>, area: Size) {
    val raw = listOf(Offset(0f, 0f), Offset(10f, 0f), Offset(8.6f, -6f), Offset(1.4f, -5.2f))
    val ps = fit(raw, area, 32.dp.toPx())
    shape(ps)
    val ctr = centroid(ps)
    for (i in 0..3) edgeLabel(m, labels[i], ps[i], ps[(i + 1) % 4], ctr)
    listOf("A", "B", "C", "D").forEachIndexed { i, s -> vertex(m, s, ps[i], ctr) }
}

private fun DrawScope.rectangle(m: TextMeasurer, labels: List<String>, area: Size, square: Boolean) {
    val len = value(labels[0]) ?: 6.0
    val wid = if (square) len else value(labels.getOrNull(1) ?: "") ?: (len * 0.6)
    val ratio = if (square) 1f else (wid / len).toFloat().coerceIn(0.32f, 0.85f)
    val maxW = min(area.width * 0.58f, (area.height - 56.dp.toPx()) / ratio)
    val w = maxW; val h = w * ratio
    val l = (area.width - w) / 2f; val t = (area.height - h) / 2f
    val ps = listOf(Offset(l, t + h), Offset(l + w, t + h), Offset(l + w, t), Offset(l, t))
    shape(ps)
    rightAngle(ps[0], Offset(1f, 0f), Offset(0f, -1f))
    rightAngle(ps[1], Offset(-1f, 0f), Offset(0f, -1f))
    rightAngle(ps[2], Offset(-1f, 0f), Offset(0f, 1f))
    rightAngle(ps[3], Offset(1f, 0f), Offset(0f, 1f))
    val ctr = centroid(ps)
    edgeLabel(m, labels[0], ps[0], ps[1], ctr)
    if (square) edgeLabel(m, labels[0], ps[1], ps[2], ctr) else edgeLabel(m, labels.getOrElse(1) { "?" }, ps[1], ps[2], ctr)
    listOf("A", "B", "C", "D").forEachIndexed { i, s -> vertex(m, s, ps[i], ctr) }
}

private fun DrawScope.circle(m: TextMeasurer, radius: String, diameter: String, area: Size, givenIsRadius: Boolean) {
    val r = min(area.width * 0.24f, area.height * 0.36f)
    val o = Offset(area.width / 2f, area.height / 2f)
    drawCircle(Dg.ShapeFill, r, o)
    drawCircle(Dg.Ink, r, o, style = Stroke(2.dp.toPx()))
    drawCircle(Dg.Ink, 3.dp.toPx(), o)
    textAt(m, "O", Offset(o.x - 10.dp.toPx(), o.y - 10.dp.toPx()), Dg.Muted, 12f, FontWeight.SemiBold)

    // bán kính: chéo lên bên phải; đường kính: ngang
    val rEnd = Offset(o.x + r * 0.766f, o.y - r * 0.643f)
    val dA = Offset(o.x - r, o.y); val dB = Offset(o.x + r, o.y)
    val diameterShown = diameter.isNotEmpty()
    if (diameterShown) {
        val unknown = diameter.startsWith("?")
        drawLine(if (unknown) Dg.Unknown else Dg.Given, dA, dB, 2.dp.toPx(), StrokeCap.Round, if (unknown) dash(5f, 4f) else null)
        drawCircle(Dg.Ink, 2.5.dp.toPx(), dA); drawCircle(Dg.Ink, 2.5.dp.toPx(), dB)
        pill(m, if (unknown) "Đường kính: ?" else "Đường kính: $diameter", Offset(o.x, o.y + r + 16.dp.toPx()),
            if (unknown) Dg.Unknown else Dg.Given, unknown, fs = 12f)
    }
    if (radius.isNotEmpty()) {
        val unknown = radius.startsWith("?")
        drawLine(if (unknown) Dg.Unknown else Dg.Given, o, rEnd, 2.dp.toPx(), StrokeCap.Round, if (unknown) dash(5f, 4f) else null)
        drawCircle(Dg.Ink, 2.5.dp.toPx(), rEnd)
        val tx = if (unknown) "Bán kính: ?" else "Bán kính: $radius"
        val sz = m.layout(tx, Dg.Given, 12f).size
        pill(m, tx, Offset(rEnd.x + sz.width / 2f + 12.dp.toPx(), rEnd.y - 8.dp.toPx()), if (unknown) Dg.Unknown else Dg.Given, unknown, fs = 12f)
    }
}

private fun DrawScope.midpoint(m: TextMeasurer, labels: List<String>, area: Size) {
    val y = area.height * 0.48f
    val a = Offset(area.width * 0.12f, y); val b = Offset(area.width * 0.88f, y)
    val mid = Offset((a.x + b.x) / 2f, y)
    drawLine(Dg.Ink, a, b, 2.dp.toPx(), StrokeCap.Round)
    listOf(a to "A", mid to "M", b to "B").forEach { (p, n) ->
        drawCircle(Dg.Ink, 3.5.dp.toPx(), p)
        textAt(m, n, Offset(p.x, p.y - 14.dp.toPx()), Dg.Muted, 12f, FontWeight.SemiBold)
    }
    // vạch bằng nhau
    listOf((a.x + mid.x) / 2f, (mid.x + b.x) / 2f).forEach { x ->
        drawLine(Dg.Ink, Offset(x - 2.dp.toPx(), y - 6.dp.toPx()), Offset(x + 2.dp.toPx(), y + 6.dp.toPx()), 1.6.dp.toPx())
    }
    // AM = ? phía trên, AB phía dưới
    val yTop = y - 32.dp.toPx()
    hBrace(a.x, mid.x, yTop, Dg.Unknown, open = 1, dashed = true)
    pill(m, labels.getOrElse(1) { "?" }, Offset((a.x + mid.x) / 2f, yTop - 12.dp.toPx()), Dg.Unknown, unknown = true, fs = 12f)
    val yBot = y + 22.dp.toPx()
    hBrace(a.x, b.x, yBot, Dg.Given, open = -1)
    pill(m, labels[0], Offset(mid.x, yBot + 15.dp.toPx()), Dg.Given, fs = 12f)
}

private fun DrawScope.rectAndSquare(m: TextMeasurer, area: Size) {
    val h = area.height * 0.46f
    val gap = 34.dp.toPx()
    val rw = h * 1.6f
    val total = rw + gap + h
    val l = (area.width - total) / 2f
    val t = (area.height - h) / 2f - 8.dp.toPx()
    shape(listOf(Offset(l, t), Offset(l + rw, t), Offset(l + rw, t + h), Offset(l, t + h)))
    val sl = l + rw + gap
    shape(listOf(Offset(sl, t), Offset(sl + h, t), Offset(sl + h, t + h), Offset(sl, t + h)))
    textAt(m, "Hình chữ nhật", Offset(l + rw / 2f, t + h + 16.dp.toPx()), Dg.Muted, 12f, FontWeight.SemiBold)
    textAt(m, "Hình vuông", Offset(sl + h / 2f, t + h + 16.dp.toPx()), Dg.Muted, 12f, FontWeight.SemiBold)
}

private fun DrawScope.heightLine(m: TextMeasurer, top: Offset, foot: Offset, label: String) {
    drawLine(Dg.Unknown, top, foot, 1.8.dp.toPx(), StrokeCap.Round, dash(5f, 4f))
    rightAngle(foot, Offset(1f, 0f), Offset(0f, -1f))
    val unknown = label.startsWith("?")
    val sz = m.layout(label, Dg.Given, 12f).size
    pill(m, label, Offset(foot.x + sz.width / 2f + 14.dp.toPx(), (top.y + foot.y) / 2f), if (unknown) Dg.Unknown else Dg.Given, unknown, fs = 12f, filled = unknown)
}

private fun DrawScope.parallelogram(m: TextMeasurer, labels: List<String>, area: Size) {
    val raw = listOf(Offset(0f, 0f), Offset(10f, 0f), Offset(12.5f, -6f), Offset(2.5f, -6f))
    val ps = fit(raw, area, 34.dp.toPx())
    shape(ps)
    val ctr = centroid(ps)
    edgeLabel(m, labels[0], ps[0], ps[1], ctr)
    heightLine(m, ps[3], Offset(ps[3].x, ps[0].y), labels[1])
}

private fun DrawScope.rhombus(m: TextMeasurer, labels: List<String>, area: Size) {
    val d1 = (value(labels[0]) ?: 8.0).toFloat(); val d2 = (value(labels[1]) ?: 5.0).toFloat()
    val ratio = (d2 / d1).coerceIn(0.4f, 1.6f)
    val raw = listOf(Offset(-1f, 0f), Offset(0f, -ratio), Offset(1f, 0f), Offset(0f, ratio))
    val ps = fit(raw, area, 30.dp.toPx())
    shape(ps)
    drawLine(Dg.Given, ps[0], ps[2], 1.6.dp.toPx(), pathEffect = dash(5f, 4f))
    drawLine(Dg.Given, ps[1], ps[3], 1.6.dp.toPx(), pathEffect = dash(5f, 4f))
    val c = centroid(ps)
    pill(m, labels[0], Offset((ps[0].x + c.x) / 2f, c.y + 13.dp.toPx()), Dg.Given, fs = 12f, filled = false)
    val sz = m.layout(labels[1], Dg.Given, 12f).size
    pill(m, labels[1], Offset(c.x + sz.width / 2f + 14.dp.toPx(), (ps[1].y + c.y) / 2f), Dg.Given, fs = 12f, filled = false)
}

private fun DrawScope.triangleHeight(m: TextMeasurer, labels: List<String>, area: Size) {
    val raw = listOf(Offset(0f, 0f), Offset(10f, 0f), Offset(3.6f, -6.5f))
    val ps = fit(raw, area, 34.dp.toPx())
    shape(ps)
    val ctr = centroid(ps)
    edgeLabel(m, labels[0], ps[0], ps[1], ctr)
    heightLine(m, ps[2], Offset(ps[2].x, ps[0].y), labels[1])
}

private fun DrawScope.trapezoid(m: TextMeasurer, labels: List<String>, area: Size) {
    val a = value(labels[0]) ?: 6.0; val b = value(labels[1]) ?: 10.0
    val topL = if (a <= b) labels[0] else labels[1]
    val botL = if (a <= b) labels[1] else labels[0]
    val ratio = (min(a, b) / max(a, b)).toFloat().coerceIn(0.35f, 0.85f)
    val bw = 10f; val tw = bw * ratio
    val raw = listOf(Offset(0f, 0f), Offset(bw, 0f), Offset((bw + tw) / 2f + 0.6f, -5.5f), Offset((bw - tw) / 2f + 0.6f, -5.5f))
    val ps = fit(raw, area, 34.dp.toPx())
    shape(ps)
    val ctr = centroid(ps)
    edgeLabel(m, botL, ps[0], ps[1], ctr)
    edgeLabel(m, topL, ps[2], ps[3], ctr)
    heightLine(m, ps[3], Offset(ps[3].x, ps[0].y), labels[2])
}
