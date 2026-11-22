package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.entity.ReaderPreferences
import com.example.ui.theme.ReaderThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "al_hadith_reader_settings")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val ARABIC_FONT_SIZE = floatPreferencesKey("arabic_font_size_sp")
        val BANGLA_FONT_SIZE = floatPreferencesKey("bangla_font_size_sp")
        val SHOW_ARABIC = booleanPreferencesKey("show_arabic")
        val SHOW_TRANSLITERATION = booleanPreferencesKey("show_transliteration")
        val SHOW_EXPLANATION = booleanPreferencesKey("show_explanation")
        val SHOW_TASHKEEL = booleanPreferencesKey("show_tashkeel")
        val THEME_MODE = stringPreferencesKey("reader_theme_mode")
    }

    val readerPreferencesFlow: Flow<ReaderPreferences> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: ReaderThemeMode.MANUSCRIPT_LIGHT.name
        val themeMode = runCatching { ReaderThemeMode.valueOf(themeStr) }
            .getOrDefault(ReaderThemeMode.MANUSCRIPT_LIGHT)

        ReaderPreferences(
            arabicFontSizeSp = prefs[Keys.ARABIC_FONT_SIZE] ?: 26f,
            banglaFontSizeSp = prefs[Keys.BANGLA_FONT_SIZE] ?: 17f,
            showArabic = prefs[Keys.SHOW_ARABIC] ?: true,
            showTransliteration = prefs[Keys.SHOW_TRANSLITERATION] ?: true,
            showExplanation = prefs[Keys.SHOW_EXPLANATION] ?: true,
            showTashkeel = prefs[Keys.SHOW_TASHKEEL] ?: true,
            themeMode = themeMode
        )
    }

    suspend fun updateArabicFontSize(sizeSp: Float) {
        context.dataStore.edit { it[Keys.ARABIC_FONT_SIZE] = sizeSp.coerceIn(20f, 40f) }
    }

    suspend fun updateBanglaFontSize(sizeSp: Float) {
        context.dataStore.edit { it[Keys.BANGLA_FONT_SIZE] = sizeSp.coerceIn(14f, 26f) }
    }

    suspend fun toggleShowArabic(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_ARABIC] = enabled }
    }

    suspend fun toggleShowTransliteration(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_TRANSLITERATION] = enabled }
    }

    suspend fun toggleShowExplanation(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_EXPLANATION] = enabled }
    }

    suspend fun toggleShowTashkeel(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_TASHKEEL] = enabled }
    }

    suspend fun setThemeMode(mode: ReaderThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }
}
