package com.example.data.repository

import android.net.Uri
import androidx.room.withTransaction
import com.example.data.local.dao.HadithDao
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.AyahEntity
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.BookmarkCollectionEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.BookmarkedHadithWithDetail
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.ReadingProgressEntity
import com.example.data.local.entity.SectionEntity
import com.example.data.local.entity.SqliteDatabaseFileReport
import com.example.data.local.entity.SurahEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.sqlite.QuranJsonDataSource
import com.example.data.sqlite.SqliteAssetDataSource
import kotlinx.coroutines.flow.Flow

class HadithRepository(
    private val database: AppDatabase,
    private val hadithDao: HadithDao,
    private val sqliteAssetDataSource: SqliteAssetDataSource,
    private val quranJsonDataSource: QuranJsonDataSource,
    private val preferencesRepository: UserPreferencesRepository
) {

    val allBooksFlow: Flow<List<BookEntity>> = hadithDao.getAllBooks()
    val featuredHadithsFlow: Flow<List<HadithEntity>> = hadithDao.getFeaturedHadiths()
    val allSurahsFlow: Flow<List<SurahEntity>> = hadithDao.getAllSurahs()
    val bookmarkedHadithsFlow: Flow<List<BookmarkedHadithWithDetail>> = hadithDao.getBookmarkedHadiths()
    val rawBookmarksFlow: Flow<List<BookmarkEntity>> = hadithDao.getAllBookmarksRaw()
    val bookmarkCollectionsFlow: Flow<List<BookmarkCollectionEntity>> = hadithDao.getBookmarkCollections()
    val readingProgressFlow: Flow<ReadingProgressEntity?> = hadithDao.getReadingProgress()

    fun getChaptersByBook(bookId: Int): Flow<List<ChapterEntity>> =
        hadithDao.getChaptersByBook(bookId)

    fun getSectionsByChapter(chapterId: Int): Flow<List<SectionEntity>> =
        hadithDao.getSectionsByChapter(chapterId)

    fun getHadithsByChapter(chapterId: Int): Flow<List<HadithEntity>> =
        hadithDao.getHadithsByChapter(chapterId)

    fun getAyahsBySurah(surahId: Int): Flow<List<AyahEntity>> =
        hadithDao.getAyahsBySurah(surahId)

    suspend fun findHadithByBookAndNumber(bookId: Int, hadithNumber: Int): HadithEntity? =
        hadithDao.findHadithByBookAndNumber(bookId, hadithNumber)

    suspend fun getTotalIndexedHadithCount(): Int = hadithDao.getHadithCount()

    suspend fun getTotalIndexedAyahCount(): Int = hadithDao.getAyahCount()

    fun searchHadiths(
        query: String,
        bookId: Int?,
        gradeType: String?
    ): Flow<List<HadithEntity>> = hadithDao.searchHadiths(
        query = query.trim(),
        bookId = bookId,
        gradeType = gradeType
    )

    fun searchAyahs(query: String): Flow<List<AyahEntity>> =
        hadithDao.searchAyahs(query.trim())

    /**
     * Synchronizes all bundled `.sqlite` / `.db` Hadith sources and `quran_bn.json` into Room.
     * Uses a fast zero-copy fingerprint check so subsequent app launches complete in <40ms,
     * and wraps multi-table writes in an atomic Room transaction for crash safety.
     */
    suspend fun syncAllSourcesToRoom(forceRefresh: Boolean = false): List<SqliteDatabaseFileReport> {
        ensureDefaultCollections()

        val currentFingerprint = sqliteAssetDataSource.computeSourcesFingerprint()
        val lastFingerprint = preferencesRepository.getLastSyncFingerprint()
        val existingHadithCount = hadithDao.getHadithCount()
        val existingSurahCount = hadithDao.getSurahCount()
        val existingAyahCount = hadithDao.getAyahCount()

        if (!forceRefresh &&
            currentFingerprint == lastFingerprint &&
            existingHadithCount > 0 &&
            existingSurahCount == 114 &&
            existingAyahCount > 0
        ) {
            return emptyList()
        }

        val reports = mutableListOf<SqliteDatabaseFileReport>()

        // 1. Scan & Sync Hadith SQLite databases atomically
        val bundle = sqliteAssetDataSource.scanAndParseAllSqliteSources()
        reports.addAll(bundle.reports)

        if (forceRefresh || existingHadithCount != bundle.hadiths.size || currentFingerprint != lastFingerprint) {
            database.withTransaction {
                hadithDao.clearBooks()
                hadithDao.clearChapters()
                hadithDao.clearSections()
                hadithDao.clearHadiths()

                if (bundle.books.isNotEmpty()) {
                    hadithDao.insertBooks(bundle.books)
                }
                if (bundle.chapters.isNotEmpty()) {
                    hadithDao.insertChapters(bundle.chapters)
                }
                if (bundle.sections.isNotEmpty()) {
                    for (chunk in bundle.sections.chunked(800)) {
                        hadithDao.insertSections(chunk)
                    }
                }
                if (bundle.hadiths.isNotEmpty()) {
                    for (chunk in bundle.hadiths.chunked(800)) {
                        hadithDao.insertHadiths(chunk)
                    }
                }
            }
        }

        // 2. Scan & Sync Al-Quran JSON database (quran_bn.json) atomically if needed
        if (forceRefresh || existingSurahCount != 114 || existingAyahCount == 0) {
            val quranBundle = quranJsonDataSource.parseQuranFromAssets()
            if (quranBundle.report != null) {
                reports.add(quranBundle.report)
            }
            if (quranBundle.surahs.isNotEmpty() && quranBundle.ayahs.isNotEmpty()) {
                database.withTransaction {
                    hadithDao.insertSurahs(quranBundle.surahs)
                    for (chunk in quranBundle.ayahs.chunked(1000)) {
                        hadithDao.insertAyahs(chunk)
                    }
                }
            }
        }

        preferencesRepository.saveLastSyncFingerprint(currentFingerprint)
        return reports
    }

    private suspend fun ensureDefaultCollections() {
        if (hadithDao.getCollectionCount() == 0) {
            hadithDao.insertBookmarkCollection(
                BookmarkCollectionEntity(nameBn = "পছন্দের হাদিস", colorHex = "#0D6E52")
            )
            hadithDao.insertBookmarkCollection(
                BookmarkCollectionEntity(nameBn = "দৈনন্দিন আমল ও সুন্নাহ", colorHex = "#C6932F")
            )
            hadithDao.insertBookmarkCollection(
                BookmarkCollectionEntity(nameBn = "দারস ও খুতবা নোট", colorHex = "#2E5A88")
            )
        }
    }

    suspend fun importExternalSqliteFile(uri: Uri): Result<String> {
        return sqliteAssetDataSource.importExternalSqliteUri(uri)
    }

    suspend fun saveBookmark(
        hadithId: Int,
        collectionName: String,
        personalNote: String
    ) {
        hadithDao.insertBookmark(
            BookmarkEntity(
                hadithId = hadithId,
                collectionName = collectionName,
                personalNote = personalNote.trim()
            )
        )
    }

    suspend fun removeBookmark(hadithId: Int) {
        hadithDao.removeBookmark(hadithId)
    }

    suspend fun createBookmarkCollection(nameBn: String, colorHex: String) {
        if (nameBn.isNotBlank()) {
            hadithDao.insertBookmarkCollection(
                BookmarkCollectionEntity(
                    nameBn = nameBn.trim(),
                    colorHex = colorHex
                )
            )
        }
    }

    suspend fun recordReadingProgress(
        book: BookEntity,
        chapter: ChapterEntity,
        hadith: HadithEntity
    ) {
        hadithDao.saveReadingProgress(
            ReadingProgressEntity(
                id = 1,
                bookId = book.id,
                bookTitleBn = book.titleBn,
                chapterId = chapter.id,
                chapterTitleBn = chapter.titleBn,
                hadithId = hadith.id,
                hadithNumber = hadith.hadithNumber,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
