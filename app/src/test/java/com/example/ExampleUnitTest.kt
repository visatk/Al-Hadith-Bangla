package com.example

import com.example.data.local.entity.AyahEntity
import com.example.data.local.entity.HadithEntity
import com.example.ui.components.formatHadithForShare
import com.example.ui.components.stripArabicTashkeel
import com.example.ui.components.toBanglaDigits
import com.example.ui.components.toEnglishDigits
import com.example.ui.screens.formatAyahForShare
import com.example.ui.theme.ArabicFontStyle
import com.example.ui.theme.BanglaFontStyle
import com.example.ui.theme.ReaderThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExampleUnitTest {

    @Test
    fun `verify all 9 Hadith SQLite files exist and have valid SQLite headers`() {
        val dbDir = File("src/main/assets/databases/bangla-hadiths").let {
            if (it.exists()) it else File("app/src/main/assets/databases/bangla-hadiths")
        }
        assertTrue("bangla-hadiths directory must exist", dbDir.exists())
        val sqliteFiles = dbDir.listFiles { f -> f.name.endsWith(".sqlite") }?.sortedBy { it.name }.orEmpty()
        assertEquals(9, sqliteFiles.size)

        for (file in sqliteFiles) {
            assertTrue("SQLite file ${file.name} should not be empty", file.length() > 4096)
            val header = file.inputStream().use { input ->
                val bytes = ByteArray(15)
                input.read(bytes)
                String(bytes, Charsets.US_ASCII)
            }
            assertEquals("SQLite format 3", header)
        }
    }

    @Test
    fun `verify quran_bn json contains 114 surahs and 6236 ayahs`() {
        val quranFile = File("src/main/assets/databases/quran_bn.json").let {
            if (it.exists()) it else File("app/src/main/assets/databases/quran_bn.json")
        }
        assertTrue("quran_bn.json must exist", quranFile.exists())
        val content = quranFile.readText(Charsets.UTF_8)
        val surahCount = Regex("\"total_verses\"\\s*:").findAll(content).count()
        assertEquals(114, surahCount)
        assertTrue(content.contains("الفاتحة"))
        assertTrue(content.contains("الناس"))
    }

    @Test
    fun `verify Bangla digit conversion and Arabic Tashkeel helpers`() {
        assertEquals("৭৩৬০", 7360.toBanglaDigits())
        assertEquals("2:255", "২:২৫৫".toEnglishDigits())
        val vocalized = "بِسْمِ اللَّهِ"
        val stripped = vocalized.stripArabicTashkeel(keepTashkeel = false)
        assertTrue(stripped.length < vocalized.length)
        assertEquals(vocalized, vocalized.stripArabicTashkeel(keepTashkeel = true))
    }

    @Test
    fun `verify ReaderThemeMode palettes and multi-font typography enums`() {
        assertEquals(4, ReaderThemeMode.entries.size)
        assertFalse(ReaderThemeMode.MANUSCRIPT_LIGHT.isDark)
        assertFalse(ReaderThemeMode.SEPIA_SCHOLAR.isDark)
        assertFalse(ReaderThemeMode.MADINAH_GREEN.isDark)
        assertTrue(ReaderThemeMode.TAHAJJUD_DARK.isDark)

        assertEquals(3, ArabicFontStyle.entries.size)
        assertEquals(3, BanglaFontStyle.entries.size)

        val fontDir = File("src/main/res/font").let {
            if (it.exists()) it else File("app/src/main/res/font")
        }
        assertTrue("Font directory must exist", fontDir.exists())
        val expectedFonts = listOf(
            "amiri.ttf",
            "scheherazade_new.ttf",
            "noto_naskh_arabic.ttf",
            "hind_siliguri.ttf",
            "noto_serif_bengali.ttf",
            "tiro_bangla.ttf",
            "plus_jakarta_sans.ttf"
        )
        for (fontName in expectedFonts) {
            val fontFile = File(fontDir, fontName)
            assertTrue("Bundled font $fontName must exist", fontFile.exists() && fontFile.length() > 1024)
        }
    }

    @Test
    fun `verify formatted share strings omit blank narrator and preserve references`() {
        val hadith = HadithEntity(
            id = 1,
            bookId = 1,
            chapterId = 101,
            sectionId = 1,
            hadithNumber = 1,
            internationalNumber = "বুখারী ১",
            narratorBn = "",
            arabicText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ",
            banglaText = "সকল আমল নিয়তের উপর নির্ভরশীল।",
            transliterationBn = "",
            explanationBn = "",
            gradeBn = "সহীহ হাদিস",
            gradeType = "SAHIH",
            referenceBn = "সহীহ বুখারী • হাদিস নং ১",
            topicTags = "নিয়ত,ইখলাস"
        )
        val formattedHadith = formatHadithForShare(hadith)
        assertFalse(formattedHadith.contains("হতে বর্ণিত:"))
        assertTrue(formattedHadith.contains("সহীহ বুখারী"))

        val ayah = AyahEntity(
            id = 200255,
            surahId = 2,
            ayahNumber = 255,
            surahNameBn = "আল-বাকারা",
            surahNameAr = "البقرة",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ",
            banglaTranslation = "আল্লাহ, তিনি ব্যতীত কোনো উপাস্য নেই।"
        )
        val formattedAyah = formatAyahForShare(ayah)
        assertTrue(formattedAyah.contains("২৫৫"))
        assertTrue(formattedAyah.contains("আল-বাকারা"))
    }
}
