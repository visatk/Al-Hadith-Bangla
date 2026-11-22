package com.example.data.sqlite

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.SectionEntity
import com.example.data.local.entity.SqliteDatabaseFileReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class ParsedSqliteBundle(
    val reports: List<SqliteDatabaseFileReport>,
    val books: List<BookEntity>,
    val chapters: List<ChapterEntity>,
    val sections: List<SectionEntity>,
    val hadiths: List<HadithEntity>
)

private data class ScholarBookMetadata(
    val bookId: Int,
    val titleBn: String,
    val titleAr: String,
    val titleEn: String,
    val authorBn: String,
    val bookCode: String,
    val accentHex: String,
    val descriptionBn: String
)

/**
 * Modular SQLite Engine that recursively scans `app/src/main/assets/databases/`
 * (including `app/src/main/assets/databases/bangla-hadiths/`) and `filesDir/imported_sqlite_sources/`
 * for any `.sqlite`, `.db`, or `.sqlite3` files, inspects their tables via native SQLiteDatabase,
 * adapts both explicit-books and community single-book Bangla Hadith schemas (`chapter`, `section`, `hadith`),
 * and returns structured entities for Room indexing.
 */
class SqliteAssetDataSource(private val context: Context) {

    companion object {
        const val ASSETS_DB_FOLDER = "databases"
        const val ASSETS_BANGLA_HADITHS_SUBFOLDER = "databases/bangla-hadiths"
        private const val IMPORTED_DB_FOLDER = "imported_sqlite_sources"
        private const val WORK_CACHE_FOLDER = "sqlite_inspection_cache"

        private val KNOWN_BOOK_CATALOG = mapOf(
            "hadith.db" to ScholarBookMetadata(
                bookId = 1,
                titleBn = "সিহাহ সিত্তাহ ও মূল হাদিস সংকলন",
                titleAr = "الكتب الستة ومصادر الحديث",
                titleEn = "Al-Hadith Master Collection",
                authorBn = "মুহাদ্দিস ইমামগণ (রহ.)",
                bookCode = "হাদিস",
                accentHex = "#0B5D48",
                descriptionBn = "বিশুদ্ধ হাদিস সংকলন, অধ্যায়, পরিচ্ছেদ ও তাহকীককৃত হাদিস ভান্ডার।"
            ),
            "bn_hadis-somvar.sqlite" to ScholarBookMetadata(
                bookId = 101,
                titleBn = "হাদিস সম্ভার",
                titleAr = "مجموعة الحديث",
                titleEn = "Hadith Somvar",
                authorBn = "ড. খোন্দকার আব্দুল্লাহ জাহাঙ্গীর ও আস-সুন্নাহ ট্রাস্ট",
                bookCode = "সম্ভার",
                accentHex = "#0B5D48",
                descriptionBn = "ঈমান, আকীদাহ, ইবাদত, মুআমালাত ও আখলাক বিষয়ক সহীহ হাদিসের বিশাল বিষয়ভিত্তিক সংকলন।"
            ),
            "bn_luluwal-marjan.sqlite" to ScholarBookMetadata(
                bookId = 102,
                titleBn = "আল-লুলু ওয়াল মারজান",
                titleAr = "اللؤلؤ والمرجان",
                titleEn = "Al-Lulu Wal-Marjan",
                authorBn = "মুহাম্মাদ ফুয়াদ আব্দুল বাকী (রহ.)",
                bookCode = "লুলু",
                accentHex = "#1A5276",
                descriptionBn = "সহীহ বুখারী ও সহীহ মুসলিমের ঐকমত্যপূর্ণ (মুত্তাফাকুন আলাইহি) সর্বোচ্চ বিশুদ্ধ হাদিস সংকলন।"
            ),
            "bn_bulugul-maram.sqlite" to ScholarBookMetadata(
                bookId = 103,
                titleBn = "বুলুগুল মারাম",
                titleAr = "بلوغ المرام",
                titleEn = "Bulugh al-Maram",
                authorBn = "হাফেয ইবনু হাজার আসকালানী (রহ.)",
                bookCode = "বুলুগ",
                accentHex = "#7E5109",
                descriptionBn = "ইসলামী ফিকহ ও আহকাম (পবিত্রতা, সালাত, যাকাত, সিয়াম, হজ্জ ও লেনদেন) বিষয়ক দলীলভিত্তিক হাদিস গ্রন্থ।"
            ),
            "bn_adabul-mufrad.sqlite" to ScholarBookMetadata(
                bookId = 104,
                titleBn = "আল-আদাবুল মুফরাদ",
                titleAr = "الأدب المفرد",
                titleEn = "Al-Adab Al-Mufrad",
                authorBn = "ইমাম মুহাম্মাদ ইবনু ইসমাঈল আল-বুখারী (রহ.)",
                bookCode = "আদাব",
                accentHex = "#117864",
                descriptionBn = "পিতা-মাতার খেদমত, আত্মীয়তার সম্পর্ক, শিষ্টাচার ও উত্তম চরিত্র গঠনে ইমাম বুখারীর অনন্য হাদিস সংকলন।"
            ),
            "bn_riyadus-salihin.sqlite" to ScholarBookMetadata(
                bookId = 105,
                titleBn = "রিয়াদুস সালেহীন",
                titleAr = "رياض الصالحين",
                titleEn = "Riyad as-Salihin",
                authorBn = "ইমাম মুহিউদ্দীন ইয়াহইয়া আন-নববী (রহ.)",
                bookCode = "রিয়াদুস",
                accentHex = "#0E6251",
                descriptionBn = "আখলাক, তাজকিয়া, ইবাদত ও মুমিনের আত্মশুদ্ধির জন্য বিশ্বের সর্বাধিক পঠিত হাদিস গ্রন্থ।"
            ),
            "bn_mishkatul-masabih.sqlite" to ScholarBookMetadata(
                bookId = 106,
                titleBn = "মিশকাতুল মাসাবীহ",
                titleAr = "مشكاة المصابيح",
                titleEn = "Mishkat al-Masabih",
                authorBn = "ইমাম ওয়ালীউদ্দীন আল-খতীব আত-তাবরীযী (রহ.)",
                bookCode = "মিশকাত",
                accentHex = "#1F618D",
                descriptionBn = "আকীদাহ, ফিকহ, ফাযায়েল ও ফিতান বিষয়ক হাদিসের পূর্ণাঙ্গ ও সুবিন্যস্ত সংকলন।"
            ),
            "bn_muwatta-malik.sqlite" to ScholarBookMetadata(
                bookId = 107,
                titleBn = "মুয়াত্তা ইমাম মালিক",
                titleAr = "موطأ الإمام مالك",
                titleEn = "Muwatta Imam Malik",
                authorBn = "ইমাম মালিক ইবনু আনাস (রহ.)",
                bookCode = "মুয়াত্তা",
                accentHex = "#7D6608",
                descriptionBn = "হিজরী দ্বিতীয় শতাব্দীতে মদীনার সুন্নাহ ও ফিকহ ভিত্তিক প্রাচীনতম প্রামাণিক হাদিস সংকলন।"
            ),
            "bn_shamayele-tirmidhi.sqlite" to ScholarBookMetadata(
                bookId = 108,
                titleBn = "শামায়েলে তিরমিযী",
                titleAr = "الشمائل المحمدية",
                titleEn = "Ash-Shama'il Al-Muhammadiyyah",
                authorBn = "ইমাম আবূ ঈসা আত-তিরমিযী (রহ.)",
                bookCode = "শামায়েল",
                accentHex = "#0B5345",
                descriptionBn = "রাসূলুল্লাহ (ﷺ)-এর দৈহিক অবয়ব, পোশাক, আহার, ইবাদত ও মহান চরিত্র মাধুর্যের হৃদয়গ্রাহী হাদিস সংকলন।"
            ),
            "bn_sahih-hadise-qudsi.sqlite" to ScholarBookMetadata(
                bookId = 109,
                titleBn = "সহীহ হাদিসে কুদসী",
                titleAr = "الأحاديث القدسية الصحيحة",
                titleEn = "Sahih Hadith Qudsi",
                authorBn = "মুহাদ্দিস সংকলন",
                bookCode = "কুদসী",
                accentHex = "#6C3483",
                descriptionBn = "মহান আল্লাহ তাআলার পক্ষ হতে রাসূলুল্লাহ (ﷺ)-এর ভাষায় বর্ণিত বিশুদ্ধ হাদিসে কুদসী সংকলন।"
            ),
            "bn_silsila-sahiha.sqlite" to ScholarBookMetadata(
                bookId = 110,
                titleBn = "সিলসিলা সহীহাহ",
                titleAr = "سلسلة الأحاديث الصحيحة",
                titleEn = "Silsilat al-Ahadith as-Sahihah",
                authorBn = "ইমাম নাসিরুদ্দীন আল-আলবানী (রহ.)",
                bookCode = "সহীহাহ",
                accentHex = "#145A32",
                descriptionBn = "বিভিন্ন হাদিস গ্রন্থ হতে বাছাইকৃত ও সনদ যাচাইকৃত বিশুদ্ধ হাদিস সিরিজ।"
            ),
            "bn_targib-wattahrib.sqlite" to ScholarBookMetadata(
                bookId = 111,
                titleBn = "আত-তারগীব ওয়াত-তারহীব",
                titleAr = "الترغيب والترهيب",
                titleEn = "At-Targhib wat-Tarhib",
                authorBn = "হাফেয যাকীউদ্দীন আল-মুনযিরী (রহ.)",
                bookCode = "তারগীব",
                accentHex = "#1B4F72",
                descriptionBn = "নেক আমলের প্রতি উৎসাহ এবং গুনাহ ও নাফরমানী হতে ভীতিপ্রদর্শন বিষয়ক হাদিস সংকলন।"
            ),
            "bn_fazayele-amal.sqlite" to ScholarBookMetadata(
                bookId = 112,
                titleBn = "সহীহ ফাযায়েলে আমল",
                titleAr = "فضائل الأعمال",
                titleEn = "Sahih Fazayele Amal",
                authorBn = "শাইখ আব্দুল হামিদ ফাইযী আল-মাদানী",
                bookCode = "ফাযায়েল",
                accentHex = "#2E4053",
                descriptionBn = "কুরআন ও বিশুদ্ধ সুন্নাহর আলোকে তাওহীদ, ইবাদত ও নেক আমলের ফযীলত সম্পর্কিত প্রামাণিক হাদিস সংকলন।"
            ),
            "bn_upodesh.sqlite" to ScholarBookMetadata(
                bookId = 113,
                titleBn = "নবীজীর (ﷺ) উপদেশমালা",
                titleAr = "وصايا الرسول ﷺ",
                titleEn = "Prophetic Counsels",
                authorBn = "নির্বাচিত হাদিস সংকলন",
                bookCode = "উপদেশ",
                accentHex = "#784212",
                descriptionBn = "সাহাবীগণের প্রতি রাসূলুল্লাহ (ﷺ)-এর অমূল্য নসীহত ও জীবনঘনিষ্ঠ উপদেশমূলক হাদিস সংকলন।"
            ),
            "bn_100-hadith.sqlite" to ScholarBookMetadata(
                bookId = 114,
                titleBn = "সুন্নাত ও আদব ১০০ হাদিস",
                titleAr = "مائة حديث",
                titleEn = "100 Sunnah Hadiths",
                authorBn = "নির্বাচিত মুহাদ্দিস সংকলন",
                bookCode = "১০০",
                accentHex = "#1F618D",
                descriptionBn = "দৈনন্দিন জীবনের সুন্নত, ঘুম, আহার, পোশাক ও মুমিন জীবনের অপরিহার্য ১০০টি হাদিস সংকলন।"
            ),
            "bn_40-hadith.sqlite" to ScholarBookMetadata(
                bookId = 115,
                titleBn = "ইমাম নববীর ৪০ হাদিস",
                titleAr = "الأربعون النووية",
                titleEn = "Al-Arba'in An-Nawawiyyah",
                authorBn = "ইমাম মুহিউদ্দীন ইয়াহইয়া আন-নববী (রহ.)",
                bookCode = "নববী",
                accentHex = "#B7950B",
                descriptionBn = "ইসলামের মূল ভিত্তি, ঈমান, ইখলাস, হালাল-হারাম ও তাকওয়া বিষয়ক ৪২টি কালজয়ী জাওয়ামিউল কালিম হাদিস।"
            ),
            "bn_jujul-rafayel-yadain.sqlite" to ScholarBookMetadata(
                bookId = 116,
                titleBn = "জুযউল রাফউল ইয়াদাইন",
                titleAr = "جزء رفع اليدين",
                titleEn = "Juz Raful Yadain",
                authorBn = "ইমাম মুহাম্মাদ ইবনু ইসমাঈল আল-বুখারী (রহ.)",
                bookCode = "রাফউল",
                accentHex = "#6C3483",
                descriptionBn = "সালাতে তাকবীরে তাহরীমা ও রুকুতে গমনাগমনের সময় রাফউল ইয়াদাইন করার সুন্নাহ ও সাহাবীগণের আমল।"
            ),
            "bn_dhaif-hadis-sirij.sqlite" to ScholarBookMetadata(
                bookId = 117,
                titleBn = "জাল ও যঈফ হাদিস সিরিজ",
                titleAr = "سلسلة الأحاديث الضعيفة",
                titleEn = "Dhaif & Mawdu Hadith Series",
                authorBn = "ইমাম নাসিরুদ্দীন আল-আলবানী (রহ.)",
                bookCode = "তাহকীক",
                accentHex = "#922B21",
                descriptionBn = "সমাজে প্রচলিত দুর্বল, বানোয়াট ও ভিত্তিহীন বর্ণনাসমূহ চিহ্নিতকরণ ও মুহাদ্দিসগণের তাহকীক সংকলন।"
            ),
            "bn_miskate-dhaif-hadis.sqlite" to ScholarBookMetadata(
                bookId = 118,
                titleBn = "মিশকাতের যঈফ হাদিসসমূহ",
                titleAr = "ضعيف مشكاة المصابيح",
                titleEn = "Dhaif Mishkat al-Masabih",
                authorBn = "ইমাম নাসিরুদ্দীন আল-আলবানী (রহ.)",
                bookCode = "তাহকীক",
                accentHex = "#943126",
                descriptionBn = "মিশকাতুল মাসাবীহ গ্রন্থে বর্ণিত যঈফ ও দুর্বল সনদের হাদিসসমূহের তাহকীক সংকলন।"
            ),
            "bn_ramadaner-durbol-hadis.sqlite" to ScholarBookMetadata(
                bookId = 119,
                titleBn = "রমাদানের দুর্বল ও জাল হাদিস",
                titleAr = "أحاديث رمضان الضعيفة",
                titleEn = "Weak Hadiths of Ramadan",
                authorBn = "গবেষণামূলক তাহকীক সংকলন",
                bookCode = "রমাদান",
                accentHex = "#7B241C",
                descriptionBn = "রমাদান ও সিয়াম সম্পর্কে সমাজে প্রচলিত ভিত্তিহীন ও দুর্বল বর্ণনাসমূহের প্রামাণিক তাহকীক।"
            )
        )
    }

