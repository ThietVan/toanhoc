package com.example.toanhoc.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

/**
 * Sơ đồ đoạn thẳng (mô hình thanh – Singapore bar model) tối giản.
 *  - Mỗi hàng là một đối tượng (tên bên trái), gồm các đoạn theo đúng tỉ lệ dữ kiện.
 *  - Đoạn đề cho: xanh. Đoạn cần tìm: cam nét đứt "?". Phần bớt đi: tím gạch chéo.
 *  - Ngoặc trên (tổng một hàng), ngoặc phải (tổng các hàng), khoảng chênh lệch (hiệu).
 */
@Composable
fun BarModelView(d: BarDiagram, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer()
    val barH = 32.dp
    val rowGap = if (d.gap != null) 30.dp else 18.dp
    val topRoom = if (d.rowBrace != null) 40.dp else 8.dp
    val bottomRoom = 22.dp
    val height = topRoom + barH * d.rows.size + rowGap * (d.rows.size - 1).coerceAtLeast(0) + bottomRoom

    Canvas(modifier.fillMaxWidth().height(height)) {
        val names = d.rows.map { it.name }
        val nameW = if (names.all { it.isEmpty() }) 0f
        else names.maxOf { m.layout(it, Dg.Ink, 13f).size.width.toFloat() } + 12.dp.toPx()
        val right = when {
            d.totalBrace != null -> 64.dp.toPx()
            d.rows.any { it.askMark } -> 40.dp.toPx()
            else -> 12.dp.toPx()
        }
        val x0 = 8.dp.toPx() + nameW
        val avail = size.width - x0 - right
        val maxUnits = d.rows.maxOf { it.totalUnits }.coerceAtLeast(1e-9)
        val upx = (avail / maxUnits).toFloat()
        val bh = barH.toPx()
        fun rowTop(i: Int) = topRoom.toPx() + i * (bh + rowGap.toPx())

        val ends = FloatArray(d.rows.size)
        d.rows.forEachIndexed { ri, row ->
            val y = rowTop(ri)
            if (row.name.isNotEmpty()) {
                val r = m.layout(row.name, Dg.Ink, 13f)
                drawText(r, topLeft = Offset(8.dp.toPx(), y + (bh - r.size.height) / 2f))
            }
            var x = x0
            row.segs.forEach { s ->
                val w = (s.units * upx).toFloat().coerceAtLeast(18.dp.toPx())
                val (fill, stroke) = when {
                    s.unknown -> Dg.UnknownSoft to Dg.Unknown
                    s.removed -> Dg.SubtractSoft to Dg.Subtract
                    s.faint -> Color(0xFFF3F4F6) to Dg.Axis
                    else -> Dg.GivenSoft to Dg.Given
                }
                val tl = Offset(x, y)
                val sz = Size(w, bh)
                drawRoundRect(fill, tl, sz, CornerRadius(4.dp.toPx()))
                if (s.removed) {
                    clipRect(x, y, x + w, y + bh) {
                        var hx = x - bh
                        while (hx < x + w) {
                            drawLine(Dg.Subtract.copy(alpha = 0.35f), Offset(hx, y + bh), Offset(hx + bh, y), 1.2.dp.toPx())
                            hx += 8.dp.toPx()
                        }
                    }
                }
                drawRoundRect(
                    stroke, tl, sz, CornerRadius(4.dp.toPx()),
                    style = Stroke(1.6.dp.toPx(), pathEffect = if (s.unknown) dash(5f, 4f) else null)
                )
                s.label?.let { lbl ->
                    val color = when { s.unknown -> Dg.Unknown; s.removed -> Dg.Subtract; else -> Dg.Given }
                    val r = m.layout(lbl, color, 13f)
                    if (r.size.width + 8.dp.toPx() <= w) {
                        drawText(r, topLeft = Offset(x + (w - r.size.width) / 2f, y + (bh - r.size.height) / 2f))
                    } else {
                        textAt(m, lbl, Offset(x + w / 2f, y + bh + 10.dp.toPx()), color, 12f)
                    }
                }
                x += w
            }
            ends[ri] = x
            if (row.askMark) pill(m, "?", Offset(x + 20.dp.toPx(), y + bh / 2f), Dg.Unknown, unknown = true)
        }

        // Ngoặc trên một hàng (tổng)
        d.rowBrace?.let { b ->
            val y = rowTop(b.row) - 10.dp.toPx()
            val c = if (b.unknown) Dg.Unknown else Dg.Given
            hBrace(x0, ends[b.row], y, c, open = 1, dashed = b.unknown)
            pill(m, b.label, Offset((x0 + ends[b.row]) / 2f, y - 13.dp.toPx()), c, b.unknown)
        }

        // Ngoặc phải gộp các hàng
        d.totalBrace?.let { b ->
            val bx = ends.max() + 12.dp.toPx()
            val y1 = rowTop(0); val y2 = rowTop(d.rows.lastIndex) + bh
            val c = if (b.unknown) Dg.Unknown else Dg.Given
            vBrace(bx, y1, y2, c, dashed = b.unknown)
            val r = m.layout(b.label, c, 13f)
            pill(m, b.label, Offset(bx + 10.dp.toPx() + (r.size.width + 14.dp.toPx()) / 2f, (y1 + y2) / 2f), c, b.unknown)
        }

        // Hiệu: khoảng giữa cuối hàng ngắn và cuối hàng dài
        d.gap?.let { g ->
            if (d.rows.size < 2) return@let
            val longI = if (ends[0] >= ends[1]) 0 else 1
            val shortI = 1 - longI
            val xs = ends[shortI]; val xl = ends[longI]
            val c = if (g.unknown) Dg.Unknown else Dg.Given
            val yGuideTop = minOf(rowTop(longI), rowTop(shortI))
            val yGuideBot = maxOf(rowTop(longI), rowTop(shortI)) + bh
            drawLine(Dg.Axis, Offset(xl, yGuideTop), Offset(xl, yGuideBot), 1.2.dp.toPx(), pathEffect = dash(3f, 3f))
            val y = rowTop(shortI) + bh / 2f
            drawLine(c, Offset(xs + 3.dp.toPx(), y), Offset(xl, y), 1.6.dp.toPx(), StrokeCap.Round, pathEffect = if (g.unknown) dash(4f, 3f) else null)
            arrowHead(Offset(xs + 3.dp.toPx(), y), Offset(xl, y), c)
            arrowHead(Offset(xl, y), Offset(xs, y), c)
            pill(m, g.label, Offset((xs + xl) / 2f, y - 15.dp.toPx()), c, g.unknown, fs = 12f)
        }
    }
}
