package com.example.toanhoc.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

/**
 * Đặt tính theo cột (SGK): các chữ số thẳng hàng theo hàng đơn vị (số thập phân thẳng dấu phẩy).
 * Hàng kết quả là một ô "?" duy nhất – không lộ số chữ số của đáp án.
 * Phép chia: trình bày kiểu Việt Nam (số bị chia | số chia, thương "?" dưới số chia).
 */
@Composable
fun VerticalArithmeticView(d: VerticalDiagram, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer()
    val h = if (d.op == ':') 120.dp else 168.dp
    Canvas(modifier.fillMaxWidth().height(h)) {
        if (d.op == ':') drawDivision(m, d) else drawColumn(m, d)
    }
}

private fun align(a: String, b: String): Pair<String, String> {
    val fa = a.substringAfter(",", ""); val fb = b.substringAfter(",", "")
    val f = maxOf(fa.length, fb.length)
    fun pad(s: String, frac: String) = when {
        f == 0 -> s
        frac.isEmpty() -> s + " ".repeat(f + 1)
        else -> s + " ".repeat(f - frac.length)
    }
    return pad(a, fa) to pad(b, fb)
}

private fun DrawScope.drawColumn(m: TextMeasurer, d: VerticalDiagram) {
    val (a, b) = if (d.op == '×') d.a to d.b else align(d.a, d.b)
    val cols = maxOf(a.length, b.length) + 1
    val cw = 24.dp.toPx()
    val rowH = 36.dp.toPx()
    val totalW = cols * cw
    val left = (size.width - totalW) / 2f
    val right = left + totalW
    val top = 6.dp.toPx()
    val fs = 24f

    fun row(s: String, r: Int) {
        s.forEachIndexed { i, ch ->
            if (ch == ' ') return@forEachIndexed
            val col = cols - s.length + i
            textAt(m, ch.toString(), Offset(left + col * cw + cw / 2f, top + r * rowH + rowH / 2f), Dg.Ink, fs, FontWeight.Bold)
        }
    }
    row(a, 0)
    row(b, 1)
    // dấu phép tính, đặt chéo giữa hai số như SGK
    textAt(m, d.op.toString(), Offset(left - 4.dp.toPx(), top + rowH), Dg.Given, 22f, FontWeight.Bold)
    // gạch ngang
    val ly = top + 2 * rowH + 2.dp.toPx()
    drawLine(Dg.Ink, Offset(left + cw * 0.5f, ly), Offset(right, ly), 2.dp.toPx(), StrokeCap.Round)
    // ô kết quả "?"
    val boxH = rowH - 6.dp.toPx()
    val tl = Offset(left + cw * 0.5f, ly + 7.dp.toPx())
    val sz = Size(right - tl.x, boxH)
    drawRoundRect(Dg.UnknownSoft, tl, sz, CornerRadius(8.dp.toPx()))
    drawRoundRect(Dg.Unknown, tl, sz, CornerRadius(8.dp.toPx()), style = Stroke(1.6.dp.toPx(), pathEffect = dash(5f, 4f)))
    textAt(m, "?", Offset(tl.x + sz.width / 2f, tl.y + boxH / 2f), Dg.Unknown, 20f, FontWeight.ExtraBold)
}

private fun DrawScope.drawDivision(m: TextMeasurer, d: VerticalDiagram) {
    val fs = 24f
    val ra = m.layout(d.a, Dg.Ink, fs)
    val rb = m.layout(d.b, Dg.Ink, fs)
    val gap = 14.dp.toPx()
    val qW = maxOf(rb.size.width.toFloat(), 44.dp.toPx())
    val totalW = ra.size.width + gap * 2 + qW
    val left = (size.width - totalW) / 2f
    val top = 10.dp.toPx()
    val lineX = left + ra.size.width + gap
    val rowH = 38.dp.toPx()

    textAt(m, d.a, Offset(left + ra.size.width / 2f, top + rowH / 2f), Dg.Ink, fs)
    textAt(m, d.b, Offset(lineX + gap + rb.size.width / 2f, top + rowH / 2f), Dg.Ink, fs)
    drawLine(Dg.Ink, Offset(lineX, top), Offset(lineX, top + rowH * 2.4f), 2.dp.toPx(), StrokeCap.Round)
    drawLine(Dg.Ink, Offset(lineX, top + rowH), Offset(lineX + gap + qW + 6.dp.toPx(), top + rowH), 2.dp.toPx(), StrokeCap.Round)
    val tl = Offset(lineX + 8.dp.toPx(), top + rowH + 6.dp.toPx())
    val sz = Size(qW + 4.dp.toPx(), rowH - 8.dp.toPx())
    drawRoundRect(Dg.UnknownSoft, tl, sz, CornerRadius(8.dp.toPx()))
    drawRoundRect(Dg.Unknown, tl, sz, CornerRadius(8.dp.toPx()), style = Stroke(1.6.dp.toPx(), pathEffect = dash(5f, 4f)))
    textAt(m, "?", Offset(tl.x + sz.width / 2f, tl.y + sz.height / 2f), Dg.Unknown, 20f, FontWeight.ExtraBold)
}
