package com.example.toanhoc.ui.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.ui.theme.NunitoFontFamily

/* ------------------------------------------------------------------------------------------
 * Hệ thống thiết kế giấy kẻ ô li (Paper & Ink Design System)
 *  - Dãy Fibonacci cho khoảng cách: S1(5dp), S2(8dp), S3(13dp), S4(21dp), S5(34dp), S6(55dp)
 *  - Tỉ lệ vàng: PHI = 1.618f
 *  - Bảng màu giấy kẻ: Mực xanh Ink, Giấy Paper, Đường kẻ Hairline, Ô li GridDot
 * ---------------------------------------------------------------------------------------- */

object Fib {
    val S1 = 5.dp
    val S2 = 8.dp
    val S3 = 13.dp
    val S4 = 21.dp
    val S5 = 34.dp
    val S6 = 55.dp
}

object PaperColors {
    val Ink = Color(0xFF111827)
    val InkSoft = Color(0xFF4B5563)
    val Paper = Color(0xFFF7F8FA)
    val GridDot = Color(0xFFE4E7EC)
    val Hairline = Color(0xFFE4E7EC)
}

private const val CHILD_SCALE = 0.8f
private const val MIN_CHILD_SP = 11f

private val BAR_THICKNESS = 1.5.dp
private val BAR_GAP = 1.dp
private val BAR_INSET = 2.dp
private val SIDE_PADDING = 4.dp
private val LINE_VERTICAL_PADDING = 4.dp

private fun formatMathStatementLines(raw: String): String {
    if (raw.contains("\n")) return raw
    val i = raw.indexOf(":")
    if (i > 0 && i < raw.length - 1) {
        val instruction = raw.substring(0, i + 1).trim()
        val expr = raw.substring(i + 1).trim()
        if (expr.isNotEmpty()) {
            val formattedExpr = if (!expr.endsWith("=?") && !expr.endsWith("= ?") && !expr.endsWith(":")) "$expr = ?" else expr
            return "$instruction\n$formattedExpr"
        }
    }
    val match = Regex("^(Đặt tính rồi tính [^0-9:]+)\\s*(\\d+.*)$").find(raw)
    if (match != null) {
        val instruction = match.groupValues[1].trim()
        val expr = match.groupValues[2].trim().removeSuffix(":")
        val formattedExpr = if (!expr.endsWith("=?") && !expr.endsWith("= ?")) "$expr = ?" else expr
        val cleanInstruction = if (!instruction.endsWith(":")) "$instruction:" else instruction
        return "$cleanInstruction\n$formattedExpr"
    }
    return raw
}

/**
 * Hiển thị một câu/biểu thức có thể chứa phân số, tự động phân tách đề bài và biểu thức toán học.
 */
@Composable
fun FormattedMathStatement(
    statement: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    color: Color = PaperColors.Ink,
    fontWeight: FontWeight = FontWeight.Bold,
    fontFamily: FontFamily = NunitoFontFamily,
    textAlign: TextAlign = TextAlign.Start
) {
    val formattedText = remember(statement) { formatMathStatementLines(statement) }
    val nodes = remember(formattedText) { MathParser.parse(formattedText) }
    MathContent(
        nodes = nodes,
        modifier = modifier,
        fontSizeSp = fontSize.value,
        color = color,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        textAlign = textAlign,
        lineHeightMultiplier = 1.618f
    )
}

/**
 * Một phân số đứng độc lập. [fontSize] là cỡ chữ của tử và mẫu.
 */
@Composable
fun StackedFractionView(
    numerator: String,
    denominator: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    color: Color = PaperColors.Ink,
    fontWeight: FontWeight = FontWeight.Bold,
    fontFamily: FontFamily = NunitoFontFamily
) {
    val nodes = remember(numerator, denominator) {
        listOf<MathNode>(
            MathNode.Fraction(
                numerator = MathParser.parse(numerator.trim()),
                denominator = MathParser.parse(denominator.trim())
            )
        )
    }
    MathContent(
        nodes = nodes,
        modifier = modifier,
        fontSizeSp = fontSize.value / CHILD_SCALE,
        color = color,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        textAlign = TextAlign.Unspecified,
        lineHeightMultiplier = null
    )
}

