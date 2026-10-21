package com.example.toanhoc.ui.family

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.family.CoinEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlin.math.cos
import kotlin.math.sin

private val CoinLight = Color(0xFFFFE27A)
private val CoinMid = Color(0xFFFFC21A)
private val CoinDark = Color(0xFFE09A00)
private val CoinRim = Color(0xFFB87800)
val CoinText = Color(0xFFB45F06)

/** Đồng xu vàng vẽ bằng Canvas: viền, vành trong, ngôi sao nổi và vệt sáng. */
@Composable
fun CoinIcon(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val r = this.size.minDimension / 2f
        val c = center
        drawCircle(CoinRim, r, c)
        drawCircle(Brush.linearGradient(listOf(CoinLight, CoinMid, CoinDark), start = Offset(c.x - r, c.y - r), end = Offset(c.x + r, c.y + r)), r * 0.88f, c)
        drawCircle(CoinRim.copy(alpha = 0.55f), r * 0.68f, c, style = Stroke(width = r * 0.08f))
        // ngôi sao 5 cánh
        val star = Path()
        for (i in 0 until 10) {
            val rad = if (i % 2 == 0) r * 0.42f else r * 0.18f
            val a = Math.toRadians(-90.0 + i * 36.0)
            val p = Offset(c.x + (rad * cos(a)).toFloat(), c.y + (rad * sin(a)).toFloat())
            if (i == 0) star.moveTo(p.x, p.y) else star.lineTo(p.x, p.y)
        }
        star.close()
        drawPath(star, CoinRim.copy(alpha = 0.8f))
        drawPath(star, Color.White.copy(alpha = 0.35f), style = Stroke(width = r * 0.04f))
        // vệt sáng
        drawArc(Color.White.copy(alpha = 0.6f), 200f, 70f, false, topLeft = Offset(c.x - r * 0.76f, c.y - r * 0.76f),
            size = androidx.compose.ui.geometry.Size(r * 1.52f, r * 1.52f), style = Stroke(width = r * 0.1f))
    }
}

/**
 * Đồng xu + số xu: dùng ở mọi chỗ có thưởng (bước học, bài kiểm tra, huy hiệu) và giá trong cửa hàng.
 * [plus] = true hiện "+10" (thưởng), false hiện "60" (giá).
 */
@Composable
fun CoinAmount(amount: Int, modifier: Modifier = Modifier, plus: Boolean = true, size: Dp = 18.dp, onDark: Boolean = false, muted: Boolean = false) {
    Row(
        modifier.semantics { contentDescription = if (plus) "Thưởng $amount xu" else "$amount xu" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(size * 0.25f)
    ) {
        CoinIcon(size)
        FText(
            if (plus) "+$amount" else "$amount",
            size = (size.value * 0.82f).sp, weight = FontWeight.ExtraBold,
            color = when { onDark -> Color.White; muted -> FamilyTone.Muted; else -> CoinText }
        )
    }
}

/** Nhãn thưởng xu dạng viên thuốc nổi bật (nền vàng nhạt). */
@Composable
fun CoinRewardChip(amount: Int, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Box(
        modifier.clip(CircleShape).background(Color(0xFFFFF4D6)).border(1.dp, Color(0xFFF5D07A), CircleShape)
            .padding(start = 5.dp, end = 10.dp, top = 3.dp, bottom = 3.dp)
    ) { CoinAmount(amount, size = size) }
}

/**
 * Thông báo "+10 xu" trượt xuống ở đầu màn hình mỗi khi con được thưởng.
 * Hiện lần lượt từng thông báo, mỗi cái ~2,2 giây.
 */
@Composable
fun CoinToastHost(events: Flow<CoinEvent>, childId: String?, modifier: Modifier = Modifier) {
    var current by remember { mutableStateOf<CoinEvent?>(null) }
    var visible by remember { mutableStateOf(false) }
    val sfxContext = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(events, childId) {
        val queue = Channel<CoinEvent>(Channel.UNLIMITED)
        launch { events.collect { e -> if (e.childId == childId) queue.send(e) } }
        for (e in queue) {
            current = e
            visible = true
            com.example.toanhoc.ui.sound.SoundEffectsManager.play(sfxContext, com.example.toanhoc.ui.sound.Sfx.COIN)
            delay(2200)
            visible = false
            delay(400)
        }
    }

    Box(modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            val e = current
            if (e != null) {
                Row(
                    Modifier.widthIn(max = 420.dp).padding(horizontal = 16.dp)
                        .shadow(10.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp)).background(Color(0xFF1B2440))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CoinIcon(40.dp)
                    Column {
                        FText("+${e.amount} xu", size = 20.sp, weight = FontWeight.ExtraBold, color = Color(0xFFFFD25A))
                        FText(e.reason, size = 13.sp, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
                    }
                }
            }
        }
    }
}
