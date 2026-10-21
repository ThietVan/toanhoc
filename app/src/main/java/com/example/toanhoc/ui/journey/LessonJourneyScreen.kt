package com.example.toanhoc.ui.journey

import com.example.toanhoc.domain.curriculum.LessonRating
import com.example.toanhoc.domain.curriculum.ThinkingCurricula

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Exposure
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Pattern
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import android.content.pm.ApplicationInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import com.example.toanhoc.domain.curriculum.Curricula
import com.example.toanhoc.domain.curriculum.GradeCurriculum
import com.example.toanhoc.domain.curriculum.doneCount
import com.example.toanhoc.domain.curriculum.isUnlocked
import com.example.toanhoc.domain.curriculum.LessonKeys
import com.example.toanhoc.domain.curriculum.examReady
import androidx.compose.material.icons.rounded.Replay
import com.example.toanhoc.domain.curriculum.stages
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.local.entity.StudentProfileEntity
import com.example.toanhoc.ui.dashboard.DashboardUiState
import com.example.toanhoc.ui.design.AutoSizeText
import com.example.toanhoc.ui.sound.SoundEffectsManager
import com.example.toanhoc.ui.theme.NunitoFontFamily
import kotlin.math.ceil

enum class NodeState { MASTERED, ACTIVE, LOCKED }

/* ==========================================================================================
 *  TAB HỌC TẬP - "Hình khối" (Bauhaus-for-math)
 *
 *  Ý TƯỞNG
 *  Toán học bắt đầu từ hình khối: tròn, vuông, tam giác. Hình học phẳng là thứ không bao giờ
 *  lỗi thời (Bauhaus đã 100 tuổi), và là ngôn ngữ chung của cả bé lớp 1 lẫn bé lớp 5.
 *  Mỗi nhóm kiến thức có một MÀU và một BỨC TRANH HÌNH KHỐI riêng, vẽ bằng chính các hình cơ bản:
 *     Hình học   xanh dương   vuông + tam giác + tròn
 *     Số học     đỏ cam       dấu cộng + chấm + dấu bằng
 *     Suy luận   tím          hai vòng tròn giao nhau (biểu đồ Venn)
 *     Dữ liệu    xanh ngọc    ba cột tăng dần + một điểm
 *     Đại số     hồng         chữ x + ô trống
 *  => Bé lớp 1 chưa đọc thạo vẫn nhận ra "chặng xanh có hình vuông tròn" là chặng nào.
 *  => Bé lớp 5 thấy một bố cục kiểu in ấn, chữ to rõ, không có nhân vật hay hiệu ứng.
 *
 *  BỐ CỤC (đọc từ trên xuống, mỗi tầng một câu hỏi)
 *   1. Chào + chuỗi ngày            "Mình là ai, hôm nay thế nào?"
 *   2. Thẻ màu lớn "Học tiếp"       "Bây giờ làm gì?"        -> MỘT nút duy nhất, to, bấm là học
 *   3. Dải chặng (cuộn ngang)       "Có những mảng kiến thức nào?"
 *   4. Danh sách bài của chặng      "Trong mảng này có những bài nào?"
 *   5. Thẻ thi thăng cấp            "Còn bao xa nữa thì lên cấp?"
 *
 *  KỶ LUẬT THỊ GIÁC
 *   - Lưới 4dp. Lề 20dp. Khoảng cách giữa các tầng 32dp, trong tầng 16dp.
 *   - Ba mức bo góc: 28 (thẻ lớn), 20 (thẻ thường), 14 (ô nhỏ); nút và nhãn bo tròn hoàn toàn.
 *   - Chữ: một họ Nunito. Cỡ 28 / 22 / 17 / 16 / 14. Tiêu đề lớn co chữ giãn nhẹ -0.4sp.
 *   - Nền giấy ấm, thẻ trắng, mực xanh đen; màu rực chỉ xuất hiện ở hình khối và hành động.
 *   - Màu mang nghĩa cố định: xanh lá = đã xong (mọi nơi), màu nhóm = đang học, xám = chưa mở,
 *     vàng = chuỗi ngày và thành tích. Mọi trạng thái đều có biểu tượng hoặc chữ đi kèm.
 *   - Co dãn: không chiều cao cố định; tiêu đề lớn tự co chữ; nội dung tối đa 640dp.
 * ======================================================================================== */

/* ------------------------------------------ Màu ------------------------------------------ */

private object Tone {
    val Paper = Color(0xFFF7F8FA)
    val Surface = Color.White
    val Ink = Color(0xFF111827)
    val InkMuted = Color(0xFF4B5563)
    val InkFaint = Color(0xFF6B7280)
    val Line = Color(0xFFE4E7EC)
    val LockedFill = Color(0xFFEEF0F3)
    val Done = Color(0xFF1E7F4F)
    val Sun = Color(0xFFFFB81F)
}

/** Mỗi nhóm kiến thức: màu đậm (nền + chữ trắng đạt tương phản AA), màu vừa (hình khối), màu nhạt (nền phụ). */
private enum class Domain(
    val key: String,
    val deep: Color,
    val mid: Color,
    val tint: Color
) {
    GEOMETRY("GEOMETRY_MEASUREMENT", Color(0xFF2F4FE0), Color(0xFF7C97FF), Color(0xFFE9EEFF)),
    ARITHMETIC("ARITHMETIC", Color(0xFFD13F1F), Color(0xFFFF8A65), Color(0xFFFFECE5)),
    REASONING("HEURISTICS_COMBINATORICS", Color(0xFF5B3FD6), Color(0xFF9F8AFF), Color(0xFFEEEAFF)),
    DATA("DATA_STATS_PROBABILITY", Color(0xFF0B7A6F), Color(0xFF3CC4B1), Color(0xFFDDF5F0)),
    ALGEBRA("ALGEBRAIC_THINKING", Color(0xFFC0245F), Color(0xFFFF7FAB), Color(0xFFFFE8F0)),
    OTHER("", Color(0xFF3E4A6B), Color(0xFF8D99BA), Color(0xFFE8EBF3));

    /** Biến thể đậm hơn 12% dùng cho CHỮ trên nền sáng: đạt tối thiểu 5.1:1 trên nền nhạt, giấy và trắng. */
    val text: Color get() = lerp(deep, Color.Black, 0.12f)

    companion object {
        fun from(category: String): Domain =
            values().firstOrNull { it != OTHER && it.key == category } ?: OTHER
    }
}

/* ---------------------------------------- Kích thước ---------------------------------------- */