/* ------------------------------ Dựng và đo ------------------------------ */

private class MathEnv(
    val measurer: TextMeasurer,
    val density: Density,
    val color: Color,
    val fontWeight: FontWeight,
    val fontFamily: FontFamily
)

private class BlockLayout(
    val node: MathNode,
    val widthPx: Float,
    val heightPx: Float,
    val ascentPx: Float,
    val children: List<BlockLayout> = emptyList(),
    val textResult: TextLayoutResult? = null
)

@Composable
private fun MathContent(
    nodes: List<MathNode>,
    modifier: Modifier = Modifier,
    fontSizeSp: Float,
    color: Color,
    fontWeight: FontWeight,
    fontFamily: FontFamily,
    textAlign: TextAlign,
    lineHeightMultiplier: Float?
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val env = remember(measurer, density, color, fontWeight, fontFamily) {
        MathEnv(measurer, density, color, fontWeight, fontFamily)
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val maxWidthPx = if (this@BoxWithConstraints.constraints.hasBoundedWidth && this@BoxWithConstraints.constraints.maxWidth > 0) {
            with(density) { this@BoxWithConstraints.maxWidth.toPx() }
        } else -1f

        val lines = remember(nodes, env, fontSizeSp, maxWidthPx) {
            measureDocument(nodes, env, fontSizeSp, maxWidthPx)
        }

        val totalHeightPx = lines.sumOf { it.heightPx.toDouble() }.toFloat()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = LINE_VERTICAL_PADDING)
        ) {
            Canvas(
                modifier = Modifier.fillMaxWidth()
            ) {
                var currentY = 0f
                lines.forEach { line ->
                    val startX = when (textAlign) {
                        TextAlign.Center -> (size.width - line.widthPx) / 2f
                        TextAlign.End -> size.width - line.widthPx
                        else -> 0f
                    }
                    drawLineLayout(line, env, startX, currentY)
                    currentY += line.heightPx
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = with(density) { totalHeightPx.toDp() })
            )
        }
    }
}

private class LineLayout(
    val blocks: List<BlockLayout>,
    val widthPx: Float,
    val heightPx: Float,
    val baselinePx: Float
)

private fun measureDocument(
    nodes: List<MathNode>,
    env: MathEnv,
    fontSizeSp: Float,
    maxWidthPx: Float
): List<LineLayout> {
    val rawLines = mutableListOf<MutableList<MathNode>>()
    var currentLine = mutableListOf<MathNode>()
    var currentLineWidth = 0f

    fun startNewLine() {
        if (currentLine.isNotEmpty()) {
            rawLines.add(currentLine)
            currentLine = mutableListOf()
            currentLineWidth = 0f
        }
    }

    nodes.forEach { node ->
        when (node) {
            is MathNode.Plain -> {
                val paragraphs = node.text.split("\n")
                paragraphs.forEachIndexed { pIdx, paragraph ->
                    if (pIdx > 0) {
                        startNewLine()
                    }
                    if (paragraph.isNotEmpty()) {
                        val words = paragraph.split(Regex("(?<=\\s)|(?=\\s)"))
                        words.forEach { word ->
                            if (word.isNotEmpty()) {
                                val block = measureNode(MathNode.Plain(word), env, fontSizeSp)
                                if (maxWidthPx > 0f && currentLineWidth + block.widthPx > maxWidthPx && currentLine.isNotEmpty() && word.trim().isNotEmpty()) {
                                    startNewLine()
                                }
                                currentLine.add(MathNode.Plain(word))
                                currentLineWidth += block.widthPx
                            }
                        }
                    }
                }
            }
            is MathNode.Fraction -> {
                val block = measureNode(node, env, fontSizeSp)
                if (maxWidthPx > 0f && currentLineWidth + block.widthPx > maxWidthPx && currentLine.isNotEmpty()) {
                    startNewLine()
                }
                currentLine.add(node)
                currentLineWidth += block.widthPx
            }
        }
    }
    if (currentLine.isNotEmpty()) {
        rawLines.add(currentLine)
    }

    return rawLines.map { lineNodes ->
        val blocks = lineNodes.map { measureNode(it, env, fontSizeSp) }
        val maxAscent = blocks.maxOfOrNull { it.ascentPx } ?: (fontSizeSp * 0.8f * env.density.density)
        val maxDescent = blocks.maxOfOrNull { it.heightPx - it.ascentPx } ?: (fontSizeSp * 0.2f * env.density.density)
        val height = maxAscent + maxDescent
        val width = blocks.sumOf { it.widthPx.toDouble() }.toFloat()
        LineLayout(blocks, width, height, maxAscent)
    }
}

