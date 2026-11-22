package com.example.data.sqlite

import android.content.Context
import com.example.data.local.entity.AyahEntity
import com.example.data.local.entity.SqliteDatabaseFileReport
import com.example.data.local.entity.SurahEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

data class ParsedQuranBundle(
    val surahs: List<SurahEntity>,
    val ayahs: List<AyahEntity>,
    val report: SqliteDatabaseFileReport?
)

/**
 * Parses `app/src/main/assets/databases/quran_bn.json` (114 Surahs, 6,236 Ayahs)
 * into Room entities with authentic Bangla Surah names, meanings, revelation types,
 * Arabic Uthmani script, and Bangla translations.
 */
class QuranJsonDataSource(private val context: Context) {

    companion object {
        const val QURAN_ASSET_PATH = "databases/quran_bn.json"

        private val SURAH_NAMES_BN = mapOf(
            1 to "আল-ফাতিহা", 2 to "আল-বাকারা", 3 to "আলে-ইমরান", 4 to "আন-নিসা", 5 to "আল-মায়িদাহ",
            6 to "আল-আনআম", 7 to "আল-আরাফ", 8 to "আল-আনফাল", 9 to "আত-তাওবাহ", 10 to "ইউনুস",
            11 to "হুদ", 12 to "ইউসুফ", 13 to "আর-রাদ", 14 to "ইবরাহীম", 15 to "আল-হিজর",
            16 to "আন-নাহল", 17 to "বনী-ইসরাঈল (আল-ইসরা)", 18 to "আল-কাহফ", 19 to "মারইয়াম", 20 to "ত্বা-হা",
            21 to "আল-আম্বিয়া", 22 to "আল-হজ্জ", 23 to "আল-মুমিনুন", 24 to "আন-নূর", 25 to "আল-ফুরকান",
            26 to "আশ-শুআরা", 27 to "আন-নামল", 28 to "আল-কাসাস", 29 to "আল-আনকাবুত", 30 to "আর-রূম",
            31 to "লুকমান", 32 to "আস-সাজদাহ", 33 to "আল-আহযাব", 34 to "সাবা", 35 to "ফাতির",
            36 to "ইয়াসীন", 37 to "আস-সাফফাত", 38 to "সোয়াদ", 39 to "আয-যুমার", 40 to "আল-মুমিন (গাফির)",
            41 to "হা-মীম সাজদাহ (ফুসসিলাত)", 42 to "আশ-শূরা", 43 to "আয-যুখরুফ", 44 to "আদ-দুখান", 45 to "আল-জাসিয়াহ",
            46 to "আল-আহকাফ", 47 to "মুহাম্মাদ", 48 to "আল-ফাতহ", 49 to "আল-হুজুরাত", 50 to "ক্বাফ",
            51 to "আয-যারিয়াত", 52 to "আত-তূর", 53 to "আন-নাজম", 54 to "আল-কামার", 55 to "আর-রাহমান",
            56 to "আল-ওয়াকিয়াহ", 57 to "আল-হাদীদ", 58 to "আল-মুজাদালাহ", 59 to "আল-হাশর", 60 to "আল-মুমতাহিনাহ",
            61 to "আস-সাফ", 62 to "আল-জুমুআহ", 63 to "আল-মুনাফিকুন", 64 to "আত-তাগাবুন", 65 to "আত-তালাক",
            66 to "আত-তাহরীম", 67 to "আল-মুলক", 68 to "আল-কলম", 69 to "আল-হাক্কাহ", 70 to "আল-মাআরিজ",
            71 to "নূহ", 72 to "আল-জিন", 73 to "আল-মুযযাম্মিল", 74 to "আল-মুদ্দাসসির", 75 to "আল-কিয়ামাহ",
            76 to "আল-ইনসান (আদ-দাহর)", 77 to "আল-মুরসালাত", 78 to "আন-নাবা", 79 to "আন-নাযিয়াত", 80 to "আবাসা",
            81 to "আত-তাকভীর", 82 to "আল-ইনফিতার", 83 to "আল-মুতাফফিফীন", 84 to "আল-ইনশিকাক", 85 to "আল-বুরূজ",
            86 to "আত-তারিক", 87 to "আল-আলা", 88 to "আল-গাশিয়াহ", 89 to "আল-ফজর", 90 to "আল-বালাদ",
            91 to "আশ-শামস", 92 to "আল-লাইল", 93 to "আদ-দুহা", 94 to "আল-ইনশিরাহ", 95 to "আত-তীন",
            96 to "আল-আলাক", 97 to "আল-কদর", 98 to "আল-বাইয়্যিনাহ", 99 to "আয-যিলযাল", 100 to "আল-আদিয়াত",
            101 to "আল-কারিয়াহ", 102 to "আত-তাকাসুর", 103 to "আল-আসর", 104 to "আল-হুমাযাহ", 105 to "আল-ফীল",
            106 to "কুরাইশ", 107 to "আল-মাউন", 108 to "আল-কাওসার", 109 to "আল-কাফিরুন", 110 to "আন-নাসর",
            111 to "আল-লাহাব", 112 to "আল-ইখলাস", 113 to "আল-ফালাক", 114 to "আন-নাস"
        )
    }