private val R_LARGE = 28.dp
private val R_CARD = 20.dp
private val R_SMALL = 14.dp

private val MaxContentWidth = 640.dp
private val SectionGap = 32.dp

/* ---------------------------------------- Dữ liệu hiển thị ---------------------------------------- */

private enum class ChapterState { DONE, ACTIVE, LOCKED }

private class Chapter(
    val index: Int,
    val domain: Domain,
    val title: String,
    val start: Int,
    val end: Int,
    val done: Int,
    val state: ChapterState,
    /** Nhãn nhỏ trên thẻ ("Chặng 2" / "Bài 10") và dòng phụ trên thẻ lớn. */
    val label: String = "Chặng ${index + 1}",
    val kicker: String? = null,
    val statusOverride: String? = null,
    /** Bài được duyệt "thành thạo" qua thử thách vượt bài của bố mẹ. */
    val mastered: Boolean = false,
    /** Xếp loại khi đã học xong (Hiểu bài / Thành thạo) – hiện thay cho "4/4". */
    val rating: LessonRating? = null
) {
    val total: Int get() = end - start
}

private class LessonUi(
    val nodeId: String,
    val title: String,
    val state: NodeState
)

/* ====================================== MÀN HÌNH ====================================== */

@Composable
fun LessonJourneyScreen(
    uiState: DashboardUiState,
    studentProfile: StudentProfileEntity,
    onStartSession: (String?) -> Unit,
    onStartLevelUpExam: () -> Unit,
    modifier: Modifier = Modifier,
    lessonProgress: Map<String, Int> = emptyMap(),
    onOpenLesson: (String) -> Unit = {},
    onOpenExam: (Int) -> Unit = {},
    coins: Int? = null,
    topBanner: (@Composable () -> Unit)? = null
) {
    // Lớp đã có mục lục theo sách → trang chủ theo Bài (giữ nguyên bố cục)
    val curriculum = Curricula.forGrade(uiState.selectedGrade)
    if (curriculum != null) {
        CurriculumJourney(curriculum, studentProfile, lessonProgress, onOpenLesson, onOpenExam, modifier, coins, topBanner)
        return
    }

    val context = LocalContext.current
    val nodes = uiState.gradeNodes
    val masteredCount = nodes.count { uiState.masteryMap[it.nodeId]?.isMastered == true }
    val totalNodes = nodes.size.coerceAtLeast(1)
    val isGatekeeperUnlocked = masteredCount.toFloat() / totalNodes >= 0.8f
    val needToUnlock = ceil(totalNodes * 0.8f).toInt()

    // Trạng thái từng bài (giữ nguyên logic cũ)
    val lessons = nodes.mapIndexed { index, node ->
        val isMastered = uiState.masteryMap[node.nodeId]?.isMastered == true
        val isActive = node.nodeId == uiState.phase2TargetNode?.nodeId ||
                (index == 0 && masteredCount == 0)
        LessonUi(
            nodeId = node.nodeId,
            title = node.titleVi,
            state = when {
                isMastered -> NodeState.MASTERED
                isActive -> NodeState.ACTIVE
                else -> NodeState.LOCKED
            }
        )
    }
    val activeIndex = lessons.indexOfFirst { it.state == NodeState.ACTIVE }

    // Gom các bài liên tiếp cùng nhóm kiến thức thành một chặng - Mở khóa chặng theo tuyến tính
    val chapterRanges = categoryRanges(nodes.map { it.domainCategory })
    val chapters = mutableListOf<Chapter>()
    for (i in chapterRanges.indices) {
        val (category, start, end) = chapterRanges[i]
        val totalInChapter = end - start
        val done = (start until end).count { lessons[it].state == NodeState.MASTERED }
        val domain = Domain.from(category)

        val isPrevChapterDone = if (i == 0) true else chapters[i - 1].state == ChapterState.DONE
        val state = when {
            done == totalInChapter -> ChapterState.DONE
            isPrevChapterDone -> ChapterState.ACTIVE
            else -> ChapterState.LOCKED
        }
        chapters.add(
            Chapter(
                index = i,
                domain = domain,
                title = if (domain == Domain.OTHER) categoryLabel(category) else categoryLabel(domain.key),
                start = start,
                end = end,
                done = done,
                state = state
            )
        )
    }
    val activeChapter = chapters.firstOrNull { activeIndex in it.start until it.end }

    // Chặng đang xem danh sách bài: mặc định là chặng đang học, giữ nguyên khi xoay màn hình
    var selectedIndex by rememberSaveable(activeChapter?.index) {
        mutableStateOf(activeChapter?.index ?: 0)
    }
    val selectedChapter = chapters.getOrNull(selectedIndex) ?: chapters.firstOrNull()

    fun startSession(nodeId: String) {
        SoundEffectsManager.playSelect(context)
        onStartSession(nodeId)
    }

    fun startExam() {
        if (!isGatekeeperUnlocked) return
        SoundEffectsManager.playSelect(context)
        onStartLevelUpExam()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Tone.Paper)
    ) {
        val narrow = maxWidth < 360.dp
        val side = if (narrow) 16.dp else 20.dp
        val tileWidth = if (narrow) 140.dp else 156.dp
        val showArt = maxWidth >= 340.dp
        val sidePadding = Modifier.padding(horizontal = side)

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(SectionGap)
        ) {
            // 1. Chào + chuỗi ngày
            Header(
                name = greetingName(studentProfile.fullName),
                streakDays = studentProfile.streakDays,
                modifier = sidePadding
            )

            // 2. Thẻ "Học tiếp": một hành động chính
            val lessonTitle = lessons.getOrNull(activeIndex)?.title
            val allDone = activeIndex < 0
            HeroCard(
                domain = activeChapter?.domain ?: Domain.OTHER,
                pillText = if (allDone) "Đã học xong" else "Bài tiếp theo",
                pillIcon = if (allDone) Icons.Rounded.EmojiEvents else Icons.Rounded.PlayArrow,
                kicker = activeChapter?.let { "Chặng ${it.index + 1} · ${it.title}" },
                title = lessonTitle ?: if (isGatekeeperUnlocked) "Sẵn sàng thi thăng cấp" else "Ôn lại để thăng cấp",
                progressText = activeChapter?.let { "Đã học ${it.done}/${it.total} bài" },
                learned = activeChapter?.done ?: 0,
                total = activeChapter?.total ?: 0,
                buttonText = when {
                    lessonTitle != null -> "Học tiếp"
                    isGatekeeperUnlocked -> "Làm bài thi"
                    else -> null
                },
                showArt = showArt,
                modifier = sidePadding,
                onContinue = {
                    val active = lessons.getOrNull(activeIndex)
                    if (active != null) startSession(active.nodeId) else startExam()
                }
            )

            if (chapters.isNotEmpty() && selectedChapter != null) {
                // 3. Dải chặng
                ChapterRail(
                    chapters = chapters,
                    selectedIndex = selectedChapter.index,
                    tileWidth = tileWidth,
                    side = side,
                    onSelect = { selectedIndex = it }
                )

                // 4. Danh sách bài của chặng đang chọn
                LessonsSection(
                    chapter = selectedChapter,
                    lessons = lessons.subList(selectedChapter.start, selectedChapter.end),
                    modifier = sidePadding,
                    onStartLesson = ::startSession
                )
            }

            // 5. Thi thăng cấp
            ExamCard(
                unlocked = isGatekeeperUnlocked,
                masteredCount = masteredCount,
                needToUnlock = needToUnlock,
                modifier = sidePadding,
                onClick = ::startExam
            )
        }
    }
}