private fun measureNode(
    node: MathNode,
    env: MathEnv,
    fontSizeSp: Float
): BlockLayout {
    return try {
        when (node) {
            is MathNode.Plain -> {
                val textStyle = TextStyle(
                    fontSize = fontSizeSp.sp,
                    fontWeight = env.fontWeight,
                    fontFamily = env.fontFamily,
                    color = env.color
                )
                val textLayout = env.measurer.measure(
                    text = AnnotatedString(node.text),
                    style = textStyle
                )
                BlockLayout(
                    node = node,
                    widthPx = textLayout.size.width.toFloat(),
                    heightPx = textLayout.size.height.toFloat(),
                    ascentPx = textLayout.firstBaseline,
                    textResult = textLayout
                )
            }
            is MathNode.Fraction -> {
                val childFontSize = (fontSizeSp * CHILD_SCALE).coerceAtLeast(MIN_CHILD_SP)
                val numBlocks = node.numerator.map { measureNode(it, env, childFontSize) }
                val denBlocks = node.denominator.map { measureNode(it, env, childFontSize) }

                val sidePx = with(env.density) { SIDE_PADDING.toPx() }
                val gapPx = with(env.density) { BAR_GAP.toPx() }
                val thicknessPx = with(env.density) { BAR_THICKNESS.toPx() }

                val numWidth = numBlocks.sumOf { it.widthPx.toDouble() }.toFloat()
                val numHeight = numBlocks.maxOfOrNull { it.heightPx } ?: (childFontSize * env.density.density)

                val denWidth = denBlocks.sumOf { it.widthPx.toDouble() }.toFloat()
                val denHeight = denBlocks.maxOfOrNull { it.heightPx } ?: (childFontSize * env.density.density)

                val maxContentWidth = maxOf(numWidth, denWidth)
                val totalWidth = maxContentWidth + (sidePx * 2)
                val totalHeight = numHeight + gapPx + thicknessPx + gapPx + denHeight
                val baseline = numHeight + gapPx + (thicknessPx / 2f)

                BlockLayout(
                    node = node,
                    widthPx = totalWidth,
                    heightPx = totalHeight,
                    ascentPx = baseline,
                    children = numBlocks + denBlocks
                )
            }
        }
    } catch (_: Throwable) {
        BlockLayout(node, 0f, 0f, 0f)
    }
}

private fun DrawScope.drawLineLayout(
    line: LineLayout,
    env: MathEnv,
    startX: Float,
    startY: Float
) {
    var x = startX
    line.blocks.forEach { block ->
        val y = startY + (line.baselinePx - block.ascentPx)
        drawBlockLayout(block, env, x, y)
        x += block.widthPx
    }
}

