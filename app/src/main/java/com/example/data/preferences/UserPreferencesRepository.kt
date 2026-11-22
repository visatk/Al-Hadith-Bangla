package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.entity.QuranReadingProgress
import com.example.data.local.entity.ReaderPreferences
import com.example.ui.theme.ReaderThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "al_hadith_reader_settings")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val ARABIC_FONT_SIZE = floatPreferencesKey("arabic_font_size_sp")
        val BANGLA_FONT_SIZE = floatPreferencesKey("bangla_font_size_sp")
        val LINE_HEIGHT_MULTIPLIER = floatPreferencesKey("line_height_multiplier")
        val SHOW_ARABIC = booleanPreferencesKey("show_arabic")
        val SHOW_TRANSLITERATION = booleanPreferencesKey("show_transliteration")
        val SHOW_EXPLANATION = booleanPreferencesKey("show_explanation")
        val SHOW_TASHKEEL = booleanPreferencesKey("show_tashkeel")
        val EYE_CARE_WARM_FILTER = floatPreferencesKey("eye_care_warm_filter")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on_reading")
        val THEME_MODE = stringPreferencesKey("reader_theme_mode")

        // Fast Startup Sync Fingerprint
        val LAST_SYNC_FINGERPRINT = stringPreferencesKey("last_sync_fingerprint_v2")

        // Al-Quran Reading Continuity
        val LAST_QURAN_SURAH_ID = intPreferencesKey("last_quran_surah_id")
        val LAST_QURAN_SURAH_NAME_BN = stringPreferencesKey("last_quran_surah_name_bn")
        val LAST_QURAN_AYAH_NUMBER = intPreferencesKey("last_quran_ayah_number")
    }

    private val safeDataFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    val readerPreferencesFlow: Flow<ReaderPreferences> = safeDataFlow.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: ReaderThemeMode.MANUSCRIPT_LIGHT.name
        val themeMode = runCatching { ReaderThemeMode.valueOf(themeStr) }
            .getOrDefault(ReaderThemeMode.MANUSCRIPT_LIGHT)

        ReaderPreferences(
            arabicFontSizeSp = (prefs[Keys.ARABIC_FONT_SIZE] ?: 26f).coerceIn(20f, 40f),
            banglaFontSizeSp = (prefs[Keys.BANGLA_FONT_SIZE] ?: 17f).coerceIn(14f, 26f),
            lineHeightMultiplier = (prefs[Keys.LINE_HEIGHT_MULTIPLIER] ?: 1.65f).coerceIn(1.4f, 2.1f),
            showArabic = prefs[Keys.SHOW_ARABIC] ?: true,
            showTransliteration = prefs[Keys.SHOW_TRANSLITERATION] ?: false,
            showExplanation = prefs[Keys.SHOW_EXPLANATION] ?: true,
            showTashkeel = prefs[Keys.SHOW_TASHKEEL] ?: true,
            eyeCareWarmFilter = (prefs[Keys.EYE_CARE_WARM_FILTER] ?: 0.0f).coerceIn(0f, 0.28f),
            keepScreenOnWhileReading = prefs[Keys.KEEP_SCREEN_ON] ?: true,
            themeMode = themeMode
        )
    }

    val quranReadingProgressFlow: Flow<QuranReadingProgress?> = safeDataFlow.map { prefs ->
        val surahId = prefs[Keys.LAST_QURAN_SURAH_ID] ?: return@map null
        val surahNameBn = prefs[Keys.LAST_QURAN_SURAH_NAME_BN] ?: return@map null
        val ayahNumber = prefs[Keys.LAST_QURAN_AYAH_NUMBER] ?: 1
        QuranReadingProgress(
            surahId = surahId,
            surahNameBn = surahNameBn,
            ayahNumber = ayahNumber
        )
    }

    suspend fun getLastSyncFingerprint(): String {
        return safeDataFlow.first()[Keys.LAST_SYNC_FINGERPRINT].orEmpty()
    }

    suspend fun saveLastSyncFingerprint(fingerprint: String) {
        context.dataStore.edit { it[Keys.LAST_SYNC_FINGERPRINT] = fingerprint }
    }

    suspend fun saveQuranReadingProgress(surahId: Int, surahNameBn: String, ayahNumber: Int) {
        context.dataStore.edit {
            it[Keys.LAST_QURAN_SURAH_ID] = surahId
            it[Keys.LAST_QURAN_SURAH_NAME_BN] = surahNameBn
            it[Keys.LAST_QURAN_AYAH_NUMBER] = ayahNumber.coerceAtLeast(1)
        }
    }

    suspend fun updateArabicFontSize(sizeSp: Float) {
        context.dataStore.edit { it[Keys.ARABIC_FONT_SIZE] = sizeSp.coerceIn(20f, 40f) }
    }

    suspend fun updateBanglaFontSize(sizeSp: Float) {
        context.dataStore.edit { it[Keys.BANGLA_FONT_SIZE] = sizeSp.coerceIn(14f, 26f) }
    }

    suspend fun updateLineHeightMultiplier(multiplier: Float) {
        context.dataStore.edit { it[Keys.LINE_HEIGHT_MULTIPLIER] = multiplier.coerceIn(1.4f, 2.1f) }
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

    suspend fun updateEyeCareWarmFilter(intensity: Float) {
        context.dataStore.edit { it[Keys.EYE_CARE_WARM_FILTER] = intensity.coerceIn(0f, 0.28f) }
    }

    suspend fun toggleKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }

    suspend fun setThemeMode(mode: ReaderThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }
}
