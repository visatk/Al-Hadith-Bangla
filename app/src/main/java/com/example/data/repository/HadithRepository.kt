package com.example.data.repository

import android.net.Uri
import com.example.data.local.dao.HadithDao
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.BookmarkCollectionEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.BookmarkedHadithWithDetail
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.ReadingProgressEntity
import com.example.data.local.entity.SectionEntity
import com.example.data.local.entity.SqliteDatabaseFileReport
import com.example.data.sqlite.SqliteAssetDataSource
import kotlinx.coroutines.flow.Flow

class HadithRepository(
    private val hadithDao: HadithDao,
    private val sqliteAssetDataSource: SqliteAssetDataSource
) {

    val allBooksFlow: Flow<List<BookEntity>> = hadithDao.getAllBooks()
    val allHadithsFlow: Flow<List<HadithEntity>> = hadithDao.getAllHadiths()
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

    fun searchHadiths(
        query: String,
        bookId: Int?,
        gradeType: String?
    ): Flow<List<HadithEntity>> = hadithDao.searchHadiths(
        query = query.trim(),
        bookId = bookId,
        gradeType = gradeType
    )

    suspend fun syncAllSqliteSourcesToRoom(): List<SqliteDatabaseFileReport> {
        // Ensure default bookmark collections exist
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

        val bundle = sqliteAssetDataSource.scanAndParseAllSqliteSources()
        if (bundle.books.isNotEmpty()) {
            hadithDao.insertBooks(bundle.books)
        }
        if (bundle.chapters.isNotEmpty()) {
            hadithDao.insertChapters(bundle.chapters)
        }
        if (bundle.sections.isNotEmpty()) {
            hadithDao.insertSections(bundle.sections)
        }
        if (bundle.hadiths.isNotEmpty()) {
            hadithDao.insertHadiths(bundle.hadiths)
        }
        return bundle.reports
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
