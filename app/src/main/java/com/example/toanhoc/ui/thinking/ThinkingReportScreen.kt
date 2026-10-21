package com.example.toanhoc.ui.thinking

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.family.League
import java.time.LocalDate
import kotlin.random.Random

/** Một dòng trên bảng xếp hạng nhiệm vụ (bạn thi là tên ảo, như giải đấu). */
data class MissionRank(val name: String, val isMe: Boolean, val correct: Int, val seconds: Int)

object MissionLeaderboard {
    /** 4 bạn thi mô phỏng, cố định theo con + bài + ngày; mức đúng quanh 55–85%. */
    fun build(childName: String, lessonKey: String, myCorrect: Int, mySeconds: Int, total: Int = 12): List<MissionRank> {
        val rng = Random((childName + lessonKey + LocalDate.now()).hashCode().toLong())
        val bots = League.NAMES.shuffled(rng).take(4).map { n ->
            val p = 0.55 + rng.nextDouble() * 0.30
            val c = (0 until total).count { rng.nextDouble() < p }
            MissionRank(n, false, c, total * (6 + rng.nextInt(9)))
        }
        return (bots + MissionRank(childName.ifBlank { "Con" }, true, myCorrect, mySeconds))
            .sortedWith(compareByDescending<MissionRank> { it.correct }.thenBy { it.seconds })
    }
}

@Composable
fun ThinkingReportScreen(
    lessonTitle: String,
    lessonKey: String,
    stage: String?,
    childName: String,
    grade: Int,
    correct: Int,
    total: Int,
    averageLatencyMs: Long,
    passed: Boolean,
    perfectRuns: Int?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDone)
    val outcomes = ThinkingRunLog.outcomes.toList()
    val fixedN = outcomes.count { it == Outcome.FIXED }
    val best = ThinkingRunLog.bestStreak
    val seconds = ((averageLatencyMs * total) / 1000).toInt().coerceAtLeast(1)
    val ranks = remember(lessonKey, correct) { MissionLeaderboard.build(childName, lessonKey, correct, seconds, total.coerceAtLeast(1)) }
    val myRank = ranks.indexOfFirst { it.isMe } + 1
    val context = androidx.compose.ui.platform.LocalContext.current
    var shown by androidx.compose.runtime.remember(lessonKey) { androidx.compose.runtime.mutableIntStateOf(0) }
    androidx.compose.runtime.LaunchedEffect(lessonKey) {
        kotlinx.coroutines.delay(350)
        for (i in 1..correct) { shown = i; com.example.toanhoc.ui.sound.SoundEffectsManager.play(context, com.example.toanhoc.ui.sound.Sfx.TICK); kotlinx.coroutines.delay(85) }
        kotlinx.coroutines.delay(150)
        com.example.toanhoc.ui.sound.SoundEffectsManager.play(context, if (passed) com.example.toanhoc.ui.sound.Sfx.COMPLETE else com.example.toanhoc.ui.sound.Sfx.HINT)
    }

    Box(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(TD.BgTop, TD.BgBottom)))) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TText("BÁO CÁO NHIỆM VỤ · ${stageLabel(stage).uppercase()}", 12.sp, FontWeight.Black, TD.Cyan, letterSpacing = 1.4.sp)
            TText(lessonTitle, 24.sp, FontWeight.Black, lineHeight = 30.sp)

            // Vòng điểm + kết luận
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(TD.Card).border(1.dp, TD.Line, RoundedCornerShape(22.dp)).padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreRing(shown, total)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.clip(CircleShape).background(if (passed) TD.Green.copy(alpha = 0.18f) else TD.Coral.copy(alpha = 0.18f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        TText(if (passed) "HOÀN THÀNH NHIỆM VỤ" else "CHƯA ĐẠT", 12.sp, FontWeight.Black, if (passed) TD.Green else TD.Coral, letterSpacing = 0.8.sp)
                    }
                    TText(if (passed) "Bước tiếp theo đã được mở." else "Cần đúng ngay lần đầu từ ${(total * 8 + 9) / 10} câu. Làm lại nhé!", 15.sp, FontWeight.SemiBold, TD.Muted, lineHeight = 20.sp)
                }
            }

            ProgressChain(outcomes, current = -1)
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Legend(TD.Green, "Đúng ngay"); Legend(TD.Amber, "Tự sửa được"); Legend(TD.Coral, "Chưa được")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat(Icons.Rounded.Verified, TD.Amber, "$fixedN", "lỗi đã tự sửa", Modifier.weight(1f))
                Stat(Icons.Rounded.LocalFireDepartment, Color(0xFFFF9F45), "$best", "câu đúng liền", Modifier.weight(1f))
                Stat(Icons.Rounded.Timer, TD.Cyan, fmtSec(averageLatencyMs), "mỗi câu", Modifier.weight(1f))
            }

            if (stage == "HARD" && perfectRuns != null) {
                val left = (2 - perfectRuns).coerceAtLeast(0)
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(TD.Violet.copy(alpha = 0.16f)).padding(14.dp)) {
                    TText(
                        if (correct >= total) (if (left == 0) "Đúng trọn 12/12! Chuyên đề đã lên xếp loại Thành thạo." else "Đúng trọn 12/12! Thêm $left lượt nữa để lên Thành thạo.")
                        else "Đúng trọn 12/12 câu Dạng 2 hai lượt để được xếp loại Thành thạo.",
                        15.sp, FontWeight.Bold, TD.Ink, lineHeight = 21.sp
                    )
                }
            }

            // Bảng xếp hạng
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(TD.Card).border(1.dp, TD.Line, RoundedCornerShape(22.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = TD.Amber, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    TText("Bảng xếp hạng nhiệm vụ hôm nay", 16.sp, FontWeight.Black, modifier = Modifier.weight(1f))
                    TText("Hạng $myRank/${ranks.size}", 14.sp, FontWeight.Black, TD.Amber)
                }
                ranks.forEachIndexed { i, r -> RankRow(i + 1, r) }
                TText("Các bạn trong bảng là bạn thi mô phỏng.", 12.sp, FontWeight.SemiBold, TD.Faint)
            }

            Spacer(Modifier.height(4.dp))
            PrimaryButton("Hoàn thành", onClick = onDone)
            StudentCard(childName, grade)
        }
    }
}

