package com.example.toanhoc.ui.design

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Chữ tự co để vừa chiều rộng và số dòng cho phép (Crash-Proof).
 *
 * - Tìm cỡ chữ lớn nhất trong [minFontSize]..[maxFontSize] mà văn bản còn vừa [maxLines] dòng.
 * - An toàn tuyệt đối: bọc try-catch và kiểm tra giới hạn kích thước maxWidth > 0 trước khi đo.
 */
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    style: TextStyle = TextStyle.Default,
    maxLines: Int = 1,
    textAlign: TextAlign = TextAlign.Unspecified,
    lineHeightMultiplier: Float? = null
) {
    if (text.isEmpty()) return

    val measurer = rememberTextMeasurer()
    val maxSp = maxFontSize.value.coerceAtLeast(8f)
    val minSp = minOf(minFontSize.value.coerceAtLeast(8f), maxSp)

    BoxWithConstraints(modifier = modifier) {
        val widthPx = if (this@BoxWithConstraints.constraints.hasBoundedWidth && this@BoxWithConstraints.constraints.maxWidth > 0) {
            this@BoxWithConstraints.constraints.maxWidth
        } else -1

        val fittedSp = remember(text, widthPx, maxSp, minSp, maxLines, style, lineHeightMultiplier, measurer) {
            if (widthPx <= 0) maxSp
            else try {
                fitFontSize(text, style, maxSp, minSp, maxLines, widthPx, lineHeightMultiplier, measurer)
            } catch (_: Throwable) {
                maxSp
            }
        }

        val fill = textAlign != TextAlign.Unspecified && textAlign != TextAlign.Start
        Text(
            text = text,
            modifier = if (fill) Modifier.fillMaxWidth() else Modifier,
            style = style.copy(
                fontSize = fittedSp.sp,
                lineHeight = lineHeightMultiplier?.let { (fittedSp * it).sp } ?: TextUnit.Unspecified,
                textAlign = textAlign
            ),
            maxLines = maxLines,
            softWrap = maxLines > 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun fitFontSize(
    text: String,
    style: TextStyle,
    maxSp: Float,
    minSp: Float,
    maxLines: Int,
    widthPx: Int,
    lineHeightMultiplier: Float?,
    measurer: TextMeasurer
): Float {
    if (widthPx <= 0) return maxSp

    fun fits(sp: Float): Boolean {
        return try {
            val s = style.copy(
                fontSize = sp.sp,
                lineHeight = lineHeightMultiplier?.let { (sp * it).sp } ?: TextUnit.Unspecified
            )
            val r = measurer.measure(
                text = text,
                style = s,
                softWrap = maxLines > 1,
                maxLines = maxLines,
                constraints = Constraints(maxWidth = widthPx.coerceAtLeast(1))
            )
            !r.didOverflowWidth && !r.didOverflowHeight && r.lineCount <= maxLines
        } catch (_: Throwable) {
            true
        }
    }

    if (fits(maxSp)) return maxSp
    if (!fits(minSp)) return minSp

    var lo = minSp
    var hi = maxSp
    repeat(8) {
        val mid = (lo + hi) / 2f
        if (fits(mid)) lo = mid else hi = mid
    }
    return (kotlin.math.floor(lo * 2f) / 2f).coerceAtLeast(minSp)
}
