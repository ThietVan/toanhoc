package com.example.toanhoc.ui.profile

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.local.entity.MisconceptionLogEntity
import com.example.toanhoc.data.local.entity.StudentProfileEntity
import com.example.toanhoc.domain.engine.CognitiveBugTag
import com.example.toanhoc.domain.model.AcademicTier
import com.example.toanhoc.ui.theme.ErrorDiagnosticRed
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.MasteryVerifiedGreen
import com.example.toanhoc.ui.theme.NunitoFontFamily
import com.example.toanhoc.ui.theme.SoftBlueBg
import com.example.toanhoc.ui.theme.TargetUnknownAmber
import java.time.LocalDate

/* ------------------------------------------------------------------------------------------
 * Hồ sơ học sinh - cùng ngôn ngữ thiết kế với tab học tập và màn luyện tập:
 *  - Nền giấy kẻ ô li, thẻ trắng viền mảnh, tiêu đề Serif, nội dung Nunito.
 *  - Bỏ font Monospace và chữ in hoa toàn bộ: thay bằng câu chữ thường dễ đọc.
 *  - Phụ huynh nhìn thấy số liệu chính trước (thẻ 1 : 1.618), chi tiết chẩn đoán đặt sau.
 * ---------------------------------------------------------------------------------------- */

private const val PHI = 1.618f

private val Ink = Color(0xFF14213D)
private val InkSoft = Color(0xFF5B6784)
private val Paper = Color(0xFFF3F6FB)
private val GridDot = Color(0xFFCFD8EA)
private val Hairline = Color(0xFFDDE4F1)

private val S1 = 5.dp
private val S2 = 8.dp
private val S3 = 13.dp
private val S4 = 21.dp
private val S5 = 34.dp
private val S6 = 55.dp

