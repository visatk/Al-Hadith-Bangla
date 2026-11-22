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

/**
 * Modular SQLite Engine that scans `app/src/main/assets/databases/` and `filesDir/sqlite_sources/`
 * for any `.sqlite`, `.db`, or `.sqlite3` files, inspects their tables via native SQLiteDatabase,
 * adapts standard or community Bangla Hadith schemas, and returns structured entities for Room indexing.
 */
class SqliteAssetDataSource(private val context: Context) {

    companion object {
        const val ASSETS_DB_FOLDER = "databases"
        private const val IMPORTED_DB_FOLDER = "imported_sqlite_sources"
        private const val WORK_CACHE_FOLDER = "sqlite_inspection_cache"
    }

    private val importedDir: File
        get() = File(context.filesDir, IMPORTED_DB_FOLDER).apply { mkdirs() }

    private val cacheWorkDir: File
        get() = File(context.cacheDir, WORK_CACHE_FOLDER).apply { mkdirs() }

    suspend fun scanAndParseAllSqliteSources(): ParsedSqliteBundle = withContext(Dispatchers.IO) {
        val reports = mutableListOf<SqliteDatabaseFileReport>()
        val allBooks = mutableListOf<BookEntity>()
        val allChapters = mutableListOf<ChapterEntity>()
        val allSections = mutableListOf<SectionEntity>()
        val allHadiths = mutableListOf<HadithEntity>()

        // 1. Discover .sqlite / .db files in app/src/main/assets/databases/
        val assetFiles = runCatching {
            context.assets.list(ASSETS_DB_FOLDER)?.filter { name ->
                name.endsWith(".sqlite", ignoreCase = true) ||
                    name.endsWith(".db", ignoreCase = true) ||
                    name.endsWith(".sqlite3", ignoreCase = true)
            }.orEmpty()
        }.getOrDefault(emptyList())

        for (fileName in assetFiles) {
            val stagedFile = File(cacheWorkDir, "asset_$fileName")
            runCatching {
                context.assets.open("$ASSETS_DB_FOLDER/$fileName").use { input ->
                    FileOutputStream(stagedFile).use { output ->
                        input.copyTo(output)
                    }
                }
                parseSingleSqliteFile(
                    file = stagedFile,
                    displayFileName = fileName,
                    originType = "ASSETS_FOLDER",
                    relativePath = "app/src/main/assets/databases/$fileName",
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
                        relativePath = "app/src/main/assets/databases/$fileName",
                        fileSizeKb = stagedFile.length() / 1024,
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
            file.isFile && (
                file.name.endsWith(".sqlite", ignoreCase = true) ||
                    file.name.endsWith(".db", ignoreCase = true) ||
                    file.name.endsWith(".sqlite3", ignoreCase = true)
                )
        }.orEmpty()

        for (file in importedFiles) {
            runCatching {
                parseSingleSqliteFile(
                    file = file,
                    displayFileName = file.name,
                    originType = "USER_IMPORTED",
                    relativePath = "files/$IMPORTED_DB_FOLDER/${file.name}",
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
            books = allBooks.distinctBy { it.id },
            chapters = allChapters.distinctBy { it.id },
            sections = allSections.distinctBy { it.id },
            hadiths = allHadiths.distinctBy { it.id }
        )
    }

    suspend fun importExternalSqliteUri(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = resolveFileName(uri) ?: "custom_hadith_${System.currentTimeMillis()}.sqlite"
            val sanitizedName = if (
                fileName.endsWith(".sqlite", true) ||
                fileName.endsWith(".db", true) ||
                fileName.endsWith(".sqlite3", true)
            ) {
                fileName
            } else {
                "$fileName.sqlite"
            }

            val targetFile = File(importedDir, sanitizedName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: error("ফাইলটি খোলা সম্ভব হয়নি")

            // Verify it's a valid SQLite 3 database header
            val db = SQLiteDatabase.openDatabase(
                targetFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            val tables = getTableNames(db)
            db.close()

            if (tables.isEmpty()) {
                targetFile.delete()
                error("ফাইলটিতে কোনো SQLite টেবিল পাওয়া যায়নি")
            }
            sanitizedName
        }
    }

    private fun parseSingleSqliteFile(
        file: File,
        displayFileName: String,
        originType: String,
        relativePath: String,
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

            var parsedBooks = 0
            var parsedChapters = 0
            var parsedHadiths = 0

            if (booksTable != null) {
                db.rawQuery("SELECT * FROM $booksTable", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getIntFlex("id", "book_id", defaultVal = booksOut.size + 1)
                        val titleBn = cursor.getStringFlex("title_bn", "title", "book_name", "name_bn", defaultVal = "হাদিস গ্রন্থ #$id")
                        val titleAr = cursor.getStringFlex("title_ar", "ar_title", "arabic_title", defaultVal = "كتاب الحديث")
                        val titleEn = cursor.getStringFlex("title_en", "en_title", defaultVal = "Hadith Book")
                        val authorBn = cursor.getStringFlex("author_bn", "author", "imam", defaultVal = "মুহাদ্দিস ইমাম (রহ.)")
                        val hadithCount = cursor.getIntFlex("hadith_count", "number_of_hadis", "total_hadith", defaultVal = 0)
                        val chapterCount = cursor.getIntFlex("chapter_count", "total_chapter", defaultVal = 0)
                        val bookCode = cursor.getStringFlex("book_code", "abvr_code", "code", defaultVal = "HDT")
                        val accentHex = cursor.getStringFlex("accent_hex", "color_code", defaultVal = "#0D6E52")
                        val descBn = cursor.getStringFlex("description_bn", "book_descr", "description", defaultVal = "বিশুদ্ধ হাদিস সংকলন")

                        booksOut.add(
                            BookEntity(
                                id = id,
                                titleBn = titleBn,
                                titleAr = titleAr,
                                titleEn = titleEn,
                                authorBn = authorBn,
                                hadithCount = hadithCount,
                                chapterCount = chapterCount,
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

            if (chaptersTable != null) {
                db.rawQuery("SELECT * FROM $chaptersTable", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getIntFlex("id", "chapter_id", defaultVal = chaptersOut.size + 1000)
                        val bookId = cursor.getIntFlex("book_id", defaultVal = 1)
                        val number = cursor.getIntFlex("chapter_number", "number", "ch_no", defaultVal = 1)
                        val titleBn = cursor.getStringFlex("title_bn", "title", "name_bn", defaultVal = "অধ্যায় $number")
                        val titleAr = cursor.getStringFlex("title_ar", "arabic", defaultVal = "")
                        val range = cursor.getStringFlex("hadith_range", "range", defaultVal = "")
                        val count = cursor.getIntFlex("hadith_count", "total", defaultVal = 0)

                        chaptersOut.add(
                            ChapterEntity(
                                id = id,
                                bookId = bookId,
                                chapterNumber = number,
                                titleBn = titleBn,
                                titleAr = titleAr,
                                hadithRange = range,
                                hadithCount = count
                            )
                        )
                        parsedChapters++
                    }
                }
            }

            if (sectionsTable != null) {
                db.rawQuery("SELECT * FROM $sectionsTable", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getIntFlex("id", "section_id", defaultVal = sectionsOut.size + 5000)
                        val bookId = cursor.getIntFlex("book_id", defaultVal = 1)
                        val chapterId = cursor.getIntFlex("chapter_id", defaultVal = 101)
                        val secNum = cursor.getStringFlex("section_number", "number", defaultVal = "পরিচ্ছেদ")
                        val titleBn = cursor.getStringFlex("title_bn", "title", defaultVal = "")
                        val prefaceBn = cursor.getStringFlex("preface_bn", "preface", defaultVal = "")

                        sectionsOut.add(
                            SectionEntity(
                                id = id,
                                bookId = bookId,
                                chapterId = chapterId,
                                sectionNumber = secNum,
                                titleBn = titleBn,
                                prefaceBn = prefaceBn
                            )
                        )
                    }
                }
            }

            if (hadithsTable != null) {
                db.rawQuery("SELECT * FROM $hadithsTable", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getIntFlex("id", "hadith_id", defaultVal = hadithsOut.size + 1)
                        val bookId = cursor.getIntFlex("book_id", defaultVal = 1)
                        val chapterId = cursor.getIntFlex("chapter_id", defaultVal = 101)
                        val sectionId = cursor.getIntFlex("section_id", defaultVal = 0)
                        val hadithNumber = cursor.getIntFlex("hadith_number", "hadith_no", "number", defaultVal = id)
                        val intlNumber = cursor.getStringFlex("international_number", "ref_no", defaultVal = "হাদিস: $hadithNumber")
                        val arabic = cursor.getStringFlex("arabic_text", "ar", "arabic", defaultVal = "")
                        val bangla = cursor.getStringFlex("bangla_text", "bn", "bangla", defaultVal = "")
                        val translit = cursor.getStringFlex("transliteration_bn", "transliteration", defaultVal = "")
                        val narrator = cursor.getStringFlex("narrator_bn", "narrator", "rabi", defaultVal = "সাহাবী (রা.)")
                        val explanation = cursor.getStringFlex("explanation_bn", "note", "explanation", defaultVal = "")
                        val gradeBn = cursor.getStringFlex("grade_bn", "grade", "grade_name", defaultVal = "সহীহ হাদিস")
                        val gradeType = cursor.getStringFlex("grade_type", defaultVal = inferGradeType(gradeBn))
                        val reference = cursor.getStringFlex("reference_bn", "reference", defaultVal = intlNumber)
                        val tags = cursor.getStringFlex("topic_tags", "tags", defaultVal = "ঈমান,আমল")

                        hadithsOut.add(
                            HadithEntity(
                                id = id,
                                bookId = bookId,
                                chapterId = chapterId,
                                sectionId = sectionId,
                                hadithNumber = hadithNumber,
                                internationalNumber = intlNumber,
                                arabicText = arabic,
                                banglaText = bangla,
                                transliterationBn = translit,
                                narratorBn = narrator,
                                explanationBn = explanation,
                                gradeBn = gradeBn,
                                gradeType = gradeType,
                                referenceBn = reference,
                                topicTags = tags
                            )
                        )
                        parsedHadiths++
                    }
                }
            }

            val isCompatible = parsedBooks > 0 || parsedHadiths > 0
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
                        "সক্রিয় ও ইনডেক্সড ($parsedBooks টি গ্রন্থ, $parsedHadiths টি হাদিস)"
                    } else {
                        "টেবিল পাওয়া গেছে (${tables.joinToString()}), কিন্তু হাদিস সারি নেই"
                    }
                )
            )
        } finally {
            db.close()
        }
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
            gradeBn.contains("হাসান", ignoreCase = true) || gradeBn.contains("hasan", ignoreCase = true) -> "HASAN"
            gradeBn.contains("যঈফ", ignoreCase = true) || gradeBn.contains("দঈফ", ignoreCase = true) -> "DAIF"
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