    private val importedDir: File
        get() = File(context.filesDir, IMPORTED_DB_FOLDER).apply { mkdirs() }

    private val cacheWorkDir: File
        get() = File(context.cacheDir, WORK_CACHE_FOLDER).apply { mkdirs() }

    /**
     * Computes a lightweight deterministic fingerprint of all asset and user-imported SQLite files
     * without copying or opening SQLite databases, enabling <40ms warm/cold startup when Room is up to date.
     */
    suspend fun computeSourcesFingerprint(): String = withContext(Dispatchers.IO) {
        val assetSig = discoverAssetSqliteFiles().joinToString("|") { "${it.first}/${it.second}" }
        val importedSig = importedDir.listFiles()
            ?.filter { it.isFile && isSqliteFileName(it.name) }
            ?.sortedBy { it.name }
            ?.joinToString("|") { "${it.name}:${it.length()}:${it.lastModified()}" }
            .orEmpty()
        "v3#$assetSig#$importedSig"
    }

    suspend fun scanAndParseAllSqliteSources(): ParsedSqliteBundle = withContext(Dispatchers.IO) {
        val reports = mutableListOf<SqliteDatabaseFileReport>()
        val allBooks = mutableListOf<BookEntity>()
        val allChapters = mutableListOf<ChapterEntity>()
        val allSections = mutableListOf<SectionEntity>()
        val allHadiths = mutableListOf<HadithEntity>()

        // 1. Discover .sqlite / .db files in assets/databases and assets/databases/bangla-hadiths
        val discoveredAssets = discoverAssetSqliteFiles()

        var dynamicBookSeq = 200
        for ((assetFolder, fileName) in discoveredAssets) {
            val safeStageName = "asset_${fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")}"
            val stagedFile = File(cacheWorkDir, safeStageName)
            val assetRelPath = "$assetFolder/$fileName"
            runCatching {
                context.assets.open(assetRelPath).use { input ->
                    FileOutputStream(stagedFile).use { output ->
                        input.copyTo(output)
                    }
                }
                val fallbackBookId = KNOWN_BOOK_CATALOG[fileName]?.bookId ?: dynamicBookSeq++
                parseSingleSqliteFile(
                    file = stagedFile,
                    displayFileName = fileName,
                    originType = "ASSETS_FOLDER",
                    relativePath = "app/src/main/assets/$assetRelPath",
                    fallbackBookId = fallbackBookId,
                    reports = reports,
                    booksOut = allBooks,
                    chaptersOut = allChapters,
                    sectionsOut = allSections,
                    hadithsOut = allHadiths
                )
            }.onFailure { err ->
                reports.add(
                    SqliteDatabaseFileReport(
                        fileName = fileName,
                        originType = "ASSETS_FOLDER",
                        relativePath = "app/src/main/assets/$assetRelPath",
                        fileSizeKb = (stagedFile.length() / 1024).coerceAtLeast(1L),
                        detectedTables = emptyList(),
                        booksCount = 0,
                        chaptersCount = 0,
                        hadithsCount = 0,
                        isCompatible = false,
                        statusNoteBn = "ত্রুটি: ${err.localizedMessage ?: "অকার্যকর SQLite ফাইল"}"
                    )
                )
            }
        }

        // 2. Discover user-imported .sqlite / .db files in filesDir/imported_sqlite_sources/
        val importedFiles = importedDir.listFiles()?.filter { file ->
            file.isFile && isSqliteFileName(file.name)
        }?.sortedBy { it.name }.orEmpty()

        for (file in importedFiles) {
            runCatching {
                val fallbackBookId = KNOWN_BOOK_CATALOG[file.name]?.bookId ?: dynamicBookSeq++
                parseSingleSqliteFile(
                    file = file,
                    displayFileName = file.name,
                    originType = "USER_IMPORTED",
                    relativePath = "files/$IMPORTED_DB_FOLDER/${file.name}",
                    fallbackBookId = fallbackBookId,
                    reports = reports,
                    booksOut = allBooks,
                    chaptersOut = allChapters,
                    sectionsOut = allSections,
                    hadithsOut = allHadiths
                )
            }.onFailure { err ->
                reports.add(
                    SqliteDatabaseFileReport(
                        fileName = file.name,
                        originType = "USER_IMPORTED",
                        relativePath = "files/$IMPORTED_DB_FOLDER/${file.name}",
                        fileSizeKb = (file.length() / 1024).coerceAtLeast(1L),
                        detectedTables = emptyList(),
                        booksCount = 0,
                        chaptersCount = 0,
                        hadithsCount = 0,
                        isCompatible = false,
                        statusNoteBn = "ইম্পোর্ট ত্রুটি: ${err.localizedMessage ?: "স্কিমা পাওয়া যায়নি"}"
                    )
                )
            }
        }

        ParsedSqliteBundle(
            reports = reports,
            books = allBooks.distinctBy { it.id }.sortedBy { it.id },
            chapters = allChapters.distinctBy { it.id },
            sections = allSections.distinctBy { it.id },
            hadiths = allHadiths.distinctBy { it.id }
        )
    }

    private fun discoverAssetSqliteFiles(): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        val foldersToScan = listOf(ASSETS_BANGLA_HADITHS_SUBFOLDER, ASSETS_DB_FOLDER)
        for (folder in foldersToScan) {
            val entries = runCatching { context.assets.list(folder).orEmpty().toList() }.getOrDefault(emptyList())
            for (name in entries) {
                if (isSqliteFileName(name)) {
                    result.add(folder to name)
                }
            }
        }
        // Sort by known bookId first so the primary books load in clean scholarly order
        return result.sortedWith(
            compareBy<Pair<String, String>> { KNOWN_BOOK_CATALOG[it.second]?.bookId ?: 999 }
                .thenBy { it.second }
        )
    }

    private fun isSqliteFileName(name: String): Boolean {
        return name.endsWith(".sqlite", ignoreCase = true) ||
            name.endsWith(".db", ignoreCase = true) ||
            name.endsWith(".sqlite3", ignoreCase = true)
    }

    private fun sanitizeExternalFileName(rawName: String?): String {
        val base = File(rawName.orEmpty()).name
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .trimStart('.')
            .take(80)
        val safeBase = if (base.isBlank()) "custom_hadith_${System.currentTimeMillis()}" else base
        return if (isSqliteFileName(safeBase)) safeBase else "$safeBase.sqlite"
    }

    private fun hasValidSqlite3Header(file: File): Boolean {
        if (!file.exists() || file.length() < 100L) return false
        val header = ByteArray(16)
        val bytesRead = file.inputStream().use { it.read(header) }
        if (bytesRead < 16) return false
        return String(header, 0, 15, Charsets.US_ASCII) == "SQLite format 3"
    }

    suspend fun importExternalSqliteUri(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val sanitizedName = sanitizeExternalFileName(resolveFileName(uri))
            val targetDirCanonical = importedDir.canonicalPath
            val targetFile = File(importedDir, sanitizedName)
            require(targetFile.canonicalPath.startsWith("$targetDirCanonical${File.separator}")) {
                "অনিরাপদ ফাইলের নাম সনাক্ত হয়েছে"
            }

            val maxBytes = 120L * 1024L * 1024L // 120 MB safety cap
            var totalCopied = 0L
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        totalCopied += read
                        if (totalCopied > maxBytes) {
                            output.close()
                            targetFile.delete()
                            error("ফাইলটি ১২০ মেগাবাইটের চেয়ে বড় হওয়ায় ইম্পোর্ট করা যায়নি")
                        }
                        output.write(buffer, 0, read)
                    }
                }
            } ?: error("ফাইলটি খোলা সম্ভব হয়নি")

            if (!hasValidSqlite3Header(targetFile)) {
                targetFile.delete()
                error("নির্বাচিত ফাইলটি বৈধ SQLite 3 ডেটাবেস নয়")
            }

            val db = SQLiteDatabase.openDatabase(
                targetFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            val tables = try {
                getTableNames(db)
            } finally {
                db.close()
            }

            if (tables.isEmpty()) {
                targetFile.delete()
                error("ফাইলটিতে কোনো SQLite টেবিল পাওয়া যায়নি")
            }
            sanitizedName
        }
    }

    private fun quoteSqlIdentifier(identifier: String): String {
        return "\"${identifier.replace("\"", "\"\"")}\""
    }

    private fun parseSingleSqliteFile(
        file: File,
        displayFileName: String,
        originType: String,
        relativePath: String,
        fallbackBookId: Int,
        reports: MutableList<SqliteDatabaseFileReport>,
        booksOut: MutableList<BookEntity>,
        chaptersOut: MutableList<ChapterEntity>,
        sectionsOut: MutableList<SectionEntity>,
        hadithsOut: MutableList<HadithEntity>
    ) {
        val db = SQLiteDatabase.openDatabase(
            file.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
        try {
            val tables = getTableNames(db)
            val booksTable = tables.firstOrNull { it.equals("books", true) || it.equals("book", true) }
            val chaptersTable = tables.firstOrNull { it.equals("chapters", true) || it.equals("chapter", true) }
            val sectionsTable = tables.firstOrNull { it.equals("sections", true) || it.equals("section", true) }
            val hadithsTable = tables.firstOrNull { it.equals("hadiths", true) || it.equals("hadith", true) }

            val catalogMeta = KNOWN_BOOK_CATALOG[displayFileName] ?: buildFallbackMetadata(displayFileName, fallbackBookId)
            val currentBookId = catalogMeta.bookId

            var parsedBooks = 0
            var parsedChapters = 0
            var parsedSections = 0
            var parsedHadiths = 0

            val fileChapters = mutableListOf<ChapterEntity>()
            val chapterTitleMap = HashMap<Int, String>()
            val fileSections = mutableListOf<SectionEntity>()
            val sectionLookup = HashMap<Long, SectionEntity>()
            val fileHadiths = mutableListOf<HadithEntity>()

            if (chaptersTable != null) {
                db.rawQuery("SELECT * FROM ${quoteSqlIdentifier(chaptersTable)}", null).use { cursor ->
                    var seq = 1
                    while (cursor.moveToNext()) {
                        val rawChapterId = cursor.getIntFlex("chapter_id", "id", defaultVal = seq)
                        val bookId = cursor.getIntFlex("book_id", defaultVal = currentBookId)
                        val chapterUid = makeChapterUid(bookId, rawChapterId)
                        val number = cursor.getIntFlex("chapter_number", "number", "ch_no", "chapter_id", defaultVal = seq)
                        val titleBn = cursor.getStringFlex("title_bn", "title", "name_bn", defaultVal = "অধ্যায় $number").trim()
                        val titleAr = cursor.getStringFlex("title_ar", "arabic", defaultVal = "").trim()
                        val range = cursor.getStringFlex("hadith_range", "hadis_range", "range", defaultVal = "").trim()
                        val count = cursor.getIntFlex("hadith_count", "total", defaultVal = 0)

                        fileChapters.add(
                            ChapterEntity(
                                id = chapterUid,
                                bookId = bookId,
                                chapterNumber = if (number <= 0) seq else number,
                                titleBn = titleBn,
                                titleAr = titleAr,
                                hadithRange = range,
                                hadithCount = count
                            )
                        )
                        chapterTitleMap[chapterUid] = titleBn
                        seq++
                        parsedChapters++
                    }
                }
            }

            if (sectionsTable != null) {
                db.rawQuery("SELECT * FROM ${quoteSqlIdentifier(sectionsTable)}", null).use { cursor ->
                    var seq = 1
                    while (cursor.moveToNext()) {
                        val rawChapterId = cursor.getIntFlex("chapter_id", defaultVal = 1)
                        val rawSectionId = cursor.getIntFlex("section_id", "id", defaultVal = seq)
                        val bookId = cursor.getIntFlex("book_id", defaultVal = currentBookId)
                        val chapterUid = makeChapterUid(bookId, rawChapterId)
                        val sectionUid = makeSectionUid(bookId, rawChapterId, rawSectionId, seq)
                        val secNum = cursor.getStringFlex("section_number", "number", defaultVal = "পরিচ্ছেদ").trim()
                        val titleBn = cursor.getStringFlex("title_bn", "title", defaultVal = "").trim()
                        val prefaceBn = cursor.getStringFlex("preface_bn", "preface", defaultVal = "").trim()

                        val entity = SectionEntity(
                            id = sectionUid,
                            bookId = bookId,
                            chapterId = chapterUid,
                            sectionNumber = secNum.ifBlank { "পরিচ্ছেদ" },
                            titleBn = titleBn,
                            prefaceBn = prefaceBn
                        )
                        fileSections.add(entity)
                        val key = (bookId.toLong() shl 44) or
                            ((rawChapterId.toLong() and 0xFFFFFL) shl 24) or
                            (rawSectionId.toLong() and 0xFFFFFFL)
                        sectionLookup[key] = entity
                        seq++
                        parsedSections++
                    }
                }
            }

            val chapterHadithCounter = HashMap<Int, Int>()

            if (hadithsTable != null) {
                db.rawQuery("SELECT * FROM ${quoteSqlIdentifier(hadithsTable)}", null).use { cursor ->
                    var rowSeq = 1
                    while (cursor.moveToNext()) {
                        val bookId = cursor.getIntFlex("book_id", defaultVal = currentBookId)
                        val rawChapterId = cursor.getIntFlex("chapter_id", defaultVal = 1)
                        val rawSectionId = cursor.getIntFlex("section_id", defaultVal = 1)
                        val rawHadithId = cursor.getIntFlex("hadith_id", "hadith_number", "hadith_no", "number", "id", defaultVal = rowSeq)

                        val chapterUid = makeChapterUid(bookId, rawChapterId)
                        val sectionKey = (bookId.toLong() shl 44) or
                            ((rawChapterId.toLong() and 0xFFFFFL) shl 24) or
                            (rawSectionId.toLong() and 0xFFFFFFL)
                        val matchedSection = sectionLookup[sectionKey]
                        val sectionUid = matchedSection?.id ?: makeSectionUid(bookId, rawChapterId, rawSectionId, rowSeq)

                        val hadithUid = bookId * 1_000_000 + rowSeq
                        val hadithNumber = if (rawHadithId > 0) rawHadithId else rowSeq

                        val arabic = cursor.getStringFlex("arabic_text", "ar", "arabic", defaultVal = "").trim()
                        val arabicDiacless = cursor.getStringFlex("ar_diacless", "arabic_text_diacless", defaultVal = "").trim()
                        val bangla = cursor.getStringFlex("bangla_text", "content", "bn", "bangla", defaultVal = "").trim()
                        val translit = cursor.getStringFlex("transliteration_bn", "transliteration", defaultVal = "").trim()
                        val narratorRaw = cursor.getStringFlex("narrator_bn", "narrator", "rabi", defaultVal = "").trim()
                        val narrator = narratorRaw.ifBlank { "সাহাবী (রা.)" }
                        val explanation = cursor.getStringFlex("explanation_bn", "note", "explanation", defaultVal = "").trim()

                        val gradeId = cursor.getIntFlex("grade_id", defaultVal = 2)
                        val explicitGradeBn = cursor.getStringFlex("grade_bn", "grade", "grade_name", defaultVal = "").trim()
                        val (resolvedGradeBn, resolvedGradeType) = resolveTahqiqGrade(gradeId, explicitGradeBn, displayFileName)

                        val chapterTitle = chapterTitleMap[chapterUid].orEmpty()
                        val sectionTitle = matchedSection?.titleBn.orEmpty()
                        val reference = cursor.getStringFlex(
                            "reference_bn",
                            "reference",
                            defaultVal = "${catalogMeta.titleBn} • হাদিস নং $hadithNumber"
                        )
                        val intlNumber = cursor.getStringFlex(
                            "international_number",
                            "ref_no",
                            defaultVal = "${catalogMeta.titleBn} • অধ্যায়: ${chapterTitle.ifBlank { rawChapterId.toString() }}"
                        )

                        val explicitTags = cursor.getStringFlex("topic_tags", "tags", defaultVal = "").trim()
                        val tags = explicitTags.ifBlank {
                            inferTopicTags(chapterTitle, sectionTitle, bangla)
                        }

                        if (bangla.isNotBlank() || arabic.isNotBlank()) {
                            fileHadiths.add(
                                HadithEntity(
                                    id = hadithUid,
                                    bookId = bookId,
                                    chapterId = chapterUid,
                                    sectionId = sectionUid,
                                    hadithNumber = hadithNumber,
                                    internationalNumber = intlNumber,
                                    arabicText = arabic,
                                    arabicTextDiacless = arabicDiacless,
                                    banglaText = bangla,
                                    transliterationBn = translit,
                                    narratorBn = narrator,
                                    explanationBn = explanation,
                                    gradeId = gradeId,
                                    gradeBn = resolvedGradeBn,
                                    gradeType = resolvedGradeType,
                                    referenceBn = reference,
                                    topicTags = tags
                                )
                            )
                            chapterHadithCounter[chapterUid] = (chapterHadithCounter[chapterUid] ?: 0) + 1
                            parsedHadiths++
                            rowSeq++
                        }
                    }
                }
            }

            // Update chapter hadith counts if not explicitly stored
            val enrichedChapters = fileChapters.map { ch ->
                val actualCount = chapterHadithCounter[ch.id] ?: ch.hadithCount
                val computedRange = if (ch.hadithRange.isNotBlank()) {
                    ch.hadithRange
                } else if (actualCount > 0) {
                    "$actualCount টি হাদিস"
                } else {
                    ""
                }
                ch.copy(
                    hadithCount = actualCount,
                    hadithRange = computedRange
                )
            }

            if (booksTable != null) {
                db.rawQuery("SELECT * FROM ${quoteSqlIdentifier(booksTable)}", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getIntFlex("id", "book_id", defaultVal = currentBookId)
                        val titleBn = cursor.getStringFlex("title_bn", "title", "book_name", "name_bn", defaultVal = catalogMeta.titleBn)
                        val titleAr = cursor.getStringFlex("title_ar", "ar_title", "arabic_title", defaultVal = catalogMeta.titleAr)
                        val titleEn = cursor.getStringFlex("title_en", "en_title", defaultVal = catalogMeta.titleEn)
                        val authorBn = cursor.getStringFlex("author_bn", "author", "imam", defaultVal = catalogMeta.authorBn)
                        val hadithCount = cursor.getIntFlex("hadith_count", "number_of_hadis", "total_hadith", defaultVal = parsedHadiths)
                        val chapterCount = cursor.getIntFlex("chapter_count", "total_chapter", defaultVal = parsedChapters)
                        val bookCode = cursor.getStringFlex("book_code", "abvr_code", "code", defaultVal = catalogMeta.bookCode)
                        val accentHex = cursor.getStringFlex("accent_hex", "color_code", defaultVal = catalogMeta.accentHex)
                        val descBn = cursor.getStringFlex("description_bn", "book_descr", "description", defaultVal = catalogMeta.descriptionBn)

                        booksOut.add(
                            BookEntity(
                                id = id,
                                titleBn = titleBn,
                                titleAr = titleAr,
                                titleEn = titleEn,
                                authorBn = authorBn,
                                hadithCount = if (hadithCount > 0) hadithCount else parsedHadiths,
                                chapterCount = if (chapterCount > 0) chapterCount else parsedChapters,
                                bookCode = bookCode,
                                accentHex = accentHex,
                                descriptionBn = descBn,
                                sourceFile = displayFileName
                            )
                        )
                        parsedBooks++
                    }
                }
            }

            // If no explicit `books` table exists in this SQLite file, synthesize the BookEntity from our catalog metadata
            if (parsedBooks == 0 && (parsedHadiths > 0 || parsedChapters > 0)) {
                booksOut.add(
                    BookEntity(
                        id = currentBookId,
                        titleBn = catalogMeta.titleBn,
                        titleAr = catalogMeta.titleAr,
                        titleEn = catalogMeta.titleEn,
                        authorBn = catalogMeta.authorBn,
                        hadithCount = parsedHadiths,
                        chapterCount = enrichedChapters.size,
                        bookCode = catalogMeta.bookCode,
                        accentHex = catalogMeta.accentHex,
                        descriptionBn = catalogMeta.descriptionBn,
                        sourceFile = displayFileName
                    )
                )
                parsedBooks = 1
            }

            chaptersOut.addAll(enrichedChapters)
            sectionsOut.addAll(fileSections)
            hadithsOut.addAll(fileHadiths)

            val isCompatible = parsedBooks > 0 && parsedHadiths > 0
            reports.add(
                SqliteDatabaseFileReport(
                    fileName = displayFileName,
                    originType = originType,
                    relativePath = relativePath,
                    fileSizeKb = (file.length() / 1024).coerceAtLeast(1L),
                    detectedTables = tables,
                    booksCount = parsedBooks,
                    chaptersCount = parsedChapters,
                    hadithsCount = parsedHadiths,
                    isCompatible = isCompatible,
                    statusNoteBn = if (isCompatible) {
                        "সক্রিয় ও ইনডেক্সড (${catalogMeta.titleBn}: $parsedChapters টি অধ্যায়, $parsedSections টি পরিচ্ছেদ, $parsedHadiths টি হাদিস)"
                    } else {
                        "টেবিল পাওয়া গেছে (${tables.joinToString()}), কিন্তু হাদিস সারি নেই"
                    }
                )
            )
        } finally {
            db.close()
        }
    }

    private fun makeChapterUid(bookId: Int, rawChapterId: Int): Int {
        return bookId * 10_000 + (rawChapterId + 100)
    }

    private fun makeSectionUid(bookId: Int, rawChapterId: Int, rawSectionId: Int, seq: Int): Int {
        return bookId * 1_000_000 + (rawChapterId + 50) * 5_000 + (if (rawSectionId > 0) rawSectionId else seq)
    }

    private fun buildFallbackMetadata(fileName: String, bookId: Int): ScholarBookMetadata {
        val cleanName = fileName
            .removePrefix("bn_")
            .removeSuffix(".sqlite")
            .removeSuffix(".db")
            .replace('-', ' ')
            .replace('_', ' ')
            .replaceFirstChar { it.uppercase() }
        return ScholarBookMetadata(
            bookId = bookId,
            titleBn = "হাদিস সংকলন ($cleanName)",
            titleAr = "كتاب الحديث",
            titleEn = cleanName,
            authorBn = "মুহাদ্দিস ইমাম সংকলন",
            bookCode = "হাদিস",
            accentHex = "#0D6E52",
            descriptionBn = "$fileName ডেটাবেস হতে লোডকৃত বিশুদ্ধ হাদিস ও অধ্যায় সংকলন।"
        )
    }

    private fun resolveTahqiqGrade(
        gradeId: Int,
        explicitGradeBn: String,
        fileName: String
    ): Pair<String, String> {
        if (explicitGradeBn.isNotBlank()) {
            return explicitGradeBn to inferGradeType(explicitGradeBn)
        }
        val isDhaifBook = fileName.contains("dhaif", ignoreCase = true) || fileName.contains("durbol", ignoreCase = true)
        return when (gradeId) {
            2 -> "সহীহ হাদিস" to "SAHIH"
            3 -> "হাসান হাদিস" to "HASAN"
            4 -> "যঈফ (দুর্বল)" to "DAIF"
            5 -> "জাল / মাওযূ" to "MAWDU"
            6 -> "সহীহ লিগাইরিহী" to "SAHIH"
            7 -> "হাসান সহীহ" to "SAHIH"
            8 -> "শয (বিচ্ছিন্ন)" to "DAIF"
            9 -> "মুনকার" to "DAIF"
            10 -> "মাতরূক" to "DAIF"
            12 -> "হাসান লিগাইরিহী" to "HASAN"
            13 -> "সনদ দুর্বল" to "DAIF"
            14 -> "মওকূফ সহীহ" to "SAHIH"
            15 -> "সনদ সহীহ" to "SAHIH"
            16 -> "সনদ যঈফ" to "DAIF"
            18 -> "মাকতূ" to "HASAN"
            19 -> "সহীহ মওকূফ" to "SAHIH"
            20 -> "হাসান মওকূফ" to "HASAN"
            21 -> "বাতিল / ভিত্তিহীন" to "MAWDU"
            34 -> "সহীহ (মুত্তাফাকুন আলাইহি)" to "SAHIH"
            1 -> if (isDhaifBook) "যঈফ / তাহকীককৃত" to "DAIF" else "তাহকীককৃত" to "SAHIH"
            else -> if (isDhaifBook) "যঈফ হাদিস" to "DAIF" else "সহীহ হাদিস" to "SAHIH"
        }
    }

    private fun inferTopicTags(chapterTitle: String, sectionTitle: String, banglaText: String): String {
        val tags = mutableSetOf<String>()
        val combined = "$chapterTitle $sectionTitle ${banglaText.take(220)}"
        val keywordMap = listOf(
            "ঈমান" to "ঈমান",
            "তাওহীদ" to "তাওহীদ",
            "সালাত" to "সালাত",
            "নামায" to "সালাত",
            "ওজু" to "পবিত্রতা",
            "পবিত্র" to "পবিত্রতা",
            "যাকাত" to "যাকাত",
            "সিয়াম" to "সিয়াম",
            "রোযা" to "সিয়াম",
            "হজ্জ" to "হজ্জ",
            "পিতা-মাতা" to "আখলাক",
            "আদব" to "আদব",
            "শিষ্টাচার" to "আখলাক",
            "দোয়া" to "দুআ ও যিকর",
            "যিকির" to "দুআ ও যিকর",
            "ইলম" to "ইলম",
            "জ্ঞান" to "ইলম",
            "রাফউল" to "সালাত",
            "জান্নাত" to "আখিরাত",
            "তাকওয়া" to "তাকওয়া",
            "সুন্নত" to "সুন্নাহ"
        )
        for ((kw, tag) in keywordMap) {
            if (combined.contains(kw)) {
                tags.add(tag)
                if (tags.size >= 3) break
            }
        }
        if (tags.isEmpty()) {
            val cleanCh = chapterTitle.take(18).trim()
            if (cleanCh.isNotBlank()) tags.add(cleanCh) else tags.add("সুন্নাহ")
        }
        return tags.joinToString(",")
    }

    private fun getTableNames(db: SQLiteDatabase): List<String> {
        val list = mutableListOf<String>()
        db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%'",
            null
        ).use { c ->
            while (c.moveToNext()) {
                list.add(c.getString(0))
            }
        }
        return list
    }

    private fun inferGradeType(gradeBn: String): String {
        return when {
            gradeBn.contains("জাল", ignoreCase = true) || gradeBn.contains("মাওযূ", ignoreCase = true) || gradeBn.contains("বাতিল", ignoreCase = true) -> "MAWDU"
            gradeBn.contains("যঈফ", ignoreCase = true) || gradeBn.contains("দঈফ", ignoreCase = true) || gradeBn.contains("দুর্বল", ignoreCase = true) -> "DAIF"
            gradeBn.contains("হাসান", ignoreCase = true) || gradeBn.contains("hasan", ignoreCase = true) -> "HASAN"
            else -> "SAHIH"
        }
    }

    private fun resolveFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) result = cursor.getString(idx)
                }
            }
        }
        if (result == null) {
            result = uri.path?.substringAfterLast('/')
        }
        return result
    }

    private fun Cursor.getStringFlex(vararg columnNames: String, defaultVal: String = ""): String {
        for (col in columnNames) {
            val idx = getColumnIndex(col)
            if (idx >= 0 && !isNull(idx)) {
                return getString(idx) ?: defaultVal
            }
        }
        return defaultVal
    }

    private fun Cursor.getIntFlex(vararg columnNames: String, defaultVal: Int = 0): Int {
        for (col in columnNames) {
            val idx = getColumnIndex(col)
            if (idx >= 0 && !isNull(idx)) {
                return runCatching { getInt(idx) }.getOrDefault(defaultVal)
            }
        }
        return defaultVal
    }
}
