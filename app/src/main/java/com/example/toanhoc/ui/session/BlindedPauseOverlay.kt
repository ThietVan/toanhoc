package com.example.toanhoc.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.TargetUnknownAmber
import java.util.Locale

/**
 * Full-screen Blinded Pause Overlay covering problem statement completely during pause.
 */
@Composable
fun BlindedPauseOverlay(
    elapsedSeconds: Int,
    onResumeSession: () -> Unit,
    onSaveAndExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val min = elapsedSeconds / 60
    val sec = elapsedSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", min, sec)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable { /* Block clicks to problem content beneath */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ĐÃ TẠM DỪNG PHIÊN HỌC",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TargetUnknownAmber
            )

            Text(
                text = "Thời gian làm bài: $timeFormatted",
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )

            val context = androidx.compose.ui.platform.LocalContext.current

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(GivenDataBlue)
                    .clickable {
                        com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                        onResumeSession()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶ TIẾP TỤC PHIÊN HỌC",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                    .clickable {
                        com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                        onSaveAndExit()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "← LƯU TIẾN ĐỘ & VỀ TRANG CHÍNH",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
