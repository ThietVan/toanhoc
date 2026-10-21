package com.example.toanhoc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.ui.theme.ErrorDiagnosticRed
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.SoftAmberBg
import com.example.toanhoc.ui.theme.SoftBlueBg
import com.example.toanhoc.ui.theme.SoftRedBg
import com.example.toanhoc.ui.theme.TargetUnknownAmber

/**
 * Pristine Dual-Layer Number Line Canvas Component.
 * Implements Singapore Math CPA / Part-Whole Model:
 * - Top Layer: Whole Equation Arc (0 -> Total)
 * - Bottom Layer: Part 1 Arc (0 -> a) and Part 2 Arc (a -> a+b) for Addition,
 *   or Backward Jump Arc (a <- b) for Subtraction.
 */
@Composable
fun NumberLineCanvas(
    modifier: Modifier = Modifier,
    minVal: Int = 0,
    maxVal: Int = 20,
    startPos: Int? = null,
    endPos: Int? = null,
    part1End: Int? = null,
    equationLabel: String = ""
) {
    val textMeasurer = rememberTextMeasurer()
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        val strokeWidth = 2.dp.toPx()
        val totalRange = (maxVal - minVal).coerceAtLeast(1)
        val paddingX = 20.dp.toPx()
        val lineY = size.height / 2f + 5.dp.toPx() // Axis line at center
        val availableWidth = size.width - (paddingX * 2)
        val stepX = availableWidth / totalRange

        // Main Axis Line
        drawLine(
            color = axisColor,
            start = Offset(paddingX, lineY),
            end = Offset(size.width - paddingX, lineY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        val targetResult = endPos ?: part1End

        // Draw Ticks & Number Labels
        for (i in minVal..maxVal) {
            val tickX = paddingX + (i - minVal) * stepX
            val isDestination = targetResult != null && i == targetResult
            val tickColor = if (isDestination) GivenDataBlue else axisColor

            drawLine(
                color = tickColor,
                start = Offset(tickX, lineY - (if (isDestination) 8.dp.toPx() else 5.dp.toPx())),
                end = Offset(tickX, lineY + (if (isDestination) 8.dp.toPx() else 5.dp.toPx())),
                strokeWidth = if (isDestination) strokeWidth * 1.5f else strokeWidth,
                cap = StrokeCap.Round
            )

            val stepDivisor = if (totalRange > 15) 2 else 1
            if (i % stepDivisor == 0 || i == minVal || i == maxVal || isDestination) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = i.toString(),
                    style = TextStyle(
                        color = if (isDestination) GivenDataBlue else axisColor,
                        fontSize = 11.sp,
                        fontWeight = if (isDestination) FontWeight.Bold else FontWeight.Medium
                    ),
                    topLeft = Offset(tickX - 6.dp.toPx(), lineY + 8.dp.toPx())
                )
            }
        }

        val start = startPos ?: 0
        val end = endPos ?: 0

        if (startPos != null && endPos != null) {
            val isAddition = end >= start

            if (isAddition) {
                // --- ADDITION DUAL-LAYER MODEL (PART + PART = WHOLE) ---
                val p1End = part1End ?: ((start + end) / 2).coerceAtLeast(start + 1)

                // 1. TOP ARC: WHOLE EQUATION (0 -> end)
                val x1 = paddingX + (start - minVal) * stepX
                val x2 = paddingX + (end - minVal) * stepX
                val midXTop = (x1 + x2) / 2f
                val topPeakY = lineY - 38.dp.toPx()

                val topPath = Path().apply {
                    moveTo(x1, lineY)
                    quadraticTo(midXTop, topPeakY - 8.dp.toPx(), x2, lineY)
                }
                drawPath(
                    path = topPath,
                    color = GivenDataBlue,
                    style = Stroke(width = strokeWidth * 1.3f, cap = StrokeCap.Round)
                )

                // Arrow head on top arc
                val headSize = 5.dp.toPx()
                drawLine(
                    color = GivenDataBlue,
                    start = Offset(x2, lineY),
                    end = Offset(x2 - headSize, lineY - headSize * 1.2f),
                    strokeWidth = strokeWidth * 1.2f,
                    cap = StrokeCap.Round
                )

                // Top Badge Pill
                val labelText = if (equationLabel.isNotEmpty()) equationLabel else "$start + ${end - start}"
                val topTextLayout = textMeasurer.measure(
                    text = labelText,
                    style = TextStyle(color = GivenDataBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                )
                val topBadgeWidth = topTextLayout.size.width + 12.dp.toPx()
                val topBadgeHeight = topTextLayout.size.height + 4.dp.toPx()
                drawRoundRect(
                    color = SoftBlueBg,
                    topLeft = Offset(midXTop - topBadgeWidth / 2f, topPeakY - 14.dp.toPx()),
                    size = Size(topBadgeWidth, topBadgeHeight),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = labelText,
                    style = TextStyle(color = GivenDataBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    topLeft = Offset(midXTop - topTextLayout.size.width / 2f, topPeakY - 12.dp.toPx())
                )

                // 2. BOTTOM LAYER ARC 1: PART 1 (start -> p1End)
                val xP1 = paddingX + (p1End - minVal) * stepX
                val midXBot1 = (x1 + xP1) / 2f
                val botPeakY1 = lineY + 28.dp.toPx()

                val botPath1 = Path().apply {
                    moveTo(x1, lineY)
                    quadraticTo(midXBot1, botPeakY1 + 6.dp.toPx(), xP1, lineY)
                }
                drawPath(botPath1, color = GivenDataBlue, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

                // Part 1 Badge
                val p1Text = "${p1End - start}"
                val p1Layout = textMeasurer.measure(p1Text, style = TextStyle(color = GivenDataBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                drawRoundRect(
                    color = SoftBlueBg,
                    topLeft = Offset(midXBot1 - 10.dp.toPx(), botPeakY1 - 2.dp.toPx()),
                    size = Size(20.dp.toPx(), 16.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = p1Text,
                    style = TextStyle(color = GivenDataBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    topLeft = Offset(midXBot1 - p1Layout.size.width / 2f, botPeakY1)
                )

                // 3. BOTTOM LAYER ARC 2: PART 2 (p1End -> end)
                val midXBot2 = (xP1 + x2) / 2f
                val botPeakY2 = lineY + 28.dp.toPx()

                val botPath2 = Path().apply {
                    moveTo(xP1, lineY)
                    quadraticTo(midXBot2, botPeakY2 + 6.dp.toPx(), x2, lineY)
                }
                drawPath(botPath2, color = TargetUnknownAmber, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

                // Part 2 Badge
                val p2Text = "+${end - p1End}"
                val p2Layout = textMeasurer.measure(p2Text, style = TextStyle(color = TargetUnknownAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                drawRoundRect(
                    color = SoftAmberBg,
                    topLeft = Offset(midXBot2 - 12.dp.toPx(), botPeakY2 - 2.dp.toPx()),
                    size = Size(24.dp.toPx(), 16.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = p2Text,
                    style = TextStyle(color = TargetUnknownAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    topLeft = Offset(midXBot2 - p2Layout.size.width / 2f, botPeakY2)
                )
            } else {
                // --- SUBTRACTION DUAL-LAYER MODEL (WHOLE - PART = REMAINING) ---
                // Top Arc: Whole (0 -> start)
                val x0 = paddingX + (0 - minVal) * stepX
                val xStart = paddingX + (start - minVal) * stepX
                val xEnd = paddingX + (end - minVal) * stepX

                val midXTop = (x0 + xStart) / 2f
                val topPeakY = lineY - 38.dp.toPx()

                val topPath = Path().apply {
                    moveTo(x0, lineY)
                    quadraticTo(midXTop, topPeakY - 8.dp.toPx(), xStart, lineY)
                }
                drawPath(topPath, color = GivenDataBlue, style = Stroke(width = strokeWidth * 1.3f, cap = StrokeCap.Round))

                // Top Badge (Whole)
                val wholeText = "$start"
                drawRoundRect(
                    color = SoftBlueBg,
                    topLeft = Offset(midXTop - 12.dp.toPx(), topPeakY - 14.dp.toPx()),
                    size = Size(24.dp.toPx(), 18.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = wholeText,
                    style = TextStyle(color = GivenDataBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    topLeft = Offset(midXTop - 5.dp.toPx(), topPeakY - 12.dp.toPx())
                )

                // Backward Jump Arc (start <- end)
                val midXBot = (xStart + xEnd) / 2f
                val botPeakY = lineY + 28.dp.toPx()

                val backPath = Path().apply {
                    moveTo(xStart, lineY)
                    quadraticTo(midXBot, botPeakY + 6.dp.toPx(), xEnd, lineY)
                }
                drawPath(backPath, color = ErrorDiagnosticRed, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

                // Left Arrowhead pointing to end
                val headSize = 5.dp.toPx()
                drawLine(
                    color = ErrorDiagnosticRed,
                    start = Offset(xEnd, lineY),
                    end = Offset(xEnd + headSize, lineY + headSize * 1.2f),
                    strokeWidth = strokeWidth * 1.2f,
                    cap = StrokeCap.Round
                )

                // Subtraction Badge (-b)
                val subText = "-${start - end}"
                drawRoundRect(
                    color = SoftRedBg,
                    topLeft = Offset(midXBot - 12.dp.toPx(), botPeakY - 2.dp.toPx()),
                    size = Size(24.dp.toPx(), 16.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = subText,
                    style = TextStyle(color = ErrorDiagnosticRed, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    topLeft = Offset(midXBot - 8.dp.toPx(), botPeakY)
                )
            }
        }
    }
}