private fun fmtSec(ms: Long): String {
    val tenths = (ms + 50) / 100
    return "${tenths / 10},${tenths % 10} giây"
}

@Composable
private fun ScoreRing(correct: Int, total: Int) {
    val frac = if (total == 0) 0f else correct.toFloat() / total
    Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(96.dp)) {
            val stroke = 10.dp.toPx()
            val inset = stroke / 2
            drawArc(TD.Line, -90f, 360f, false, Offset(inset, inset), Size(size.width - stroke, size.height - stroke), style = Stroke(stroke))
            drawArc(if (frac >= 0.8f) TD.Green else TD.Amber, -90f, 360f * frac, false, Offset(inset, inset), Size(size.width - stroke, size.height - stroke), style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TText("$correct/$total", 24.sp, FontWeight.Black)
            TText("đúng ngay", 11.sp, FontWeight.ExtraBold, TD.Muted)
        }
    }
}

@Composable
private fun Legend(c: Color, t: String) = Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(10.dp).clip(CircleShape).background(c)); Spacer(Modifier.width(5.dp)); TText(t, 12.sp, FontWeight.SemiBold, TD.Muted)
}

@Composable
private fun Stat(icon: ImageVector, tint: Color, value: String, label: String, modifier: Modifier) = Column(
    modifier.clip(RoundedCornerShape(18.dp)).background(TD.Card).border(1.dp, TD.Line, RoundedCornerShape(18.dp)).padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
) {
    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    TText(value, 18.sp, FontWeight.Black, maxLines = 1)
    TText(label, 12.sp, FontWeight.SemiBold, TD.Muted, maxLines = 1)
}

@Composable
private fun RankRow(rank: Int, r: MissionRank) = Row(
    Modifier.fillMaxWidth().heightIn(min = 44.dp).clip(RoundedCornerShape(12.dp))
        .background(if (r.isMe) TD.Amber.copy(alpha = 0.16f) else Color.Transparent)
        .border(if (r.isMe) 1.dp else 0.dp, if (r.isMe) TD.Amber.copy(alpha = 0.6f) else Color.Transparent, RoundedCornerShape(12.dp))
        .padding(horizontal = 10.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Box(
        Modifier.size(26.dp).clip(CircleShape).background(when (rank) { 1 -> TD.Amber; 2 -> Color(0xFFC9D2E8); 3 -> Color(0xFFE0A26E); else -> TD.CardHi }),
        contentAlignment = Alignment.Center
    ) { TText("$rank", 13.sp, FontWeight.Black, if (rank <= 3) TD.AmberInk else TD.Muted) }
    Spacer(Modifier.width(10.dp))
    TText(if (r.isMe) "${r.name} (con)" else r.name, 15.sp, if (r.isMe) FontWeight.Black else FontWeight.Bold, if (r.isMe) TD.Amber else TD.Ink, Modifier.weight(1f), maxLines = 1)
    TText("${r.correct} đúng", 14.sp, FontWeight.ExtraBold, TD.Ink)
    Spacer(Modifier.width(10.dp))
    TText("${r.seconds / 60}:${(r.seconds % 60).toString().padStart(2, '0')}", 13.sp, FontWeight.SemiBold, TD.Muted, align = TextAlign.End)
}
