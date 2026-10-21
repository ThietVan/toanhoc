package com.example.toanhoc.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.toanhoc.R

/**
 * Official Nunito Font Family (Regular, SemiBold, Bold) loaded from res/font/.
 */
val NunitoFontFamily = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold)
)

val TabularMonospaceFont = TextStyle(
    fontFamily = NunitoFontFamily,
    fontWeight = FontWeight.Bold,
    fontFeatureSettings = "tnum"
)

private fun style(
    size: Int,
    line: Int,
    weight: FontWeight,
    tracking: Double = 0.0
) = TextStyle(
    fontFamily = NunitoFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    // Line height tỉ lệ theo cỡ chữ: khi màn hình tự đặt fontSize nhỏ,
    // khoảng cách dòng vẫn cân đối (không bị hở như khi dùng sp cố định).
    lineHeight = (line.toFloat() / size).em,
    letterSpacing = tracking.sp
)

/**
 * Thang chữ đầy đủ, nhịp đều: tiêu đề đậm vừa phải, thân chữ thoáng.
 */
val AcademicTypography = Typography(
    displayLarge = style(32, 40, FontWeight.Bold, -0.4),
    displayMedium = style(28, 36, FontWeight.Bold, -0.3),
    displaySmall = style(24, 32, FontWeight.Bold, -0.2),
    headlineLarge = style(24, 32, FontWeight.Bold, -0.2),
    headlineMedium = style(20, 28, FontWeight.Bold, -0.1),
    headlineSmall = style(18, 26, FontWeight.SemiBold),
    titleLarge = style(18, 26, FontWeight.SemiBold),
    titleMedium = style(16, 24, FontWeight.SemiBold, 0.1),
    titleSmall = style(14, 20, FontWeight.SemiBold, 0.1),
    bodyLarge = style(16, 24, FontWeight.Normal),
    bodyMedium = style(14, 21, FontWeight.Normal),
    bodySmall = style(12, 18, FontWeight.Normal, 0.1),
    labelLarge = style(14, 20, FontWeight.SemiBold, 0.1),
    labelMedium = style(12, 16, FontWeight.SemiBold, 0.3),
    labelSmall = style(11, 14, FontWeight.SemiBold, 0.4)
)
