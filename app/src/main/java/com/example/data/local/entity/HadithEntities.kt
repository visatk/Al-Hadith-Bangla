package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.ReaderThemeMode

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "title_bn") val titleBn: String,
    @ColumnInfo(name = "title_ar") val titleAr: String,
    @ColumnInfo(name = "title_en") val titleEn: String,
    @ColumnInfo(name = "author_bn") val authorBn: String,
    @ColumnInfo(name = "hadith_count") val hadithCount: Int,
    @ColumnInfo(name = "chapter_count") val chapterCount: Int,
    @ColumnInfo(name = "book_code") val bookCode: String,
    @ColumnInfo(name = "accent_hex") val accentHex: String,
    @ColumnInfo(name = "description_bn") val descriptionBn: String,
    @ColumnInfo(name = "source_file") val sourceFile: String = "al_hadith_kutub_sittah.sqlite"
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "book_id") val bookId: Int,
    @ColumnInfo(name = "chapter_number") val chapterNumber: Int,
    @ColumnInfo(name = "title_bn") val titleBn: String,
    @ColumnInfo(name = "title_ar") val titleAr: String,
    @ColumnInfo(name = "hadith_range") val hadithRange: String,
    @ColumnInfo(name = "hadith_count") val hadithCount: Int
)

@Entity(tableName = "sections")
data class SectionEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "book_id") val bookId: Int,
    @ColumnInfo(name = "chapter_id") val chapterId: Int,
    @ColumnInfo(name = "section_number") val sectionNumber: String,
    @ColumnInfo(name = "title_bn") val titleBn: String,
    @ColumnInfo(name = "preface_bn") val prefaceBn: String
)

@Entity(tableName = "hadiths")
data class HadithEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "book_id") val bookId: Int,
    @ColumnInfo(name = "chapter_id") val chapterId: Int,
    @ColumnInfo(name = "section_id") val sectionId: Int,
    @ColumnInfo(name = "hadith_number") val hadithNumber: Int,
    @ColumnInfo(name = "international_number") val internationalNumber: String,
    @ColumnInfo(name = "arabic_text") val arabicText: String,
    @ColumnInfo(name = "bangla_text") val banglaText: String,
    @ColumnInfo(name = "transliteration_bn") val transliterationBn: String,
    @ColumnInfo(name = "narrator_bn") val narratorBn: String,
    @ColumnInfo(name = "explanation_bn") val explanationBn: String,
    @ColumnInfo(name = "grade_bn") val gradeBn: String,
    @ColumnInfo(name = "grade_type") val gradeType: String, // SAHIH, HASAN, DAIF
    @ColumnInfo(name = "reference_bn") val referenceBn: String,
    @ColumnInfo(name = "topic_tags") val topicTags: String
)

@Entity(tableName = "bookmark_collections")
data class BookmarkCollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name_bn") val nameBn: String,
    @ColumnInfo(name = "color_hex") val colorHex: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val hadithId: Int,
    @ColumnInfo(name = "collection_name") val collectionName: String,
    @ColumnInfo(name = "personal_note") val personalNote: String = "",
    @ColumnInfo(name = "saved_at") val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "book_id") val bookId: Int,
    @ColumnInfo(name = "book_title_bn") val bookTitleBn: String,
    @ColumnInfo(name = "chapter_id") val chapterId: Int,
    @ColumnInfo(name = "chapter_title_bn") val chapterTitleBn: String,
    @ColumnInfo(name = "hadith_id") val hadithId: Int,
    @ColumnInfo(name = "hadith_number") val hadithNumber: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)

data class BookmarkedHadithWithDetail(
    @Embedded val hadith: HadithEntity,
    @ColumnInfo(name = "collection_name") val collectionName: String,
    @ColumnInfo(name = "personal_note") val personalNote: String,
    @ColumnInfo(name = "saved_at") val savedAt: Long
)

data class SqliteDatabaseFileReport(
    val fileName: String,
    val originType: String, // "ASSETS_FOLDER" or "USER_IMPORTED"
    val relativePath: String,
    val fileSizeKb: Long,
    val detectedTables: List<String>,
    val booksCount: Int,
    val chaptersCount: Int,
    val hadithsCount: Int,
    val isCompatible: Boolean,
    val statusNoteBn: String
)

data class ReaderPreferences(
    val arabicFontSizeSp: Float = 26f,
    val banglaFontSizeSp: Float = 17f,
    val showArabic: Boolean = true,
    val showTransliteration: Boolean = true,
    val showExplanation: Boolean = true,
    val showTashkeel: Boolean = true,
    val themeMode: ReaderThemeMode = ReaderThemeMode.MANUSCRIPT_LIGHT
)
