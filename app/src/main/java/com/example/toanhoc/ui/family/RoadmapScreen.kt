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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.IndeterminateCheckBox
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.family.ChildProfile
import com.example.toanhoc.domain.challenge.Challenge
import com.example.toanhoc.domain.challenge.ChallengeBuilder
import com.example.toanhoc.domain.challenge.ChallengeRules
import com.example.toanhoc.domain.challenge.UnitPlan
import com.example.toanhoc.domain.curriculum.Curricula
import com.example.toanhoc.domain.curriculum.GradeCurriculum
import com.example.toanhoc.domain.curriculum.LessonKeys
import com.example.toanhoc.domain.curriculum.TextbookLesson
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/* Màu riêng cho tính năng lộ trình (cùng họ màu khu phụ huynh) */
internal object RoadTone {
    val Bolt = Color(0xFFB7791F)
    val BoltSoft = Color(0xFFFFF4DB)
    val BoltLine = Color(0xFFF6DFA6)
    val Keep = Color(0xFFB45309)
    val KeepSoft = Color(0xFFFFF1E6)
    val Header2 = Color(0xFF26314F)
}

private val dayFmt = DateTimeFormatter.ofPattern("dd/MM")
private val dateTimeFmt = DateTimeFormatter.ofPattern("HH:mm · dd/MM/yyyy")
internal fun fmtDay(ms: Long): String = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(dayFmt)
internal fun fmtDateTime(ms: Long): String = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(dateTimeFmt)

/** Trạng thái một bài trong màn chọn lộ trình. */
private enum class PickState { SELECTABLE, LEARNED, MASTERED, WAITING }

private class LessonRow(val lesson: TextbookLesson, val state: PickState, val note: String?)

/** Tính trạng thái từng bài: đã học / thành thạo / đang chờ kiểm tra lại / được chọn. */
private fun rowsOf(c: GradeCurriculum, progress: Map<String, Int>, history: List<Challenge>, now: Long): List<LessonRow> {
    fun prog(id: String) = progress[id] ?: 0
    // Bài trong mảng chưa đạt ở lần gần nhất → chờ 3 ngày (hoặc đến khi con qua Dạng 1 một bài của mảng)
    val waiting = HashMap<String, Long>()
    history.filter { it.finished }.sortedBy { it.finishedAt }.forEach { ch ->
        ch.units.forEach { u ->
            if (u.passed) u.lessonIds.forEach { waiting.remove(it) }
            else if (u.done) {
                val until = ChallengeRules.retestAt(ch.finishedAt!!)
                val learnedSome = u.lessonIds.any { prog(it) >= 2 }
                if (until > now && !learnedSome) u.lessonIds.forEach { waiting[it] = until } else u.lessonIds.forEach { waiting.remove(it) }
            }
        }
    }
    return c.lessons.map { l ->
        val mastered = prog(LessonKeys.mastered(l.id)) == 1
        val best = maxOf(prog(l.id), prog(LessonKeys.ever(l.id)))
        when {
            mastered -> LessonRow(l, PickState.MASTERED, "Thành thạo")
            best >= 4 -> LessonRow(l, PickState.LEARNED, "Đã học")
            waiting[l.id] != null -> LessonRow(l, PickState.WAITING, "Kiểm tra lại từ ${fmtDay(waiting[l.id]!!)}")
            else -> LessonRow(l, PickState.SELECTABLE, if (prog(l.id) > 0) "Đang học ${prog(l.id)}/4" else null)
        }
    }
}

/**
 * Lộ trình học do bố mẹ chọn.
 * Bước 1: chọn bài con cần học (bỏ chọn = bố mẹ cho rằng con đã biết).
 * Bước 2: xem các mảng kiến thức app sẽ kiểm tra → giao thử thách cho con.
 * Bước 3: con làm → app duyệt → bố mẹ xem kết quả.
 */