/* ================================ CHỮ DÙNG CHUNG ================================ */

@Composable
private fun UiText(
    text: String,
    size: TextUnit,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.Medium,
    color: Color = Tone.Ink,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontWeight = weight,
        fontFamily = NunitoFontFamily,
        letterSpacing = letterSpacing,
        maxLines = maxLines,
        minLines = minLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign
    )
}

private fun nunito(weight: FontWeight, color: Color, letterSpacing: TextUnit = TextUnit.Unspecified) =
    TextStyle(fontFamily = NunitoFontFamily, fontWeight = weight, color = color, letterSpacing = letterSpacing)

/* ================================= 1. ĐẦU TRANG ================================= */

@Composable
private fun Header(name: String, streakDays: Int, modifier: Modifier, coins: Int? = null) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AutoSizeText(
                text = "Chào ${name.ifBlank { "bạn" }}",
                modifier = Modifier.fillMaxWidth(),
                maxFontSize = 28.sp,
                minFontSize = 20.sp,
                style = nunito(FontWeight.ExtraBold, Tone.Ink, (-0.4).sp),
                maxLines = 1
            )
            AutoSizeText(
                text = "Hôm nay mình học tiếp nhé",
                modifier = Modifier.fillMaxWidth(),
                maxFontSize = 16.sp,
                minFontSize = 12.sp,
                style = nunito(FontWeight.Medium, Tone.InkMuted),
                maxLines = 1
            )
        }
        if (coins != null) CoinChip(coins)
        StreakPill(days = streakDays)
    }
}

@Composable
private fun CoinChip(coins: Int) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFFFBF3EA))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("🪙", fontSize = 18.sp)
        UiText(text = "$coins", size = 18.sp, weight = FontWeight.ExtraBold, color = Color(0xFFC2620A), maxLines = 1)
    }
}

@Composable
private fun StreakPill(days: Int) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Tone.Ink)
            .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = "Chuỗi ngày học",
            tint = Tone.Sun,
            modifier = Modifier.size(24.dp)
        )
        UiText(text = "$days", size = 18.sp, weight = FontWeight.ExtraBold, color = Color.White, maxLines = 1)
        UiText(text = "ngày", size = 14.sp, color = Color.White.copy(alpha = 0.7f), maxLines = 1)
    }
}

/* ================================= 2. THẺ HỌC TIẾP ================================= */

@Composable
private fun HeroCard(
    domain: Domain,
    pillText: String,
    pillIcon: ImageVector,
    kicker: String?,
    title: String,
    progressText: String?,
    learned: Int,
    total: Int,
    buttonText: String?,
    showArt: Boolean,
    modifier: Modifier,
    locked: Boolean = false,
    onContinue: () -> Unit
) {
    val artColors = ArtColors(
        a = domain.mid,
        b = Color.White,
        c = lerp(domain.deep, Color.Black, 0.35f)
    )
    val cardColor = if (locked) lerp(domain.deep, Tone.Ink, 0.55f) else domain.deep

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(R_LARGE))
            .background(cardColor)
            .drawBehind {
                if (showArt) {
                    // Bức tranh hình khối tràn ra mép phải-trên của thẻ
                    val side = minOf(size.width * 0.38f, 160.dp.toPx())
                    drawArt(
                        kind = domain,
                        origin = Offset(size.width - side * 0.78f, -side * 0.12f),
                        side = side,
                        colors = artColors
                    )
                }
            }
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(if (showArt) 0.62f else 1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = pillIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                UiText(
                    text = pillText,
                    size = 14.sp,
                    weight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }

            if (kicker != null) {
                UiText(
                    text = kicker,
                    size = 14.sp,
                    color = Color.White,
                    maxLines = 2
                )
            }

            AutoSizeText(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                maxFontSize = 26.sp,
                minFontSize = 19.sp,
                style = nunito(FontWeight.ExtraBold, Color.White, (-0.4).sp),
                maxLines = 3,
                lineHeightMultiplier = 1.2f
            )
        }

        if (total > 0 && progressText != null) {
            HeroProgress(label = progressText, learned = learned, total = total)
        }

        if (buttonText != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .clip(CircleShape)
                    .background(if (locked) Color.White.copy(alpha = 0.16f) else Color.White)
                    .clickable(role = Role.Button, onClick = onContinue)
                    .padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UiText(
                    text = buttonText,
                    size = 18.sp,
                    modifier = Modifier.weight(1f),
                    weight = FontWeight.ExtraBold,
                    color = if (locked) Color.White else Tone.Ink,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (locked) Color.White.copy(alpha = 0.2f) else domain.deep),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (locked) Icons.Rounded.Lock else Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/** Mỗi đoạn là một bài trong chặng. Chặng dài quá 12 bài thì dùng một thanh liền để không bị li ti. */
@Composable
private fun HeroProgress(label: String, learned: Int, total: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        UiText(
            text = label,
            size = 14.sp,
            weight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1
        )
        if (total <= 12) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(total) { i ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(if (i < learned) Color.White else Color.White.copy(alpha = 0.25f))
                    )
                }
            }
        } else {
            MiniBar(
                fraction = learned.toFloat() / total,
                fill = Color.White,
                track = Color.White.copy(alpha = 0.25f),
                height = 6.dp
            )
        }
    }
}

@Composable
private fun MiniBar(fraction: Float, fill: Color, track: Color, height: Dp = 4.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(CircleShape)
                .background(fill)
        )
    }
}

/* ================================= 3. DẢI CHẶNG ================================= */

