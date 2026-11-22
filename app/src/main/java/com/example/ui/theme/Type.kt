package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.example.R

val AmiriArabicFontFamily = FontFamily(
    Font(R.font.amiri, FontWeight.Normal),
    Font(R.font.amiri, FontWeight.Bold)
)

val HindSiliguriBanglaFontFamily = FontFamily(
    Font(R.font.hind_siliguri, FontWeight.Normal),
    Font(R.font.hind_siliguri, FontWeight.Medium),
    Font(R.font.hind_siliguri, FontWeight.SemiBold),
    Font(R.font.hind_siliguri, FontWeight.Bold)
)

val PlusJakartaSansFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

private val ScholarLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

private val ScholarPlatformTextStyle = PlatformTextStyle(
    includeFontPadding = false
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = HindSiliguriBanglaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 44.sp,
        platformStyle = ScholarPlatformTextStyle,
        lineHeightStyle = ScholarLineHeightStyle
    ),
    displayMedium = TextStyle(
        fontFamily = AmiriArabicFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 48.sp,
        platformStyle = ScholarPlatformTextStyle,
        lineHeightStyle = ScholarLineHeightStyle,
        lineBreak = LineBreak.Paragraph
    ),
    headlineLarge = TextStyle(
        fontFamily = HindSiliguriBanglaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 36.sp,
        platformStyle = ScholarPlatformTextStyle,
        lineHeightStyle = ScholarLineHeightStyle
    ),
    headlineMedium = TextStyle(
        fontFamily = HindSiliguriBanglaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        platformStyle = ScholarPlatformTextStyle,
        lineHeightStyle = ScholarLineHeightStyle
    ),
    titleLarge = TextStyle(
        fontFamily = HindSiliguriBanglaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 27.sp,
        platformStyle = ScholarPlatformTextStyle,
        lineHeightStyle = ScholarLineHeightStyle
    ),
    titleMedium = TextStyle(
        fontFamily = HindSiliguriBanglaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        platformStyle = ScholarPlatformTextStyle,
        lineHeightStyle = ScholarLineHeightStyle
    ),
    titleSmall = TextStyle(
        fontFamily = HindSiliguriBanglaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        platformStyle = ScholarPlatformTextStyle,
        lineHeightStyle = ScholarLineHeightStyle
    ),
    bodyLarge = TextStyle(
        fontFamily = HindSiliguriBanglaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 28.sp,
        platformStyle = ScholarPlatformTextStyle,
        lineHeightStyle = ScholarLineHeightStyle,
        lineBreak = LineBreak.Paragraph
    ),
    bodyMedium = TextStyle(
        fontFamily = HindSiliguriBanglaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp,
        platformStyle = ScholarPlatformTextStyle,
        lineHeightStyle = ScholarLineHeightStyle,
        lineBreak = LineBreak.Simple
    ),
    bodySmall = TextStyle(
        fontFamily = HindSiliguriBanglaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        platformStyle = ScholarPlatformTextStyle,
        lineHeightStyle = ScholarLineHeightStyle
    ),
    labelLarge = TextStyle(
        fontFamily = HindSiliguriBanglaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        platformStyle = ScholarPlatformTextStyle
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        platformStyle = ScholarPlatformTextStyle
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        platformStyle = ScholarPlatformTextStyle
    )
)
