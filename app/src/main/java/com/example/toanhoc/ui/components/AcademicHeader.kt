package com.example.toanhoc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.SoftAmberBg
import com.example.toanhoc.ui.theme.SoftBlueBg
import com.example.toanhoc.ui.theme.SubtleBorder
import com.example.toanhoc.ui.theme.TargetUnknownAmber
import java.util.Locale

@Composable
fun MathCatMascotAvatar(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)

        // Ears
        val leftEar = Path().apply {
            moveTo(w * 0.28f, h * 0.35f)
            lineTo(w * 0.18f, h * 0.12f)
            lineTo(w * 0.42f, h * 0.22f)
            close()
        }
        drawPath(leftEar, color = Color(0xFFF59E0B))

        val rightEar = Path().apply {
            moveTo(w * 0.72f, h * 0.35f)
            lineTo(w * 0.82f, h * 0.12f)
            lineTo(w * 0.58f, h * 0.22f)
            close()
        }
        drawPath(rightEar, color = Color(0xFFF59E0B))

        // Face Base
        drawCircle(
            color = Color(0xFFFBBF24),
            radius = w * 0.38f,
            center = Offset(center.x, center.y + h * 0.05f)
        )

        // Cheeks
        drawCircle(
            color = Color(0xFFFFFFFF),
            radius = w * 0.22f,
            center = Offset(center.x, center.y + h * 0.18f)
        )

        // Eyes
        drawCircle(
            color = Color(0xFF1E293B),
            radius = w * 0.06f,
            center = Offset(center.x - w * 0.14f, center.y - h * 0.02f)
        )
        drawCircle(
            color = Color(0xFF1E293B),
            radius = w * 0.06f,
            center = Offset(center.x + w * 0.14f, center.y - h * 0.02f)
        )

        // Nose
        val nose = Path().apply {
            moveTo(center.x, center.y + h * 0.08f)
            lineTo(center.x - w * 0.05f, center.y + h * 0.14f)
            lineTo(center.x + w * 0.05f, center.y + h * 0.14f)
            close()
        }
        drawPath(nose, color = Color(0xFFEF4444))

        // Graduation cap
        val capTop = Path().apply {
            moveTo(center.x, center.y - h * 0.38f)
            lineTo(center.x + w * 0.35f, center.y - h * 0.26f)
            lineTo(center.x, center.y - h * 0.14f)
            lineTo(center.x - w * 0.35f, center.y - h * 0.26f)
            close()
        }
        drawPath(capTop, color = Color(0xFF1E1B4B))
    }
}

@Composable
fun TimerVectorIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f + 1.dp.toPx())
        val r = w * 0.4f
        drawCircle(color = tint, radius = r, center = center, style = Stroke(width = 1.8.dp.toPx()))
        drawLine(color = tint, start = center, end = Offset(center.x, center.y - r * 0.55f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = tint, start = center, end = Offset(center.x + r * 0.45f, center.y), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w / 2f - 2.dp.toPx(), 1.dp.toPx()), end = Offset(w / 2f + 2.dp.toPx(), 1.dp.toPx()), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
    }
}

/**
 * Pristine Academic Header with Upward Count-Up Stopwatch Timer (00:00 -> 00:01...).
 */
@Composable
fun AcademicHeader(
    elapsedSeconds: Int,
    modifier: Modifier = Modifier,
    phaseName: String = "",
    gradeLevel: Int = 1,
    currentProblemIndex: Int = 4,
    totalProblems: Int = 12,
    onBackClick: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    val progress = (currentProblemIndex.toFloat() / totalProblems.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Group: Vector Circular Back Button + Mascot Vector Avatar + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SoftBlueBg)
                        .clickable {
                            com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                            onBackClick?.invoke()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = GivenDataBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GivenDataBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    MathCatMascotAvatar(modifier = Modifier.size(28.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = if (phaseName.isNotEmpty() && !phaseName.contains("Pha")) phaseName else "Toán • Lớp $gradeLevel",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Bài $currentProblemIndex / $totalProblems",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Right Group: Count-Up Stopwatch Timer Pill Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SoftAmberBg)
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                TimerVectorIcon(
                    tint = TargetUnknownAmber,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = timeFormatted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TargetUnknownAmber
                )
            }
        }

        // Smooth Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = GivenDataBlue,
                trackColor = SoftBlueBg
            )
        }

        // Bottom Subtle Divider Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SubtleBorder)
        )
    }
}