@Composable
fun RoadmapScreen(
    child: ChildProfile,
    progress: Map<String, Int>,
    history: List<Challenge>,
    pending: Challenge?,
    onCreate: suspend (kept: List<String>, plans: List<UnitPlan>, startNow: Boolean) -> Unit,
    onStartPending: () -> Unit,
    onCancelPending: () -> Unit,
    onOpenReport: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val curriculum = Curricula.forGrade(child.grade)
    var step by rememberSaveable { mutableIntStateOf(0) }
    BackHandler { if (step == 1) step = 0 else onBack() }
    val now = remember { System.currentTimeMillis() }
    val rows = remember(curriculum, progress, history) { curriculum?.let { rowsOf(it, progress, history, now) }.orEmpty() }
    // Bài bị bỏ chọn (bố mẹ muốn con bỏ qua)
    val skipped = remember(child.id) { mutableStateMapOf<String, Boolean>() }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val selectable = rows.filter { it.state == PickState.SELECTABLE }
    val skipIds = selectable.filter { skipped[it.lesson.id] == true }.map { it.lesson.id }
    val plans = remember(skipIds.toSet(), curriculum) { curriculum?.let { ChallengeBuilder.plan(it, skipIds) }.orEmpty() }
    val questionCount = plans.size * 3
    val minutes = ((questionCount * ChallengeRules.SECONDS_PER_QUESTION) + 59) / 60

    Column(modifier.fillMaxSize().background(FamilyTone.Paper)) {
        // Đầu trang
        Column(Modifier.fillMaxWidth().background(FamilyTone.Parent).padding(bottom = 14.dp)) {
            BackRow("Lộ trình học của ${child.name}", onBack = { if (step == 1) step = 0 else onBack() }, dark = true)
            Stepper(current = if (pending != null && step == 0) 1 else step, modifier = Modifier.padding(horizontal = 16.dp))
        }

        if (curriculum == null) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                FText("Lớp ${child.grade} chưa có mục lục bài học.", color = FamilyTone.Muted)
            }
            return@Column
        }

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)
                .align(Alignment.CenterHorizontally).widthIn(max = 640.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when {
                pending != null && step == 0 -> PendingCard(pending, onStartPending, onCancelPending)
                step == 0 -> PickStep(curriculum, rows, skipped)
                else -> ReviewStep(plans, curriculum, minutes)
            }
            if (step == 0) HistoryCard(history.filter { it.finished }, onOpenReport)
            Gap(8.dp)
        }

        // Thanh hành động cố định
        if (pending == null) {
            Column(
                Modifier.fillMaxWidth().background(Color.White).border(1.dp, FamilyTone.Line)
                    .navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (step == 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            FText(
                                "Giữ ${selectable.size - skipIds.size} bài · Bỏ qua ${skipIds.size} bài",
                                size = 15.sp, weight = FontWeight.ExtraBold
                            )
                            FText(
                                if (skipIds.isEmpty()) "Bỏ chọn những bài bố mẹ thấy con đã vững"
                                else "Con cần vượt ${plans.size} thử thách · $questionCount câu · khoảng $minutes phút",
                                size = 13.sp, color = FamilyTone.Muted
                            )
                        }
                    }
                    FButton("Tiếp tục", Modifier.fillMaxWidth(), enabled = skipIds.isNotEmpty()) { step = 1 }
                } else {
                    if (busy) {
                        Row(Modifier.fillMaxWidth().height(52.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(22.dp), color = FamilyTone.Primary, strokeWidth = 2.5.dp)
                            Spacer(Modifier.width(10.dp))
                            FText("Đang soạn đề thử thách…", size = 15.sp, weight = FontWeight.Bold, color = FamilyTone.Muted)
                        }
                    } else {
                        val kept = selectable.map { it.lesson.id } - skipIds.toSet()
                        FButton("Đưa máy cho con làm ngay", Modifier.fillMaxWidth()) {
                            busy = true
                            scope.launch { onCreate(kept, plans, true); busy = false; skipped.clear(); step = 0 }
                        }
                        FButton("Gửi cho con, làm sau", Modifier.fillMaxWidth(), outlined = true) {
                            busy = true
                            scope.launch { onCreate(kept, plans, false); busy = false; skipped.clear(); step = 0 }
                        }
                    }
                }
            }
        }
    }
}

/* -------------------------------- Thanh bước 1-2-3 -------------------------------- */

