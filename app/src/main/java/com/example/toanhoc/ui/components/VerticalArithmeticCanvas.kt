package com.example.toanhoc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
 * Technical Vertical Arithmetic Grid Canvas.
 * Renders vertical multi-digit operation layout (Đặt tính rồi tính) on Compose Canvas.
 */
@Composable
fun VerticalArithmeticCanvas(
    operand1: String,
    operator: String,
    operand2: String,
    userAnswer: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp)
    ) {
        val strokeWidth = 2.dp.toPx()
        val fontSize = 28.sp
        val textStyle = TextStyle(
            color = TechnicalBlue,
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        val answerStyle = TextStyle(
            color = AmberOrange,
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        val rightX = size.width / 2 + 50.dp.toPx()
        val y1 = 10.dp.toPx()
        val y2 = y1 + 38.dp.toPx()
        val yLine = y2 + 42.dp.toPx()
        val yAns = yLine + 8.dp.toPx()

        // Operand 1
        drawText(
            textMeasurer = textMeasurer,
            text = operand1,
            style = textStyle,
            topLeft = Offset(rightX - (operand1.length * 18.dp.toPx()), y1)
        )

        // Operator
        drawText(
            textMeasurer = textMeasurer,
            text = operator,
            style = textStyle,
            topLeft = Offset(rightX - ((maxOf(operand1.length, operand2.length) + 2) * 18.dp.toPx()), y2)
        )

        // Operand 2
        drawText(
            textMeasurer = textMeasurer,
            text = operand2,
            style = textStyle,
            topLeft = Offset(rightX - (operand2.length * 18.dp.toPx()), y2)
        )

        // Horizontal line
        val lineLeft = rightX - ((maxOf(operand1.length, operand2.length) + 2) * 18.dp.toPx())
        drawLine(
            color = Color.DarkGray,
            start = Offset(lineLeft, yLine),
            end = Offset(rightX + 10.dp.toPx(), yLine),
            strokeWidth = strokeWidth
        )

        // User answer / placeholder
        val displayAns = if (userAnswer.isBlank()) "?" else userAnswer
        drawText(
            textMeasurer = textMeasurer,
            text = displayAns,
            style = answerStyle,
            topLeft = Offset(rightX - (displayAns.length * 18.dp.toPx()), yAns)
        )
    }
}