@Composable
private fun ChapterRail(
    chapters: List<Chapter>,
    selectedIndex: Int,
    tileWidth: Dp,
    side: Dp,
    heading: String = "Các chặng học",
    countText: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onSelect: (Int) -> Unit
) {
    val doneCount = chapters.count { it.state == ChapterState.DONE }
    // Mở trang là thấy ngay thẻ đang chọn (danh sách bài có thể dài 40+ thẻ)
    val railScroll = rememberScrollState()
    val stepPx = with(LocalDensity.current) { (tileWidth + 12.dp).toPx() }
    LaunchedEffect(chapters.size) {
        val local = chapters.indexOfFirst { it.index == selectedIndex }
        if (local >= 0) railScroll.scrollTo((stepPx * (local - 0.3f)).toInt().coerceAtLeast(0))
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = side),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AutoSizeText(
                text = heading,
                modifier = Modifier.weight(1f),
                maxFontSize = 22.sp,
                minFontSize = 18.sp,
                style = nunito(FontWeight.ExtraBold, Tone.Ink, (-0.3).sp),
                maxLines = 1
            )
            UiText(
                text = countText ?: "$doneCount/${chapters.size} đã xong",
                size = 14.sp,
                weight = FontWeight.SemiBold,
                color = Tone.InkMuted,
                maxLines = 1
            )
        }

        // Cuộn ngang tràn hết bề rộng màn hình; tile dài hơn màn hình một chút để gợi ý còn nữa
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .horizontalScroll(railScroll)
                .padding(horizontal = side),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            chapters.forEach { chapter ->
                ChapterTile(
                    chapter = chapter,
                    selected = chapter.index == selectedIndex,
                    width = tileWidth,
                    onClick = { onSelect(chapter.index) }
                )
            }
            trailing?.invoke()
        }
    }
}

@Composable
private fun ChapterTile(chapter: Chapter, selected: Boolean, width: Dp, onClick: () -> Unit) {
    val locked = chapter.state == ChapterState.LOCKED
    val filled = selected && !locked // nền đặc màu nhóm khi được chọn

    val background by animateColorAsState(
        targetValue = if (filled) chapter.domain.deep else Tone.Surface,
        animationSpec = tween(durationMillis = 160),
        label = "tileBackground"
    )
    val primaryText = if (filled) Color.White else if (locked) Tone.InkMuted else Tone.Ink
    val secondaryText = if (filled) Color.White else Tone.InkMuted
    val shape = RoundedCornerShape(R_CARD)

    val statusLabel = chapter.statusOverride ?: if (chapter.mastered) "Thành thạo" else when (chapter.state) {
        ChapterState.DONE -> "Đã xong"
        ChapterState.ACTIVE -> "Đang học"
        ChapterState.LOCKED -> "Chưa mở"
    }

    Column(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .heightIn(min = 176.dp)
            .clip(shape)
            .background(background)
            .border(
                width = if (selected && !filled) 2.dp else 1.dp,
                color = when {
                    filled -> Color.Transparent
                    selected -> Tone.Ink
                    else -> Tone.Line
                },
                shape = shape
            )
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            ArtTile(
                domain = chapter.domain,
                locked = locked,
                onDeep = filled,
                size = 56.dp
            )
            when (chapter.state) {
                ChapterState.DONE -> if (chapter.mastered) StatusDot(
                    background = if (filled) Color.White else Color(0xFFF6C453),
                    icon = Icons.Rounded.Bolt,
                    iconTint = if (filled) chapter.domain.deep else Color(0xFF1B2440),
                    description = "Thành thạo qua thử thách"
                ) else StatusDot(
                    background = if (filled) Color.White else Tone.Done,
                    icon = Icons.Rounded.Check,
                    iconTint = if (filled) chapter.domain.deep else Color.White,
                    description = "Đã hoàn thành"
                )
                ChapterState.LOCKED -> StatusDot(
                    background = Tone.LockedFill,
                    icon = Icons.Rounded.Lock,
                    iconTint = Tone.InkFaint,
                    description = "Chưa mở khóa"
                )
                ChapterState.ACTIVE -> Unit
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(12.dp))

        UiText(text = chapter.label, size = 14.sp, color = secondaryText, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        UiText(
            text = chapter.title,
            size = 17.sp,
            weight = FontWeight.ExtraBold,
            color = primaryText,
            maxLines = 2,
            minLines = 2
        )
        Spacer(Modifier.height(8.dp))
        UiText(
            text = chapter.rating?.takeIf { chapter.statusOverride == null }?.let { r ->
                if (chapter.mastered) "Thành thạo (thử thách)" else r.label
            } ?: "$statusLabel · ${chapter.done}/${chapter.total}",
            size = 14.sp,
            weight = FontWeight.SemiBold,
            color = secondaryText,
            maxLines = 1
        )
        Spacer(Modifier.height(8.dp))
        MiniBar(
            fraction = if (chapter.total == 0) 0f else chapter.done.toFloat() / chapter.total,
            fill = if (filled) Color.White else if (locked) Tone.InkFaint else chapter.domain.deep,
            track = if (filled) Color.White.copy(alpha = 0.25f) else Tone.LockedFill
        )
    }
}

@Composable
private fun StatusDot(background: Color, icon: ImageVector, iconTint: Color, description: String) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
    }
}

/** Ô vuông bo góc chứa bức tranh hình khối của một nhóm kiến thức. */
@Composable
private fun ArtTile(domain: Domain, locked: Boolean, onDeep: Boolean, size: Dp) {
    val colors = when {
        locked -> ArtColors(Color(0xFFC5CAD3), Color(0xFFD9DDE4), Color(0xFFB3B9C4))
        onDeep -> ArtColors(domain.mid, Color.White, lerp(domain.deep, Color.Black, 0.35f))
        else -> ArtColors(domain.deep, domain.mid, lerp(domain.deep, Color.Black, 0.35f))
    }
    val tileBackground = when {
        locked -> Tone.LockedFill
        onDeep -> Color.White.copy(alpha = 0.14f)
        else -> domain.tint
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(R_SMALL))
            .background(tileBackground)
            .padding(size * 0.12f)
            .drawBehind {
                drawArt(domain, Offset.Zero, minOf(this.size.width, this.size.height), colors)
            }
    )
}

/* ============================= 4. DANH SÁCH BÀI CỦA CHẶNG ============================= */

