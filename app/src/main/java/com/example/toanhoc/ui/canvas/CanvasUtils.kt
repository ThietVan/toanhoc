package com.example.toanhoc.ui.canvas

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.NunitoFontFamily
import com.example.toanhoc.ui.theme.SoftAmberBg
import com.example.toanhoc.ui.theme.SoftBlueBg
import com.example.toanhoc.ui.theme.TargetUnknownAmber
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/* ==========================================================================================
 * NGÔN NGỮ HÌNH ẢNH CHUNG CHO MỌI SƠ ĐỒ
 *   Xanh dương  = dữ kiện đề cho
 *   Cam         = ẩn số "?" (viền nét đứt, nền cam nhạt)
 *   Tím         = bớt đi / phép trừ
 *   Xám mực     = trục, nét hình, chú thích
 * Không vẽ khung/nền riêng: sơ đồ nằm trong thẻ câu hỏi, chỉ có nét & chữ.
 * ======================================================================================== */
internal object Dg {
    val Ink = Color(0xFF111827)
    val Muted = Color(0xFF6B7280)
    val Axis = Color(0xFF9CA3AF)
    val Hairline = Color(0xFFE5E7EB)
    val Given = GivenDataBlue
    val GivenSoft = SoftBlueBg
    val Unknown = TargetUnknownAmber
    val UnknownSoft = SoftAmberBg
    val Subtract = Color(0xFF7356C6)
    val SubtractSoft = Color(0xFFF1ECFB)
    val ShapeFill = Color(0xFFEEF2FC)
}

internal fun DrawScope.dash(on: Float = 6f, off: Float = 5f): PathEffect =
    PathEffect.dashPathEffect(floatArrayOf(on.dp.toPx(), off.dp.toPx()), 0f)

internal fun TextMeasurer.layout(text: String, color: Color, fs: Float, weight: FontWeight = FontWeight.Bold): TextLayoutResult =
    measure(text, TextStyle(color = color, fontSize = fs.sp, fontWeight = weight, fontFamily = NunitoFontFamily))

/** Chữ căn giữa tại [center]. */
internal fun DrawScope.textAt(
    m: TextMeasurer, text: String, center: Offset, color: Color,
    fs: Float = 14f, weight: FontWeight = FontWeight.Bold
) {
    val r = m.layout(text, color, fs, weight)
    drawText(r, topLeft = Offset(center.x - r.size.width / 2f, center.y - r.size.height / 2f))
}

/** Viên nhãn bo tròn. [unknown] = viền cam nét đứt. Trả về kích thước đã vẽ. */
internal fun DrawScope.pill(
    m: TextMeasurer, text: String, center: Offset, accent: Color,
    unknown: Boolean = false, fs: Float = 13f, filled: Boolean = true
): Size {
    val r = m.layout(text, accent, fs)
    val w = r.size.width + 14.dp.toPx()
    val h = r.size.height + 6.dp.toPx()
    val tl = Offset(center.x - w / 2f, center.y - h / 2f)
    val cr = CornerRadius(h / 2f)
    val bg = when {
        !filled -> Color.White
        unknown -> Dg.UnknownSoft
        accent == Dg.Subtract -> Dg.SubtractSoft
        else -> Dg.GivenSoft
    }
    drawRoundRect(bg, tl, Size(w, h), cr)
    drawRoundRect(
        accent, tl, Size(w, h), cr,
        style = Stroke(width = 1.5.dp.toPx(), pathEffect = if (unknown) dash(4f, 3f) else null)
    )
    drawText(r, topLeft = Offset(center.x - r.size.width / 2f, center.y - r.size.height / 2f))
    return Size(w, h)
}

/** Đầu mũi tên tại [tip], hướng từ [from]. */
internal fun DrawScope.arrowHead(tip: Offset, from: Offset, color: Color, len: Float = 7.dp.toPx(), width: Float = 2.dp.toPx()) {
    val a = atan2(tip.y - from.y, tip.x - from.x)
    val p1 = Offset(tip.x - len * cos(a - 0.5f), tip.y - len * sin(a - 0.5f))
    val p2 = Offset(tip.x - len * cos(a + 0.5f), tip.y - len * sin(a + 0.5f))
    drawLine(color, tip, p1, width, StrokeCap.Round)
    drawLine(color, tip, p2, width, StrokeCap.Round)
}

/** Ngoặc ngang. [open] = hướng hai đầu ngoặc: +1 chúc xuống, -1 chĩa lên. */
internal fun DrawScope.hBrace(x1: Float, x2: Float, y: Float, color: Color, open: Int, dashed: Boolean = false) {
    val t = 6.dp.toPx() * open
    val w = 1.6.dp.toPx()
    val pe = if (dashed) dash(4f, 3f) else null
    drawLine(color, Offset(x1, y), Offset(x2, y), w, StrokeCap.Round, pe)
    drawLine(color, Offset(x1, y), Offset(x1, y + t), w, StrokeCap.Round)
    drawLine(color, Offset(x2, y), Offset(x2, y + t), w, StrokeCap.Round)
}

/** Ngoặc dọc bên phải; hai đầu chĩa sang trái. */
internal fun DrawScope.vBrace(x: Float, y1: Float, y2: Float, color: Color, dashed: Boolean = false) {
    val t = 6.dp.toPx()
    val w = 1.6.dp.toPx()
    val pe = if (dashed) dash(4f, 3f) else null
    drawLine(color, Offset(x, y1), Offset(x, y2), w, StrokeCap.Round, pe)
    drawLine(color, Offset(x, y1), Offset(x - t, y1), w, StrokeCap.Round)
    drawLine(color, Offset(x, y2), Offset(x - t, y2), w, StrokeCap.Round)
}

/** Nhãn kích thước dọc theo một cạnh, đặt lệch ra ngoài theo pháp tuyến. */
internal fun DrawScope.edgeLabel(
    m: TextMeasurer, text: String, a: Offset, b: Offset, interior: Offset,
    gap: Float = 14.dp.toPx()
) {
    val mid = Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f)
    var nx = -(b.y - a.y); var ny = b.x - a.x
    val len = kotlin.math.sqrt(nx * nx + ny * ny).coerceAtLeast(1f)
    nx /= len; ny /= len
    // hướng ra xa tâm hình
    if ((mid.x + nx - interior.x) * nx + (mid.y + ny - interior.y) * ny < 0) { nx = -nx; ny = -ny }
    val unknown = text.trim().startsWith("?")
    pill(m, text, Offset(mid.x + nx * gap, mid.y + ny * gap), if (unknown) Dg.Unknown else Dg.Given, unknown, fs = 12f, filled = unknown)
}
