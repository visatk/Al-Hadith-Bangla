package com.example.data.local.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ui.theme.ReaderThemeMode

@Immutable
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
    @ColumnInfo(name = "source_file") val sourceFile: String = "bn_hadis-somvar.sqlite"
)

@Immutable
@Entity(
    tableName = "chapters",
    indices = [Index(value = ["book_id"])]
)
data class ChapterEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "book_id") val bookId: Int,
    @ColumnInfo(name = "chapter_number") val chapterNumber: Int,
    @ColumnInfo(name = "title_bn") val titleBn: String,
    @ColumnInfo(name = "title_ar") val titleAr: String,
    @ColumnInfo(name = "hadith_range") val hadithRange: String,
    @ColumnInfo(name = "hadith_count") val hadithCount: Int
)

@Immutable
@Entity(
    tableName = "sections",
    indices = [Index(value = ["chapter_id"]), Index(value = ["book_id"])]
)
data class SectionEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "book_id") val bookId: Int,
    @ColumnInfo(name = "chapter_id") val chapterId: Int,
    @ColumnInfo(name = "section_number") val sectionNumber: String,
    @ColumnInfo(name = "title_bn") val titleBn: String,
    @ColumnInfo(name = "preface_bn") val prefaceBn: String
)

@Immutable
@Entity(
    tableName = "hadiths",
    indices = [
        Index(value = ["book_id"]),
        Index(value = ["chapter_id"]),
        Index(value = ["hadith_number"]),
        Index(value = ["grade_type"])
    ]
)
data class HadithEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "book_id") val bookId: Int,
    @ColumnInfo(name = "chapter_id") val chapterId: Int,
    @ColumnInfo(name = "section_id") val sectionId: Int,
    @ColumnInfo(name = "hadith_number") val hadithNumber: Int,
    @ColumnInfo(name = "international_number") val internationalNumber: String,
    @ColumnInfo(name = "arabic_text") val arabicText: String,
    @ColumnInfo(name = "arabic_text_diacless") val arabicTextDiacless: String = "",
    @ColumnInfo(name = "bangla_text") val banglaText: String,
    @ColumnInfo(name = "transliteration_bn") val transliterationBn: String,
    @ColumnInfo(name = "narrator_bn") val narratorBn: String,
    @ColumnInfo(name = "explanation_bn") val explanationBn: String,
    @ColumnInfo(name = "grade_id") val gradeId: Int = 2,
    @ColumnInfo(name = "grade_bn") val gradeBn: String,
    @ColumnInfo(name = "grade_type") val gradeType: String, // SAHIH, HASAN, DAIF, MAWDU
    @ColumnInfo(name = "reference_bn") val referenceBn: String,
    @ColumnInfo(name = "topic_tags") val topicTags: String
)

@Immutable
@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name_ar") val nameAr: String,
    @ColumnInfo(name = "transliteration_en") val transliterationEn: String,
    @ColumnInfo(name = "name_bn") val nameBn: String,
    @ColumnInfo(name = "meaning_bn") val meaningBn: String,
    @ColumnInfo(name = "revelation_type") val revelationType: String, // meccan or medinan
    @ColumnInfo(name = "revelation_bn") val revelationBn: String, // মাক্কী or মাদানী
    @ColumnInfo(name = "total_verses") val totalVerses: Int
)

@Immutable
@Entity(
    tableName = "ayahs",
    indices = [Index(value = ["surah_id"]), Index(value = ["ayah_number"])]
)
data class AyahEntity(
    @PrimaryKey val id: Int, // surahId * 1000 + ayahNumber
    @ColumnInfo(name = "surah_id") val surahId: Int,
    @ColumnInfo(name = "surah_name_bn") val surahNameBn: String,
    @ColumnInfo(name = "surah_name_ar") val surahNameAr: String,
    @ColumnInfo(name = "ayah_number") val ayahNumber: Int,
    @ColumnInfo(name = "arabic_text") val arabicText: String,
    @ColumnInfo(name = "bangla_translation") val banglaTranslation: String
)

@Immutable
@Entity(tableName = "bookmark_collections")
data class BookmarkCollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name_bn") val nameBn: String,
    @ColumnInfo(name = "color_hex") val colorHex: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Immutable
@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val hadithId: Int,
    @ColumnInfo(name = "collection_name") val collectionName: String,
    @ColumnInfo(name = "personal_note") val personalNote: String = "",
    @ColumnInfo(name = "saved_at") val savedAt: Long = System.currentTimeMillis()
)

@Immutable
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

@Immutable
data class QuranReadingProgress(
    val surahId: Int,
    val surahNameBn: String,
    val ayahNumber: Int
)

@Immutable
data class BookmarkedHadithWithDetail(
    @Embedded val hadith: HadithEntity,
    @ColumnInfo(name = "collection_name") val collectionName: String,
    @ColumnInfo(name = "personal_note") val personalNote: String,
    @ColumnInfo(name = "saved_at") val savedAt: Long
)

@Immutable
data class SqliteDatabaseFileReport(
    val fileName: String,
    val originType: String, // "ASSETS_FOLDER", "QURAN_JSON", or "USER_IMPORTED"
    val relativePath: String,
    val fileSizeKb: Long,
    val detectedTables: List<String>,
    val booksCount: Int,
    val chaptersCount: Int,
    val hadithsCount: Int,
    val isCompatible: Boolean,
    val statusNoteBn: String
)

@Immutable
data class ReaderPreferences(
    val arabicFontSizeSp: Float = 26f,
    val banglaFontSizeSp: Float = 17f,
    val lineHeightMultiplier: Float = 1.65f,
    val showArabic: Boolean = true,
    val showTransliteration: Boolean = false,
    val showExplanation: Boolean = true,
    val showTashkeel: Boolean = true,
    val eyeCareWarmFilter: Float = 0.0f, // 0.0f..0.28f amber blue-light filter
    val keepScreenOnWhileReading: Boolean = true,
    val themeMode: ReaderThemeMode = ReaderThemeMode.MANUSCRIPT_LIGHT
)