private fun DrawScope.drawBlockLayout(
    block: BlockLayout,
    env: MathEnv,
    x: Float,
    y: Float
) {
    when (val node = block.node) {
        is MathNode.Plain -> {
            if (block.textResult != null) {
                drawText(
                    textLayoutResult = block.textResult,
                    topLeft = Offset(x, y)
                )
            }
        }
        is MathNode.Fraction -> {
            val sidePx = with(env.density) { SIDE_PADDING.toPx() }
            val gapPx = with(env.density) { BAR_GAP.toPx() }
            val thicknessPx = with(env.density) { BAR_THICKNESS.toPx() }

            val numBlocks = block.children.take(node.numerator.size)
            val denBlocks = block.children.drop(node.numerator.size)

            val numWidth = numBlocks.sumOf { it.widthPx.toDouble() }.toFloat()
            val numHeight = numBlocks.maxOfOrNull { it.heightPx } ?: 0f

            val denWidth = denBlocks.sumOf { it.widthPx.toDouble() }.toFloat()

            // Draw Numerator centered
            val numX = x + sidePx + ((block.widthPx - 2 * sidePx - numWidth) / 2f)
            var currentNumX = numX
            numBlocks.forEach { child ->
                drawBlockLayout(child, env, currentNumX, y)
                currentNumX += child.widthPx
            }

            // Draw Horizontal Fraction Bar
            val barY = y + numHeight + gapPx
            val barInset = with(env.density) { BAR_INSET.toPx() }
            drawLine(
                color = env.color,
                start = Offset(x + barInset, barY),
                end = Offset(x + block.widthPx - barInset, barY),
                strokeWidth = thicknessPx
            )

            // Draw Denominator centered
            val denY = barY + thicknessPx + gapPx
            val denX = x + sidePx + ((block.widthPx - 2 * sidePx - denWidth) / 2f)
            var currentDenX = denX
            denBlocks.forEach { child ->
                drawBlockLayout(child, env, currentDenX, denY)
                currentDenX += child.widthPx
            }
        }
    }
}

/* ------------------------------ Phân số trong chữ thường ------------------------------ */

/** true nếu chuỗi có ít nhất một phân số (3/4, □/5, (8 + 3)/12…) theo đúng quy tắc của MathParser. */
fun hasFraction(text: String): Boolean = text.contains('/') && MathParser.parse(text).any { it is MathNode.Fraction }

/** Một dòng có phân số (tử trên, gạch ngang, mẫu dưới), không xuống dòng, rộng vừa nội dung – dùng trong ô đáp án, viên thuốc. */
@Composable
fun InlineMath(text: String, fontSize: TextUnit, color: Color, fontWeight: FontWeight = FontWeight.Bold, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val env = remember(measurer, density, color, fontWeight) { MathEnv(measurer, density, color, fontWeight, NunitoFontFamily) }
    val nodes = remember(text) { MathParser.parse(text) }
    val lines = remember(nodes, env, fontSize) { measureDocument(nodes, env, fontSize.value, -1f) }
    val w = lines.maxOfOrNull { it.widthPx } ?: 0f
    val h = lines.sumOf { it.heightPx.toDouble() }.toFloat()
    with(density) {
        Canvas(modifier.size(w.toDp(), h.toDp())) {
            var y = 0f
            lines.forEach { drawLineLayout(it, env, 0f, y); y += it.heightPx }
        }
    }
}

/** Đoạn văn có phân số: tự xuống dòng theo bề rộng, phân số hiển thị tử trên – mẫu dưới. */
@Composable
fun MathParagraph(text: String, fontSize: TextUnit, color: Color, fontWeight: FontWeight = FontWeight.Bold, textAlign: TextAlign = TextAlign.Start, modifier: Modifier = Modifier) {
    val nodes = remember(text) { MathParser.parse(text) }
    MathContent(nodes = nodes, modifier = modifier, fontSizeSp = fontSize.value, color = color, fontWeight = fontWeight,
        fontFamily = NunitoFontFamily, textAlign = textAlign, lineHeightMultiplier = 1.45f)
}
