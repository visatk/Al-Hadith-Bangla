package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AyahEntity
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.BookmarkCollectionEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.BookmarkedHadithWithDetail
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.ReadingProgressEntity
import com.example.data.local.entity.SectionEntity
import com.example.data.local.entity.SurahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HadithDao {

    @Query("SELECT * FROM books ORDER BY id ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBookCount(): Int

    @Query("SELECT COUNT(*) FROM hadiths")
    suspend fun getHadithCount(): Int

    @Query("SELECT COUNT(*) FROM surahs")
    suspend fun getSurahCount(): Int

    @Query("SELECT COUNT(*) FROM ayahs")
    suspend fun getAyahCount(): Int

    @Query("SELECT * FROM chapters WHERE book_id = :bookId ORDER BY chapter_number ASC")
    fun getChaptersByBook(bookId: Int): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM sections WHERE chapter_id = :chapterId ORDER BY id ASC")
    fun getSectionsByChapter(chapterId: Int): Flow<List<SectionEntity>>

    @Query("SELECT * FROM hadiths WHERE chapter_id = :chapterId ORDER BY hadith_number ASC, id ASC")
    fun getHadithsByChapter(chapterId: Int): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths WHERE book_id = :bookId AND hadith_number = :hadithNumber LIMIT 1")
    suspend fun findHadithByBookAndNumber(bookId: Int, hadithNumber: Int): HadithEntity?

    @Query("SELECT * FROM hadiths ORDER BY id ASC LIMIT 300")
    fun getFeaturedHadiths(): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths WHERE id = :hadithId LIMIT 1")
    fun getHadithById(hadithId: Int): Flow<HadithEntity?>

    @Query(
        """
        SELECT * FROM hadiths 
        WHERE (:bookId IS NULL OR book_id = :bookId)
          AND (:gradeType IS NULL OR grade_type = :gradeType)
          AND (
              :query = '' 
              OR bangla_text LIKE '%' || :query || '%'
              OR arabic_text LIKE '%' || :query || '%'
              OR arabic_text_diacless LIKE '%' || :query || '%'
              OR narrator_bn LIKE '%' || :query || '%'
              OR explanation_bn LIKE '%' || :query || '%'
              OR reference_bn LIKE '%' || :query || '%'
              OR topic_tags LIKE '%' || :query || '%'
              OR CAST(hadith_number AS TEXT) = :query
          )
        ORDER BY book_id ASC, hadith_number ASC
        LIMIT 250
        """
    )
    fun searchHadiths(
        query: String,
        bookId: Int?,
        gradeType: String?
    ): Flow<List<HadithEntity>>

    // Al-Quran Queries
    @Query("SELECT * FROM surahs ORDER BY id ASC")
    fun getAllSurahs(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM ayahs WHERE surah_id = :surahId ORDER BY ayah_number ASC")
    fun getAyahsBySurah(surahId: Int): Flow<List<AyahEntity>>

    @Query(
        """
        SELECT * FROM ayahs
        WHERE :query != ''
          AND (
              bangla_translation LIKE '%' || :query || '%'
              OR arabic_text LIKE '%' || :query || '%'
              OR surah_name_bn LIKE '%' || :query || '%'
              OR (CAST(surah_id AS TEXT) || ':' || CAST(ayah_number AS TEXT)) = :query
              OR CAST(ayah_number AS TEXT) = :query
          )
        ORDER BY surah_id ASC, ayah_number ASC
        LIMIT 200
        """
    )
    fun searchAyahs(query: String): Flow<List<AyahEntity>>

    @Query(
        """
        SELECT h.*, b.collection_name, b.personal_note, b.saved_at
        FROM hadiths h
        INNER JOIN bookmarks b ON h.id = b.hadithId
        ORDER BY b.saved_at DESC
        """
    )
    fun getBookmarkedHadiths(): Flow<List<BookmarkedHadithWithDetail>>

    @Query("SELECT * FROM bookmarks")
    fun getAllBookmarksRaw(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmark_collections ORDER BY id ASC")
    fun getBookmarkCollections(): Flow<List<BookmarkCollectionEntity>>

    @Query("SELECT * FROM reading_progress WHERE id = 1 LIMIT 1")
    fun getReadingProgress(): Flow<ReadingProgressEntity?>

    @Query("DELETE FROM books")
    suspend fun clearBooks()

    @Query("DELETE FROM chapters")
    suspend fun clearChapters()

    @Query("DELETE FROM sections")
    suspend fun clearSections()

    @Query("DELETE FROM hadiths")
    suspend fun clearHadiths()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHadiths(hadiths: List<HadithEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<SurahEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE hadithId = :hadithId")
    suspend fun removeBookmark(hadithId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarkCollection(collection: BookmarkCollectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReadingProgress(progress: ReadingProgressEntity)

    @Query("SELECT COUNT(*) FROM bookmark_collections")
    suspend fun getCollectionCount(): Int
}