@Composable
private fun LessonsSection(
    chapter: Chapter,
    lessons: List<LessonUi>,
    modifier: Modifier,
    onStartLesson: (String) -> Unit
) {
    val domain = chapter.domain
    val locked = chapter.state == ChapterState.LOCKED

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                UiText(
                    text = "Chặng ${chapter.index + 1}",
                    size = 14.sp,
                    weight = FontWeight.Bold,
                    color = if (locked) Tone.InkMuted else domain.text,
                    maxLines = 1
                )
                AutoSizeText(
                    text = chapter.title,
                    modifier = Modifier.fillMaxWidth(),
                    maxFontSize = 22.sp,
                    minFontSize = 17.sp,
                    style = nunito(FontWeight.ExtraBold, Tone.Ink, (-0.3).sp),
                    maxLines = 1
                )
            }
            UiText(
                text = "${chapter.done}/${chapter.total} bài",
                size = 14.sp,
                weight = FontWeight.SemiBold,
                color = Tone.InkMuted,
                maxLines = 1
            )
        }

        if (locked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(R_SMALL))
                    .background(Tone.LockedFill)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = Tone.InkMuted,
                    modifier = Modifier.size(20.dp)
                )
                UiText(
                    text = "Hoàn thành các bài trước để mở chặng này",
                    size = 14.sp,
                    weight = FontWeight.SemiBold,
                    color = Tone.InkMuted
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(R_CARD))
                .background(Tone.Surface)
                .border(1.dp, Tone.Line, RoundedCornerShape(R_CARD))
        ) {
            lessons.forEachIndexed { i, lesson ->
                LessonRow(
                    number = i + 1,
                    lesson = lesson,
                    domain = domain,
                    onClick = { onStartLesson(lesson.nodeId) }
                )
                if (i < lessons.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 72.dp)
                            .height(1.dp)
                            .background(Tone.Line)
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonRow(number: Int, lesson: LessonUi, domain: Domain, onClick: () -> Unit) {
    val active = lesson.state == NodeState.ACTIVE
    val mastered = lesson.state == NodeState.MASTERED
    val locked = lesson.state == NodeState.LOCKED

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (active) domain.tint else Color.Transparent)
            .clickable(enabled = !locked, role = Role.Button, onClick = onClick)
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Nút trạng thái 40dp: dấu tích xanh lá / nút chạy màu nhóm / ổ khóa xám
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when (lesson.state) {
                        NodeState.MASTERED -> Tone.Done
                        NodeState.ACTIVE -> domain.deep
                        NodeState.LOCKED -> Tone.LockedFill
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when (lesson.state) {
                NodeState.MASTERED -> Icon(Icons.Rounded.Check, "Đã thành thạo", tint = Color.White, modifier = Modifier.size(22.dp))
                NodeState.ACTIVE -> Icon(Icons.Rounded.PlayArrow, "Đang học", tint = Color.White, modifier = Modifier.size(24.dp))
                NodeState.LOCKED -> Icon(Icons.Rounded.Lock, "Chưa mở khóa", tint = Tone.InkFaint, modifier = Modifier.size(18.dp))
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            UiText(
                text = "Bài $number",
                size = 14.sp,
                color = if (active) domain.text else Tone.InkMuted,
                weight = if (active) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
            UiText(
                text = lesson.title,
                size = 17.sp,
                weight = FontWeight.Bold,
                color = if (locked) Tone.InkMuted else Tone.Ink,
                maxLines = 2
            )
        }

        when {
            active -> UiText(
                text = "Học ngay",
                size = 14.sp,
                weight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(domain.deep)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
            mastered -> UiText(
                text = "Ôn lại",
                size = 14.sp,
                weight = FontWeight.SemiBold,
                color = Tone.InkMuted,
                maxLines = 1
            )
        }
    }
}

/* ================================= 5. THI THĂNG CẤP ================================= */

@Composable
private fun ExamCard(
    unlocked: Boolean,
    masteredCount: Int,
    needToUnlock: Int,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val remaining = (needToUnlock - masteredCount).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(R_LARGE))
            .background(Tone.Ink)
            .drawBehind {
                // Hai hình khối vàng mờ: vòng tròn lớn và hình vuông nghiêng
                drawCircle(
                    color = Tone.Sun.copy(alpha = 0.16f),
                    radius = size.height * 0.55f,
                    center = Offset(size.width * 0.96f, size.height * 0.10f)
                )
                val c = Offset(size.width * 0.82f, size.height * 0.78f)
                val s = size.height * 0.26f
                rotate(degrees = 18f, pivot = c) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.07f),
                        topLeft = Offset(c.x - s / 2, c.y - s / 2),
                        size = Size(s, s),
                        cornerRadius = CornerRadius(s * 0.18f)
                    )
                }
            }
            .then(
                if (unlocked) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Tone.Sun),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = Tone.Ink,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                UiText(
                    text = "Thi thăng cấp",
                    size = 22.sp,
                    weight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.3).sp,
                    maxLines = 1
                )
                UiText(
                    text = if (unlocked) "Con đã đủ điều kiện" else "Thành thạo $needToUnlock bài để mở bài thi",
                    size = 14.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }

        if (unlocked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(CircleShape)
                    .background(Tone.Sun)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UiText(
                    text = "Làm bài thi",
                    size = 18.sp,
                    weight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = Tone.Ink,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniBar(
                    fraction = if (needToUnlock == 0) 0f else masteredCount.toFloat() / needToUnlock,
                    fill = Tone.Sun,
                    track = Color.White.copy(alpha = 0.18f),
                    height = 8.dp
                )
                UiText(
                    text = "Còn $remaining bài nữa là mở bài thi",
                    size = 14.sp,
                    weight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
        }
    }
}

/* ============================ BỨC TRANH HÌNH KHỐI ============================ */

private class ArtColors(val a: Color, val b: Color, val c: Color)

/**
 * Vẽ bức tranh hình khối trong một ô vuông cạnh [side], bắt đầu từ [origin].
 * Toạ độ chuẩn hóa 0..1 nên dùng được ở mọi kích thước, từ ô 56dp đến thẻ lớn.
 */
