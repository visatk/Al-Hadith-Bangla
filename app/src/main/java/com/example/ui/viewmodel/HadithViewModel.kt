package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.BookmarkCollectionEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.BookmarkedHadithWithDetail
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.ReaderPreferences
import com.example.data.local.entity.ReadingProgressEntity
import com.example.data.local.entity.SectionEntity
import com.example.data.local.entity.SqliteDatabaseFileReport
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.HadithRepository
import com.example.data.sqlite.SqliteAssetDataSource
import com.example.ui.theme.ReaderThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppDestination(val titleBn: String) {
    HOME("হোম"),
    BOOKS("গ্রন্থসমূহ"),
    SEARCH("অনুসন্ধান"),
    BOOKMARKS("সংগ্রহ"),
    SQLITE_STUDIO("ডেটাবেস")
}

@OptIn(ExperimentalCoroutinesApi::class)
class HadithViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val sqliteDataSource = SqliteAssetDataSource(application)
    private val repository = HadithRepository(database.hadithDao(), sqliteDataSource)
    private val preferencesRepo = UserPreferencesRepository(application)

    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _selectedBook = MutableStateFlow<BookEntity?>(null)
    val selectedBook: StateFlow<BookEntity?> = _selectedBook.asStateFlow()

    private val _selectedChapter = MutableStateFlow<ChapterEntity?>(null)
    val selectedChapter: StateFlow<ChapterEntity?> = _selectedChapter.asStateFlow()

    private val _highlightedHadithId = MutableStateFlow<Int?>(null)
    val highlightedHadithId: StateFlow<Int?> = _highlightedHadithId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchBookFilter = MutableStateFlow<Int?>(null)
    val searchBookFilter: StateFlow<Int?> = _searchBookFilter.asStateFlow()

    private val _searchGradeFilter = MutableStateFlow<String?>(null)
    val searchGradeFilter: StateFlow<String?> = _searchGradeFilter.asStateFlow()

    private val _sqliteReports = MutableStateFlow<List<SqliteDatabaseFileReport>>(emptyList())
    val sqliteReports: StateFlow<List<SqliteDatabaseFileReport>> = _sqliteReports.asStateFlow()

    private val _isSyncingSqlite = MutableStateFlow(false)
    val isSyncingSqlite: StateFlow<Boolean> = _isSyncingSqlite.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    val books: StateFlow<List<BookEntity>> = repository.allBooksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHadiths: StateFlow<List<HadithEntity>> = repository.allHadithsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chaptersForSelectedBook: StateFlow<List<ChapterEntity>> = _selectedBook
        .flatMapLatest { book ->
            if (book == null) flowOf(emptyList()) else repository.getChaptersByBook(book.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sectionsForSelectedChapter: StateFlow<List<SectionEntity>> = _selectedChapter
        .flatMapLatest { chapter ->
            if (chapter == null) flowOf(emptyList()) else repository.getSectionsByChapter(chapter.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hadithsForSelectedChapter: StateFlow<List<HadithEntity>> = _selectedChapter
        .flatMapLatest { chapter ->
            if (chapter == null) flowOf(emptyList()) else repository.getHadithsByChapter(chapter.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults: StateFlow<List<HadithEntity>> = combine(
        _searchQuery,
        _searchBookFilter,
        _searchGradeFilter
    ) { query, bookId, gradeType ->
        Triple(query, bookId, gradeType)
    }.flatMapLatest { (query, bookId, gradeType) ->
        repository.searchHadiths(query, bookId, gradeType)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedHadiths: StateFlow<List<BookmarkedHadithWithDetail>> = repository.bookmarkedHadithsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawBookmarks: StateFlow<List<BookmarkEntity>> = repository.rawBookmarksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkCollections: StateFlow<List<BookmarkCollectionEntity>> = repository.bookmarkCollectionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readingProgress: StateFlow<ReadingProgressEntity?> = repository.readingProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val readerPreferences: StateFlow<ReaderPreferences> = preferencesRepo.readerPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReaderPreferences())

    init {
        refreshSqliteDatabases()
    }

    fun refreshSqliteDatabases() {
        viewModelScope.launch {
            _isSyncingSqlite.value = true
            val reports = repository.syncAllSqliteSourcesToRoom()
            _sqliteReports.value = reports
            _isSyncingSqlite.value = false
        }
    }

    fun importExternalSqliteDatabase(uri: Uri) {
        viewModelScope.launch {
            _isSyncingSqlite.value = true
            val result = repository.importExternalSqliteFile(uri)
            result.onSuccess { fileName ->
                val reports = repository.syncAllSqliteSourcesToRoom()
                _sqliteReports.value = reports
                _statusBannerMessage.value = "সফলভাবে '$fileName' ডেটাবেস যুক্ত ও ইনডেক্স করা হয়েছে!"
            }.onFailure { err ->
                _statusBannerMessage.value = "ডেটাবেস ইম্পোর্ট ব্যর্থ: ${err.localizedMessage}"
            }
            _isSyncingSqlite.value = false
        }
    }

    fun clearStatusBanner() {
        _statusBannerMessage.value = null
    }

    fun navigateToDestination(destination: AppDestination) {
        _currentDestination.value = destination
        if (destination != AppDestination.BOOKS) {
            _selectedChapter.value = null
        }
    }

    fun openBook(book: BookEntity) {
        _selectedBook.value = book
        _selectedChapter.value = null
        _currentDestination.value = AppDestination.BOOKS
    }

    fun openChapter(book: BookEntity, chapter: ChapterEntity, highlightHadithId: Int? = null) {
        _selectedBook.value = book
        _selectedChapter.value = chapter
        _highlightedHadithId.value = highlightHadithId
        _currentDestination.value = AppDestination.BOOKS
    }

    fun openHadithFromAnywhere(hadith: HadithEntity) {
        viewModelScope.launch {
            val book = books.value.firstOrNull { it.id == hadith.bookId } ?: return@launch
            _selectedBook.value = book
            repository.getChaptersByBook(book.id).collect { chapters ->
                val chapter = chapters.firstOrNull { it.id == hadith.chapterId } ?: chapters.firstOrNull()
                if (chapter != null) {
                    _selectedChapter.value = chapter
                    _highlightedHadithId.value = hadith.id
                    _currentDestination.value = AppDestination.BOOKS
                    repository.recordReadingProgress(book, chapter, hadith)
                }
                return@collect
            }
        }
    }

    fun navigateBackInBooks(): Boolean {
        return when {
            _selectedChapter.value != null -> {
                _selectedChapter.value = null
                _highlightedHadithId.value = null
                true
            }
            _selectedBook.value != null -> {
                _selectedBook.value = null
                true
            }
            else -> false
        }
    }

    fun openTopicSearch(topicTag: String) {
        _searchQuery.value = topicTag
        _searchBookFilter.value = null
        _searchGradeFilter.value = null
        _currentDestination.value = AppDestination.SEARCH
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSearchBookFilter(bookId: Int?) {
        _searchBookFilter.value = bookId
    }

    fun updateSearchGradeFilter(gradeType: String?) {
        _searchGradeFilter.value = gradeType
    }

    fun saveHadithBookmark(hadith: HadithEntity, collectionName: String, personalNote: String) {
        viewModelScope.launch {
            repository.saveBookmark(
                hadithId = hadith.id,
                collectionName = collectionName,
                personalNote = personalNote
            )
            _statusBannerMessage.value = "হাদিস #${hadith.hadithNumber} '$collectionName' ফোল্ডারে সংরক্ষিত হয়েছে"
        }
    }

    fun removeHadithBookmark(hadithId: Int) {
        viewModelScope.launch {
            repository.removeBookmark(hadithId)
            _statusBannerMessage.value = "বুকমার্ক তালিকা থেকে সরানো হয়েছে"
        }
    }

    fun createNewCollection(nameBn: String, colorHex: String) {
        viewModelScope.launch {
            repository.createBookmarkCollection(nameBn, colorHex)
            _statusBannerMessage.value = "'$nameBn' নামের নতুন সংগ্রহ ফোল্ডার তৈরি হয়েছে"
        }
    }

    fun markHadithAsRead(hadith: HadithEntity) {
        val book = _selectedBook.value ?: books.value.firstOrNull { it.id == hadith.bookId } ?: return
        val chapter = _selectedChapter.value ?: return
        viewModelScope.launch {
            repository.recordReadingProgress(book, chapter, hadith)
        }
    }

    // Reader Typography & Theme Controls
    fun setArabicFontSize(sizeSp: Float) {
        viewModelScope.launch { preferencesRepo.updateArabicFontSize(sizeSp) }
    }

    fun setBanglaFontSize(sizeSp: Float) {
        viewModelScope.launch { preferencesRepo.updateBanglaFontSize(sizeSp) }
    }

    fun setShowArabic(show: Boolean) {
        viewModelScope.launch { preferencesRepo.toggleShowArabic(show) }
    }

    fun setShowTransliteration(show: Boolean) {
        viewModelScope.launch { preferencesRepo.toggleShowTransliteration(show) }
    }

    fun setShowExplanation(show: Boolean) {
        viewModelScope.launch { preferencesRepo.toggleShowExplanation(show) }
    }

    fun setShowTashkeel(show: Boolean) {
        viewModelScope.launch { preferencesRepo.toggleShowTashkeel(show) }
    }

    fun setThemeMode(mode: ReaderThemeMode) {
        viewModelScope.launch { preferencesRepo.setThemeMode(mode) }
    }
}
