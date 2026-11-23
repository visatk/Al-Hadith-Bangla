package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ReaderThemeMode(
    val titleBn: String,
    val isDark: Boolean,
    val bgPreview: Color,
    val primaryPreview: Color,
    val accentPreview: Color
) {
    MANUSCRIPT_LIGHT(
        titleBn = "পান্ডুলিপি লাইট",
        isDark = false,
        bgPreview = ManuscriptBackground,
        primaryPreview = EmeraldPrimary,
        accentPreview = GoldAccent
    ),
    SEPIA_SCHOLAR(
        titleBn = "সেপিয়া পেপার",
        isDark = false,
        bgPreview = SepiaBackground,
        primaryPreview = Color(0xFF0D523F),
        accentPreview = Color(0xFFA9751E)
    ),
    MADINAH_GREEN(
        titleBn = "মদীনা গ্রীন",
        isDark = false,
        bgPreview = MadinahBackground,
        primaryPreview = MadinahPrimary,
        accentPreview = MadinahGold
    ),
    TAHAJJUD_DARK(
        titleBn = "তাহাজ্জুদ ডার্ক",
        isDark = true,
        bgPreview = ObsidianBackground,
        primaryPreview = Color(0xFF42C79A),
        accentPreview = GoldLight
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val ManuscriptLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = OnEmeraldContainerLight,
    secondary = GoldAccent,
    onSecondary = Color.White,
    secondaryContainer = GoldSoftContainer,
    onSecondaryContainer = OnGoldContainer,
    tertiary = EmeraldDeep,
    onTertiary = Color.White,
    background = ManuscriptBackground,
    onBackground = InkPrimary,
    surface = ManuscriptSurface,
    onSurface = InkPrimary,
    surfaceVariant = ManuscriptCardSurface,
    onSurfaceVariant = InkSecondary,
    surfaceContainerLowest = ManuscriptSurface,
    surfaceContainerLow = ManuscriptSurfaceLow,
    surfaceContainer = ManuscriptCardSurface,
    surfaceContainerHigh = ManuscriptSurfaceHigh,
    surfaceContainerHighest = ManuscriptBorder,
    outline = ManuscriptBorder,
    outlineVariant = ManuscriptBorder.copy(alpha = 0.6f)
)

private val SepiaScholarColorScheme = lightColorScheme(
    primary = Color(0xFF0D523F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCCEA8),
    onPrimaryContainer = Color(0xFF241B0B),
    secondary = Color(0xFFA9751E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7D7B5),
    onSecondaryContainer = Color(0xFF362404),
    tertiary = EmeraldDeep,
    onTertiary = Color.White,
    background = SepiaBackground,
    onBackground = SepiaInkPrimary,
    surface = SepiaSurface,
    onSurface = SepiaInkPrimary,
    surfaceVariant = SepiaCardSurface,
    onSurfaceVariant = SepiaInkSecondary,
    surfaceContainerLowest = SepiaSurface,
    surfaceContainerLow = SepiaSurfaceLow,
    surfaceContainer = SepiaCardSurface,
    surfaceContainerHigh = SepiaSurfaceHigh,
    surfaceContainerHighest = SepiaBorder,
    outline = SepiaBorder,
    outlineVariant = SepiaBorder.copy(alpha = 0.6f)
)

private val MadinahGreenColorScheme = lightColorScheme(
    primary = MadinahPrimary,
    onPrimary = Color.White,
    primaryContainer = MadinahContainer,
    onPrimaryContainer = OnMadinahContainer,
    secondary = MadinahGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF6E7C8),
    onSecondaryContainer = Color(0xFF3A2404),
    tertiary = EmeraldDeep,
    onTertiary = Color.White,
    background = MadinahBackground,
    onBackground = MadinahInkPrimary,
    surface = MadinahSurface,
    onSurface = MadinahInkPrimary,
    surfaceVariant = MadinahCardSurface,
    onSurfaceVariant = MadinahInkSecondary,
    surfaceContainerLowest = MadinahSurface,
    surfaceContainerLow = MadinahSurfaceLow,
    surfaceContainer = MadinahCardSurface,
    surfaceContainerHigh = MadinahSurfaceHigh,
    surfaceContainerHighest = MadinahBorder,
    outline = MadinahBorder,
    outlineVariant = MadinahBorder.copy(alpha = 0.6f)
)

private val TahajjudDarkColorScheme = darkColorScheme(
    primary = Color(0xFF42C79A),
    onPrimary = Color(0xFF04281C),
    primaryContainer = Color(0xFF0E4735),
    onPrimaryContainer = Color(0xFFBDF5DF),
    secondary = GoldLight,
    onSecondary = Color(0xFF332303),
    secondaryContainer = Color(0xFF47340E),
    onSecondaryContainer = Color(0xFFF9E2AF),
    tertiary = GoldAccent,
    onTertiary = Color.Black,
    background = ObsidianBackground,
    onBackground = IvoryTextPrimary,
    surface = ObsidianSurface,
    onSurface = IvoryTextPrimary,
    surfaceVariant = ObsidianCardSurface,
    onSurfaceVariant = IvoryTextSecondary,
    surfaceContainerLowest = ObsidianBackground,
    surfaceContainerLow = ObsidianSurfaceLow,
    surfaceContainer = ObsidianCardSurface,
    surfaceContainerHigh = ObsidianSurfaceHigh,
    surfaceContainerHighest = ObsidianBorder,
    outline = ObsidianBorder,
    outlineVariant = ObsidianBorder.copy(alpha = 0.65f)
)

@Composable
fun AlHadithBanglaTheme(
    themeMode: ReaderThemeMode = ReaderThemeMode.MANUSCRIPT_LIGHT,
    banglaFontStyle: BanglaFontStyle = BanglaFontStyle.HIND_SILIGURI,
    arabicFontStyle: ArabicFontStyle = ArabicFontStyle.AMIRI,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ReaderThemeMode.MANUSCRIPT_LIGHT -> ManuscriptLightColorScheme
        ReaderThemeMode.SEPIA_SCHOLAR -> SepiaScholarColorScheme
        ReaderThemeMode.MADINAH_GREEN -> MadinahGreenColorScheme
        ReaderThemeMode.TAHAJJUD_DARK -> TahajjudDarkColorScheme
    }

    val dynamicTypography = remember(banglaFontStyle, arabicFontStyle) {
        buildAppTypography(
            banglaFontStyle = banglaFontStyle,
            arabicFontStyle = arabicFontStyle
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = dynamicTypography,
        shapes = AppShapes,
        content = content
    )
}