@Composable
fun UserProfileScreen(
    profile: StudentProfileEntity,
    misconceptionLogs: List<MisconceptionLogEntity>,
    onSelectGrade: (Int) -> Unit,
    onUpdateProfile: (StudentProfileEntity) -> Unit,
    onStartLevelUpExam: () -> Unit,
    onStartParentExam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentYear = LocalDate.now().year
    val age = (currentYear - profile.birthYear).coerceAtLeast(5)
    val standardGrade = (age - 5).coerceIn(1, 5)

    val ageEval = when {
        profile.selectedGrade == standardGrade -> "Đúng chuẩn độ tuổi"
        profile.selectedGrade > standardGrade -> "Vượt chương trình ${profile.selectedGrade - standardGrade} lớp"
        else -> "Đang củng cố nền tảng"
    }
    val evalColor = when {
        profile.selectedGrade > standardGrade -> GivenDataBlue
        profile.selectedGrade == standardGrade -> MasteryVerifiedGreen
        else -> TargetUnknownAmber
    }

    val currentTier = AcademicTier.fromString(profile.currentAcademicTier)
    val initial = profile.fullName.trim().split(" ").lastOrNull()?.firstOrNull()?.uppercase() ?: "?"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(S4),
        verticalArrangement = Arrangement.spacedBy(S4)
    ) {
        // 1. Danh thiếp học sinh
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(S3)
        ) {
            Box(
                modifier = Modifier
                    .size(S6)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF1B2D5C), Ink))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = Color.White
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(S1)) {
                Text(
                    text = profile.fullName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = Ink
                )
                Text(
                    text = "$age tuổi, sinh năm ${profile.birthYear}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = NunitoFontFamily,
                    color = InkSoft
                )
            }
        }

        // 2. Chọn khối lớp (thanh phân đoạn)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(S4))
                .background(Color.White)
                .border(1.dp, Hairline, RoundedCornerShape(S4))
                .padding(S3),
            verticalArrangement = Arrangement.spacedBy(S3)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Khối lớp đang học",
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = Ink
                )
                Text(
                    text = ageEval,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = evalColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(S2))
                        .background(evalColor.copy(alpha = 0.12f))
                        .padding(horizontal = S2, vertical = S1)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(S3))
                    .background(Paper)
                    .border(1.dp, Hairline, RoundedCornerShape(S3))
                    .padding(S1),
                horizontalArrangement = Arrangement.spacedBy(S1)
            ) {
                for (grade in 1..5) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val isSelected = profile.selectedGrade == grade
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(S5 + S1) // 39dp
                            .clip(RoundedCornerShape(S2))
                            .background(if (isSelected) GivenDataBlue else Color.Transparent)
                            .clickable {
                                com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                                onSelectGrade(grade)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Lớp $grade",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NunitoFontFamily,
                            color = if (isSelected) Color.White else InkSoft
                        )
                    }
                }
            }
        }

        // 3. Hạng học thuật
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(S4))
                .background(Brush.linearGradient(listOf(Color(0xFF1B2D5C), Ink)))
                .padding(S4),
            verticalArrangement = Arrangement.spacedBy(S3)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(S3)
            ) {
                Box(
                    modifier = Modifier
                        .size(S6)
                        .clip(RoundedCornerShape(S3))
                        .background(TargetUnknownAmber),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(S4 + S1)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Hạng học thuật hiện tại",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = NunitoFontFamily,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = currentTier.titleVi,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NunitoFontFamily,
                        color = Color.White
                    )
                }
            }

            Text(
                text = currentTier.descriptionVi,
                fontSize = 13.sp,
                lineHeight = 21.sp,
                fontFamily = NunitoFontFamily,
                color = Color.White.copy(alpha = 0.85f)
            )

            val context = androidx.compose.ui.platform.LocalContext.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(S6)
                    .clip(RoundedCornerShape(S3))
                    .background(Color.White)
                    .clickable {
                        com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                        onStartLevelUpExam()
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Làm bài thi nâng hạng",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = NunitoFontFamily,
                    color = Ink
                )
                Spacer(Modifier.width(S2))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = Ink,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 4. Dành cho phụ huynh
        Column(verticalArrangement = Arrangement.spacedBy(S3)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Dành cho phụ huynh",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = Ink
                )
                Text(
                    text = "Con tự học đều đặn và tự lực đến đâu",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = NunitoFontFamily,
                    color = InkSoft
                )
            }

            // Hai chỉ số chính chia 1 : 1.618
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(S3)
            ) {
                StatTile(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    icon = {
                        Icon(
                            Icons.Rounded.LocalFireDepartment, null,
                            tint = Color(0xFFEA580C), modifier = Modifier.size(S4)
                        )
                    },
                    label = "Kỷ luật tự học",
                    value = "${profile.streakDays} ngày",
                    caption = "${profile.totalFocusMinutesLast7Days} phút trong 7 ngày"
                )
                StatTile(
                    modifier = Modifier
                        .weight(PHI)
                        .fillMaxHeight(),
                    icon = {
                        Icon(
                            Icons.Rounded.Psychology, null,
                            tint = MasteryVerifiedGreen, modifier = Modifier.size(S4)
                        )
                    },
                    label = "Tự lực thực chất",
                    value = "${profile.selfReliancePercentage}%",
                    caption = "Tự giải đúng ngay lần đầu",
                    progress = (profile.selfReliancePercentage.toFloat() / 100f).coerceIn(0f, 1f)
                )
            }

            // Lỗi tư duy cần lưu ý
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(S4))
                    .background(Color.White)
                    .border(1.dp, Hairline, RoundedCornerShape(S4))
                    .padding(S4),
                verticalArrangement = Arrangement.spacedBy(S3)
            ) {
                Text(
                    text = "Lỗi tư duy cần lưu ý",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = Ink
                )

                if (misconceptionLogs.isEmpty()) {
                    Text(
                        text = "Chưa phát hiện lỗi tư duy nghiêm trọng trong các phiên gần đây.",
                        fontSize = 13.sp,
                        lineHeight = 21.sp,
                        fontFamily = NunitoFontFamily,
                        color = InkSoft
                    )
                } else {
                    misconceptionLogs.forEach { log ->
                        val bugEnum = CognitiveBugTag.fromTagCode(log.bugTag)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .clip(RoundedCornerShape(S3))
                                .background(ErrorDiagnosticRed.copy(alpha = 0.06f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(ErrorDiagnosticRed)
                            )
                            Column(
                                modifier = Modifier.padding(S3),
                                verticalArrangement = Arrangement.spacedBy(S1)
                            ) {
                                Text(
                                    text = "${bugEnum?.titleVi ?: log.bugTag}, lặp lại ${log.occurrenceCount} lần",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = NunitoFontFamily,
                                    color = ErrorDiagnosticRed
                                )
                                val explanation = bugEnum?.socraticExplanationVi ?: ""
                                if (explanation.isNotBlank()) {
                                    Text(
                                        text = explanation,
                                        fontSize = 12.sp,
                                        lineHeight = 19.sp,
                                        fontFamily = NunitoFontFamily,
                                        color = Ink
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val context = androidx.compose.ui.platform.LocalContext.current
            // Tạo bài kiểm tra năng lực
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(S6)
                    .clip(RoundedCornerShape(S3))
                    .background(TargetUnknownAmber)
                    .clickable {
                        com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                        onStartParentExam()
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.School,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(S2))
                Text(
                    text = "Tạo bài kiểm tra năng lực",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = NunitoFontFamily,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(S4))
    }
}

/* ------------------------------ Thẻ chỉ số ------------------------------ */

@Composable
private fun StatTile(
    modifier: Modifier,
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    caption: String,
    progress: Float? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(S4))
            .background(Color.White)
            .border(1.dp, Hairline, RoundedCornerShape(S4))
            .padding(S3),
        verticalArrangement = Arrangement.spacedBy(S2)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(S1)
        ) {
            Box(
                modifier = Modifier
                    .size(S5)
                    .clip(RoundedCornerShape(S3))
                    .background(SoftBlueBg),
                contentAlignment = Alignment.Center
            ) { icon() }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NunitoFontFamily,
                color = InkSoft
            )
        }
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = NunitoFontFamily,
            color = Ink
        )
        if (progress != null) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(S1)
                    .clip(CircleShape),
                color = MasteryVerifiedGreen,
                trackColor = Hairline
            )
        }
        Text(
            text = caption,
            fontSize = 12.sp,
            fontFamily = NunitoFontFamily,
            color = InkSoft
        )
    }
}