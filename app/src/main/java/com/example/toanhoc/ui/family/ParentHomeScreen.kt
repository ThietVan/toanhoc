package com.example.toanhoc.ui.family

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.family.AvatarShop
import com.example.toanhoc.data.family.ChildProfile
import com.example.toanhoc.data.family.CoinRules
import com.example.toanhoc.data.family.FamilyData
import com.example.toanhoc.data.family.MistakeEntry
import com.example.toanhoc.data.family.Reward
import com.example.toanhoc.domain.curriculum.Curricula
import com.example.toanhoc.domain.curriculum.LessonKeys
import com.example.toanhoc.domain.curriculum.effectiveCompleted
import com.example.toanhoc.domain.curriculum.isUnlocked
import com.example.toanhoc.domain.curriculum.stages
import com.example.toanhoc.ui.theme.NunitoFontFamily
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Các thao tác phụ huynh – MainActivity nối vào FamilyStore / LessonProgressStore. */
class ParentActions(
    val progressOf: (childId: String) -> Map<String, Int>,
    val assignRelearn: (childId: String, lessonId: String) -> Unit,
    val editChild: (childId: String) -> Unit,
    val addChild: () -> Unit,
    val changePin: () -> Unit,
    val setLimit: (childId: String, minutes: Int) -> Unit,
    val addReward: (title: String, emoji: String, cost: Int, childId: String?) -> Unit,
    val deleteReward: (rewardId: String) -> Unit,
    val deliver: (requestId: String) -> Unit,
    val refund: (requestId: String) -> Unit,
    val exit: () -> Unit,
    val openRoadmap: (childId: String) -> Unit = {},
    val openChallengeReport: (challengeId: String) -> Unit = {},
    val startChallengeNow: (childId: String) -> Unit = {}
)

@Composable
fun ParentHomeScreen(data: FamilyData, actions: ParentActions, modifier: Modifier = Modifier) {
    BackHandler(onBack = actions.exit)
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var childId by rememberSaveable { mutableStateOf(data.children.firstOrNull()?.id) }
    val child = data.child(childId) ?: data.children.firstOrNull()
    val pending = data.requests.count { !it.delivered }

    Column(modifier.fillMaxSize().background(FamilyTone.Paper).imePadding()) {
        // Đầu trang tối màu: báo hiệu đây là khu người lớn
        Column(Modifier.fillMaxWidth().background(FamilyTone.Parent).padding(bottom = 12.dp)) {
            BackRow("Khu phụ huynh", onBack = actions.exit, dark = true) {
                Row(
                    Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.12f)).clickable(role = Role.Button, onClick = actions.exit)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    FText("Khóa & thoát", size = 13.sp, weight = FontWeight.Bold, color = Color.White)
                }
            }
            if (data.children.size > 1 && tab == 0) {
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    data.children.forEach { c ->
                        val sel = c.id == child?.id
                        Row(
                            Modifier.clip(CircleShape).background(if (sel) Color.White else Color.White.copy(alpha = 0.1f))
                                .clickable(role = Role.Tab) { childId = c.id }.padding(start = 4.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AvatarView(c, 30.dp)
                            FText(c.name, size = 14.sp, weight = FontWeight.Bold, color = if (sel) FamilyTone.Ink else Color.White)
                        }
                    }
                }
                Gap(10.dp)
            }
            Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.1f)).padding(4.dp)) {
                listOf("Báo cáo", if (pending > 0) "Phần thưởng ($pending)" else "Phần thưởng", "Cài đặt").forEachIndexed { i, t ->
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (tab == i) Color.White else Color.Transparent)
                            .clickable(role = Role.Tab) { tab = i }.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) { FText(t, size = 14.sp, weight = FontWeight.ExtraBold, color = if (tab == i) FamilyTone.Ink else Color.White, maxLines = 1) }
                }
            }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp).align(Alignment.CenterHorizontally).widthIn(max = 640.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (tab) {
                0 -> if (child != null) ReportTab(data, child, actions) else EmptyChildren(actions)
                1 -> RewardsTab(data, actions)
                else -> SettingsTab(data, actions)
            }
            Gap(12.dp)
        }
    }
}