@Composable
private fun Stepper(current: Int, modifier: Modifier = Modifier) {
    val labels = listOf("Chọn bài", "Con làm thử thách", "Xem kết quả")
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        labels.forEachIndexed { i, label ->
            val done = i < current
            val active = i == current
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier.size(24.dp).clip(CircleShape)
                        .background(if (done || active) Color.White else Color.White.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (done) Icon(Icons.Rounded.Check, null, tint = FamilyTone.Parent, modifier = Modifier.size(16.dp))
                    else FText("${i + 1}", size = 12.sp, weight = FontWeight.ExtraBold, color = if (active) FamilyTone.Parent else Color.White.copy(alpha = 0.7f))
                }
                FText(label, size = 12.sp, weight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (active || done) Color.White else Color.White.copy(alpha = 0.6f), maxLines = 1)
            }
            if (i < labels.lastIndex) Box(Modifier.weight(1f).padding(horizontal = 6.dp).height(1.dp).background(Color.White.copy(alpha = 0.25f)))
        }
    }
}

/* -------------------------------- Bước 1: chọn bài -------------------------------- */

@Composable
private fun PickStep(c: GradeCurriculum, rows: List<LessonRow>, skipped: MutableMap<String, Boolean>) {
    FCard {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBadge(Icons.Rounded.FilterAlt, FamilyTone.Primary, FamilyTone.PrimarySoft)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FText("Chọn những bài con cần học", size = 17.sp, weight = FontWeight.ExtraBold)
                FText(
                    "Bỏ chọn bài bố mẹ thấy con đã vững. App sẽ cho con làm thử thách để xác nhận trước khi bỏ qua – " +
                        "nếu con chưa vượt qua, bài vẫn được giữ trong lộ trình.",
                    size = 13.sp, color = FamilyTone.Muted, lineHeight = 19.sp
                )
            }
        }
        val sel = rows.filter { it.state == PickState.SELECTABLE }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val sem1 = c.lessonsOf(1).map { it.id }.toSet()
            Chip("Học tất cả", selected = sel.none { skipped[it.lesson.id] == true }) { skipped.clear() }
            Chip("Con đã vững học kì 1", selected = sel.isNotEmpty() && sel.all { (it.lesson.id in sem1) == (skipped[it.lesson.id] == true) }) {
                skipped.clear(); sel.filter { it.lesson.id in sem1 }.forEach { skipped[it.lesson.id] = true }
            }
            Chip("Kiểm tra cả lớp", selected = sel.isNotEmpty() && sel.all { skipped[it.lesson.id] == true }) {
                sel.forEach { skipped[it.lesson.id] = true }
            }
        }
        Legend()
    }

    c.topics.forEach { topic ->
        val topicRows = rows.filter { r -> r.lesson in topic.lessons }
        TopicCard(c.semesterOf(topic), topic.number, topic.title, topicRows, skipped)
    }
}

@Composable
private fun Legend() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendItem(Icons.Rounded.CheckBox, FamilyTone.Primary, "Con sẽ học")
        LegendItem(Icons.Rounded.CheckBoxOutlineBlank, FamilyTone.Muted, "Bỏ qua (cần thử thách)")
    }
}

@Composable
private fun LegendItem(icon: ImageVector, tint: Color, text: String) = Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
    FText(text, size = 12.sp, color = FamilyTone.Muted, weight = FontWeight.SemiBold)
}

@Composable
private fun TopicCard(semester: Int, number: Int, title: String, rows: List<LessonRow>, skipped: MutableMap<String, Boolean>) {
    var open by rememberSaveable(number) { mutableStateOf(false) }
    val sel = rows.filter { it.state == PickState.SELECTABLE }
    val skipN = sel.count { skipped[it.lesson.id] == true }
    val keepN = sel.size - skipN
    val boxIcon = when {
        sel.isEmpty() -> Icons.Rounded.CheckBox
        skipN == 0 -> Icons.Rounded.CheckBox
        keepN == 0 -> Icons.Rounded.CheckBoxOutlineBlank
        else -> Icons.Rounded.IndeterminateCheckBox
    }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).border(1.dp, FamilyTone.Line, RoundedCornerShape(18.dp))
    ) {
        Row(
            Modifier.fillMaxWidth().clickable(role = Role.Button) { open = !open }.padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).clickable(enabled = sel.isNotEmpty(), role = Role.Checkbox) {
                    val skipAll = skipN == 0
                    sel.forEach { skipped[it.lesson.id] = skipAll }
                },
                contentAlignment = Alignment.Center
            ) { Icon(boxIcon, contentDescription = "Chọn cả chủ đề", tint = if (sel.isEmpty()) FamilyTone.Line else if (skipN == sel.size) FamilyTone.Muted else FamilyTone.Primary) }
            Column(Modifier.weight(1f).padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                FText("CHỦ ĐỀ $number · HỌC KÌ $semester", size = 11.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Muted)
                FText(title, size = 16.sp, weight = FontWeight.ExtraBold, maxLines = 2)
                val parts = buildList {
                    if (sel.isNotEmpty()) add("Học $keepN/${sel.size}")
                    val done = rows.count { it.state == PickState.LEARNED || it.state == PickState.MASTERED }
                    if (done > 0) add("$done bài đã xong")
                    if (skipN > 0) add("bỏ qua $skipN")
                }
                FText(parts.joinToString(" · "), size = 12.sp, color = if (skipN > 0) RoadTone.Keep else FamilyTone.Muted, weight = FontWeight.SemiBold)
            }
            Icon(if (open) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, contentDescription = if (open) "Thu gọn" else "Mở rộng", tint = FamilyTone.Muted)
        }
        AnimatedVisibility(open, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(FamilyTone.Line))
                rows.forEach { r -> LessonPickRow(r, skipped) }
            }
        }
    }
}