private fun DrawScope.drawArt(kind: Domain, origin: Offset, side: Float, colors: ArtColors) {
    fun x(u: Float) = origin.x + u * side
    fun y(v: Float) = origin.y + v * side
    fun len(u: Float) = u * side

    when (kind) {
        Domain.GEOMETRY -> {
            // Hình vuông, tam giác, hình tròn
            drawRoundRect(
                color = colors.a,
                topLeft = Offset(x(0.06f), y(0.40f)),
                size = Size(len(0.50f), len(0.50f)),
                cornerRadius = CornerRadius(len(0.05f))
            )
            val triangle = Path().apply {
                moveTo(x(0.68f), y(0.08f))
                lineTo(x(0.94f), y(0.50f))
                lineTo(x(0.42f), y(0.50f))
                close()
            }
            drawPath(triangle, colors.b)
            drawCircle(colors.c, len(0.17f), Offset(x(0.76f), y(0.75f)))
        }

        Domain.ARITHMETIC -> {
            // Dấu cộng, chấm tròn, dấu bằng
            val arm = len(0.44f)
            val thick = len(0.12f)
            val corner = CornerRadius(len(0.03f))
            drawRoundRect(colors.a, Offset(x(0.30f) - arm / 2, y(0.30f) - thick / 2), Size(arm, thick), corner)
            drawRoundRect(colors.a, Offset(x(0.30f) - thick / 2, y(0.30f) - arm / 2), Size(thick, arm), corner)
            drawCircle(colors.b, len(0.15f), Offset(x(0.74f), y(0.28f)))
            drawRoundRect(colors.c, Offset(x(0.14f), y(0.66f)), Size(len(0.72f), len(0.10f)), CornerRadius(len(0.04f)))
            drawRoundRect(colors.c, Offset(x(0.14f), y(0.80f)), Size(len(0.72f), len(0.10f)), CornerRadius(len(0.04f)))
        }

        Domain.REASONING -> {
            // Hai vòng tròn giao nhau (Venn), phần giao tô màu thứ ba
            val r = len(0.30f)
            val left = Offset(x(0.38f), y(0.50f))
            val right = Offset(x(0.62f), y(0.50f))
            drawCircle(colors.a, r, left)
            drawCircle(colors.b, r, right)
            val leftCircle = Path().apply { addOval(Rect(center = left, radius = r)) }
            clipPath(leftCircle) {
                drawCircle(colors.c, r, right)
            }
        }

        Domain.DATA -> {
            // Ba cột tăng dần và một điểm dữ liệu
            val barWidth = len(0.22f)
            val corner = CornerRadius(len(0.06f))
            val base = y(0.90f)
            drawRoundRect(colors.a, Offset(x(0.10f), base - len(0.34f)), Size(barWidth, len(0.34f)), corner)
            drawRoundRect(colors.b, Offset(x(0.38f), base - len(0.54f)), Size(barWidth, len(0.54f)), corner)
            drawRoundRect(colors.c, Offset(x(0.66f), base - len(0.74f)), Size(barWidth, len(0.74f)), corner)
            drawCircle(colors.a, len(0.06f), Offset(x(0.77f), y(0.08f)))
        }

        Domain.ALGEBRA -> {
            // Chữ x làm từ hai thanh chéo, và một ô trống cần điền
            val center = Offset(x(0.40f), y(0.50f))
            val length = len(0.70f)
            val thick = len(0.13f)
            val corner = CornerRadius(thick / 2)
            rotate(45f, center) {
                drawRoundRect(colors.a, Offset(center.x - length / 2, center.y - thick / 2), Size(length, thick), corner)
            }
            rotate(-45f, center) {
                drawRoundRect(colors.b, Offset(center.x - length / 2, center.y - thick / 2), Size(length, thick), corner)
            }
            drawRoundRect(colors.c, Offset(x(0.68f), y(0.60f)), Size(len(0.22f), len(0.22f)), CornerRadius(len(0.04f)))
        }

        Domain.OTHER -> {
            drawCircle(colors.a, len(0.26f), Offset(x(0.36f), y(0.50f)))
            drawRoundRect(
                color = colors.b,
                topLeft = Offset(x(0.46f), y(0.34f)),
                size = Size(len(0.40f), len(0.40f)),
                cornerRadius = CornerRadius(len(0.05f))
            )
        }
    }
}

/* ===================================== HELPERS ===================================== */

/** Gom các bài liên tiếp có cùng nhóm kiến thức thành một chặng, giữ nguyên thứ tự giáo trình. */
private fun categoryRanges(categories: List<String>): List<Triple<String, Int, Int>> {
    val out = mutableListOf<Triple<String, Int, Int>>()
    if (categories.isEmpty()) return out
    var start = 0
    for (i in 1..categories.size) {
        if (i == categories.size || categories[i] != categories[start]) {
            out += Triple(categories[start], start, i)
            start = i
        }
    }
    return out
}

private fun categoryLabel(category: String): String = when (category) {
    "GEOMETRY_MEASUREMENT" -> "Hình học và đo lường"
    "ARITHMETIC" -> "Số học"
    "HEURISTICS_COMBINATORICS" -> "Tư duy và suy luận"
    "DATA_STATS_PROBABILITY" -> "Dữ liệu và xác suất"
    "ALGEBRAIC_THINKING" -> "Tư duy đại số"
    else -> category.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}

/** Giữ lại để các màn khác còn dùng: biểu tượng Material theo chủ đề từng bài. */
fun getTopicIcon(nodeId: String, category: String): ImageVector {
    return when {
        nodeId.contains("SPATIAL_SHAPES") -> Icons.Rounded.Category
        nodeId.contains("GEOMETRY") -> Icons.Rounded.Category

        nodeId.contains("NUM_0_10") || nodeId.contains("COMPARE") -> Icons.Rounded.Numbers
        nodeId.contains("PLACE_VALUE") || nodeId.contains("MILLIONS") -> Icons.Rounded.Layers
        nodeId.contains("SPLIT_COMBINE") -> Icons.Rounded.AccountTree

        nodeId.contains("ADD_SUB_10_MEANING") || nodeId.contains("ADD_SUB") -> Icons.Rounded.Exposure
        nodeId.contains("MUL_DIV") -> Icons.Rounded.Calculate
        nodeId.contains("FRACTIONS") || nodeId.contains("DECIMAL") -> Icons.Rounded.PieChart

        nodeId.contains("MEASURE_LENGTH") || nodeId.contains("MEASURE") -> Icons.Rounded.Straighten
        nodeId.contains("TIME_CLOCK") || nodeId.contains("TIME") -> Icons.Rounded.Schedule
        nodeId.contains("MONEY") -> Icons.Rounded.Payments

        nodeId.contains("WORD_PROBLEMS") || nodeId.contains("WORD") -> Icons.Rounded.AutoStories
        nodeId.contains("PATTERNS") -> Icons.Rounded.Pattern
        nodeId.contains("STATS") || nodeId.contains("CHART") -> Icons.Rounded.BarChart
        nodeId.contains("PROBABILITY") -> Icons.Rounded.Casino

        category == "GEOMETRY_MEASUREMENT" -> Icons.Rounded.Straighten
        category == "ARITHMETIC" -> Icons.Rounded.Calculate
        category == "HEURISTICS_COMBINATORICS" -> Icons.Rounded.Psychology
        category == "DATA_STATS_PROBABILITY" -> Icons.Rounded.BarChart
        category == "ALGEBRAIC_THINKING" -> Icons.Rounded.Functions

        else -> Icons.Rounded.School
    }
}