@Composable
private fun EmptyChildren(actions: ParentActions) {
    FCard {
        FText("Chưa có hồ sơ con", size = 17.sp, weight = FontWeight.ExtraBold)
        FText("Tạo hồ sơ để bắt đầu theo dõi việc học.", size = 14.sp, color = FamilyTone.Muted)
        FButton("Thêm hồ sơ con", modifier = Modifier.fillMaxWidth(), onClick = actions.addChild)
    }
}

/* ===================================== BÁO CÁO ===================================== */

@Composable
private fun ReportTab(data: FamilyData, child: ChildProfile, actions: ParentActions) {
    val progress = remember(child.id, data) { actions.progressOf(child.id) }
    val curriculum = Curricula.forGrade(child.grade)

    // Thẻ tóm tắt
    FCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            AvatarView(child, 60.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                FText(child.name, size = 20.sp, weight = FontWeight.ExtraBold)
                FText("${child.age} tuổi · đang học Lớp ${child.grade}", size = 14.sp, color = FamilyTone.Muted)
            }
            CoinPill(child.coins)
        }
    }

    // 1. Con có học đều không?
    val week = data.last7Days(child.id)
    val total = week.sumOf { it.second }
    val activeDays = week.count { it.second > 0 }
    val streak = data.streak(child.id)
    FCard {
        SectionTitle("Con có học đều không?", trailing = "7 ngày qua")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stat("$total phút", "tổng thời gian", Modifier.weight(1f))
            Stat("$activeDays/7 ngày", "có học", Modifier.weight(1f))
            Stat("$streak ngày", "liên tiếp", Modifier.weight(1f))
        }
        WeekBars(week, child.dailyLimitMinutes)
        val verdict = when {
            activeDays >= 5 -> "Rất đều – con giữ được thói quen học tốt." to FamilyTone.Green
            activeDays >= 3 -> "Khá đều – thêm 1–2 buổi ngắn mỗi tuần sẽ tốt hơn." to FamilyTone.Coin
            else -> "Chưa đều – 10–15 phút mỗi ngày hiệu quả hơn học dồn." to FamilyTone.Red
        }
        FText(verdict.first, size = 14.sp, weight = FontWeight.Bold, color = verdict.second)
    }

    // 2. Con đang ở đâu?
    if (curriculum != null) {
        fun prog(id: String) = progress[id] ?: 0
        val lessons = curriculum.lessons
        val finished = lessons.count { l -> l.effectiveCompleted(maxOf(prog(l.id), prog(LessonKeys.ever(l.id)))) >= 4 }
        val current = lessons.firstOrNull { l -> curriculum.isUnlocked(l) { prog(it) } && l.effectiveCompleted(prog(l.id)) < 4 }
        val relearn = lessons.filter { prog(LessonKeys.relearn(it.id)) == 1 }
        FCard {
            SectionTitle("Con đang học đến đâu?", trailing = "Lớp ${child.grade}")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FText("$finished/${lessons.size} bài", size = 22.sp, weight = FontWeight.ExtraBold)
                FText("đã học trọn 4 bước", size = 13.sp, color = FamilyTone.Muted)
            }
            ProgressBar(finished.toFloat() / lessons.size.coerceAtLeast(1), FamilyTone.Primary)
            if (current != null) {
                InfoRow("Đang học", "Bài ${current.number}: ${current.title}")
            }
            (1..2).forEach { sem ->
                val best = progress[LessonKeys.exam(child.grade, sem)]
                InfoRow(
                    "Kiểm tra học kì $sem",
                    when {
                        best == null -> "Chưa làm"
                        best >= 80 -> "Đạt · cao nhất $best%"
                        else -> "Chưa đạt · cao nhất $best%"
                    },
                    color = when { best == null -> FamilyTone.Muted; best >= 80 -> FamilyTone.Green; else -> FamilyTone.Coin }
                )
            }
            if (relearn.isNotEmpty()) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(FamilyTone.CoinSoft).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FText("Đang phải học lại (${relearn.size} bài)", size = 14.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Coin)
                    relearn.take(6).forEach { FText("• Bài ${it.number}: ${it.title}", size = 13.sp) }
                    if (relearn.size > 6) FText("… và ${relearn.size - 6} bài khác", size = 13.sp, color = FamilyTone.Muted)
                }
            }
        }
    }

    // Lộ trình học do bố mẹ chọn + thử thách vượt bài
    RoadmapSummaryCard(
        child = child,
        history = data.challengesOf(child.id),
        pending = data.pendingChallenge(child.id),
        masteredCount = progress.count { (k, v) -> k.startsWith("mastered:") && v == 1 },
        onOpenRoadmap = { actions.openRoadmap(child.id) },
        onOpenReport = actions.openChallengeReport,
        onStartNow = { actions.startChallengeNow(child.id) }
    )

    // 3. Con hay sai gì?
    MistakesCard(data, child, progress, actions)

    // 4. Giải đấu – công khai với phụ huynh cách hoạt động
    LeagueCard(child)

    // Ghi chú cách tính xu
    FCard {
        SectionTitle("Con nhận xu thế nào?")
        FText("Xu chỉ thưởng khi con lần đầu vượt qua một bước – làm lại bài cũ không có thêm xu, nên con không \"cày\" được.", size = 13.sp, color = FamilyTone.Muted, lineHeight = 19.sp)
        CoinRules.EXPLAIN.forEach { (t, n) -> InfoRow(t, "+$n xu", color = FamilyTone.Coin) }
        InfoRow("Nhận huy hiệu thành tích (${com.example.toanhoc.data.family.Badges.totalTiers} cấp)", "+10 – 150 xu", color = FamilyTone.Coin)
    }
}

