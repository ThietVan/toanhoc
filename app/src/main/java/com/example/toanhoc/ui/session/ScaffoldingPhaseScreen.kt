package com.example.toanhoc.ui.session

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.domain.model.ScaffoldingLevel
import com.example.toanhoc.domain.model.SessionState
import com.example.toanhoc.domain.model.VisualBlueprintType
import com.example.toanhoc.ui.canvas.DiagramView
import com.example.toanhoc.ui.canvas.rememberDiagram
import com.example.toanhoc.ui.components.AcademicHeader
import com.example.toanhoc.ui.components.AcademicMathKeypad
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.SoftBlueBg
import com.example.toanhoc.ui.theme.SubtleBorder
import com.example.toanhoc.ui.theme.TargetUnknownAmber

@Composable
fun ScaffoldingPhaseScreen(
    state: SessionState,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val problem = state.currentProblem

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AcademicHeader(
            elapsedSeconds = 0,
            gradeLevel = state.gradeLevel
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Problem Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SubtleBorder),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val levelBadgeText = when (problem?.scaffoldingLevel) {
                        ScaffoldingLevel.WORKED_EXAMPLE -> "Quan sát & suy nghĩ"
                        ScaffoldingLevel.FADED_STEP -> "Bài tương tự (Ẩn 1 bước)"
                        ScaffoldingLevel.INDEPENDENT -> "Tự giải theo Pólya 4 bước"
                        else -> "Giải độc lập"
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SoftBlueBg)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Rounded.Lightbulb,
                            contentDescription = null,
                            tint = GivenDataBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = levelBadgeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GivenDataBlue
                        )
                    }

                    Text(
                        text = problem?.statementVi ?: "",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Sơ đồ dựng từ chính đề bài (không lộ đáp án)
                    val diagram = rememberDiagram(problem)
                    if (diagram != null) {
                        DiagramView(diagram = diagram, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Render Solution steps
            if (problem != null && problem.steps.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SubtleBorder),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "BƯỚC GIẢI CHI TIẾT:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GivenDataBlue
                        )
                        problem.steps.forEach { step ->
                            Text(
                                text = "• ${step.promptVi} ${step.mathExpression}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Answer Input Slot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, TargetUnknownAmber, RoundedCornerShape(18.dp))
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.userInput.ifEmpty { "Nhập đáp án..." },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.userInput.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) else TargetUnknownAmber
                )
            }
        }

        AcademicMathKeypad(
            onKeyPress = onKeyPress,
            onBackspace = onBackspace,
            onSubmit = onSubmit
        )
    }
}
