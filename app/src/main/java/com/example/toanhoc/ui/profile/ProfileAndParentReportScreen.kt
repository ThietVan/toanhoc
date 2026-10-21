package com.example.toanhoc.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.toanhoc.ui.theme.SoftBlueBg
import com.example.toanhoc.ui.theme.SoftGreenBg
import com.example.toanhoc.ui.theme.SoftRedBg
import com.example.toanhoc.ui.theme.TargetUnknownAmber
import java.time.LocalDate

/**
 * Pristine Student Profile & Parent Report Screen.
 * Features 4-Card Architecture: Student Profile, Parent Self-Study Metrics, Diagnostic Misconceptions Heatmap, and Academic Tier Scorecard.
 */
@Composable
fun ProfileAndParentReportScreen(
    profile: StudentProfileEntity,
    misconceptionLogs: List<MisconceptionLogEntity> = emptyList(),
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
        profile.selectedGrade > standardGrade -> "Vượt chương trình +${profile.selectedGrade - standardGrade} lớp"
        else -> "Đang củng cố nền tảng"
    }

    val currentTier = AcademicTier.fromString(profile.currentAcademicTier)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // CARD 1: STUDENT PROFILE CARD ("HỒ SƠ HỌC SINH")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = GivenDataBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "HỒ SƠ HỌC SINH",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GivenDataBlue
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Họ và tên:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = profile.fullName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Độ tuổi:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "$age tuổi (Sinh năm ${profile.birthYear})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Khối lớp:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(SoftBlueBg)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Lớp ${profile.selectedGrade} • $ageEval",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GivenDataBlue
                            )
                        }
                    }
                }
            }
        }

        // CARD 2: PARENT REPORT CARD ("BÁO CÁO TỰ HỌC CHO PHỤ HUYNH")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📊", fontSize = 18.sp)
                    Text(
                        text = "BÁO CÁO TỰ HỌC CHO PHỤ HUYNH",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GivenDataBlue
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kỷ luật Tự học:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(SoftBlueBg)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🔥 ${profile.streakDays} ngày liên tiếp (${profile.totalFocusMinutesLast7Days}p/7 ngày)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GivenDataBlue
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tự lực Thực chất:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(SoftGreenBg)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${profile.selfReliancePercentage}% tự giải lần 1",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MasteryVerifiedGreen
                            )
                        }
                    }
                }

                // Functional Parent Verification Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(50))
                        .background(TargetUnknownAmber)
                        .clickable { onStartParentExam() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📝 TẠO BÀI KIỂM TRA NĂNG LỰC NGAY ➔",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // CARD 3: DIAGNOSTIC MISCONCEPTION HEATMAP & PARENT TIPS ("BẢN ĐỒ CHẨN ĐOÁN LỖI TƯ DUY")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🧠", fontSize = 18.sp)
                    Text(
                        text = "BẢN ĐỒ CHẨN ĐOÁN LỖI TƯ DUY",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorDiagnosticRed
                    )
                }

                if (misconceptionLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SoftBlueBg)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Bé làm bài rất tốt! Chưa phát hiện lỗ hổng nhận thức nào.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    misconceptionLogs.forEach { log ->
                        val bugEnum = CognitiveBugTag.fromTagCode(log.bugTag)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SoftRedBg)
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ErrorDiagnosticRed,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Text(
                                        text = bugEnum?.titleVi ?: log.bugTag,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorDiagnosticRed
                                    )
                                }

                                Text(
                                    text = bugEnum?.socraticExplanationVi ?: "Khi cộng hai phân số, quy đồng mẫu số trước rồi cộng các tử số.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = ErrorDiagnosticRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Đã lặp lại ${log.occurrenceCount} lần trong các bài thi",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorDiagnosticRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // CARD 4: ACADEMIC TIER CARD ("BẢNG THÀNH TÍCH HỌC THUẬT")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GivenDataBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "BẢNG THÀNH TÍCH HỌC THUẬT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GivenDataBlue
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SoftGreenBg)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "HẠNG HỌC THUẬT: ${currentTier.titleVi}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MasteryVerifiedGreen
                    )
                }

                Text(
                    text = currentTier.descriptionVi,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                // Functional Exam Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(50))
                        .background(GivenDataBlue)
                        .clickable { onStartLevelUpExam() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LÀM BÀI THI NÂNG CẤP ĐỘ ➔",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