@Composable
private fun MistakesCard(data: FamilyData, child: ChildProfile, progress: Map<String, Int>, actions: ParentActions) {
    val since = System.currentTimeMillis() - 30L * 24 * 3600 * 1000
    val recent = data.mistakes.filter { it.childId == child.id && it.at >= since }
    val groups = recent.groupBy { it.lessonId ?: it.nodeId }.entries.sortedByDescending { it.value.size }.take(3)
    FCard {
        SectionTitle("Con hay sai dạng nào?", trailing = "30 ngày qua")
        if (groups.isEmpty()) {
            FText("Chưa ghi nhận câu sai nào trong 30 ngày qua. 🎉", size = 14.sp, color = FamilyTone.Green, weight = FontWeight.Bold)
        }
        groups.forEachIndexed { i, (key, list) ->
            if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(FamilyTone.Line))
            MistakeGroup(key, list, child, progress, actions)
        }
        if (groups.isNotEmpty()) {
            FText("Gợi ý: cùng con xem lại một câu ví dụ trước khi giao học lại – con hiểu vì sao sai sẽ nhớ lâu hơn.", size = 12.sp, color = FamilyTone.Muted, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun MistakeGroup(key: String, list: List<MistakeEntry>, child: ChildProfile, progress: Map<String, Int>, actions: ParentActions) {
    val latest = list.maxBy { it.at }
    val lesson = latest.lessonId?.let { Curricula.findLesson(it) }
    val title = lesson?.let { (c, l) -> "Lớp ${c.grade} · Bài ${l.number}: ${l.title}" } ?: "Luyện tập chung ($key)"
    var justAssigned by remember(key) { mutableStateOf(false) }
    val assigned = justAssigned || (latest.lessonId != null && (progress[LessonKeys.relearn(latest.lessonId)] ?: 0) == 1)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FText(title, size = 15.sp, weight = FontWeight.ExtraBold, modifier = Modifier.weight(1f), lineHeight = 20.sp)
            Box(Modifier.clip(CircleShape).background(FamilyTone.RedSoft).padding(horizontal = 10.dp, vertical = 4.dp)) {
                FText("${list.size} câu sai", size = 12.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Red)
            }
        }
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(FamilyTone.Paper).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FText("Ví dụ gần nhất${when (latest.source) { "exam" -> " (bài kiểm tra)"; "league" -> " (giải đấu)"; else -> "" }}:", size = 12.sp, color = FamilyTone.Muted, weight = FontWeight.Bold)
            FText(latest.statement, size = 14.sp, lineHeight = 20.sp)
            FText("Con chọn: ${latest.picked}  ·  Đáp án đúng: ${latest.expected}", size = 13.sp, weight = FontWeight.Bold, color = FamilyTone.Red)
        }
        if (latest.lessonId != null && lesson != null && lesson.first.grade == child.grade) {
            if (assigned) FText("✓ Đã giao con học lại bài này", size = 13.sp, weight = FontWeight.Bold, color = FamilyTone.Green)
            else FButton("Giao con học lại bài này", small = true, outlined = true) { actions.assignRelearn(child.id, latest.lessonId); justAssigned = true }
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(FamilyTone.Paper).padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        FText(value, size = 16.sp, weight = FontWeight.ExtraBold, maxLines = 1)
        FText(label, size = 12.sp, color = FamilyTone.Muted, maxLines = 1)
    }
}

@Composable
private fun InfoRow(label: String, value: String, color: Color = FamilyTone.Ink) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FText(label, size = 14.sp, color = FamilyTone.Muted, modifier = Modifier.weight(0.9f))
        FText(value, size = 14.sp, weight = FontWeight.Bold, color = color, modifier = Modifier.weight(1.4f), align = TextAlign.End)
    }
}

