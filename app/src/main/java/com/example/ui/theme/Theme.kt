package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ReaderThemeMode(val titleBn: String) {
    MANUSCRIPT_LIGHT("পান্ডুলিপি লাইট"),
    SEPIA_SCHOLAR("সেপিয়া পেপার"),
    TAHAJJUD_DARK("তাহাজ্জুদ ডার্ক")
}

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
    outline = ManuscriptBorder
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
    outline = Color(0xFFD5C5A5)
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
    outline = ObsidianBorder
)

@Composable
fun AlHadithBanglaTheme(
    themeMode: ReaderThemeMode = ReaderThemeMode.MANUSCRIPT_LIGHT,
    systemDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ReaderThemeMode.MANUSCRIPT_LIGHT -> if (systemDark) TahajjudDarkColorScheme else ManuscriptLightColorScheme
        ReaderThemeMode.SEPIA_SCHOLAR -> SepiaScholarColorScheme
        ReaderThemeMode.TAHAJJUD_DARK -> TahajjudDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
