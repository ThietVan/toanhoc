package com.example.toanhoc.ui.family

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.example.toanhoc.R
import com.example.toanhoc.data.family.ChildProfile
import com.example.toanhoc.data.family.Look
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.random.Random

/* ------------------------------------------------------------------------------------------
 * Nhân vật = ảnh bé trai / bé gái (res/drawable-nodpi/be_trai.png, be_gai.png, nền trong suốt)
 * + ảnh "nhắm mắt" để chớp mắt + phụ kiện vẽ chồng lên theo các điểm neo đo trên ảnh
 * (mắt, đỉnh đầu, ngực…). Phông nền & thú cưng vẽ bằng Canvas.
 * Thêm bộ đồ mới: tạo ảnh cùng tư thế, cùng khung → điểm neo giữ nguyên.
 * Toạ độ cảnh: 300×300; điểm neo: pixel trên ảnh gốc.
 * ---------------------------------------------------------------------------------------- */

private class Rig(
    val res: Int, val blinkRes: Int, val w: Float, val h: Float,
    val eyeL: Offset, val eyeR: Offset,
    /** Đáy mũ/vương miện trên đỉnh đầu và góc nghiêng của đầu. */
    val top: Offset, val topTilt: Float,
    val bow: Offset, val neck: Offset, val chest: Offset,
    /** Tâm khung cắt ảnh đại diện tròn. */
    val face: Offset
)

private val BOY = Rig(
    R.drawable.be_trai, R.drawable.be_trai_blink, 400f, 958f,
    eyeL = Offset(145f, 271f), eyeR = Offset(259.5f, 253.5f),
    top = Offset(192f, 52f), topTilt = -12f,
    bow = Offset(292f, 96f), neck = Offset(206f, 372f), chest = Offset(212f, 492f),
    face = Offset(205f, 245f)
)
private val GIRL = Rig(
    R.drawable.be_gai, R.drawable.be_gai_blink, 446f, 882f,
    eyeL = Offset(108f, 223.5f), eyeR = Offset(222.5f, 242f),
    top = Offset(160f, 44f), topTilt = -6f,
    bow = Offset(66f, 112f), neck = Offset(150f, 345f), chest = Offset(150f, 458f),
    face = Offset(168f, 235f)
)
private fun rigOf(gender: String) = if (gender == "gai") GIRL else BOY

/** Chiều cao nhân vật trong cảnh 300×300 (bé trai cao 250, bé gái theo đúng tỉ lệ ảnh). */
private const val SCENE_SCALE = 250f / 958f

private val pathCache = HashMap<String, Path>()
private fun p(d: String): Path = pathCache.getOrPut(d) { PathParser().parsePathString(d).toPath() }

private fun DrawScope.circle(cx: Float, cy: Float, r: Float, c: Color) = drawCircle(c, r, Offset(cx, cy))
private fun DrawScope.oval(cx: Float, cy: Float, rx: Float, ry: Float, c: Color, alpha: Float = 1f) =
    drawOval(c, Offset(cx - rx, cy - ry), Size(rx * 2, ry * 2), alpha = alpha)
private fun DrawScope.rrect(x: Float, y: Float, w: Float, h: Float, r: Float, c: Color) =
    drawRoundRect(c, Offset(x, y), Size(w, h), CornerRadius(r, r))
private fun DrawScope.rect(x: Float, y: Float, w: Float, h: Float, c: Color, alpha: Float = 1f) = drawRect(c, Offset(x, y), Size(w, h), alpha = alpha)
private fun DrawScope.fill(d: String, c: Color, alpha: Float = 1f) = drawPath(p(d), c, alpha = alpha)
private fun DrawScope.line(d: String, c: Color, w: Float, alpha: Float = 1f) =
    drawPath(p(d), c, alpha = alpha, style = Stroke(width = w, cap = StrokeCap.Round))
private fun DrawScope.seg(x1: Float, y1: Float, x2: Float, y2: Float, c: Color, w: Float) =
    drawLine(c, Offset(x1, y1), Offset(x2, y2), w, StrokeCap.Round)

private fun col(v: Long) = Color(v)
private val White = Color.White
private val EyeInk = Color(0xFF2B2220)

/* ----------------------------------------- Phông ----------------------------------------- */