@Composable
private fun LessonPickRow(r: LessonRow, skipped: MutableMap<String, Boolean>) {
    val selectable = r.state == PickState.SELECTABLE
    val skip = skipped[r.lesson.id] == true
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp)
            .clickable(enabled = selectable, role = Role.Checkbox) { skipped[r.lesson.id] = !skip }
            .padding(start = 6.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            when (r.state) {
                PickState.SELECTABLE -> Icon(
                    if (skip) Icons.Rounded.CheckBoxOutlineBlank else Icons.Rounded.CheckBox,
                    contentDescription = if (skip) "Bỏ qua" else "Con sẽ học",
                    tint = if (skip) FamilyTone.Muted else FamilyTone.Primary
                )
                PickState.MASTERED -> Icon(Icons.Rounded.Bolt, "Thành thạo", tint = RoadTone.Bolt)
                PickState.LEARNED -> Icon(Icons.Rounded.Verified, "Đã học", tint = FamilyTone.Green)
                PickState.WAITING -> Icon(Icons.Rounded.Schedule, "Chờ kiểm tra lại", tint = RoadTone.Keep)
            }
        }
        Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
            FText(
                "Bài ${r.lesson.number}. ${r.lesson.title}", size = 14.sp,
                weight = FontWeight.SemiBold,
                color = if (selectable && !skip) FamilyTone.Ink else FamilyTone.Muted, lineHeight = 19.sp
            )
        }
        when (r.state) {
            PickState.MASTERED -> SmallPill("⚡ Thành thạo", RoadTone.Bolt, RoadTone.BoltSoft)
            PickState.LEARNED -> SmallPill("Đã học", FamilyTone.Green, FamilyTone.GreenSoft)
            PickState.WAITING -> SmallPill(r.note ?: "Chờ", RoadTone.Keep, RoadTone.KeepSoft)
            PickState.SELECTABLE -> when {
                skip -> SmallPill("Bỏ qua", FamilyTone.Muted, Color(0xFFF1F3F6))
                r.note != null -> SmallPill(r.note, FamilyTone.Primary, FamilyTone.PrimarySoft)
                else -> Unit
            }
        }
    }
}

@Composable
internal fun SmallPill(text: String, fg: Color, bg: Color, modifier: Modifier = Modifier) = Box(
    modifier.clip(CircleShape).background(bg).padding(horizontal = 9.dp, vertical = 4.dp)
) { FText(text, size = 11.sp, weight = FontWeight.ExtraBold, color = fg, maxLines = 1) }

@Composable
internal fun IconBadge(icon: ImageVector, fg: Color, bg: Color, size: Int = 40) = Box(
    Modifier.size(size.dp).clip(RoundedCornerShape(12.dp)).background(bg), contentAlignment = Alignment.Center
) { Icon(icon, null, tint = fg, modifier = Modifier.size((size * 0.55f).dp)) }

/* -------------------------------- Bước 2: xác nhận -------------------------------- */

