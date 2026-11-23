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

val ScheherazadeArabicFontFamily = FontFamily(
    Font(R.font.scheherazade_new, FontWeight.Normal),
    Font(R.font.scheherazade_new, FontWeight.Bold)
)

val NotoNaskhArabicFontFamily = FontFamily(
    Font(R.font.noto_naskh_arabic, FontWeight.Normal),
    Font(R.font.noto_naskh_arabic, FontWeight.Bold)
)

val HindSiliguriBanglaFontFamily = FontFamily(
    Font(R.font.hind_siliguri, FontWeight.Normal),
    Font(R.font.hind_siliguri, FontWeight.Medium),
    Font(R.font.hind_siliguri, FontWeight.SemiBold),
    Font(R.font.hind_siliguri, FontWeight.Bold)
)

val NotoSerifBengaliFontFamily = FontFamily(
    Font(R.font.noto_serif_bengali, FontWeight.Normal),
    Font(R.font.noto_serif_bengali, FontWeight.Medium),
    Font(R.font.noto_serif_bengali, FontWeight.SemiBold),
    Font(R.font.noto_serif_bengali, FontWeight.Bold)
)

val TiroBanglaFontFamily = FontFamily(
    Font(R.font.tiro_bangla, FontWeight.Normal),
    Font(R.font.tiro_bangla, FontWeight.Medium),
    Font(R.font.tiro_bangla, FontWeight.SemiBold),
    Font(R.font.tiro_bangla, FontWeight.Bold)
)

val PlusJakartaSansFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

enum class ArabicFontStyle(val titleBn: String, val subtitleBn: String) {
    AMIRI("আমিরি ক্লাসিক", "চিরায়ত পান্ডুলিপি লিপি"),
    SCHEHERAZADE("শেহেরজাদ মুসহাফ", "প্রশস্ত কুরআনিক হরকত"),
    NOTO_NASKH("নোটো নাসখ", "আধুনিক স্পষ্ট স্ক্রিন লিপি");

    val fontFamily: FontFamily
        get() = when (this) {
            AMIRI -> AmiriArabicFontFamily
            SCHEHERAZADE -> ScheherazadeArabicFontFamily
            NOTO_NASKH -> NotoNaskhArabicFontFamily
        }
}

enum class BanglaFontStyle(val titleBn: String, val subtitleBn: String) {
    HIND_SILIGURI("হিন্দ শিলিগুড়ি", "আধুনিক ও পরিচ্ছন্ন"),
    NOTO_SERIF_BENGALI("নোটো সেরিফ", "ধ্রুপদী গ্রন্থ শৈলী"),
    TIRO_BANGLA("তিরো বাংলা", "সাহিত্যিক ও ঐতিহ্যবাহী");

    val fontFamily: FontFamily
        get() = when (this) {
            HIND_SILIGURI -> HindSiliguriBanglaFontFamily
            NOTO_SERIF_BENGALI -> NotoSerifBengaliFontFamily
            TIRO_BANGLA -> TiroBanglaFontFamily
        }
}

private val ScholarLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

private val ScholarPlatformTextStyle = PlatformTextStyle(
    includeFontPadding = false
)

fun buildAppTypography(
    banglaFontStyle: BanglaFontStyle = BanglaFontStyle.HIND_SILIGURI,
    arabicFontStyle: ArabicFontStyle = ArabicFontStyle.AMIRI
): Typography {
    val banglaFamily = banglaFontStyle.fontFamily
    val arabicFamily = arabicFontStyle.fontFamily
    return Typography(
        displayLarge = TextStyle(
            fontFamily = banglaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 44.sp,
            platformStyle = ScholarPlatformTextStyle,
            lineHeightStyle = ScholarLineHeightStyle
        ),
        displayMedium = TextStyle(
            fontFamily = arabicFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 48.sp,
            platformStyle = ScholarPlatformTextStyle,
            lineHeightStyle = ScholarLineHeightStyle,
            lineBreak = LineBreak.Paragraph
        ),
        headlineLarge = TextStyle(
            fontFamily = banglaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 36.sp,
            platformStyle = ScholarPlatformTextStyle,
            lineHeightStyle = ScholarLineHeightStyle
        ),
        headlineMedium = TextStyle(
            fontFamily = banglaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 30.sp,
            platformStyle = ScholarPlatformTextStyle,
            lineHeightStyle = ScholarLineHeightStyle
        ),
        titleLarge = TextStyle(
            fontFamily = banglaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            lineHeight = 27.sp,
            platformStyle = ScholarPlatformTextStyle,
            lineHeightStyle = ScholarLineHeightStyle
        ),
        titleMedium = TextStyle(
            fontFamily = banglaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            platformStyle = ScholarPlatformTextStyle,
            lineHeightStyle = ScholarLineHeightStyle
        ),
        titleSmall = TextStyle(
            fontFamily = banglaFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            platformStyle = ScholarPlatformTextStyle,
            lineHeightStyle = ScholarLineHeightStyle
        ),
        bodyLarge = TextStyle(
            fontFamily = banglaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            lineHeight = 28.sp,
            platformStyle = ScholarPlatformTextStyle,
            lineHeightStyle = ScholarLineHeightStyle,
            lineBreak = LineBreak.Paragraph
        ),
        bodyMedium = TextStyle(
            fontFamily = banglaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 23.sp,
            platformStyle = ScholarPlatformTextStyle,
            lineHeightStyle = ScholarLineHeightStyle,
            lineBreak = LineBreak.Simple
        ),
        bodySmall = TextStyle(
            fontFamily = banglaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            platformStyle = ScholarPlatformTextStyle,
            lineHeightStyle = ScholarLineHeightStyle
        ),
        labelLarge = TextStyle(
            fontFamily = banglaFamily,
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
}

val Typography = buildAppTypography()
