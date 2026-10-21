package com.example.toanhoc.ui.session

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.EmojiEvents
import com.example.toanhoc.ui.theme.SoftAmberBg
import com.example.toanhoc.ui.theme.SoftGreenBg
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.domain.usecase.SessionProgressSummary
import com.example.toanhoc.ui.design.Fib
import com.example.toanhoc.ui.design.PaperColors
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.MasteryVerifiedGreen
import com.example.toanhoc.ui.theme.NunitoFontFamily
import com.example.toanhoc.ui.theme.TargetUnknownAmber

/**
 * Session Summary Screen enforcing strict 12-question first-try accuracy rules
 * and conforming to the Paper Grid & Fibonacci Design System.
 */
@Composable
fun SessionSummaryScreen(
    summary: SessionProgressSummary,
    onRestartSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val is12QuestionsDone = summary.totalAnswered >= 12
    val isPassed = is12QuestionsDone && summary.accuracyPercentage >= 80
    val sfxContext = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        if (isPassed) com.example.toanhoc.ui.sound.SoundEffectsManager.play(sfxContext, com.example.toanhoc.ui.sound.Sfx.COMPLETE)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaperColors.Paper),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fib.S4, vertical = Fib.S4),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Fib.S4)
        ) {
            // 1. Header Title & Subtitle Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Fib.S2)
            ) {
                val statusTint = if (isPassed) MasteryVerifiedGreen else TargetUnknownAmber
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (isPassed) SoftGreenBg else SoftAmberBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPassed) Icons.Rounded.EmojiEvents else Icons.Rounded.Refresh,
                        contentDescription = null,
                        tint = statusTint,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isPassed) "Chúc mừng con!" else "Gần đạt rồi, cố thêm chút nữa",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = PaperColors.Ink,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isPassed)
                        "Con đã hoàn thành xuất sắc 12 câu hỏi của vòng học này!"
                    else
                        "Con cần tự giải đúng ngay lần đầu ≥ 80% (10/12 câu) để mở khóa vòng tiếp theo nhé!",
                    fontSize = 15.sp,
                    fontFamily = NunitoFontFamily,
                    color = PaperColors.InkSoft,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // 2. Summary Statistics Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Fib.S4))
                    .background(Color.White)
                    .border(1.dp, PaperColors.Hairline, RoundedCornerShape(Fib.S4))
                    .padding(Fib.S4)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Fib.S3)) {
                    SummaryRow(
                        label = "Đúng ngay lần đầu",
                        value = "${summary.correctCount} / 12 câu",
                        valueColor = if (isPassed) MasteryVerifiedGreen else TargetUnknownAmber
                    )

                    SummaryRow(
                        label = "Độ chính xác",
                        value = "${summary.accuracyPercentage}%",
                        valueColor = if (summary.accuracyPercentage >= 80) MasteryVerifiedGreen else TargetUnknownAmber
                    )

                    SummaryRow(
                        label = "Thời gian trung bình",
                        value = "${String.format(java.util.Locale.US, "%.1f", summary.averageLatencyMs / 1000f)} giây / câu"
                    )

                    SummaryRow(
                        label = "Trạng thái",
                        value = if (isPassed) "Đã thành thạo" else "Cần ≥ 80% để qua",
                        valueColor = if (isPassed) MasteryVerifiedGreen else TargetUnknownAmber
                    )
                }
            }

            // 3. Action Button
            val buttonLabel = if (isPassed) "Về bản đồ học" else "Làm lại vòng này"
            val buttonColor = GivenDataBlue

            val context = androidx.compose.ui.platform.LocalContext.current

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Fib.S6)
                    .clip(RoundedCornerShape(Fib.S3))
                    .background(buttonColor)
                    .clickable {
                        com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                        onRestartSession()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buttonLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = NunitoFontFamily,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(Fib.S2))
                    Icon(
                        imageVector = if (isPassed) Icons.AutoMirrored.Rounded.ArrowForward else Icons.Rounded.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = PaperColors.Ink
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontFamily = NunitoFontFamily,
            color = PaperColors.InkSoft
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = NunitoFontFamily,
            color = valueColor
        )
    }
}
