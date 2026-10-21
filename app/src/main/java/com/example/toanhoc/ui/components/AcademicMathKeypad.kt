package com.example.toanhoc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.TargetUnknownAmber

/**
 * Swiss Academic Minimalist Keypad with Helper Toolbar (`[ ↺ Bước trước ]` & 2-level `[ ? Chưa biết làm / Bỏ qua ]`).
 */
@Composable
fun AcademicMathKeypad(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    onStepBack: () -> Unit = {},
    onDontKnowOrSkip: () -> Unit = {},
    hintLevel: Int = 0,
    showComparisons: Boolean = false,
    showFractionKey: Boolean = true
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = LocalHapticFeedback.current

    val handleTap = { action: () -> Unit ->
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        action()
    }

    val handleHelperTap = { action: () -> Unit ->
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
        action()
    }

    val skipLabel = when (hintLevel) {
        1 -> "⏭ Xem lời giải & Bỏ qua →"
        2 -> "⏭ Qua bài tiếp theo →"
        else -> "? Chưa biết làm / Bỏ qua"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // --- Helper Toolbar directly above numeric keypad ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Undo / Step Back
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .clickable { handleHelperTap { onStepBack() } },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "↺ Bước trước",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Don't Know / Guided Skip
            Box(
                modifier = Modifier
                    .weight(1.4f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TargetUnknownAmber.copy(alpha = 0.12f))
                    .border(1.dp, TargetUnknownAmber, RoundedCornerShape(4.dp))
                    .clickable { handleHelperTap { onDontKnowOrSkip() } },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = skipLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TargetUnknownAmber
                )
            }
        }

        if (showComparisons) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                KeypadKey(text = "<", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress("<") } })
                KeypadKey(text = "=", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress("=") } })
                KeypadKey(text = ">", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress(">") } })
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadKey(text = "1", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress("1") } })
            KeypadKey(text = "2", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress("2") } })
            KeypadKey(text = "3", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress("3") } })
            if (showFractionKey) {
                KeypadKey(text = "a/b", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress("/") } })
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadKey(text = "4", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress("4") } })
            KeypadKey(text = "5", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress("5") } })
            KeypadKey(text = "6", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress("6") } })
            KeypadKey(text = "dư (R)", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress(" R ") } })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadKey(text = "7", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress("7") } })
            KeypadKey(text = "8", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress("8") } })
            KeypadKey(text = "9", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress("9") } })
            KeypadKey(text = ".", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress(".") } })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadKey(text = "⌫", modifier = Modifier.weight(1f), onClick = { handleTap { onBackspace() } })
            KeypadKey(text = "0", modifier = Modifier.weight(1f), onClick = { handleTap { onKeyPress("0") } })
            KeypadKey(
                text = if (hintLevel == 2) "ĐÃ HIỂU ↵" else "XÁC NHẬN ↵",
                modifier = Modifier.weight(2f),
                onClick = { handleTap { onSubmit() } },
                containerColor = GivenDataBlue,
                contentColor = Color.White
            )
        }
    }
}

@Composable
private fun KeypadKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(containerColor)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = contentColor
        )
    }
}
