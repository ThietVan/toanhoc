package com.example.toanhoc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.ui.theme.AmberOrange
import com.example.toanhoc.ui.theme.TechnicalBlue

/**
 * Technical Singapore Bar Model drawn 100% on Compose Canvas.
 * Draws comparison/part-whole bars with technical stroke (1.5dp-2dp).
 */
@Composable
fun SingaporeBarModelCanvas(
    part1Label: String,
    part1ValueRatio: Float,
    part2Label: String,
    part2ValueRatio: Float,
    modifier: Modifier = Modifier,
    totalLabel: String? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val strokeWidthPx = 2.dp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp)
    ) {
        val strokeWidth = strokeWidthPx.toPx()
        val availableWidth = size.width - 120.dp.toPx()
        val barHeight = 36.dp.toPx()
        val maxRatio = maxOf(part1ValueRatio, part2ValueRatio, 1f)

        val bar1Width = (part1ValueRatio / maxRatio) * availableWidth
        val bar2Width = (part2ValueRatio / maxRatio) * availableWidth

        val startX = 100.dp.toPx()
        val bar1Y = 20.dp.toPx()
        val bar2Y = bar1Y + barHeight + 20.dp.toPx()

        // Draw Part 1 Label & Bar (Technical Blue)
        drawText(
            textMeasurer = textMeasurer,
            text = part1Label,
            style = TextStyle(
                color = TechnicalBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            topLeft = Offset(10.dp.toPx(), bar1Y + 8.dp.toPx())
        )

        drawRect(
            color = TechnicalBlue.copy(alpha = 0.15f),
            topLeft = Offset(startX, bar1Y),
            size = Size(bar1Width, barHeight)
        )
        drawRect(
            color = TechnicalBlue,
            topLeft = Offset(startX, bar1Y),
            size = Size(bar1Width, barHeight),
            style = Stroke(width = strokeWidth)
        )

        // Draw Part 2 Label & Bar (Amber Orange)
        drawText(
            textMeasurer = textMeasurer,
            text = part2Label,
            style = TextStyle(
                color = AmberOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            topLeft = Offset(10.dp.toPx(), bar2Y + 8.dp.toPx())
        )

        drawRect(
            color = AmberOrange.copy(alpha = 0.15f),
            topLeft = Offset(startX, bar2Y),
            size = Size(bar2Width, barHeight)
        )
        drawRect(
            color = AmberOrange,
            topLeft = Offset(startX, bar2Y),
            size = Size(bar2Width, barHeight),
            style = Stroke(width = strokeWidth)
        )

        // Draw Total Bracket if specified
        totalLabel?.let { label ->
            val bracketX = startX + maxOf(bar1Width, bar2Width) + 12.dp.toPx()
            val bracketTop = bar1Y
            val bracketBottom = bar2Y + barHeight
            val bracketMidY = (bracketTop + bracketBottom) / 2

            // Vertical line
            drawLine(
                color = Color.Gray,
                start = Offset(bracketX, bracketTop),
                end = Offset(bracketX, bracketBottom),
                strokeWidth = strokeWidth
            )
            // Top tick
            drawLine(
                color = Color.Gray,
                start = Offset(bracketX - 6.dp.toPx(), bracketTop),
                end = Offset(bracketX, bracketTop),
                strokeWidth = strokeWidth
            )
            // Bottom tick
            drawLine(
                color = Color.Gray,
                start = Offset(bracketX - 6.dp.toPx(), bracketBottom),
                end = Offset(bracketX, bracketBottom),
                strokeWidth = strokeWidth
            )

            drawText(
                textMeasurer = textMeasurer,
                text = "Tổng: $label",
                style = TextStyle(
                    color = Color.DarkGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                ),
                topLeft = Offset(bracketX + 8.dp.toPx(), bracketMidY - 10.dp.toPx())
            )
        }
    }
}
