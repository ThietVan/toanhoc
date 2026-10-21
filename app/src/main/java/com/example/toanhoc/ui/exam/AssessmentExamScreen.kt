package com.example.toanhoc.ui.exam

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.domain.model.ExamMode
import com.example.toanhoc.domain.model.VisualBlueprintType
import com.example.toanhoc.ui.canvas.DiagramView
import com.example.toanhoc.ui.canvas.rememberDiagram
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.MasteryVerifiedGreen
import com.example.toanhoc.ui.theme.TargetUnknownAmber
import java.util.Locale

/**
 * Assessment Exam Screen executing Level-Up Gatekeeper and Parent Verification Exams with 100% Multiple Choice UI.
 */
@Composable
fun AssessmentExamScreen(
    uiState: ExamUiState,
    onAnswerInput: (String) -> Unit,
    onPreviousProblem: () -> Unit,
    onNextProblem: () -> Unit,
    onSubmitExam: () -> Unit,
    onExitExam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    BackHandler {
        onExitExam()
    }

    val currentProblem = uiState.problems.getOrNull(uiState.currentProblemIndex)
    val currentStep = currentProblem?.steps?.lastOrNull()
    val optionsList = currentStep?.options ?: emptyList()
    val currentAnswer = uiState.studentAnswersMap[uiState.currentProblemIndex] ?: ""

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.examMode.titleVi} • CÂU ${uiState.currentProblemIndex + 1}/10",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = GivenDataBlue
                )

                val min = uiState.totalSecondsRemaining / 60
                val sec = uiState.totalSecondsRemaining % 60
                Text(
                    text = String.format(Locale.US, "%02d:%02d", min, sec),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TargetUnknownAmber
                )
            }

            // Main Scroll Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = currentProblem?.statementVi ?: "",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                val diagram = rememberDiagram(currentProblem)
                if (diagram != null) {
                    DiagramView(
                        diagram = diagram,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                    )
                }

                // 100% Multiple Choice A, B, C, D Options
                Text(
                    text = "Chọn phương án đúng A, B, C, D:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GivenDataBlue
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    optionsList.forEachIndexed { idx, optionText ->
                        val optionLabel = listOf("A", "B", "C", "D").getOrElse(idx) { "${idx + 1}" }
                        val isSelected = currentAnswer == optionText

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) TargetUnknownAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                .border(1.5.dp, if (isSelected) TargetUnknownAmber else MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                .clickable { onAnswerInput(optionText) }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "$optionLabel. $optionText",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) TargetUnknownAmber else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Previous / Next Navigation Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                            .clickable {
                                com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                                onPreviousProblem()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "◄ CÂU TRƯỚC",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(modifier = Modifier.padding(horizontal = 6.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(GivenDataBlue)
                            .clickable {
                                com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                                onNextProblem()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CÂU SAU ►",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }
                }
            }

            // Fixed Submit Button at Bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(MasteryVerifiedGreen)
                    .clickable {
                        com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                        onSubmitExam()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NỘP BÀI THI ↵",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
            }
        }

        // Exam Result Summary Overlay
        if (uiState.isSubmitted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { /* Block clicks */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "KẾT QUẢ BÀI THI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = GivenDataBlue
                    )

                    Text(
                        text = "Điểm số: ${uiState.scoreOutOf10} / 10 câu đúng",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (uiState.isPassed) MasteryVerifiedGreen else TargetUnknownAmber
                    )

                    val statusMsg = if (uiState.examMode == ExamMode.LEVEL_UP_EXAM) {
                        if (uiState.isPassed) "CHÚC MỪNG! BẠN ĐÃ THĂNG LÊN HẠNG ${uiState.targetTier.titleVi}!"
                        else "Chưa đạt ngưỡng 8/10 câu. Hãy tiếp tục rèn luyện để thi lại!"
                    } else {
                        "Đã hoàn thành bài kiểm tra năng lực của phụ huynh."
                    }

                    Text(
                        text = statusMsg,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(GivenDataBlue)
                            .clickable { onExitExam() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "QUAY VỀ BẢNG ĐIỀU KHIỂN ↵",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