private fun DrawScope.drawScene(id: String) {
    when (id) {
        "congvien" -> {
            rect(0f, 0f, 300f, 300f, col(0xFFCFEBFF))
            circle(256f, 46f, 20f, col(0xFFFFD45A))
            oval(90f, 50f, 26f, 10f, White); oval(112f, 44f, 18f, 10f, White)
            fill("M0 210 Q70 170 150 205 Q230 175 300 205 L300 300 L0 300 Z", col(0xFF8FD19E))
            fill("M0 240 Q150 222 300 240 L300 300 L0 300 Z", col(0xFF6FBF83))
            rect(30f, 140f, 12f, 74f, col(0xFF8B5A35))
            circle(36f, 132f, 26f, col(0xFF4FAF6A)); circle(20f, 150f, 16f, col(0xFF4FAF6A)); circle(52f, 150f, 16f, col(0xFF4FAF6A))
        }
        "lop" -> {
            rect(0f, 0f, 300f, 300f, col(0xFFEAF4EE))
            rect(0f, 222f, 300f, 78f, col(0xFFCFD8DC))
            rrect(17f, 25f, 182f, 98f, 8f, col(0xFF8B5A35))
            rrect(20f, 28f, 176f, 92f, 6f, col(0xFF2F6B4F))
            // phép tính viết phấn: 7 + 5 = 12
            line("M40 52 L54 52 L44 76", White, 3f)
            line("M66 64 L78 64 M72 58 L72 70", White, 3f)
            line("M100 52 L90 52 L89 62 Q100 58 102 68 Q101 77 90 75", White, 3f)
            line("M114 61 L126 61 M114 67 L126 67", White, 3f)
            line("M138 56 L142 52 L142 76", White, 3f)
            line("M150 56 Q156 49 162 55 Q164 62 150 76 L164 76", White, 3f)
            line("M40 98 Q70 92 100 98 T160 98", col(0xFFBFE3D0), 2.5f)
            circle(252f, 58f, 21.5f, col(0xFF374151)); circle(252f, 58f, 18.5f, White)
            seg(252f, 58f, 252f, 45f, col(0xFF374151), 3f); seg(252f, 58f, 262f, 62f, col(0xFF374151), 3f)
        }
        "bien" -> {
            rect(0f, 0f, 300f, 300f, col(0xFFBDE6FF))
            circle(60f, 50f, 22f, col(0xFFFFD45A))
            rect(0f, 140f, 300f, 80f, col(0xFF4FB3E8))
            line("M0 160 Q20 152 40 160 T80 160 T120 160 T160 160 T200 160 T240 160 T280 160 T320 160", White, 3f, 0.7f)
            fill("M0 210 Q150 196 300 210 L300 300 L0 300 Z", col(0xFFF6DFA6))
            seg(250f, 150f, 250f, 250f, col(0xFF8B5A35), 4f)
            fill("M210 152 Q250 112 290 152 Z", col(0xFFF27BA3))
        }
        "vutru" -> {
            rect(0f, 0f, 300f, 300f, col(0xFF1B1F4B))
            listOf(30f to 40f, 80f to 90f, 140f to 30f, 190f to 120f, 50f to 160f, 280f to 150f, 110f to 130f, 170f to 70f)
                .forEachIndexed { i, (x, y) -> circle(x, y, if (i % 2 == 0) 2f else 1.4f, White) }
            circle(240f, 70f, 30f, col(0xFF8B5CF6))
            rotate(-15f, Offset(240f, 70f)) {
                drawOval(col(0xFFFBD38D), Offset(194f, 60f), Size(92f, 20f), style = Stroke(4f))
            }
            fill("M0 236 Q150 206 300 236 L300 300 L0 300 Z", col(0xFFA7A9C9))
            oval(60f, 262f, 16f, 5f, col(0xFF8C8FB3)); oval(230f, 270f, 12f, 4f, col(0xFF8C8FB3))
        }
        else -> { // phòng ngủ
            rect(0f, 0f, 300f, 300f, col(0xFFFCE9D2))
            rect(0f, 216f, 300f, 84f, col(0xFFE5C49B))
            rect(0f, 212f, 300f, 6f, col(0xFFD4AE80))
            rrect(187f, 37f, 86f, 76f, 8f, White)
            rrect(190f, 40f, 80f, 70f, 6f, col(0xFFBFE3FF))
            seg(230f, 40f, 230f, 110f, White, 4f); seg(190f, 75f, 270f, 75f, White, 4f)
            rrect(28f, 48f, 56f, 42f, 4f, White)
            fill("M36 82 L50 64 L60 76 L67 69 L78 82 Z", col(0xFF7BC59A))
            circle(70f, 60f, 5f, col(0xFFFFC21A))
            oval(140f, 276f, 100f, 15f, col(0xFFF2A7A7), 0.55f)
        }
    }
}

