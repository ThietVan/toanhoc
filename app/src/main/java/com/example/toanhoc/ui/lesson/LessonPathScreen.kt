package com.example.toanhoc.ui.lesson

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.domain.curriculum.GradeCurriculum
import com.example.toanhoc.domain.curriculum.LessonStage
import com.example.toanhoc.domain.curriculum.StageState
import com.example.toanhoc.domain.curriculum.TextbookLesson
import com.example.toanhoc.domain.curriculum.doneCount
import com.example.toanhoc.domain.curriculum.stageState
import com.example.toanhoc.domain.curriculum.stages
import com.example.toanhoc.ui.theme.NunitoFontFamily
import com.example.toanhoc.ui.theme.TargetUnknownAmber

private val Ink = Color(0xFF111827)
private val Muted = Color(0xFF6B7280)
private val Line = Color(0xFFE4E7EC)
private val Paper = Color(0xFFF7F8FA)
private val Done = Color(0xFF1E7F4F)
private val LockedFill = Color(0xFFEEF0F3)

/** Màu chủ đạo của bài theo nhóm kiến thức (khớp màu thẻ ở trang chủ). */
internal fun domainColor(domain: String): Color = when (domain) {
    "GEOMETRY_MEASUREMENT" -> Color(0xFF2F4FE0)
    "ARITHMETIC" -> Color(0xFFD13F1F)
    "HEURISTICS_COMBINATORICS" -> Color(0xFF5B3FD6)
    "DATA_STATS_PROBABILITY" -> Color(0xFF0B7A6F)
    "ALGEBRAIC_THINKING" -> Color(0xFFC0245F)
    else -> Color(0xFF3E4A6B)
}

private fun stageIcon(stage: LessonStage): ImageVector = when (stage) {
    LessonStage.LEARN -> Icons.Rounded.PlayCircle
    LessonStage.EASY -> Icons.Rounded.StarHalf
    LessonStage.HARD -> Icons.Rounded.Star
    LessonStage.REVIEW -> Icons.Rounded.Replay
}

private fun stageHint(stage: LessonStage): String = when (stage) {
    LessonStage.LEARN -> "Xem bài giảng có hình và giọng đọc · xem xong mới mở Dạng 1"
    LessonStage.EASY -> "12 câu cơ bản · đúng 80% để qua"
    LessonStage.HARD -> "12 câu nâng cao · đúng 80% để qua"
    LessonStage.REVIEW -> "Trộn câu của cả chủ đề"
}

/**
 * Lộ trình một bài: Học bài → Dạng 1 (dễ) → Dạng 2 (khó) → Ôn tập.
 * Bước hiện tại là nút lớn có "Bắt đầu"; bước đã xong có dấu tích (bấm để làm lại); bước sau bị khóa.
 */
