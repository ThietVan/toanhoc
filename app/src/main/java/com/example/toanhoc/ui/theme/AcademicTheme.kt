package com.example.toanhoc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = GivenDataBlue,
    onPrimary = Color.White,
    primaryContainer = SoftBlueBg,
    onPrimaryContainer = Color(0xFF1A2F7A),
    secondary = TargetUnknownAmber,
    onSecondary = Color.White,
    secondaryContainer = SoftAmberBg,
    onSecondaryContainer = Color(0xFF6B3504),
    tertiary = MasteryVerifiedGreen,
    onTertiary = Color.White,
    tertiaryContainer = SoftGreenBg,
    onTertiaryContainer = Color(0xFF0F4429),
    error = ErrorDiagnosticRed,
    onError = Color.White,
    errorContainer = SoftRedBg,
    onErrorContainer = Color(0xFF6B1414),
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    surfaceTint = Color.Transparent, // tắt ánh màu khi có elevation → bề mặt trắng sạch
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBFBFC),
    surfaceContainer = LightSurfaceVariant,
    surfaceContainerHigh = Color(0xFFECEEF2),
    surfaceContainerHighest = Color(0xFFE6E9EE),
    outline = LightBorder,
    outlineVariant = Color(0xFFEDEFF3),
    scrim = Color(0x66000000)
)

private val DarkColorScheme = darkColorScheme(
    primary = GivenDataBlueDark,
    onPrimary = Color(0xFF0B1A4D),
    primaryContainer = Color(0xFF1E2B52),
    onPrimaryContainer = Color(0xFFDCE4FC),
    secondary = TargetUnknownAmberDark,
    onSecondary = Color(0xFF3D1E00),
    secondaryContainer = Color(0xFF3A2814),
    onSecondaryContainer = Color(0xFFFBE3C8),
    tertiary = MasteryVerifiedGreenDark,
    onTertiary = Color(0xFF00351C),
    tertiaryContainer = Color(0xFF14321F),
    onTertiaryContainer = Color(0xFFCDEEDB),
    error = ErrorDiagnosticRedDark,
    onError = Color(0xFF4A0B0B),
    errorContainer = Color(0xFF3B1616),
    onErrorContainer = Color(0xFFFCDADA),
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = Color(0xFF111620),
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = Color(0xFF242C3A),
    outline = DarkBorder,
    outlineVariant = Color(0xFF1F2633),
    scrim = Color(0x99000000)
)

/** Góc bo thống nhất: nhỏ gọn, tinh tế. */
val AcademicShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun AcademicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AcademicTypography,
        shapes = AcademicShapes,
        content = content
    )
}
