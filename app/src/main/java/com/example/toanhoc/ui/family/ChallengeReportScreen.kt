package com.example.toanhoc.ui.family

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.family.ChildProfile
import com.example.toanhoc.domain.challenge.Challenge
import com.example.toanhoc.domain.challenge.ChallengeQuestion
import com.example.toanhoc.domain.challenge.ChallengeRules
import com.example.toanhoc.domain.challenge.ChallengeUnit
import com.example.toanhoc.domain.curriculum.Curricula
import com.example.toanhoc.ui.theme.NunitoFontFamily

/** Báo cáo kết quả thử thách vượt bài – dành cho phụ huynh. */
@Composable
fun ChallengeReportScreen(
    child: ChildProfile,
    challenge: Challenge,
    onBack: () -> Unit,
    onEditRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val passed = challenge.passedUnits
    val kept = challenge.units.filter { !it.passed }
    val asked = challenge.units.flatMap { it.questions }.filter { it.answered }
    val correct = asked.count { it.correct }
    val totalMs = asked.sumOf { it.ms }
    val curriculum = Curricula.forGrade(challenge.grade)
    fun lessonLabel(id: String) = curriculum?.lesson(id)?.let { "Bài ${it.number}" } ?: id

    Column(modifier.fillMaxSize().background(FamilyTone.Paper)) {
        Column(Modifier.fillMaxWidth().background(FamilyTone.Parent).padding(bottom = 16.dp)) {
            BackRow("Kết quả thử thách", onBack = onBack, dark = true)
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FText("${child.name} · Lớp ${challenge.grade} · ${fmtDateTime(challenge.finishedAt ?: challenge.createdAt)}", size = 13.sp, color = Color.White.copy(alpha = 0.75f))
                Row(verticalAlignment = Alignment.Bottom) {
                    FText("${passed.size}", size = 40.sp, weight = FontWeight.ExtraBold, color = Color.White)
                    FText("/${challenge.units.size} mảng được duyệt", size = 17.sp, weight = FontWeight.Bold, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                }
                // Thanh tỉ lệ duyệt / giữ lại
                Row(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))) {
                    if (passed.isNotEmpty()) Box(Modifier.weight(passed.size.toFloat()).height(8.dp).background(Color(0xFFF6C453)))
                    if (kept.isNotEmpty()) Box(Modifier.weight(kept.size.toFloat()).height(8.dp).background(Color.White.copy(alpha = 0.35f)))
                }
            }
        }

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)
                .align(Alignment.CenterHorizontally).widthIn(max = 640.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("${challenge.masteredLessonIds.size}", "bài ⚡ thành thạo", RoadTone.Bolt, RoadTone.BoltSoft, Modifier.weight(1f))
                StatTile("${kept.sumOf { it.lessonIds.size }}", "bài giữ lại", RoadTone.Keep, RoadTone.KeepSoft, Modifier.weight(1f))
                StatTile("$correct/${asked.size}", "câu đúng", FamilyTone.Primary, FamilyTone.PrimarySoft, Modifier.weight(1f))
            }
            FText(
                "Con làm trong khoảng ${((totalMs / 60000).coerceAtLeast(1))} phút. " +
                    "Bài được duyệt đã hiện dấu ⚡ trên trang học của con và không cộng xu; bài giữ lại con học theo thứ tự như bình thường.",
                size = 13.sp, color = FamilyTone.Muted, lineHeight = 19.sp
            )

            if (kept.isNotEmpty()) {
                SectionTitle("Giữ lại trong lộ trình", trailing = "${kept.size} mảng")
                kept.forEach { u -> UnitCard(u, passed = false, finishedAt = challenge.finishedAt, labelOf = ::lessonLabel) }
            }
            if (passed.isNotEmpty()) {
                SectionTitle("Được duyệt", trailing = "${passed.size} mảng")
                passed.forEach { u -> UnitCard(u, passed = true, finishedAt = challenge.finishedAt, labelOf = ::lessonLabel) }
            }

            FCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    IconBadge(Icons.Rounded.Flag, FamilyTone.Green, FamilyTone.GreenSoft, 36)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        FText("Lưới an toàn", size = 15.sp, weight = FontWeight.ExtraBold)
                        FText(
                            "Bài được duyệt vẫn có trong đề kiểm tra học kì và giải đấu. Nếu con sai ở bài kiểm tra học kì, " +
                                "bài đó tự trở về \"Học lại\" để con học thật từ đầu.",
                            size = 13.sp, color = FamilyTone.Muted, lineHeight = 19.sp
                        )
                    }
                }
            }
            FButton("Chỉnh lộ trình", Modifier.fillMaxWidth(), outlined = true, onClick = onEditRoadmap)
            Gap(8.dp)
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, fg: Color, bg: Color, modifier: Modifier) = Column(
    modifier.clip(RoundedCornerShape(16.dp)).background(bg).padding(horizontal = 12.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(2.dp)
) {
    FText(value, size = 22.sp, weight = FontWeight.ExtraBold, color = fg)
    FText(label, size = 12.sp, weight = FontWeight.Bold, color = fg.copy(alpha = 0.85f), maxLines = 1)
}

@Composable
private fun UnitCard(u: ChallengeUnit, passed: Boolean, finishedAt: Long?, labelOf: (String) -> String) {
    FCard {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                FText(u.title, size = 16.sp, weight = FontWeight.ExtraBold, lineHeight = 21.sp)
                FText(u.lessonIds.joinToString(" · ") { labelOf(it) }, size = 12.sp, color = FamilyTone.Muted, lineHeight = 17.sp)
            }
            if (passed) SmallPill("⚡ Được duyệt", RoadTone.Bolt, RoadTone.BoltSoft)
            else SmallPill("Giữ lại", RoadTone.Keep, RoadTone.KeepSoft)
        }
        // Ba ô kết quả nhanh
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            u.questions.forEachIndexed { i, q -> ResultDot(i, q, Modifier.weight(1f)) }
        }
        val reasons = u.reasons()
        if (!passed && reasons.isNotEmpty()) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(RoadTone.KeepSoft).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FText("Vì sao chưa duyệt", size = 13.sp, weight = FontWeight.ExtraBold, color = RoadTone.Keep)
                reasons.forEach { Bullet(it) }
                if (finishedAt != null) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.Schedule, null, tint = RoadTone.Keep, modifier = Modifier.size(16.dp))
                    FText(
                        "Kiểm tra lại từ ${fmtDay(ChallengeRules.retestAt(finishedAt))} hoặc khi con học xong Dạng 1 một bài trong mảng.",
                        size = 12.sp, color = RoadTone.Keep, weight = FontWeight.SemiBold, lineHeight = 17.sp, modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        u.questions.forEachIndexed { i, q -> QuestionDetail(i, q) }
    }
}