private val dayNames = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

@Composable
private fun WeekBars(week: List<Pair<LocalDate, Int>>, limit: Int) {
    val max = maxOf(week.maxOf { it.second }, limit, 15)
    Row(Modifier.fillMaxWidth().height(130.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        week.forEachIndexed { i, (day, min) ->
            val isToday = i == week.lastIndex
            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                FText(if (min > 0) "$min" else "", size = 11.sp, weight = FontWeight.Bold, color = FamilyTone.Muted)
                Box(
                    Modifier.fillMaxWidth()
                        .height((90f * min / max).coerceAtLeast(if (min > 0) 6f else 3f).dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(if (min == 0) FamilyTone.Line else if (isToday) FamilyTone.Primary else Color(0xFF9DB0EE))
                )
                Gap(4.dp)
                FText(if (isToday) "Nay" else dayNames[day.dayOfWeek.value - 1], size = 12.sp, weight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal, color = FamilyTone.Muted)
            }
        }
    }
    if (limit > 0) FText("Giới hạn mỗi ngày: $limit phút", size = 12.sp, color = FamilyTone.Muted)
}

/* ===================================== PHẦN THƯỞNG ===================================== */

private val timeFmt = DateTimeFormatter.ofPattern("HH:mm dd/MM")
private fun fmt(ms: Long) = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(timeFmt)

@Composable
private fun RewardsTab(data: FamilyData, actions: ParentActions) {
    val pending = data.requests.filter { !it.delivered }.sortedBy { it.requestedAt }
    var refundId by remember { mutableStateOf<String?>(null) }

    if (pending.isNotEmpty()) {
        FCard {
            SectionTitle("Con xin đổi quà", trailing = "${pending.size} yêu cầu")
            pending.forEach { r ->
                val c = data.child(r.childId)
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(FamilyTone.CoinSoft).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(r.emoji, fontSize = 30.sp)
                        Column(Modifier.weight(1f)) {
                            FText(r.title, size = 16.sp, weight = FontWeight.ExtraBold)
                            FText("${c?.name ?: "Con"} · ${r.cost} xu · ${fmt(r.requestedAt)}", size = 12.sp, color = FamilyTone.Muted)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FButton("Đã trao quà", small = true, color = FamilyTone.Green, modifier = Modifier.weight(1f)) { actions.deliver(r.id) }
                        FButton("Hoàn xu", small = true, outlined = true, color = FamilyTone.Muted, modifier = Modifier.weight(0.7f)) { refundId = r.id }
                    }
                }
            }
            FText("Bấm \"Đã trao quà\" sau khi đưa quà thật cho con. \"Hoàn xu\" trả lại xu nếu chưa thể trao.", size = 12.sp, color = FamilyTone.Muted, lineHeight = 17.sp)
        }
    }

    FCard {
        SectionTitle("Quà con có thể đổi", trailing = "${data.rewards.size} món")
        if (data.rewards.isEmpty()) FText("Chưa có quà nào. Thêm quà ở bên dưới để con có mục tiêu tích xu.", size = 14.sp, color = FamilyTone.Muted)
        data.rewards.sortedBy { it.cost }.forEach { r -> RewardRow(r, data) { actions.deleteReward(r.id) } }
    }

    AddRewardCard(data, actions)

    val delivered = data.requests.filter { it.delivered }.sortedByDescending { it.deliveredAt }.take(8)
    if (delivered.isNotEmpty()) {
        FCard {
            SectionTitle("Đã trao gần đây")
            delivered.forEach { r ->
                InfoRow("${r.emoji} ${r.title} · ${data.child(r.childId)?.name ?: ""}", fmt(r.deliveredAt ?: r.requestedAt), FamilyTone.Muted)
            }
        }
    }

    refundId?.let { id ->
        AlertDialog(
            onDismissRequest = { refundId = null },
            title = { FText("Hoàn xu cho con?", size = 18.sp, weight = FontWeight.ExtraBold) },
            text = { FText("Yêu cầu sẽ bị hủy và con được nhận lại số xu đã dùng. Nên giải thích cho con lý do nhé.", size = 14.sp) },
            confirmButton = { TextButton(onClick = { actions.refund(id); refundId = null }) { FText("Hoàn xu", weight = FontWeight.ExtraBold, color = FamilyTone.Primary) } },
            dismissButton = { TextButton(onClick = { refundId = null }) { FText("Thôi") } }
        )
    }
}

