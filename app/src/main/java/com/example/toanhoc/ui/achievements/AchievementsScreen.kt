package com.example.toanhoc.ui.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.text.TextStyle
import com.example.toanhoc.ui.design.AutoSizeText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.local.entity.StudentProfileEntity
import com.example.toanhoc.ui.dashboard.DashboardUiState
import com.example.toanhoc.ui.design.Fib
import com.example.toanhoc.ui.design.PaperColors
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.MasteryVerifiedGreen
import com.example.toanhoc.ui.theme.NunitoFontFamily
import com.example.toanhoc.ui.theme.SoftAmberBg
import com.example.toanhoc.ui.theme.SoftGreenBg
import com.example.toanhoc.ui.theme.TargetUnknownAmber

private const val STREAK_GOAL = 3
private const val ACCURACY_GOAL = 80f
private const val LATENCY_GOAL_MS = 2500f
private const val TOPIC_GOAL = 20f
private const val TIER_BASELINE = "TRUNG_BINH"

@Immutable
private data class BadgeUi(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val goal: String,
    val progress: Float,
    val progressLabel: String
) {
    val isEarned: Boolean get() = progress >= 1f
}

private fun buildBadges(
    masteredCount: Int,
    streakDays: Int,
    accuracyPercent: Float,
    avgLatencyMs: Float,
    academicTier: String,
    topicCompletionPercent: Float
): List<BadgeUi> = listOf(
    BadgeUi(
        id = "first_mastery",
        icon = Icons.Rounded.Flag,
        title = "Bước đầu vững chắc",
        goal = "Thành thạo bài học đầu tiên",
        progress = masteredCount.coerceAtMost(1).toFloat(),
        progressLabel = "${masteredCount.coerceAtMost(1)}/1 bài"
    ),
    BadgeUi(
        id = "daily_streak",
        icon = Icons.Rounded.LocalFireDepartment,
        title = "Chăm ngoan mỗi ngày",
        goal = "Học liên tiếp $STREAK_GOAL ngày không gián đoạn",
        progress = (streakDays.toFloat() / STREAK_GOAL).coerceIn(0f, 1f),
        progressLabel = "${streakDays.coerceAtMost(STREAK_GOAL)}/$STREAK_GOAL ngày"
    ),
    BadgeUi(
        id = "accuracy",
        icon = Icons.Rounded.Psychology,
        title = "Thần đồng Toán học",
        goal = "Đúng từ ${ACCURACY_GOAL.toInt()}% trong buổi luyện tập hôm nay",
        progress = (accuracyPercent / ACCURACY_GOAL).coerceIn(0f, 1f),
        progressLabel = "${accuracyPercent.toInt()}%/${ACCURACY_GOAL.toInt()}%"
    ),
    BadgeUi(
        id = "speed",
        icon = Icons.Rounded.Bolt,
        title = "Phản xạ thần tốc",
        goal = "Trả lời trung bình dưới 2,5 giây",
        progress = if (avgLatencyMs <= 0f) 0f else (LATENCY_GOAL_MS / avgLatencyMs).coerceIn(0f, 1f),
        progressLabel = if (avgLatencyMs <= 0f) "Chưa có dữ liệu"
        else "Hiện tại ${"%.1f".format(avgLatencyMs / 1000f)} giây"
    ),
    BadgeUi(
        id = "tier_up",
        icon = Icons.Rounded.EmojiEvents,
        title = "Vượt cấp xuất sắc",
        goal = "Vượt qua bài thi nâng hạng học thuật",
        progress = if (academicTier != TIER_BASELINE) 1f else 0f,
        progressLabel = "Chưa nâng hạng"
    ),
    BadgeUi(
        id = "progress_star",
        icon = Icons.Rounded.Star,
        title = "Ngôi sao tiến bộ",
        goal = "Hoàn thành ${TOPIC_GOAL.toInt()}% số bài học",
        progress = (topicCompletionPercent / TOPIC_GOAL).coerceIn(0f, 1f),
        progressLabel = "${topicCompletionPercent.toInt()}%/${TOPIC_GOAL.toInt()}%"
    )
)

