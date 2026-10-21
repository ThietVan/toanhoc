package com.example.toanhoc.ui.session

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.toanhoc.domain.model.SessionState
import com.example.toanhoc.ui.components.AcademicHeader
import com.example.toanhoc.ui.components.AcademicMathKeypad
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.SoftBlueBg
import com.example.toanhoc.ui.theme.SubtleBorder
import com.example.toanhoc.ui.theme.TargetUnknownAmber

@Composable
fun AutomaticityPhaseScreen(
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
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SubtleBorder),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SoftBlueBg)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("⚡", fontSize = 13.sp)
                        Text(
                            text = "Mục tiêu phản xạ: < 2500ms",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GivenDataBlue
                        )
                    }

                    Text(
                        text = problem?.statementVi ?: "",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(2.dp, TargetUnknownAmber, RoundedCornerShape(18.dp))
                            .padding(vertical = 14.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.userInput.ifEmpty { "Nhập..." },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.userInput.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) else TargetUnknownAmber
                        )
                    }
                }
            }
        }

        AcademicMathKeypad(
            onKeyPress = onKeyPress,
            onBackspace = onBackspace,
            onSubmit = onSubmit
        )
    }
}