@Composable
private fun RewardRow(r: Reward, data: FamilyData, onDelete: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(FamilyTone.Paper), contentAlignment = Alignment.Center) { Text(r.emoji, fontSize = 24.sp) }
        Column(Modifier.weight(1f)) {
            FText(r.title, size = 15.sp, weight = FontWeight.Bold)
            FText(if (r.childId == null) "Cho tất cả các con" else "Chỉ cho ${data.child(r.childId)?.name ?: "con"}", size = 12.sp, color = FamilyTone.Muted)
        }
        CoinPill(r.cost)
        Box(Modifier.size(40.dp).clip(CircleShape).clickable(role = Role.Button) { confirm = true }, contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Xóa quà ${r.title}", tint = FamilyTone.Muted)
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { FText("Xóa quà \"${r.title}\"?", size = 18.sp, weight = FontWeight.ExtraBold) },
            text = { FText("Con sẽ không thấy món quà này nữa. Các yêu cầu đã gửi vẫn giữ nguyên.", size = 14.sp) },
            confirmButton = { TextButton(onClick = { confirm = false; onDelete() }) { FText("Xóa", weight = FontWeight.ExtraBold, color = FamilyTone.Red) } },
            dismissButton = { TextButton(onClick = { confirm = false }) { FText("Giữ lại") } }
        )
    }
}