/* ============================= TRANG CHỦ THEO SÁCH GIÁO KHOA ============================= */

/** "Học sinh" (tên mặc định) hoặc trống → "con"; còn lại lấy tên gọi (từ cuối). */
private fun greetingName(fullName: String): String {
    val n = fullName.trim()
    return if (n.isBlank() || n.equals("Học sinh", ignoreCase = true)) "con" else n.split(" ").last()
}

/**
 * Bố cục như trang chủ cũ; các Bài chia hai hàng: Học kì 1 (Tập 1) và Học kì 2 (Tập 2).
 * Cuối mỗi hàng là ô "Kiểm tra học kì". Chạm một ô → thẻ lớn phía trên đổi theo ô đó.
 */
@Composable
private fun CurriculumJourney(
    curriculum: GradeCurriculum,
    studentProfile: StudentProfileEntity,
    progress: Map<String, Int>,
    onOpenLesson: (String) -> Unit,
    onOpenExam: (Int) -> Unit,
    modifier: Modifier,
    coins: Int? = null,
    topBanner: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    // Bản thử nghiệm (debug) cho mở cả bài đang khóa để kiểm tra nội dung
    val debuggable = remember { (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0 }
    // Bài theo sách + (lớp có thí điểm) các chuyên đề Toán tư duy – luôn mở, không khóa theo tiến độ sách
    val thinking = remember(curriculum.grade) { ThinkingCurricula.forGrade(curriculum.grade) }
    val lessons = curriculum.lessons + (thinking?.lessons ?: emptyList())
    fun prog(id: String) = progress[id] ?: 0

    val chapters = lessons.mapIndexed { i, l ->
        if (thinking != null && ThinkingCurricula.isThinking(l.id)) {
            val total = l.stages().size
            val done = l.doneCount(prog(l.id))
            val topic = thinking.topicOf(l)
            return@mapIndexed Chapter(
                index = i,
                domain = Domain.from(l.domain),
                title = l.title,
                start = 0,
                end = total,
                done = done,
                state = if (done >= total) ChapterState.DONE else ChapterState.ACTIVE,
                label = "Chuyên đề ${l.number}",
                kicker = "Toán tư duy · ${topic.title}",
                rating = LessonRating.of(l) { prog(it) }
            )
        }
        val total = l.stages().size
        val done = l.doneCount(prog(l.id))
        val topic = curriculum.topicOf(l)
        val relearn = prog(LessonKeys.relearn(l.id)) == 1
        Chapter(
            index = i,
            domain = Domain.from(l.domain),
            title = l.title,
            start = 0,
            end = total,
            done = done,
            state = when {
                done >= total -> ChapterState.DONE
                curriculum.isUnlocked(l) { prog(it) } -> ChapterState.ACTIVE
                else -> ChapterState.LOCKED
            },
            label = "Bài ${l.number}",
            kicker = "Chủ đề ${topic.number} · ${topic.title}",
            statusOverride = if (relearn) "Cần học lại" else null,
            mastered = prog(LessonKeys.mastered(l.id)) == 1,
            rating = LessonRating.of(l) { prog(it) }
        )
    }
    // Ưu tiên bài phải học lại, rồi đến bài đang học
    val activeIndex = chapters.indexOfFirst { prog(LessonKeys.relearn(lessons[it.index].id)) == 1 }
        .takeIf { it >= 0 }
        ?: chapters.indexOfFirst { it.state == ChapterState.ACTIVE && !ThinkingCurricula.isThinking(lessons[it.index].id) }
            .let { if (it < 0) 0 else it }
    // "L…" = một bài; "exam:1" / "exam:2" = bài kiểm tra học kì
    var selectedKey by rememberSaveable(curriculum.grade) { mutableStateOf(lessons[activeIndex].id) }
    val examSemester = selectedKey.removePrefix("exam:").toIntOrNull()?.takeIf { selectedKey.startsWith("exam:") }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Tone.Paper)
    ) {
        val narrow = maxWidth < 360.dp
        val side = if (narrow) 16.dp else 20.dp
        val tileWidth = if (narrow) 140.dp else 156.dp
        val showArt = maxWidth >= 340.dp
        val sidePadding = Modifier.padding(horizontal = side)

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(SectionGap)
        ) {
            Header(
                name = greetingName(studentProfile.fullName),
                streakDays = studentProfile.streakDays,
                modifier = sidePadding,
                coins = coins
            )
            if (topBanner != null) Box(sidePadding) { topBanner() }

            if (examSemester != null) {
                // ---- Thẻ lớn: bài kiểm tra học kì ----
                val ready = curriculum.examReady(examSemester) { prog(it) }
                val best = progress[LessonKeys.exam(curriculum.grade, examSemester)]
                val passed = best != null && best >= 80
                val semLessons = curriculum.lessonsOf(examSemester)
                HeroCard(
                    domain = Domain.REASONING,
                    pillText = when {
                        passed -> "Đã đạt · $best%"
                        best != null -> "Lần trước: $best%"
                        ready -> "Sẵn sàng kiểm tra"
                        else -> "Chưa mở khóa"
                    },
                    pillIcon = if (passed) Icons.Rounded.EmojiEvents else if (ready) Icons.Rounded.School else Icons.Rounded.Lock,
                    kicker = "Bài ${semLessons.first().number} – Bài ${semLessons.last().number} · 20 câu nâng cao · cần đạt 80%",
                    title = "Kiểm tra học kì $examSemester",
                    progressText = null,
                    learned = 0,
                    total = 0,
                    buttonText = when {
                        ready -> if (best != null) "Làm lại bài kiểm tra" else "Làm bài kiểm tra"
                        debuggable -> "Chưa mở · mở thử"
                        else -> "Qua Dạng 1 mọi bài trong kì để mở"
                    },
                    locked = !ready,
                    showArt = showArt,
                    modifier = sidePadding,
                    onContinue = {
                        if (ready || debuggable) {
                            SoundEffectsManager.playSelect(context)
                            onOpenExam(examSemester)
                        }
                    }
                )
            } else {
                // ---- Thẻ lớn: một bài ----
                val selected = chapters.firstOrNull { lessons[it.index].id == selectedKey } ?: chapters[activeIndex]
                val lesson = lessons[selected.index]
                val locked = selected.state == ChapterState.LOCKED
                val relearn = selected.statusOverride != null
                val isThinking = ThinkingCurricula.isThinking(lesson.id)
                HeroCard(
                    domain = selected.domain,
                    pillText = when {
                        relearn -> "Cần học lại"
                        isThinking && selected.rating != null -> "Toán tư duy · ${selected.rating.label}"
                        isThinking -> "Toán tư duy · luôn mở"
                        selected.mastered -> "Thành thạo qua thử thách"
                        selected.rating == LessonRating.THANH_THAO -> "Xếp loại: Thành thạo"
                        selected.rating != null -> "Xếp loại: Hiểu bài"
                        locked -> "Chưa mở khóa"
                        selected.index == activeIndex -> "Bài đang học"
                        else -> "Bài đã chọn"
                    },
                    pillIcon = when {
                        relearn -> Icons.Rounded.Replay
                        isThinking -> Icons.Rounded.Bolt
                        selected.mastered -> Icons.Rounded.Bolt
                        selected.state == ChapterState.DONE -> Icons.Rounded.EmojiEvents
                        locked -> Icons.Rounded.Lock
                        else -> Icons.Rounded.PlayArrow
                    },
                    kicker = selected.kicker,
                    title = if (isThinking) "Chuyên đề ${lesson.number}: ${lesson.title}" else "Bài ${lesson.number}: ${lesson.title}",
                    progressText = when (selected.rating) {
                        LessonRating.HIEU_BAI -> "Đúng trọn 12/12 Dạng 2 thêm ${(LessonRating.PERFECT_RUNS - prog(LessonKeys.perfect(lesson.id))).coerceAtLeast(1)} lượt để lên Thành thạo"
                        LessonRating.THANH_THAO -> "Đã học xong ${selected.total}/${selected.total} bước"
                        null -> "Đã xong ${selected.done}/${selected.total} bước"
                    },
                    learned = selected.done,
                    total = selected.total,
                    buttonText = when {
                        relearn -> "Học lại từ đầu"
                        isThinking && selected.done == 0 -> "Bắt đầu chuyên đề"
                        isThinking && selected.state == ChapterState.DONE -> "Luyện thêm"
                        isThinking -> "Học tiếp"
                        selected.mastered -> "Luyện thêm (không tính xu)"
                        selected.state == ChapterState.DONE -> "Ôn lại bài"
                        locked && debuggable -> "Chưa mở · mở thử"
                        locked -> "Học xong bài trước để mở"
                        else -> "Học tiếp"
                    },
                    locked = locked,
                    showArt = showArt,
                    modifier = sidePadding,
                    onContinue = {
                        if (!locked || debuggable) {
                            SoundEffectsManager.playSelect(context)
                            onOpenLesson(lesson.id)
                        }
                    }
                )
            }

            // ---- Hai hàng: Học kì 1, Học kì 2 ----
            listOf(1, 2).forEach { sem ->
                val semIds = curriculum.lessonsOf(sem).map { it.id }.toSet()
                val semChapters = chapters.filter { lessons[it.index].id in semIds }
                if (semChapters.isEmpty()) return@forEach
                val best = progress[LessonKeys.exam(curriculum.grade, sem)]
                val ready = curriculum.examReady(sem) { prog(it) }
                ChapterRail(
                    chapters = semChapters,
                    selectedIndex = chapters.firstOrNull { lessons[it.index].id == selectedKey }?.index ?: -1,
                    tileWidth = tileWidth,
                    side = side,
                    heading = "Học kì $sem",
                    countText = "${semChapters.count { it.state == ChapterState.DONE }}/${semChapters.size} bài xong",
                    trailing = {
                        ExamTile(
                            semester = sem,
                            selected = selectedKey == "exam:$sem",
                            ready = ready,
                            best = best,
                            width = tileWidth,
                            onClick = { selectedKey = "exam:$sem" }
                        )
                    },
                    onSelect = { selectedKey = lessons[it].id }
                )
            }

            // ---- Toán tư duy (thí điểm): luôn mở, học song song với sách ----
            val thinkingChapters = chapters.filter { ThinkingCurricula.isThinking(lessons[it.index].id) }
            if (thinkingChapters.isNotEmpty()) {
                ChapterRail(
                    chapters = thinkingChapters,
                    selectedIndex = chapters.firstOrNull { lessons[it.index].id == selectedKey }?.index ?: -1,
                    tileWidth = tileWidth,
                    side = side,
                    heading = "Toán tư duy",
                    countText = "Thử nghiệm · luôn mở",
                    onSelect = { selectedKey = lessons[it].id }
                )
            }
        }
    }
}