@Composable
private fun ReviewStep(plans: List<UnitPlan>, c: GradeCurriculum, minutes: Int) {
    FCard {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBadge(Icons.Rounded.Verified, RoadTone.Bolt, RoadTone.BoltSoft)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FText("Con cần vượt ${plans.size} thử thách", size = 18.sp, weight = FontWeight.ExtraBold)
                FText("${plans.size * 3} câu · khoảng $minutes phút · có thể tạm dừng và làm tiếp sau", size = 13.sp, color = FamilyTone.Muted)
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(FamilyTone.Line))
        plans.forEachIndexed { i, p ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Box(Modifier.size(28.dp).clip(CircleShape).background(FamilyTone.PrimarySoft), contentAlignment = Alignment.Center) {
                    FText("${i + 1}", size = 13.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Primary)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    FText(p.title, size = 15.sp, weight = FontWeight.ExtraBold)
                    FText(
                        p.lessons.joinToString(" · ") { "Bài ${it.number}" } + " (${p.lessons.size} bài)",
                        size = 12.sp, color = FamilyTone.Muted, lineHeight = 17.sp
                    )
                }
            }
        }
    }

    FCard {
        SectionTitle("App duyệt thế nào?")
        RuleRow(1, "Cơ bản", "Mức Dạng 1 – con có biết cách làm không")
        RuleRow(2, "Có dữ kiện thừa", "Mức Dạng 2, đề chèn thêm thông tin không liên quan – con có lọc được không")
        RuleRow(3, "Nâng cao", "Mức khó nhất bài kiểm tra học kì – con có vận dụng được không")
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(FamilyTone.Paper).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Bullet("Đúng cả 3 câu → các bài trong mảng được đánh dấu ⚡ Thành thạo (không cộng xu).")
            Bullet("Chưa đạt → bài được giữ lại; kiểm tra lại sau ${ChallengeRules.RETEST_DAYS} ngày hoặc khi con học xong Dạng 1 của một bài trong mảng.")
            Bullet("Câu trả lời quá nhanh (đoán) không được tính. Sai câu 1 thì dừng mảng đó, không bắt con làm tiếp câu khó.")
            Bullet("Con không thấy đúng/sai trong lúc làm; kết quả chi tiết chỉ bố mẹ xem.")
        }
    }
}

@Composable
private fun RuleRow(n: Int, title: String, desc: String) = Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
    val (fg, bg) = when (n) {
        1 -> FamilyTone.Primary to FamilyTone.PrimarySoft
        2 -> RoadTone.Keep to RoadTone.KeepSoft
        else -> Color(0xFF6D28D9) to Color(0xFFEDE9FE)
    }
    Box(Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(bg), contentAlignment = Alignment.Center) {
        FText("$n", size = 13.sp, weight = FontWeight.ExtraBold, color = fg)
    }
    Column(Modifier.weight(1f)) {
        FText("Câu $n · $title", size = 14.sp, weight = FontWeight.ExtraBold)
        FText(desc, size = 13.sp, color = FamilyTone.Muted, lineHeight = 18.sp)
    }
}

@Composable
internal fun Bullet(text: String) = Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    Box(Modifier.padding(top = 7.dp).size(5.dp).clip(CircleShape).background(FamilyTone.Muted))
    FText(text, size = 13.sp, color = FamilyTone.Ink, lineHeight = 19.sp, modifier = Modifier.weight(1f))
}

/* -------------------------------- Đang chờ con làm -------------------------------- */

