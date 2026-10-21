package com.aivora.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aivora.app.R

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

val AivoraTypography = Typography(
    displayLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
)