@Composable
fun AchievementsScreen(
    uiState: DashboardUiState,
    studentProfile: StudentProfileEntity,
    modifier: Modifier = Modifier
) {
    var filterIdx by rememberSaveable { mutableIntStateOf(0) } // 0 = tất cả, 1 = đã đạt, 2 = chưa đạt

    val masteredCount = remember(uiState.gradeNodes, uiState.masteryMap) {
        uiState.gradeNodes.count { uiState.masteryMap[it.nodeId]?.isMastered == true }
    }

    val accuracy = uiState.todayAccuracyPercentage.toFloat()
    val latency = uiState.todayAvgLatencyMs.toFloat()
    val topic = uiState.topicCompletionPercentage.toFloat()
    val streak = studentProfile.streakDays
    val tier = studentProfile.currentAcademicTier

    val badges = remember(masteredCount, streak, accuracy, latency, tier, topic) {
        buildBadges(masteredCount, streak, accuracy, latency, tier, topic)
    }
    val earnedCount = remember(badges) { badges.count { it.isEarned } }
    val nextBadge = remember(badges) { badges.filter { !it.isEarned }.maxByOrNull { it.progress } }
    val displayed = remember(badges, filterIdx) {
        when (filterIdx) {
            1 -> badges.filter { it.isEarned }
            2 -> badges.filter { !it.isEarned }
            else -> badges
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaperColors.Paper)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Fib.S4, vertical = Fib.S4),
        verticalArrangement = Arrangement.spacedBy(Fib.S4)
    ) {
        // 1. Tiêu đề + chuỗi ngày
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Fib.S1)) {
                Text(
                    text = "Thành tích của ${studentProfile.fullName.trim().split(" ").lastOrNull().orEmpty()}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = PaperColors.Ink
                )
                Text(
                    text = "Lớp ${studentProfile.selectedGrade}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = NunitoFontFamily,
                    color = PaperColors.InkSoft
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(Fib.S3))
                    .background(Color.White)
                    .border(1.dp, PaperColors.Hairline, RoundedCornerShape(Fib.S3))
                    .padding(horizontal = Fib.S3, vertical = Fib.S2),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Fib.S1)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFEA580C),
                    modifier = Modifier.size(Fib.S4)
                )
                Text(
                    text = "$streak ngày",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = NunitoFontFamily,
                    color = PaperColors.Ink
                )
            }
        }

        // 2. Thẻ tổng quan
        SummaryCard(
            earned = earnedCount,
            total = badges.size,
            nextBadge = nextBadge
        )

        // 3. Bộ lọc
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filterOptions = listOf(
                "Tất cả (${badges.size})",
                "Đã đạt ($earnedCount)",
                "Chưa đạt (${badges.size - earnedCount})"
            )
            filterOptions.forEachIndexed { idx, label ->
                val context = androidx.compose.ui.platform.LocalContext.current
                val isSelected = filterIdx == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) GivenDataBlue else Color.White)
                        .border(1.dp, if (isSelected) GivenDataBlue else PaperColors.Hairline, RoundedCornerShape(50))
                        .clickable {
                            com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                            filterIdx = idx
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else PaperColors.Ink
                    )
                }
            }
        }

        // 4. Bộ sưu tập
        Column(verticalArrangement = Arrangement.spacedBy(Fib.S1)) {
            Text(
                text = "Bộ sưu tập huy hiệu",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NunitoFontFamily,
                color = PaperColors.Ink
            )
            Text(
                text = "Mỗi huy hiệu ghi nhận một nỗ lực của con",
                fontSize = 13.sp,
                fontFamily = NunitoFontFamily,
                color = PaperColors.InkSoft
            )
        }

        if (displayed.isEmpty()) {
            EmptyState(
                message = if (filterIdx == 1) "Con chưa có huy hiệu nào. Hoàn thành một bài học để nhận huy hiệu đầu tiên."
                else "Con đã đạt tất cả huy hiệu. Tuyệt vời!"
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Fib.S3)) {
                displayed.chunked(2).forEach { rowBadges ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(Fib.S3)
                    ) {
                        rowBadges.forEach { badge ->
                            BadgeCard(
                                badge = badge,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                        if (rowBadges.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(Fib.S4))
    }
}

/* ------------------------------ Thẻ tổng quan ------------------------------ */

@Composable
private fun SummaryCard(earned: Int, total: Int, nextBadge: BadgeUi?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(144.dp)
            .clip(RoundedCornerShape(Fib.S4))
            .background(Brush.linearGradient(listOf(GivenDataBlue, PaperColors.Ink)))
            .padding(Fib.S3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { if (total == 0) 0f else earned.toFloat() / total },
                modifier = Modifier.size(89.dp),
                color = TargetUnknownAmber,
                trackColor = Color.White.copy(alpha = 0.16f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$earned/$total",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = Color.White
                )
                Text(
                    text = "huy hiệu",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = NunitoFontFamily,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1.618f)
                .fillMaxHeight()
                .padding(start = Fib.S3),
            verticalArrangement = Arrangement.Center
        ) {
            if (nextBadge != null) {
                Text(
                    text = "Gần đạt nhất",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = NunitoFontFamily,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(Fib.S1))
                Text(
                    text = nextBadge.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = Color.White
                )
                Spacer(Modifier.height(Fib.S2))
                LinearProgressIndicator(
                    progress = { nextBadge.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Fib.S2)
                        .clip(CircleShape),
                    color = TargetUnknownAmber,
                    trackColor = Color.White.copy(alpha = 0.16f)
                )
                Spacer(Modifier.height(Fib.S1))
                Text(
                    text = nextBadge.progressLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = NunitoFontFamily,
                    color = Color.White.copy(alpha = 0.85f)
                )
            } else {
                Text(
                    text = "Tuyệt vời!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = Color.White
                )
                Spacer(Modifier.height(Fib.S1))
                Text(
                    text = "Con đã sưu tập đủ tất cả huy hiệu",
                    fontSize = 13.sp,
                    fontFamily = NunitoFontFamily,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/* ------------------------------- Thẻ huy hiệu ------------------------------- */

@Composable
private fun BadgeCard(badge: BadgeUi, modifier: Modifier = Modifier) {
    val earned = badge.isEarned
    val shape = RoundedCornerShape(Fib.S4)

    Column(
        modifier = modifier
            .clip(shape)
            .background(if (earned) Color.White else Color.White.copy(alpha = 0.6f))
            .border(
                width = 1.dp,
                color = if (earned) TargetUnknownAmber.copy(alpha = 0.5f) else PaperColors.Hairline,
                shape = shape
            )
            .padding(Fib.S3),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Fib.S2)
    ) {
        Box(
            modifier = Modifier
                .size(Fib.S6)
                .clip(RoundedCornerShape(Fib.S3))
                .background(if (earned) SoftAmberBg else PaperColors.Paper)
                .border(
                    1.dp,
                    if (earned) TargetUnknownAmber.copy(alpha = 0.3f) else PaperColors.Hairline,
                    RoundedCornerShape(Fib.S3)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = badge.icon,
                contentDescription = null,
                tint = if (earned) TargetUnknownAmber else Color(0xFFAAB3C8),
                modifier = Modifier.size(Fib.S4 + Fib.S1)
            )
        }

        Text(
            text = badge.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = NunitoFontFamily,
            color = if (earned) PaperColors.Ink else PaperColors.InkSoft,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        Text(
            text = badge.goal,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontFamily = NunitoFontFamily,
            color = PaperColors.InkSoft,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2
        )

        if (earned) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SoftGreenBg)
                    .padding(horizontal = Fib.S2, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MasteryVerifiedGreen,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Đã đạt",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = MasteryVerifiedGreen
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LinearProgressIndicator(
                    progress = { badge.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Fib.S1)
                        .clip(CircleShape),
                    color = TargetUnknownAmber,
                    trackColor = PaperColors.Hairline
                )
                Text(
                    text = badge.progressLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = NunitoFontFamily,
                    color = PaperColors.InkSoft
                )
            }
        }
    }
}

/* ------------------------------ Trạng thái rỗng ------------------------------ */

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Fib.S4))
            .background(Color.White)
            .border(1.dp, PaperColors.Hairline, RoundedCornerShape(Fib.S4))
            .padding(Fib.S5),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            fontFamily = NunitoFontFamily,
            color = PaperColors.InkSoft,
            textAlign = TextAlign.Center
        )
    }
}