@Composable
private fun PendingCard(p: Challenge, onStart: () -> Unit, onCancel: () -> Unit) {
    var confirmCancel by remember { mutableStateOf(false) }
    FCard {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBadge(Icons.Rounded.Timer, RoadTone.Keep, RoadTone.KeepSoft)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FText("Thử thách đang chờ con làm", size = 17.sp, weight = FontWeight.ExtraBold)
                FText("Giao lúc ${fmtDateTime(p.createdAt)} · ${p.units.size} mảng kiến thức", size = 13.sp, color = FamilyTone.Muted)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            FText("Đã làm ${p.answeredCount}/${p.totalQuestions} câu", size = 14.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
            FText("${(p.answeredCount * 100) / p.totalQuestions.coerceAtLeast(1)}%", size = 14.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Primary)
        }
        ProgressBar(p.answeredCount.toFloat() / p.totalQuestions.coerceAtLeast(1), FamilyTone.Primary)
        p.units.forEach { u ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(if (u.done) Icons.Rounded.Check else Icons.Rounded.Schedule, null, tint = if (u.done) FamilyTone.Green else FamilyTone.Line, modifier = Modifier.size(18.dp))
                FText(u.title, size = 13.sp, color = if (u.done) FamilyTone.Ink else FamilyTone.Muted, modifier = Modifier.weight(1f), maxLines = 1)
            }
        }
        FButton("Đưa máy cho con làm ngay", Modifier.fillMaxWidth(), onClick = onStart)
        if (!confirmCancel) {
            FButton("Hủy thử thách và chọn lại", Modifier.fillMaxWidth(), outlined = true, color = FamilyTone.Red) { confirmCancel = true }
        } else {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(FamilyTone.RedSoft).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FText("Hủy thử thách này? Các câu con đã làm sẽ không được tính.", size = 14.sp, weight = FontWeight.Bold, color = FamilyTone.Red)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FButton("Giữ lại", Modifier.weight(1f), outlined = true, small = true) { confirmCancel = false }
                    FButton("Hủy thử thách", Modifier.weight(1f), color = FamilyTone.Red, small = true) { confirmCancel = false; onCancel() }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(done: List<Challenge>, onOpen: (String) -> Unit) {
    if (done.isEmpty()) return
    FCard {
        SectionTitle("Các lần thử thách", trailing = "${done.size} lần")
        done.sortedByDescending { it.finishedAt }.forEach { ch ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button) { onOpen(ch.id) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconBadge(Icons.Rounded.TrendingUp, FamilyTone.Primary, FamilyTone.PrimarySoft, 36)
                Column(Modifier.weight(1f)) {
                    FText("${ch.passedUnits.size}/${ch.units.size} mảng được duyệt", size = 14.sp, weight = FontWeight.ExtraBold)
                    FText(fmtDateTime(ch.finishedAt ?: ch.createdAt) + " · ${ch.masteredLessonIds.size} bài thành thạo", size = 12.sp, color = FamilyTone.Muted)
                }
                if (!ch.seenByParent) SmallPill("Mới", Color.White, FamilyTone.Red)
                FText("Xem", size = 13.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Primary)
            }
        }
    }
}

/** Thẻ "Lộ trình học" trong tab Báo cáo của khu phụ huynh. */
@Composable
internal fun RoadmapSummaryCard(
    child: ChildProfile, history: List<Challenge>, pending: Challenge?, masteredCount: Int,
    onOpenRoadmap: () -> Unit, onOpenReport: (String) -> Unit, onStartNow: () -> Unit
) {
    val latest = history.filter { it.finished }.maxByOrNull { it.finishedAt ?: 0 }
    FCard {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBadge(Icons.Rounded.Bolt, RoadTone.Bolt, RoadTone.BoltSoft)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                FText("Lộ trình học", size = 18.sp, weight = FontWeight.ExtraBold)
                FText(
                    when {
                        pending != null -> "Thử thách đang chờ ${child.name} làm (${pending.answeredCount}/${pending.totalQuestions} câu)."
                        latest != null -> "Lần gần nhất: ${latest.passedUnits.size}/${latest.units.size} mảng được duyệt · $masteredCount bài ⚡ thành thạo."
                        else -> "Chọn bài con cần học. Bài bố mẹ muốn bỏ qua sẽ được app kiểm tra bằng thử thách trước khi duyệt."
                    },
                    size = 13.sp, color = FamilyTone.Muted, lineHeight = 19.sp
                )
            }
            if (latest != null && !latest.seenByParent) SmallPill("Kết quả mới", Color.White, FamilyTone.Red)
        }
        when {
            pending != null -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FButton("Cho con làm ngay", Modifier.weight(1f), small = true, onClick = onStartNow)
                FButton("Xem lộ trình", Modifier.weight(1f), small = true, outlined = true, onClick = onOpenRoadmap)
            }
            latest != null && !latest.seenByParent -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FButton("Xem kết quả", Modifier.weight(1f), small = true) { onOpenReport(latest.id) }
                FButton("Chỉnh lộ trình", Modifier.weight(1f), small = true, outlined = true, onClick = onOpenRoadmap)
            }
            else -> FButton(if (latest != null) "Chỉnh lộ trình" else "Thiết lập lộ trình", Modifier.fillMaxWidth(), small = true, onClick = onOpenRoadmap)
        }
    }
}
