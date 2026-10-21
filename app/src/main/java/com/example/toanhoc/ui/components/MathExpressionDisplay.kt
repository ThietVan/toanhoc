package com.example.toanhoc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.ui.theme.TargetUnknownAmber

/**
 * Math Expression Display rendering expressions, stacked fractions, and Amber highlighted input box '?'.
 */
@Composable
fun MathExpressionDisplay(
    expressionText: String,
    studentInput: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Expression rendering
        if (expressionText.contains("/")) {
            // Render 2-story stacked fraction display if expression contains a fraction
            val parts = expressionText.split(" ")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                parts.forEach { part ->
                    if (part.contains("/") && part.split("/").size == 2) {
                        val fracParts = part.split("/")
                        StackedFractionView(numerator = fracParts[0], denominator = fracParts[1])
                    } else {
                        Text(
                            text = part,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        } else {
            Text(
                text = expressionText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Student Input Box highlighted in Target Unknown Amber (#D97706)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .clip(RoundedCornerShape(6.dp))
                .background(TargetUnknownAmber.copy(alpha = 0.1f))
                .border(2.dp, TargetUnknownAmber, RoundedCornerShape(6.dp))
                .padding(vertical = 10.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            val displayText = studentInput.ifEmpty { "?" }
            Text(
                text = displayText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TargetUnknownAmber
            )
        }
    }
}

@Composable
fun StackedFractionView(
    numerator: String,
    denominator: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = numerator,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .width(28.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.onSurface)
        )

        Text(
            text = denominator,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
