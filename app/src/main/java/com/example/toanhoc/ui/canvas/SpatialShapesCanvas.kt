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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Dãy hình phẳng / khối hình. Khối được vẽ phối cảnh 3 mặt để trẻ phân biệt với hình phẳng.
 * [ShapesDiagram.showNames]: ghi tên dưới hình; [ShapesDiagram.showPositions]: ghi "Bên trái / Ở giữa / Bên phải".
 */
@Composable
fun ShapesRowView(d: ShapesDiagram, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer()
    val captions = d.showNames || d.showPositions
    Canvas(modifier.fillMaxWidth().height(if (captions) 132.dp else 108.dp)) {
        val n = d.shapes.size.coerceAtLeast(1)
        val cellW = size.width / n
        val s = min(cellW * 0.62f, 66.dp.toPx())
        val cy = 12.dp.toPx() + s / 2f + 6.dp.toPx()
        d.shapes.forEachIndexed { i, name ->
            val c = Offset(cellW * i + cellW / 2f, cy)
            drawShape(name, c, s)
            val cap = when {
                d.showPositions && n == 3 -> listOf("Bên trái", "Ở giữa", "Bên phải")[i]
                d.showNames -> name
                else -> null
            }
            cap?.let { textAt(m, it, Offset(c.x, cy + s / 2f + 18.dp.toPx()), Dg.Muted, 12f, FontWeight.SemiBold) }
            if (i < n - 1) {
                val x = cellW * (i + 1)
                drawLine(Dg.Hairline, Offset(x, cy - s / 2f), Offset(x, cy + s / 2f), 1.dp.toPx())
            }
        }
    }
}

private val Mint = Color(0xFF2E9E79)
private val Coral = Color(0xFFE06C4F)
private val Sky = Color(0xFF2F55D4)
private val Sun = Color(0xFFD99A1E)

private fun DrawScope.drawShape(name: String, c: Offset, s: Float) {
    val stroke = Stroke(2.dp.toPx(), join = StrokeJoin.Round)
    fun soft(col: Color) = lerp(col, Color.White, 0.82f)
    when (name.lowercase()) {
        "hình vuông" -> {
            val tl = Offset(c.x - s * 0.42f, c.y - s * 0.42f); val sz = Size(s * 0.84f, s * 0.84f)
            drawRoundRect(soft(Sky), tl, sz, CornerRadius(3.dp.toPx())); drawRoundRect(Sky, tl, sz, CornerRadius(3.dp.toPx()), style = stroke)
        }
        "hình chữ nhật" -> {
            val tl = Offset(c.x - s * 0.5f, c.y - s * 0.3f); val sz = Size(s, s * 0.6f)
            drawRoundRect(soft(Coral), tl, sz, CornerRadius(3.dp.toPx())); drawRoundRect(Coral, tl, sz, CornerRadius(3.dp.toPx()), style = stroke)
        }
        "hình tam giác" -> {
            val p = Path().apply { moveTo(c.x, c.y - s * 0.45f); lineTo(c.x + s * 0.5f, c.y + s * 0.4f); lineTo(c.x - s * 0.5f, c.y + s * 0.4f); close() }
            drawPath(p, soft(Mint)); drawPath(p, Mint, style = stroke)
        }
        "hình tròn" -> {
            drawCircle(soft(Sun), s * 0.45f, c); drawCircle(Sun, s * 0.45f, c, style = stroke)
        }
        "khối lập phương" -> box(c, s * 0.62f, s * 0.62f, s * 0.62f, Sky)
        "khối hộp chữ nhật" -> box(c, s * 0.82f, s * 0.48f, s * 0.42f, Coral)
    }
}

/** Khối hộp phối cảnh: mặt trước, mặt trên, mặt bên. */
private fun DrawScope.box(c: Offset, w: Float, h: Float, depth: Float, col: Color) {
    val dx = depth * 0.55f; val dy = -depth * 0.45f
    val l = c.x - (w + dx) / 2f; val t = c.y - (h + dy) / 2f
    val fTL = Offset(l, t); val fTR = Offset(l + w, t); val fBR = Offset(l + w, t + h); val fBL = Offset(l, t + h)
    val bTL = Offset(fTL.x + dx, fTL.y + dy); val bTR = Offset(fTR.x + dx, fTR.y + dy); val bBR = Offset(fBR.x + dx, fBR.y + dy)
    fun poly(p: List<Offset>) = Path().apply { moveTo(p[0].x, p[0].y); p.drop(1).forEach { lineTo(it.x, it.y) }; close() }
    val front = poly(listOf(fTL, fTR, fBR, fBL)); val top = poly(listOf(fTL, bTL, bTR, fTR)); val side = poly(listOf(fTR, bTR, bBR, fBR))
    drawPath(front, lerp(col, Color.White, 0.78f))
    drawPath(top, lerp(col, Color.White, 0.9f))
    drawPath(side, lerp(col, Color.White, 0.62f))
    val st = Stroke(2.dp.toPx(), join = StrokeJoin.Round)
    listOf(front, top, side).forEach { drawPath(it, col, style = st) }
}
