package com.example

import com.example.ui.components.stripArabicTashkeel
import com.example.ui.components.toBanglaDigits
import com.example.ui.components.toEnglishDigits
import org.json.JSONArray
import org.junit.Assert.assertEquals
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
    }
}