    suspend fun parseQuranFromAssets(): ParsedQuranBundle = withContext(Dispatchers.IO) {
        runCatching {
            val rawJson = context.assets.open(QURAN_ASSET_PATH).bufferedReader(Charsets.UTF_8).use {
                it.readText()
            }
            val sizeKb = (rawJson.toByteArray(Charsets.UTF_8).size / 1024L).coerceAtLeast(1L)
            val jsonArray = JSONArray(rawJson)

            val surahs = ArrayList<SurahEntity>(114)
            val ayahs = ArrayList<AyahEntity>(6300)

            for (i in 0 until jsonArray.length()) {
                val surahObj = jsonArray.getJSONObject(i)
                val surahId = surahObj.getInt("id")
                val nameAr = surahObj.optString("name", "").trim()
                val translitEn = surahObj.optString("transliteration", "Surah $surahId").trim()
                val meaningBn = surahObj.optString("translation", "").trim()
                val typeRaw = surahObj.optString("type", "meccan").trim().lowercase()
                val revBn = if (typeRaw == "medinan") "মাদানী" else "মাক্কী"
                val totalVerses = surahObj.optInt("total_verses", 0)
                val surahNameBn = SURAH_NAMES_BN[surahId] ?: translitEn

                val versesArray = surahObj.optJSONArray("verses")
                val verseCount = versesArray?.length() ?: totalVerses

                surahs.add(
                    SurahEntity(
                        id = surahId,
                        nameAr = nameAr,
                        transliterationEn = translitEn,
                        nameBn = surahNameBn,
                        meaningBn = meaningBn,
                        revelationType = typeRaw,
                        revelationBn = revBn,
                        totalVerses = verseCount
                    )
                )

                if (versesArray != null) {
                    for (vIdx in 0 until versesArray.length()) {
                        val vObj = versesArray.getJSONObject(vIdx)
                        val ayahNumber = vObj.optInt("id", vIdx + 1)
                        val arabicText = vObj.optString("text", "").trim()
                        val banglaTranslation = vObj.optString("translation", "").trim()

                        ayahs.add(
                            AyahEntity(
                                id = surahId * 1000 + ayahNumber,
                                surahId = surahId,
                                surahNameBn = surahNameBn,
                                surahNameAr = nameAr,
                                ayahNumber = ayahNumber,
                                arabicText = arabicText,
                                banglaTranslation = banglaTranslation
                            )
                        )
                    }
                }
            }

            val report = SqliteDatabaseFileReport(
                fileName = "quran_bn.json",
                originType = "QURAN_JSON",
                relativePath = "app/src/main/assets/$QURAN_ASSET_PATH",
                fileSizeKb = sizeKb,
                detectedTables = listOf("surahs", "verses"),
                booksCount = 1,
                chaptersCount = surahs.size,
                hadithsCount = ayahs.size,
                isCompatible = surahs.isNotEmpty() && ayahs.isNotEmpty(),
                statusNoteBn = "সক্রিয় ও ইনডেক্সড (আল-কুরআনুল কারীম: ${surahs.size} টি সূরা, ${ayahs.size} টি আয়াত)"
            )

            ParsedQuranBundle(
                surahs = surahs,
                ayahs = ayahs,
                report = report
            )
        }.getOrElse { err ->
            ParsedQuranBundle(
                surahs = emptyList(),
                ayahs = emptyList(),
                report = SqliteDatabaseFileReport(
                    fileName = "quran_bn.json",
                    originType = "QURAN_JSON",
                    relativePath = "app/src/main/assets/$QURAN_ASSET_PATH",
                    fileSizeKb = 0L,
                    detectedTables = emptyList(),
                    booksCount = 0,
                    chaptersCount = 0,
                    hadithsCount = 0,
                    isCompatible = false,
                    statusNoteBn = "কুরআন ডেটাবেস লোড ত্রুটি: ${err.localizedMessage}"
                )
            )
        }
    }
}