@Composable
fun LessonPathScreen(
    curriculum: GradeCurriculum,
    lesson: TextbookLesson,
    completed: Int,
    onBack: () -> Unit,
    onStartStage: (LessonStage) -> Unit,
    modifier: Modifier = Modifier,
    /** Xu thưởng của bước nếu con chưa từng qua bước đó (null = không còn thưởng). */
    coinsFor: (LessonStage) -> Int? = { null }
) {
    BackHandler(onBack = onBack)
    val accent = domainColor(lesson.domain)
    val topic = curriculum.topicOf(lesson)
    val stages = lesson.stages()
    val done = lesson.doneCount(completed)
    val next = curriculum.lessons.getOrNull(curriculum.lessons.indexOf(lesson) + 1)

    Column(modifier.fillMaxSize().background(Paper)) {
        // Đầu trang
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .drawBehind { drawLine(Line, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }
                .padding(start = 12.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Quay lại", tint = Ink)
                }
                Text(
                    "Chủ đề ${topic.number} · ${topic.title}",
                    fontFamily = NunitoFontFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = Muted, maxLines = 1, modifier = Modifier.weight(1f)
                )
            }
            Row(
                Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(accent)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("BÀI ${lesson.number}", fontFamily = NunitoFontFamily, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f), letterSpacing = 0.6.sp)
                    Text(lesson.title, fontFamily = NunitoFontFamily, fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold, color = Color.White, lineHeight = 23.sp)
                }
                Text(
                    "$done/${stages.size}",
                    fontFamily = NunitoFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        // Con đường
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            stages.forEachIndexed { i, stage ->
                val state = lesson.stageState(stage, completed)
                // lượn nhẹ trái – phải cho có cảm giác "đường đi"
                val shift = when (i % 4) { 1 -> 44.dp; 3 -> (-44).dp; else -> 0.dp }
                if (i > 0) Connector(shift = shift, prevShift = when ((i - 1) % 4) { 1 -> 44.dp; 3 -> (-44).dp; else -> 0.dp })
                StageNode(
                    stage = stage,
                    state = state,
                    accent = accent,
                    coins = coinsFor(stage),
                    modifier = Modifier.offset(x = shift),
                    onClick = { if (state != StageState.LOCKED) onStartStage(stage) }
                )
            }

            if (lesson.practiceGap) {
                Spacer(Modifier.height(20.dp))
                Text(
                    "Dạng đề riêng của bài này đang được bổ sung – tạm thời luyện với dạng gần nhất.",
                    fontFamily = NunitoFontFamily, fontSize = 12.sp, color = Muted, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
            Spacer(Modifier.height(28.dp))
            Row(
                Modifier
                    .padding(horizontal = 24.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Line, CircleShape)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = TargetUnknownAmber, modifier = Modifier.size(18.dp))
                Text(
                    if (next != null) "Qua Dạng 1 để mở Bài ${next.number}" else "Bài cuối cùng của lớp",
                    fontFamily = NunitoFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5563)
                )
            }
        }
    }
}

@Composable
private fun Connector(shift: Dp, prevShift: Dp) {
    Box(
        Modifier
            .width(120.dp)
            .height(34.dp)
            .drawBehind {
                val x0 = size.width / 2f + prevShift.toPx()
                val x1 = size.width / 2f + shift.toPx()
                drawLine(
                    Color(0xFFD1D5DB), Offset(x0, 0f), Offset(x1, size.height), 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()))
                )
            }
    )
}

@Composable
private fun StageNode(
    stage: LessonStage,
    state: StageState,
    accent: Color,
    coins: Int?,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (state) {
            StageState.CURRENT -> {
                Row(
                    Modifier
                        .shadow(6.dp, RoundedCornerShape(12.dp), clip = false)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(stage.label, fontFamily = NunitoFontFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                    if (coins != null) com.example.toanhoc.ui.family.CoinAmount(coins, size = 18.dp)
                }
                Box(
                    Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f))
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(accent)
                        .clickable(role = Role.Button, onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(stageIcon(stage), contentDescription = stage.label, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(Ink)
                        .clickable(role = Role.Button, onClick = onClick)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Bắt đầu", fontFamily = NunitoFontFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text(
                    stageHint(stage),
                    fontFamily = NunitoFontFamily, fontSize = 12.sp, color = Muted, textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 220.dp)
                )
            }
            StageState.DONE -> {
                Box(
                    Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Done)
                        .clickable(role = Role.Button, onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = "${stage.label} – đã xong, bấm để làm lại", tint = Color.White, modifier = Modifier.size(34.dp))
                }
                Text(stage.label, fontFamily = NunitoFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Done)
            }
            StageState.LOCKED -> {
                Box(
                    Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(LockedFill)
                        .border(2.dp, Line, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = "${stage.label} – chưa mở", tint = Color(0xFF9CA3AF), modifier = Modifier.size(26.dp))
                }
                Text(stage.label, fontFamily = NunitoFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Muted)
                if (coins != null) com.example.toanhoc.ui.family.CoinAmount(coins, size = 15.dp, muted = true)
            }
        }
    }
}