@Composable
private fun AddRewardCard(data: FamilyData, actions: ParentActions) {
    var title by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(AvatarShop.REWARD_EMOJIS.first()) }
    var cost by remember { mutableIntStateOf(150) }
    var target by remember { mutableStateOf<String?>(null) }
    FCard {
        SectionTitle("Thêm quà mới")
        OutlinedTextField(
            value = title, onValueChange = { if (it.length <= 40) title = it },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            placeholder = { FText("Ví dụ: Đi ăn kem, 30 phút xem hoạt hình", color = FamilyTone.Muted, size = 14.sp) },
            textStyle = TextStyle(fontFamily = NunitoFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FamilyTone.Ink),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FamilyTone.Primary, unfocusedBorderColor = FamilyTone.Line)
        )
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AvatarShop.REWARD_EMOJIS.forEach { e ->
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(if (e == emoji) FamilyTone.PrimarySoft else FamilyTone.Paper)
                        .border(2.dp, if (e == emoji) FamilyTone.Primary else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable(role = Role.RadioButton) { emoji = e },
                    contentAlignment = Alignment.Center
                ) { Text(e, fontSize = 22.sp) }
            }
        }
        FText("Giá (xu)", size = 14.sp, weight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RoundStep(Icons.Rounded.Remove, "Giảm 10 xu", cost > 10) { cost = (cost - 10).coerceAtLeast(10) }
            FText("$cost xu", size = 22.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Coin, modifier = Modifier.weight(1f), align = TextAlign.Center)
            RoundStep(Icons.Rounded.Add, "Tăng 10 xu", cost < 5000) { cost = (cost + 10).coerceAtMost(5000) }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CoinRules.SUGGESTED_COSTS.forEach { (c, label) -> Chip("$label", cost == c) { cost = c } }
        }
        if (data.children.size > 1) {
            FText("Dành cho", size = 14.sp, weight = FontWeight.Bold)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("Tất cả các con", target == null) { target = null }
                data.children.forEach { c -> Chip(c.name, target == c.id) { target = c.id } }
            }
        }
        FText("Học đều mỗi ngày con nhận khoảng 25–35 xu. Đặt giá vừa sức để con thấy mục tiêu trong tầm tay.", size = 12.sp, color = FamilyTone.Muted, lineHeight = 17.sp)
        FButton("Thêm quà", modifier = Modifier.fillMaxWidth(), enabled = title.isNotBlank()) {
            actions.addReward(title.trim(), emoji, cost, target)
            title = ""
        }
    }
}

@Composable
private fun RoundStep(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(46.dp).clip(CircleShape).background(if (enabled) FamilyTone.CoinSoft else FamilyTone.Paper)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = desc, tint = if (enabled) FamilyTone.Coin else FamilyTone.Line) }
}

/* ===================================== CÀI ĐẶT ===================================== */

