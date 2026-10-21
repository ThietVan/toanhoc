package com.example.toanhoc.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.family.AvatarShop
import com.example.toanhoc.data.family.ChildProfile
import com.example.toanhoc.ui.theme.NunitoFontFamily

/* Bảng màu chung cho khu gia đình – cùng tông với trang chủ */
internal object FamilyTone {
    val Ink = Color(0xFF111827)
    val Muted = Color(0xFF6B7280)
    val Line = Color(0xFFE4E7EC)
    val Paper = Color(0xFFF7F8FA)
    val Primary = Color(0xFF2F55D4)
    val PrimarySoft = Color(0xFFEEF2FC)
    val Coin = Color(0xFFC2620A)
    val CoinSoft = Color(0xFFFBF3EA)
    val Green = Color(0xFF1E7F4F)
    val GreenSoft = Color(0xFFEAF4EE)
    val Red = Color(0xFFC53030)
    val RedSoft = Color(0xFFFBEDED)
    val Parent = Color(0xFF1B2440)
}

@Composable
internal fun FText(
    text: String,
    size: TextUnit = 15.sp,
    weight: FontWeight = FontWeight.Normal,
    color: Color = FamilyTone.Ink,
    modifier: Modifier = Modifier,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = TextUnit.Unspecified
) = Text(
    text, modifier = modifier, fontFamily = NunitoFontFamily, fontSize = size, fontWeight = weight,
    color = color, textAlign = align, maxLines = maxLines, lineHeight = lineHeight
)

@Composable
internal fun CoinPill(coins: Int, modifier: Modifier = Modifier, big: Boolean = false) {
    Row(
        modifier.clip(CircleShape).background(FamilyTone.CoinSoft).padding(start = if (big) 8.dp else 6.dp, end = if (big) 14.dp else 10.dp, top = if (big) 6.dp else 4.dp, bottom = if (big) 6.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CoinIcon(if (big) 26.dp else 18.dp)
        FText("$coins", size = if (big) 19.sp else 14.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Coin)
        FText("xu", size = if (big) 14.sp else 12.sp, weight = FontWeight.Bold, color = FamilyTone.Coin)
    }
}

@Composable
internal fun FCard(modifier: Modifier = Modifier, padding: Dp = 16.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
            .border(1.dp, FamilyTone.Line, RoundedCornerShape(18.dp)).padding(padding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
internal fun FButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = FamilyTone.Primary,
    outlined: Boolean = false,
    small: Boolean = false,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(if (small) 10.dp else 14.dp)
    Box(
        modifier
            .heightIn(min = if (small) 40.dp else 52.dp)
            .clip(shape)
            .background(if (outlined) Color.White else if (enabled) color else FamilyTone.Line)
            .border(1.dp, if (outlined) (if (enabled) color else FamilyTone.Line) else Color.Transparent, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (small) 12.dp else 18.dp),
        contentAlignment = Alignment.Center
    ) {
        FText(
            text, size = if (small) 14.sp else 16.sp, weight = FontWeight.ExtraBold,
            color = if (outlined) (if (enabled) color else FamilyTone.Muted) else if (enabled) Color.White else FamilyTone.Muted,
            align = TextAlign.Center
        )
    }
}

@Composable
internal fun BackRow(title: String, onBack: () -> Unit, modifier: Modifier = Modifier, dark: Boolean = false, trailing: @Composable () -> Unit = {}) {
    Row(modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onBack), contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Quay lại", tint = if (dark) Color.White else FamilyTone.Ink)
        }
        FText(title, size = 19.sp, weight = FontWeight.ExtraBold, color = if (dark) Color.White else FamilyTone.Ink, modifier = Modifier.weight(1f), maxLines = 1)
        trailing()
    }
}

/**
 * Ô nhập mã 4 số với bàn phím số riêng (không bật bàn phím hệ thống).
 * [resetKey] đổi → xóa các số đã nhập.
 */
@Composable
internal fun PinEntry(
    title: String,
    subtitle: String?,
    error: String?,
    onComplete: (String) -> Unit,
    resetKey: Any,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secret: Boolean = true,
    leftAction: Pair<String, () -> Unit>? = null
) {
    var digits by remember(resetKey) { mutableStateOf("") }
    LaunchedEffect(digits) {
        if (digits.length == 4) {
            val code = digits
            onComplete(code)
            digits = ""
        }
    }
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FText(title, size = 22.sp, weight = FontWeight.ExtraBold, align = TextAlign.Center)
        if (subtitle != null) FText(subtitle, size = 14.sp, color = FamilyTone.Muted, align = TextAlign.Center, lineHeight = 20.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            repeat(4) { i ->
                val filled = i < digits.length
                Box(
                    Modifier.size(if (secret) 18.dp else 40.dp).clip(if (secret) CircleShape else RoundedCornerShape(10.dp))
                        .background(if (secret && filled) FamilyTone.Ink else Color.White)
                        .border(2.dp, if (filled) FamilyTone.Ink else FamilyTone.Line, if (secret) CircleShape else RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!secret && filled) FText(digits[i].toString(), size = 20.sp, weight = FontWeight.ExtraBold)
                }
            }
        }
        FText(error ?: " ", size = 14.sp, weight = FontWeight.Bold, color = FamilyTone.Red, align = TextAlign.Center)
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "L", "0", "<")
        keys.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { k ->
                    when (k) {
                        "L" -> Box(Modifier.size(width = 84.dp, height = 64.dp).clip(RoundedCornerShape(16.dp))
                            .then(if (leftAction != null) Modifier.clickable(role = Role.Button, onClick = leftAction.second) else Modifier),
                            contentAlignment = Alignment.Center
                        ) { if (leftAction != null) FText(leftAction.first, size = 13.sp, weight = FontWeight.Bold, color = FamilyTone.Primary, align = TextAlign.Center) }
                        "<" -> Box(
                            Modifier.size(width = 84.dp, height = 64.dp).clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = enabled && digits.isNotEmpty(), role = Role.Button) { digits = digits.dropLast(1) },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Xóa", tint = FamilyTone.Ink) }
                        else -> Box(
                            Modifier.size(width = 84.dp, height = 64.dp).clip(RoundedCornerShape(16.dp)).background(Color.White)
                                .border(1.dp, FamilyTone.Line, RoundedCornerShape(16.dp))
                                .clickable(enabled = enabled && digits.length < 4, role = Role.Button) { digits += k },
                            contentAlignment = Alignment.Center
                        ) { FText(k, size = 26.sp, weight = FontWeight.Bold, color = if (enabled) FamilyTone.Ink else FamilyTone.Line) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        FText(text, size = 18.sp, weight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        if (trailing != null) FText(trailing, size = 13.sp, weight = FontWeight.SemiBold, color = FamilyTone.Muted)
    }
}

@Composable
internal fun Chip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(CircleShape).background(if (selected) FamilyTone.Ink else Color.White)
            .border(1.dp, if (selected) FamilyTone.Ink else FamilyTone.Line, CircleShape)
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) { FText(text, size = 14.sp, weight = FontWeight.Bold, color = if (selected) Color.White else FamilyTone.Ink, maxLines = 1) }
}

@Composable
internal fun ProgressBar(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().heightIn(min = 8.dp).clip(CircleShape).background(Color(0xFFEEF0F3))) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).heightIn(min = 8.dp).clip(CircleShape).background(color))
    }
}

@Composable
internal fun Gap(h: Dp) = Spacer(Modifier.size(h))

internal fun Modifier.square() = this.aspectRatio(1f)