/** Màu nền dịu của phông – dùng cho ảnh đại diện tròn. */
fun sceneTint(id: String): Color = when (id) {
    "congvien" -> Color(0xFFCFEBFF)
    "lop" -> Color(0xFFDDEFE4)
    "bien" -> Color(0xFFBDE6FF)
    "vutru" -> Color(0xFF2A2F66)
    else -> Color(0xFFFCE9D2)
}

/* ---------------------------------------- Nhân vật ---------------------------------------- */

/** Vẽ ảnh nhân vật + phụ kiện, trong hệ toạ độ pixel của ảnh. */
private fun DrawScope.drawCharacter(img: ImageBitmap, rig: Rig, accessory: String) {
    drawImage(img)
    drawAccessory(rig, accessory)
}

private fun DrawScope.drawAccessory(rig: Rig, id: String) {
    val ink = Color(0xFF2B2220)
    val dx = rig.eyeR.x - rig.eyeL.x
    val dy = rig.eyeR.y - rig.eyeL.y
    val len = hypot(dx, dy)
    val ux = dx / len
    val uy = dy / len
    val tilt = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()
    when (id) {
        "kinh" -> {
            listOf(rig.eyeL, rig.eyeR).forEach { e ->
                drawCircle(Color.White.copy(alpha = 0.18f), 38f, e)
                drawCircle(ink, 38f, e, style = Stroke(7f))
            }
            drawLine(ink, Offset(rig.eyeL.x + ux * 38f, rig.eyeL.y + uy * 38f), Offset(rig.eyeR.x - ux * 38f, rig.eyeR.y - uy * 38f), 6f, StrokeCap.Round)
        }
        "kinhram" -> {
            listOf(rig.eyeL, rig.eyeR).forEach { e ->
                rotate(tilt, e) {
                    drawRoundRect(Color(0xFF111827), Offset(e.x - 46f, e.y - 30f), Size(92f, 62f), CornerRadius(26f, 26f), alpha = 0.93f)
                    drawLine(Color.White.copy(alpha = 0.55f), Offset(e.x - 28f, e.y - 14f), Offset(e.x - 10f, e.y - 20f), 6f, StrokeCap.Round)
                }
            }
            drawLine(Color(0xFF111827), Offset(rig.eyeL.x + ux * 44f, rig.eyeL.y + uy * 44f), Offset(rig.eyeR.x - ux * 44f, rig.eyeR.y - uy * 44f), 7f, StrokeCap.Round)
        }
        "no" -> {
            val b = rig.bow
            rotate(-20f, b) {
                val pink = Color(0xFFF27BA3)
                drawPath(Path().apply { moveTo(b.x, b.y); lineTo(b.x - 52f, b.y - 32f); lineTo(b.x - 52f, b.y + 32f); close() }, pink)
                drawPath(Path().apply { moveTo(b.x, b.y); lineTo(b.x + 52f, b.y - 32f); lineTo(b.x + 52f, b.y + 32f); close() }, pink)
                drawCircle(Color(0xFFD9608A), 15f, b)
                drawLine(Color.White.copy(alpha = 0.5f), Offset(b.x - 40f, b.y - 18f), Offset(b.x - 18f, b.y - 6f), 5f, StrokeCap.Round)
            }
        }
        "mu" -> {
            val t = rig.top
            rotate(rig.topTilt, t) {
                val hat = Path().apply { moveTo(t.x - 62f, t.y + 18f); lineTo(t.x + 62f, t.y + 18f); lineTo(t.x, t.y - 128f); close() }
                drawPath(hat, Color(0xFF7C3AED))
                listOf(-80f to 0.55f, -40f to 0.75f, 0f to 0.9f).forEach { (yy, f) ->
                    drawLine(Color(0xFFFFC21A), Offset(t.x - 62f * f * 0.85f, t.y + yy + 6f), Offset(t.x + 62f * f * 0.85f, t.y + yy - 8f), 9f, StrokeCap.Round)
                }
                drawCircle(Color(0xFFF27BA3), 18f, Offset(t.x, t.y - 130f))
            }
        }
        "vuongmien" -> {
            val t = rig.top
            rotate(rig.topTilt, t) {
                val crown = Path().apply {
                    moveTo(t.x - 72f, t.y + 20f); lineTo(t.x - 80f, t.y - 50f); lineTo(t.x - 38f, t.y - 14f)
                    lineTo(t.x, t.y - 66f); lineTo(t.x + 38f, t.y - 14f); lineTo(t.x + 80f, t.y - 50f); lineTo(t.x + 72f, t.y + 20f); close()
                }
                drawPath(crown, Color(0xFFFFC21A))
                drawPath(crown, Color(0xFFE09A00), style = Stroke(5f))
                drawCircle(Color(0xFFE5484D), 10f, Offset(t.x, t.y - 4f))
                drawCircle(Color(0xFF4F7BF0), 8f, Offset(t.x - 44f, t.y + 2f))
                drawCircle(Color(0xFF4FBF7A), 8f, Offset(t.x + 44f, t.y + 2f))
            }
        }
        "huychuong" -> {
            val n = rig.neck
            val c = rig.chest
            drawLine(Color(0xFF2F55D4), Offset(n.x - 34f, n.y), Offset(c.x - 8f, c.y - 30f), 16f, StrokeCap.Round)
            drawLine(Color(0xFFE5484D), Offset(n.x + 34f, n.y), Offset(c.x + 8f, c.y - 30f), 16f, StrokeCap.Round)
            drawCircle(Color(0xFFB87800), 36f, c)
            drawCircle(Color(0xFFFFC21A), 31f, c)
            drawPath(star(c, 18f, 8f), Color(0xFFB87800))
        }
    }
}