@Composable
private fun SettingsTab(data: FamilyData, actions: ParentActions) {
    data.children.forEach { c ->
        FCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AvatarView(c, 52.dp)
                Column(Modifier.weight(1f)) {
                    FText(c.name, size = 17.sp, weight = FontWeight.ExtraBold)
                    FText("${c.age} tuổi · Lớp ${c.grade}", size = 13.sp, color = FamilyTone.Muted)
                }
                FButton("Sửa", small = true, outlined = true) { actions.editChild(c.id) }
            }
            FText("Thời gian học tối đa mỗi ngày", size = 14.sp, weight = FontWeight.Bold)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0 to "Không giới hạn", 20 to "20 phút", 30 to "30 phút", 45 to "45 phút", 60 to "60 phút").forEach { (m, t) ->
                    Chip(t, c.dailyLimitMinutes == m) { actions.setLimit(c.id, m) }
                }
            }
            FText(
                "Hết giờ, con sẽ thấy lời nhắc nghỉ ngơi. Bố mẹ có thể nhập mã để cho thêm 15 phút.",
                size = 12.sp, color = FamilyTone.Muted, lineHeight = 17.sp
            )
        }
    }
    FButton("+ Thêm hồ sơ con", modifier = Modifier.fillMaxWidth(), outlined = true, onClick = actions.addChild)
    FCard {
        SectionTitle("Âm thanh")
        val ctx = androidx.compose.ui.platform.LocalContext.current
        androidx.compose.runtime.LaunchedEffect(Unit) { com.example.toanhoc.ui.sound.SoundEffectsManager.init(ctx) }
        val sm = com.example.toanhoc.ui.sound.SoundEffectsManager
        FText("Hiệu ứng âm thanh khi học", size = 14.sp, weight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Bật", sm.enabled) { sm.setEnabled(ctx, true); sm.play(ctx, com.example.toanhoc.ui.sound.Sfx.CORRECT) }
            Chip("Tắt", !sm.enabled) { sm.setEnabled(ctx, false) }
        }
        if (sm.enabled) {
            FText("Mức to nhỏ", size = 14.sp, weight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                com.example.toanhoc.ui.sound.SfxVolume.entries.forEach { v ->
                    Chip(v.label, sm.volume == v) { sm.setVolume(ctx, v); sm.play(ctx, com.example.toanhoc.ui.sound.Sfx.CORRECT) }
                }
            }
        }
        FText(
            "Tiếng sai là tiếng nhỏ, mềm để con không ngại thử lại. Bài kiểm tra chỉ có tiếng chạm. Khi bài giảng đang đọc, hiệu ứng tự nhỏ lại. Giọng đọc chỉnh riêng trong bài giảng.",
            size = 12.sp, color = FamilyTone.Muted, lineHeight = 17.sp
        )
    }
    FCard {
        SectionTitle("Bảo mật")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                FText("Mã PIN phụ huynh", size = 15.sp, weight = FontWeight.Bold)
                FText("Nhập sai 5 lần sẽ khóa 1 phút", size = 12.sp, color = FamilyTone.Muted)
            }
            FButton("Đổi mã", small = true, outlined = true, onClick = actions.changePin)
        }
    }
    FCard {
        SectionTitle("Giấy phép & mã nguồn")
        val ctx = androidx.compose.ui.platform.LocalContext.current
        val lic = com.example.toanhoc.ui.about.AppLicense
        FText(lic.SUMMARY, size = 13.sp, color = FamilyTone.Muted, lineHeight = 18.sp)
        FButton("Xem mã nguồn", small = true, outlined = true) {
            runCatching {
                ctx.startActivity(
                    android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(lic.SOURCE_URL))
                        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
        FText("Thành phần bên thứ ba", size = 14.sp, weight = FontWeight.Bold)
        lic.CREDITS.forEach { (name, license) -> InfoRow(name, license) }
    }
    FText(
        "Dữ liệu học của con chỉ lưu trên máy này, không gửi đi đâu. Ứng dụng chỉ lưu tên gọi và năm sinh của con.",
        size = 12.sp, color = FamilyTone.Muted, lineHeight = 17.sp, align = TextAlign.Center, modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LeagueCard(child: ChildProfile) {
    val l = child.league
    val tier = com.example.toanhoc.data.family.League.TIERS[l.tier]
    FCard {
        SectionTitle("Giải đấu", trailing = "${tier.emoji} Hạng ${tier.name}")
        if (l.played == 0) FText("Con chưa thi trận nào.", size = 14.sp, color = FamilyTone.Muted)
        else {
            InfoRow("Số trận đã thi", "${l.played} trận · về nhất ${l.wins}")
            InfoRow("Tỉ lệ đúng trong giải", "${if (l.answered > 0) l.correct * 100 / l.answered else 0}%")
            InfoRow("Trận trong tuần này", "${l.matchesThisWeek}")
        }
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(FamilyTone.Paper).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FText("Bố mẹ lưu ý", size = 13.sp, weight = FontWeight.ExtraBold)
            FText(
                "Hiện tại các bạn cùng thi trong giải là nhân vật mô phỏng, có trình độ tương đương học sinh trung bình và tự điều chỉnh theo sức của con " +
                    "(con về nhất khoảng 4–5 trận trên 10). Ứng dụng sẽ chuyển sang thi với bạn thật khi có tính năng thi đấu trực tuyến. " +
                    "Mỗi trận tốn 20 xu (mỗi ngày 1 vé miễn phí, tối đa 5 trận/ngày); câu con làm sai trong giải cũng được tính vào mục \"Con hay sai dạng nào\".",
                size = 12.sp, color = FamilyTone.Muted, lineHeight = 17.sp
            )
        }
    }
}
