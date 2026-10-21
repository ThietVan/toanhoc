package com.example.toanhoc.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Bảng màu tối giản, chuyên nghiệp (Minimal Professional).
 * - Nền trung tính (slate/gray) ít bão hòa, nhiều khoảng trắng.
 * - Một màu nhấn chính (xanh mực) + 3 màu ngữ nghĩa trầm hơn.
 * Tên biến giữ nguyên để tương thích với toàn bộ màn hình hiện có.
 */

// Neutral – Light
val LightBackground = Color(0xFFF7F8FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F3F6)
val LightBorder = Color(0xFFE4E7EC)
val LightText = Color(0xFF111827)
val LightTextSecondary = Color(0xFF4B5563)

// Neutral – Dark
val DarkBackground = Color(0xFF0B0F17)
val DarkSurface = Color(0xFF141A24)
val DarkSurfaceVariant = Color(0xFF1C2330)
val DarkBorder = Color(0xFF2A3241)
val DarkText = Color(0xFFF3F4F6)
val DarkTextSecondary = Color(0xFF9CA3AF)

// Semantic (trầm, ít chói)
val GivenDataBlue = Color(0xFF2F55D4)        // màu nhấn chính / dữ kiện đã cho
val TargetUnknownAmber = Color(0xFFC2620A)   // ẩn số cần tìm
val ErrorDiagnosticRed = Color(0xFFC53030)   // lỗi
val MasteryVerifiedGreen = Color(0xFF1E7F4F) // đã thành thạo

// Dark-mode variants (sáng hơn để đủ tương phản)
val GivenDataBlueDark = Color(0xFF8AA4F2)
val TargetUnknownAmberDark = Color(0xFFF0A65A)
val ErrorDiagnosticRedDark = Color(0xFFF08A8A)
val MasteryVerifiedGreenDark = Color(0xFF6CC79A)

// Nền mềm cho thẻ & huy hiệu – rất nhạt, gần trung tính
val SoftBlueContainer = Color(0xFFF3F6FD)
val SoftBlueBg = Color(0xFFEEF2FC)
val SoftAmberBg = Color(0xFFFBF3EA)
val SoftGreenBg = Color(0xFFEAF4EE)
val SoftRedBg = Color(0xFFFBEDED)
val SubtleBorder = LightBorder

val TextDark = LightText
val TextMuted = Color(0xFF6B7280)

// Backward compatibility aliases
val TechnicalBlue = GivenDataBlue
val AmberOrange = TargetUnknownAmber
val DeepRed = ErrorDiagnosticRed
val SuccessGreen = MasteryVerifiedGreen