private fun star(c: Offset, outer: Float, inner: Float): Path = Path().apply {
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outer else inner
        val a = Math.toRadians(-90.0 + i * 36.0)
        val x = c.x + (r * kotlin.math.cos(a)).toFloat()
        val y = c.y + (r * kotlin.math.sin(a)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

/* ---------------------------------------- Thú cưng ---------------------------------------- */

private fun DrawScope.drawPet(id: String) {
    when (id) {
        "meo" -> {
            val c = col(0xFFF2A65A)
            line("M264 262 Q292 250 282 226", c, 7f)
            oval(245f, 262f, 24f, 19f, c)
            line("M232 256 L240 254 M231 264 L239 263", col(0xFFD9853A), 3f)
            fill("M231 218 L233 200 L244 213 Z", c); fill("M259 218 L257 200 L246 213 Z", c)
            circle(245f, 228f, 17f, c)
            circle(239f, 227f, 2.5f, EyeInk); circle(251f, 227f, 2.5f, EyeInk); circle(245f, 233f, 2f, col(0xFFE07A7A))
        }
        "cun" -> {
            val c = col(0xFFC58B5C)
            line("M266 254 Q282 244 278 232", c, 6f)
            oval(245f, 262f, 25f, 19f, c); oval(236f, 266f, 7f, 6f, col(0xFFE8C4A0))
            circle(245f, 228f, 18f, c)
            oval(228f, 228f, 7f, 13f, col(0xFF8B5A35)); oval(262f, 228f, 7f, 13f, col(0xFF8B5A35))
            circle(239f, 226f, 2.5f, EyeInk); circle(251f, 226f, 2.5f, EyeInk); oval(245f, 234f, 4f, 3f, EyeInk)
        }
        "tho" -> {
            val c = col(0xFFF7F7F7); val edge = col(0xFFE5E7EB)
            circle(266f, 268f, 6f, White)
            oval(245f, 264f, 23f, 19f, edge); oval(245f, 264f, 22f, 18f, c)
            oval(238f, 204f, 7f, 17f, edge); oval(238f, 204f, 6f, 16f, c)
            oval(252f, 204f, 7f, 17f, edge); oval(252f, 204f, 6f, 16f, c)
            oval(238f, 205f, 2.5f, 10f, col(0xFFF7B3C8)); oval(252f, 205f, 2.5f, 10f, col(0xFFF7B3C8))
            circle(245f, 232f, 17f, edge); circle(245f, 232f, 16f, c)
            circle(239f, 231f, 2.5f, EyeInk); circle(251f, 231f, 2.5f, EyeInk); circle(245f, 237f, 2f, col(0xFFF27BA3))
        }
        "rong" -> {
            val c = col(0xFF4FBF7A)
            line("M226 270 Q206 276 210 258", c, 7f)
            fill("M262 248 Q292 228 286 258 Q277 254 268 262 Z", col(0xFF3FA866))
            oval(245f, 262f, 24f, 20f, c); oval(245f, 268f, 14f, 12f, col(0xFFCFF3D9))
            fill("M234 212 L236 200 L242 210 Z M256 212 L254 200 L248 210 Z", col(0xFFFFD45A))
            circle(245f, 226f, 18f, c)
            circle(239f, 225f, 3f, EyeInk); circle(251f, 225f, 3f, EyeInk)
            circle(240f, 224f, 1f, White); circle(252f, 224f, 1f, White)
            line("M240 234 Q245 238 250 234", col(0xFF2B6B44), 2f)
        }
    }
}

/* ---------------------------------------- Composable ---------------------------------------- */

/**
 * Cảnh đầy đủ: phông + nhân vật + thú cưng. Đứng yên, chỉ thỉnh thoảng chớp mắt (đỡ tốn pin).
 */
@Composable
fun CharacterScene(look: Look, modifier: Modifier = Modifier, animate: Boolean = true) {
    val rig = rigOf(look.gender)
    val img = ImageBitmap.imageResource(rig.res)
    val blinkImg = ImageBitmap.imageResource(rig.blinkRes)
    var blink by remember { mutableStateOf(false) }
    if (animate) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(Random.nextLong(2800, 5200))
                blink = true; delay(130); blink = false
                if (Random.nextFloat() < 0.25f) { delay(170); blink = true; delay(110); blink = false }
            }
        }
    }
    Canvas(modifier.semantics { contentDescription = "Nhân vật của bé" }) {
        val w = size.width
        val h = size.height
        val s = maxOf(w / 300f, h / 300f)
        withTransform({
            translate((w - 300f * s) / 2f, (h - 300f * s) / 2f)
            scale(s, s, Offset.Zero)
        }) {
            drawScene(look.scene)
            drawOval(Color.Black, Offset(80f, 286f), Size(100f, 12f), alpha = 0.13f)
            if (look.pet != "none") drawOval(Color.Black, Offset(222f, 282f), Size(56f, 10f), alpha = 0.12f)
            withTransform({
                translate(130f - rig.w * SCENE_SCALE / 2f, 292f - rig.h * SCENE_SCALE)
                scale(SCENE_SCALE, SCENE_SCALE, Offset.Zero)
            }) { drawCharacter(if (blink) blinkImg else img, rig, look.accessory) }
            drawPet(look.pet)
        }
    }
}