@Composable
private fun ResultDot(i: Int, q: ChallengeQuestion, modifier: Modifier) {
    val (fg, bg, icon) = when {
        !q.answered -> Triple(FamilyTone.Muted, Color(0xFFF1F3F6), Icons.Rounded.Remove)
        q.correct && !q.tooFast -> Triple(FamilyTone.Green, FamilyTone.GreenSoft, Icons.Rounded.Check)
        q.correct -> Triple(RoadTone.Keep, RoadTone.KeepSoft, Icons.Rounded.Bolt)
        else -> Triple(FamilyTone.Red, FamilyTone.RedSoft, Icons.Rounded.Close)
    }
    Row(
        modifier.clip(RoundedCornerShape(10.dp)).background(bg).padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(16.dp))
        FText("Câu ${i + 1}", size = 12.sp, weight = FontWeight.ExtraBold, color = fg, maxLines = 1)
    }
}

@Composable
private fun QuestionDetail(i: Int, q: ChallengeQuestion) {
    var open by rememberSaveable(q.statement) { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, FamilyTone.Line, RoundedCornerShape(12.dp))) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button) { open = !open }.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                FText("Câu ${i + 1} · ${ChallengeRules.ROLE_NAMES.getOrElse(q.role) { "" }}", size = 13.sp, weight = FontWeight.ExtraBold)
                FText(
                    when {
                        !q.answered -> "Không làm (dừng sớm sau câu 1)"
                        q.correct && q.tooFast -> "Đúng nhưng quá nhanh · ${q.ms / 1000} giây"
                        q.correct -> "Đúng · ${q.ms / 1000} giây"
                        q.trap != null && q.picked == q.trap -> "Sai · chọn đáp án bẫy · ${q.ms / 1000} giây"
                        else -> "Sai · ${q.ms / 1000} giây"
                    },
                    size = 12.sp, color = FamilyTone.Muted
                )
            }
            Icon(if (open) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, if (open) "Thu gọn" else "Xem đề", tint = FamilyTone.Muted)
        }
        AnimatedVisibility(open, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Đề bài, tô màu câu dữ kiện thừa
                Text(
                    buildAnnotatedString {
                        val noise = q.noise
                        val idx = if (noise != null) q.statement.indexOf(noise) else -1
                        if (idx < 0 || noise == null) append(q.statement) else {
                            append(q.statement.substring(0, idx))
                            withStyle(SpanStyle(background = RoadTone.BoltSoft, color = RoadTone.Keep, fontWeight = FontWeight.Bold)) { append(noise) }
                            append(q.statement.substring(idx + noise.length))
                        }
                    },
                    fontFamily = NunitoFontFamily, fontSize = 14.sp, lineHeight = 21.sp, color = FamilyTone.Ink
                )
                if (q.noise != null) FText("Phần tô màu là dữ kiện thừa app chèn vào để kiểm tra khả năng lọc thông tin.", size = 12.sp, color = FamilyTone.Muted, lineHeight = 17.sp)
                if (q.answered) AnswerLine("Con chọn", q.picked!!, if (q.correct) FamilyTone.Green else FamilyTone.Red)
                AnswerLine("Đáp án đúng", q.answer, FamilyTone.Green)
                if (q.trap != null) AnswerLine("Đáp án bẫy", q.trap, RoadTone.Keep)
            }
        }
    }
}

@Composable
private fun AnswerLine(label: String, value: String, color: Color) = Row(verticalAlignment = Alignment.CenterVertically) {
    FText(label, size = 13.sp, color = FamilyTone.Muted, modifier = Modifier.weight(1f))
    FText(value, size = 14.sp, weight = FontWeight.ExtraBold, color = color)
}