/** Ô "Kiểm tra học kì" ở cuối mỗi hàng. */
@Composable
private fun ExamTile(semester: Int, selected: Boolean, ready: Boolean, best: Int?, width: Dp, onClick: () -> Unit) {
    val passed = best != null && best >= 80
    val accent = Domain.REASONING
    val filled = selected && ready
    val shape = RoundedCornerShape(R_CARD)
    Column(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .heightIn(min = 176.dp)
            .clip(shape)
            .background(if (filled) accent.deep else accent.tint)
            .border(if (selected) 2.dp else 1.dp, if (selected) Tone.Ink else accent.deep.copy(alpha = 0.25f), shape)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(R_SMALL))
                    .background(if (filled) Color.White.copy(alpha = 0.16f) else Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (passed) Icons.Rounded.EmojiEvents else Icons.Rounded.School,
                    contentDescription = null,
                    tint = if (filled) Color.White else accent.deep,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            // Thưởng xu khi đạt lần đầu
            if (!passed) com.example.toanhoc.ui.family.CoinAmount(100, size = 16.dp, onDark = filled)
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(12.dp))
        UiText(text = "Kiểm tra", size = 14.sp, color = if (filled) Color.White else accent.text, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        UiText(
            text = "Học kì $semester",
            size = 17.sp, weight = FontWeight.ExtraBold,
            color = if (filled) Color.White else Tone.Ink, maxLines = 2, minLines = 2
        )
        Spacer(Modifier.height(8.dp))
        UiText(
            text = when {
                passed -> "Đã đạt · $best%"
                best != null -> "Lần trước $best% · chưa đạt"
                ready -> "Sẵn sàng · 20 câu"
                else -> "Chưa mở"
            },
            size = 14.sp, weight = FontWeight.SemiBold,
            color = if (filled) Color.White else Tone.InkMuted, maxLines = 1
        )
    }
}
