package com.example.toanhoc.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.SoftAmberBg
import com.example.toanhoc.ui.theme.SoftBlueBg
import com.example.toanhoc.ui.theme.TargetUnknownAmber
import java.util.Locale

/**
 * Top Control Bar displaying Upward Elapsed Stopwatch Timer (00:00 -> 00:01 -> ...) and controls.
 */
@Composable
fun SessionControlTopBar(
    elapsedSeconds: Int,
    onSaveAndExit: () -> Unit,
    onOpenTheory: () -> Unit,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val min = elapsedSeconds / 60
    val sec = elapsedSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", min, sec)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Save & Exit Pill Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.background)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
                .clickable {
                    com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                    onSaveAndExit()
                }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "← Lưu & Thoát",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Center: Theory Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(SoftBlueBg)
                .border(1.dp, GivenDataBlue.copy(alpha = 0.4f), RoundedCornerShape(50))
                .clickable {
                    com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                    onOpenTheory()
                }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Bài mẫu",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GivenDataBlue
            )
        }

        // Right: Upward Timer & Pause
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SoftAmberBg)
                    .border(1.dp, TargetUnknownAmber.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("⏱️", fontSize = 11.sp)
                Text(
                    text = timeFormatted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TargetUnknownAmber
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SoftAmberBg)
                    .border(1.dp, TargetUnknownAmber.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .clickable {
                        com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                        onTogglePause()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⏸",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TargetUnknownAmber
                )
            }
        }
    }
}
