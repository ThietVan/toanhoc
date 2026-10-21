package com.example.toanhoc.ui.canvas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Tia số tối giản.
 *  - Điểm đề cho: chấm xanh + số đậm.  Ẩn số: vòng cam nét đứt + nhãn "?" (số tại đó bị ẩn).
 *  - Bước nhảy: cung phía trên trục, có mũi tên và nhãn (+3 / −2 / ?). Trừ = tím.
 *  - Ngoặc dưới trục cho độ dài đoạn (dạng thước đo).
 * Cung được vẽ dần một lần khi câu hỏi xuất hiện.
 */
@Composable
fun NumberLineView(d: NumberLineDiagram, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer()
    val progress = remember(d) { Animatable(if (d.jumps.isEmpty()) 1f else 0f) }
    LaunchedEffect(d) {
        if (d.jumps.isNotEmpty()) progress.animateTo(1f, tween(700 + 250 * d.jumps.size, easing = FastOutSlowInEasing))
    }

    val hasArcs = d.jumps.isNotEmpty()
    // Cung chồng lên cung trước (vd 8 − 3 + 4) được nâng cao hơn để nhãn không đè nhau
    val lift = remember(d) {
        d.jumps.mapIndexed { i, j ->
            val lo = minOf(j.from, j.to); val hi = maxOf(j.from, j.to)
            d.jumps.take(i).count { p -> minOf(p.from, p.to) < hi && maxOf(p.from, p.to) > lo }
        }
    }
    val maxLift = lift.maxOrNull() ?: 0
    val top = if (hasArcs) 64.dp + 22.dp * maxLift else if (d.ruler) 52.dp else 18.dp
    val bottom = 36.dp + if (d.spans.isNotEmpty()) 40.dp else 0.dp

    Canvas(modifier.fillMaxWidth().height(top + bottom)) {
        val axisY = top.toPx()
        val padL = 22.dp.toPx()
        val padR = 30.dp.toPx()
        val x0 = padL
        val x1 = size.width - padR
        val range = (d.max - d.min).coerceAtLeast(1e-9)
        fun xOf(v: Double) = x0 + ((v - d.min) / range).toFloat() * (x1 - x0)

        // 1. Thước kẻ (nếu có): thân thước nhạt phía trên trục
        if (d.ruler) {
            val tl = Offset(x0 - 10.dp.toPx(), axisY - 26.dp.toPx())
            val sz = Size(x1 - x0 + 20.dp.toPx(), 26.dp.toPx())
            drawRoundRect(Color(0xFFFAFAF7), tl, sz, CornerRadius(4.dp.toPx()))
            drawRoundRect(Dg.Hairline, tl, sz, CornerRadius(4.dp.toPx()), style = Stroke(1.dp.toPx()))
            // vật được đo: dải xanh nằm trên thước
            d.spans.firstOrNull()?.let { s ->
                val a = xOf(s.from); val b = xOf(s.to)
                drawRoundRect(
                    Dg.Given.copy(alpha = 0.85f), Offset(a, axisY - 40.dp.toPx()), Size(b - a, 10.dp.toPx()),
                    CornerRadius(5.dp.toPx())
                )
            }
        }

        // 2. Trục + mũi tên bên phải
        val axisStart = if (d.min <= 0.0) x0 else x0 - 12.dp.toPx()
        drawLine(Dg.Axis, Offset(axisStart, axisY), Offset(x1 + 18.dp.toPx(), axisY), 2.dp.toPx(), StrokeCap.Round)
        arrowHead(Offset(x1 + 20.dp.toPx(), axisY), Offset(x1, axisY), Dg.Axis, 7.dp.toPx(), 2.dp.toPx())

        // 3. Vạch chia
        val labels = d.axisLabels()
        for (i in 0..d.tickCount) {
            val v = d.min + i * d.step
            val x = xOf(v)
            val major = labels.any { abs(it.first - v) < d.step * 0.01 }
            val h = if (d.ruler) (if (i % 5 == 0) 12.dp else 7.dp).toPx() else (if (major) 6.dp else 4.dp).toPx()
            val y0 = if (d.ruler) axisY - h else axisY - h
            drawLine(Dg.Axis, Offset(x, y0), Offset(x, if (d.ruler) axisY else axisY + h), 1.5.dp.toPx(), StrokeCap.Round)
        }

        // 4. Nhãn số dưới trục
        val givenSet = d.points.filter { it.mark == Mark.GIVEN }.map { it.v }
        labels.forEach { (v, txt) ->
            val isGiven = givenSet.any { abs(it - v) < 1e-9 }
            textAt(
                m, txt, Offset(xOf(v), axisY + 18.dp.toPx()),
                if (isGiven) Dg.Given else Dg.Muted,
                fs = if (isGiven) 14f else 12f,
                weight = if (isGiven) FontWeight.ExtraBold else FontWeight.SemiBold
            )
        }
        if (d.unit.isNotEmpty()) {
            textAt(m, d.unit, Offset(x1 + 18.dp.toPx(), axisY + 18.dp.toPx()), Dg.Muted, 11f, FontWeight.SemiBold)
        }

        // 5. Cung nhảy (vẽ lần lượt)
        val n = d.jumps.size
        d.jumps.forEachIndexed { i, j ->
            val local = ((progress.value * n) - i).coerceIn(0f, 1f)
            if (local <= 0f) return@forEachIndexed
            val color = when { j.unknown -> Dg.Unknown; j.subtract -> Dg.Subtract; else -> Dg.Given }
            val xa = xOf(j.from); val xb = xOf(j.to)
            val dist = abs(xb - xa)
            val peak = (dist * 0.45f).coerceIn(18.dp.toPx(), 30.dp.toPx()) + lift[i] * 22.dp.toPx()
            val path = Path().apply {
                moveTo(xa, axisY - 3.dp.toPx())
                quadraticTo((xa + xb) / 2f, axisY - 2 * peak, xb, axisY - 3.dp.toPx())
            }
            val pm = PathMeasure().apply { setPath(path, false) }
            val seg = Path()
            pm.getSegment(0f, pm.length * local, seg, true)
            drawPath(
                seg, color,
                style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, pathEffect = if (j.unknown) dash(5f, 4f) else null)
            )
            if (local >= 0.99f) {
                val tan = pm.getTangent(pm.length)
                val tip = Offset(xb, axisY - 3.dp.toPx())
                arrowHead(tip, Offset(tip.x - tan.x * 10, tip.y - tan.y * 10), color)
                if (j.label.isNotEmpty()) pill(m, j.label, Offset((xa + xb) / 2f, axisY - peak - 17.dp.toPx()), color, j.unknown, fs = 12f)
            }
        }

        // 6. Điểm
        d.points.forEach { p ->
            val c = Offset(xOf(p.v), axisY)
            when (p.mark) {
                Mark.GIVEN -> {
                    drawCircle(Color.White, 6.dp.toPx(), c)
                    drawCircle(Dg.Given, 4.5.dp.toPx(), c)
                }
                Mark.PLAIN -> {
                    if (progress.value > 0.5f) drawCircle(Dg.Axis, 3.5.dp.toPx(), c)
                }
                Mark.UNKNOWN -> {
                    if (progress.value >= 0.99f) {
                        drawCircle(Color.White, 6.dp.toPx(), c)
                        drawCircle(Dg.Unknown, 4.5.dp.toPx(), c, style = Stroke(2.dp.toPx()))
                        pill(m, "?", Offset(c.x, axisY + 20.dp.toPx()), Dg.Unknown, unknown = true, fs = 13f)
                    }
                }
            }
        }

        // 7. Ngoặc độ dài dưới trục
        d.spans.forEach { s ->
            val a = xOf(s.from); val b = xOf(s.to)
            val y = axisY + 40.dp.toPx()
            val c = if (s.unknown) Dg.Unknown else Dg.Given
            hBrace(a, b, y, c, open = -1, dashed = s.unknown)
            pill(m, s.label, Offset((a + b) / 2f, y + 16.dp.toPx()), c, s.unknown, fs = 12f)
        }
    }
}
