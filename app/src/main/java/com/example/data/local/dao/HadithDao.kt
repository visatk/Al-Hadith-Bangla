package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.BookmarkCollectionEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.BookmarkedHadithWithDetail
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.ReadingProgressEntity
import com.example.data.local.entity.SectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HadithDao {

    @Query("SELECT * FROM books ORDER BY id ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM chapters WHERE book_id = :bookId ORDER BY chapter_number ASC")
    fun getChaptersByBook(bookId: Int): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM sections WHERE chapter_id = :chapterId ORDER BY id ASC")
    fun getSectionsByChapter(chapterId: Int): Flow<List<SectionEntity>>

    @Query("SELECT * FROM hadiths WHERE chapter_id = :chapterId ORDER BY hadith_number ASC")
    fun getHadithsByChapter(chapterId: Int): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths ORDER BY id ASC")
    fun getAllHadiths(): Flow<List<HadithEntity>>

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
              OR narrator_bn LIKE '%' || :query || '%'
              OR explanation_bn LIKE '%' || :query || '%'
              OR reference_bn LIKE '%' || :query || '%'
              OR topic_tags LIKE '%' || :query || '%'
              OR CAST(hadith_number AS TEXT) = :query
          )
        ORDER BY book_id ASC, hadith_number ASC
        """
    )
    fun searchHadiths(
        query: String,
        bookId: Int?,
        gradeType: String?
    ): Flow<List<HadithEntity>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHadiths(hadiths: List<HadithEntity>)

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