/** Ảnh đại diện tròn: khuôn mặt nhân vật trên nền màu của phông. */
@Composable
fun CharacterPortrait(look: Look, size: Dp, modifier: Modifier = Modifier) {
    val rig = rigOf(look.gender)
    val img = ImageBitmap.imageResource(rig.res)
    Canvas(modifier.size(size).clip(CircleShape).semantics { contentDescription = "Ảnh đại diện" }) {
        drawRect(sceneTint(look.scene))
        val w = this.size.width
        val crop = 460f
        val s = w / crop
        withTransform({
            translate(w / 2f - rig.face.x * s, w / 2f - rig.face.y * s)
            scale(s, s, Offset.Zero)
        }) { drawCharacter(img, rig, look.accessory) }
    }
}

/** Nhân vật toàn thân (không phông) – dùng khi chọn bé trai / bé gái. */
@Composable
fun CharacterFigure(look: Look, modifier: Modifier = Modifier) {
    val rig = rigOf(look.gender)
    val img = ImageBitmap.imageResource(rig.res)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val s = minOf(w / rig.w, h / 958f)
        withTransform({
            translate(w / 2f - rig.w * s / 2f, h - rig.h * s)
            scale(s, s, Offset.Zero)
        }) { drawCharacter(img, rig, look.accessory) }
    }
}

/** Thú cưng đứng một mình – dùng cho ô thú cưng trong cửa hàng. */
@Composable
fun PetIcon(id: String, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val s = minOf(w / 96f, h / 96f)
        withTransform({
            translate(w / 2f - 248f * s, h / 2f - 242f * s)
            scale(s, s, Offset.Zero)
        }) { drawPet(id) }
    }
}

/** Ảnh đại diện của một hồ sơ con. */
@Composable
fun AvatarView(child: ChildProfile, size: Dp, modifier: Modifier = Modifier) = CharacterPortrait(child.look, size, modifier)

@Composable
fun AvatarView(look: Look, size: Dp, modifier: Modifier = Modifier) = CharacterPortrait(look, size, modifier)
