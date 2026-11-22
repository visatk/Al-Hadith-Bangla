package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.AyahEntity
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.BookmarkCollectionEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.BookmarkedHadithWithDetail
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.QuranReadingProgress
import com.example.data.local.entity.ReaderPreferences
import com.example.data.local.entity.ReadingProgressEntity
import com.example.data.local.entity.SectionEntity
import com.example.data.local.entity.SqliteDatabaseFileReport
import com.example.data.local.entity.SurahEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.HadithRepository
import com.example.data.sqlite.QuranJsonDataSource
import com.example.data.sqlite.SqliteAssetDataSource
import com.example.ui.components.toEnglishDigits
import com.example.ui.theme.ReaderThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppDestination(val titleBn: String) {
    HOME("হোম"),
    BOOKS("হাদিস গ্রন্থ"),
    QURAN("আল-কুরআন"),
    SEARCH("অনুসন্ধান"),
    BOOKMARKS("সংগ্রহ")
}

enum class SearchScopeFilter(val labelBn: String) {
    ALL("সব (হাদিস ও কুরআন)"),
    HADITH_ONLY("শুধু হাদিস"),
    QURAN_ONLY("শুধু আল-কুরআন")
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class HadithViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val sqliteDataSource = SqliteAssetDataSource(application)
    private val quranDataSource = QuranJsonDataSource(application)
    private val preferencesRepo = UserPreferencesRepository(application)
    private val repository = HadithRepository(
        database = database,
        hadithDao = database.hadithDao(),
        sqliteAssetDataSource = sqliteDataSource,
        quranJsonDataSource = quranDataSource,
        preferencesRepository = preferencesRepo
    )

    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _isFocusMode = MutableStateFlow(false)
    val isFocusMode: StateFlow<Boolean> = _isFocusMode.asStateFlow()

    private val _selectedBook = MutableStateFlow<BookEntity?>(null)
    val selectedBook: StateFlow<BookEntity?> = _selectedBook.asStateFlow()

    private val _selectedChapter = MutableStateFlow<ChapterEntity?>(null)
    val selectedChapter: StateFlow<ChapterEntity?> = _selectedChapter.asStateFlow()

    private val _highlightedHadithId = MutableStateFlow<Int?>(null)
    val highlightedHadithId: StateFlow<Int?> = _highlightedHadithId.asStateFlow()

    // Al-Quran State
    private val _selectedSurah = MutableStateFlow<SurahEntity?>(null)
    val selectedSurah: StateFlow<SurahEntity?> = _selectedSurah.asStateFlow()

    private val _highlightedAyahNumber = MutableStateFlow<Int?>(null)
    val highlightedAyahNumber: StateFlow<Int?> = _highlightedAyahNumber.asStateFlow()

    // Unified Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchScopeFilter = MutableStateFlow(SearchScopeFilter.ALL)
    val searchScopeFilter: StateFlow<SearchScopeFilter> = _searchScopeFilter.asStateFlow()

    private val _searchBookFilter = MutableStateFlow<Int?>(null)
    val searchBookFilter: StateFlow<Int?> = _searchBookFilter.asStateFlow()

    private val _searchGradeFilter = MutableStateFlow<String?>(null)
    val searchGradeFilter: StateFlow<String?> = _searchGradeFilter.asStateFlow()

    private val _sqliteReports = MutableStateFlow<List<SqliteDatabaseFileReport>>(emptyList())
    val sqliteReports: StateFlow<List<SqliteDatabaseFileReport>> = _sqliteReports.asStateFlow()

    private val _totalIndexedHadiths = MutableStateFlow(0)
    val totalIndexedHadiths: StateFlow<Int> = _totalIndexedHadiths.asStateFlow()

    private val _totalIndexedAyahs = MutableStateFlow(0)
    val totalIndexedAyahs: StateFlow<Int> = _totalIndexedAyahs.asStateFlow()

    private val _isSyncingSqlite = MutableStateFlow(false)
    val isSyncingSqlite: StateFlow<Boolean> = _isSyncingSqlite.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    val books: StateFlow<List<BookEntity>> = repository.allBooksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHadiths: StateFlow<List<HadithEntity>> = repository.featuredHadithsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val surahs: StateFlow<List<SurahEntity>> = repository.allSurahsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ayahsForSelectedSurah: StateFlow<List<AyahEntity>> = _selectedSurah
        .flatMapLatest { surah ->
            if (surah == null) flowOf(emptyList()) else repository.getAyahsBySurah(surah.id)
        }
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

    private val debouncedSearchQuery = _searchQuery
        .debounce(200L)
        .distinctUntilChanged()

    val searchResults: StateFlow<List<HadithEntity>> = combine(
        debouncedSearchQuery,
        _searchBookFilter,
        _searchGradeFilter,
        _searchScopeFilter
    ) { query, bookId, gradeType, scope ->
        val normalized = query.trim().toEnglishDigits()
        when {
            scope == SearchScopeFilter.QURAN_ONLY -> null
            normalized.isBlank() && bookId == null && gradeType == null -> null
            else -> Triple(normalized, bookId, gradeType)
        }
    }.flatMapLatest { params ->
        if (params == null) flowOf(emptyList())
        else repository.searchHadiths(params.first, params.second, params.third)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quranSearchResults: StateFlow<List<AyahEntity>> = combine(
        debouncedSearchQuery,
        _searchScopeFilter
    ) { query, scope ->
        if (scope == SearchScopeFilter.HADITH_ONLY) null else query.trim().toEnglishDigits()
    }.flatMapLatest { normalizedQuery ->
        if (normalizedQuery.isNullOrBlank()) flowOf(emptyList())
        else repository.searchAyahs(normalizedQuery)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedHadiths: StateFlow<List<BookmarkedHadithWithDetail>> = repository.bookmarkedHadithsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawBookmarks: StateFlow<List<BookmarkEntity>> = repository.rawBookmarksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkCollections: StateFlow<List<BookmarkCollectionEntity>> = repository.bookmarkCollectionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readingProgress: StateFlow<ReadingProgressEntity?> = repository.readingProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val quranReadingProgress: StateFlow<QuranReadingProgress?> = preferencesRepo.quranReadingProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val readerPreferences: StateFlow<ReaderPreferences> = preferencesRepo.readerPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReaderPreferences())

    init {
        refreshSqliteDatabases(forceRefresh = false)
    }

    fun refreshSqliteDatabases(forceRefresh: Boolean = true) {
        viewModelScope.launch {
            _isSyncingSqlite.value = true
            val reports = repository.syncAllSourcesToRoom(forceRefresh = forceRefresh)
            _sqliteReports.value = reports
            _totalIndexedHadiths.value = repository.getTotalIndexedHadithCount()
            _totalIndexedAyahs.value = repository.getTotalIndexedAyahCount()
            _isSyncingSqlite.value = false
        }
    }

    fun importExternalSqliteDatabase(uri: Uri) {
        viewModelScope.launch {
            _isSyncingSqlite.value = true
            val result = repository.importExternalSqliteFile(uri)
            result.onSuccess { fileName ->
                val reports = repository.syncAllSourcesToRoom(forceRefresh = true)
                _sqliteReports.value = reports
                _totalIndexedHadiths.value = repository.getTotalIndexedHadithCount()
                _totalIndexedAyahs.value = repository.getTotalIndexedAyahCount()
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
        _isFocusMode.value = false
        if (destination != AppDestination.BOOKS) {
            _selectedChapter.value = null
        }
    }

    fun toggleFocusMode() {
        _isFocusMode.value = !_isFocusMode.value
    }

    fun setFocusMode(enabled: Boolean) {
        _isFocusMode.value = enabled
    }

    fun openBook(book: BookEntity) {
        _selectedBook.value = book
        _selectedChapter.value = null
        _isFocusMode.value = false
        _currentDestination.value = AppDestination.BOOKS
    }

    fun openChapter(book: BookEntity, chapter: ChapterEntity, highlightHadithId: Int? = null) {
        _selectedBook.value = book
        _selectedChapter.value = chapter
        _highlightedHadithId.value = highlightHadithId
        _currentDestination.value = AppDestination.BOOKS
    }

    fun openPreviousChapter() {
        val book = _selectedBook.value ?: return
        val current = _selectedChapter.value ?: return
        val list = chaptersForSelectedBook.value
        val idx = list.indexOfFirst { it.id == current.id }
        if (idx > 0) {
            openChapter(book, list[idx - 1])
        }
    }

    fun openNextChapter() {
        val book = _selectedBook.value ?: return
        val current = _selectedChapter.value ?: return
        val list = chaptersForSelectedBook.value
        val idx = list.indexOfFirst { it.id == current.id }
        if (idx in 0 until list.lastIndex) {
            openChapter(book, list[idx + 1])
        }
    }

    fun openHadithFromAnywhere(hadith: HadithEntity) {
        viewModelScope.launch {
            val book = books.value.firstOrNull { it.id == hadith.bookId } ?: return@launch
            _selectedBook.value = book
            val chapters = repository.getChaptersByBook(book.id).first()
            val chapter = chapters.firstOrNull { it.id == hadith.chapterId } ?: chapters.firstOrNull()
            if (chapter != null) {
                _selectedChapter.value = chapter
                _highlightedHadithId.value = hadith.id
                _currentDestination.value = AppDestination.BOOKS
                repository.recordReadingProgress(book, chapter, hadith)
            }
        }
    }

    fun jumpToHadithNumberInBook(book: BookEntity, hadithNumber: Int) {
        viewModelScope.launch {
            val target = repository.findHadithByBookAndNumber(book.id, hadithNumber)
            if (target != null) {
                openHadithFromAnywhere(target)
                _statusBannerMessage.value = "${book.titleBn} • হাদিস #${target.hadithNumber}-এ নেওয়া হয়েছে"
            } else {
                _statusBannerMessage.value = "${book.titleBn}-এ হাদিস নং #$hadithNumber পাওয়া যায়নি"
            }
        }
    }

    fun openSurah(surah: SurahEntity, highlightAyahNumber: Int? = null) {
        _selectedSurah.value = surah
        _highlightedAyahNumber.value = highlightAyahNumber
        _currentDestination.value = AppDestination.QURAN
        markSurahAyahAsRead(surah, highlightAyahNumber ?: 1)
    }

    fun openPreviousSurah() {
        val current = _selectedSurah.value ?: return
        val list = surahs.value
        val idx = list.indexOfFirst { it.id == current.id }
        if (idx > 0) {
            openSurah(list[idx - 1])
        }
    }

    fun openNextSurah() {
        val current = _selectedSurah.value ?: return
        val list = surahs.value
        val idx = list.indexOfFirst { it.id == current.id }
        if (idx in 0 until list.lastIndex) {
            openSurah(list[idx + 1])
        }
    }

    fun markSurahAyahAsRead(surah: SurahEntity, ayahNumber: Int) {
        viewModelScope.launch {
            preferencesRepo.saveQuranReadingProgress(
                surahId = surah.id,
                surahNameBn = surah.nameBn,
                ayahNumber = ayahNumber.coerceIn(1, surah.totalVerses.coerceAtLeast(1))
            )
        }
    }

    fun openAyahFromSearch(ayah: AyahEntity) {
        val targetSurah = surahs.value.firstOrNull { it.id == ayah.surahId }
        if (targetSurah != null) {
            openSurah(targetSurah, highlightAyahNumber = ayah.ayahNumber)
        }
    }

    fun jumpToSurahAndAyah(surahId: Int, ayahNumber: Int) {
        val targetSurah = surahs.value.firstOrNull { it.id == surahId }
        if (targetSurah != null) {
            val validAyah = ayahNumber.coerceIn(1, targetSurah.totalVerses.coerceAtLeast(1))
            openSurah(targetSurah, highlightAyahNumber = validAyah)
            _statusBannerMessage.value = "সূরা ${targetSurah.nameBn} • আয়াত #$validAyah-এ নেওয়া হয়েছে"
        } else {
            _statusBannerMessage.value = "সূরা নং #$surahId পাওয়া যায়নি (১-১১৪ এর মধ্যে দিন)"
        }
    }

    fun navigateBackInQuran(): Boolean {
        if (_isFocusMode.value) {
            _isFocusMode.value = false
            return true
        }
        return if (_selectedSurah.value != null) {
            _selectedSurah.value = null
            _highlightedAyahNumber.value = null
            true
        } else {
            false
        }
    }

    fun navigateBackInBooks(): Boolean {
        if (_isFocusMode.value) {
            _isFocusMode.value = false
            return true
        }
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
        _searchScopeFilter.value = SearchScopeFilter.ALL
        _isFocusMode.value = false
        _currentDestination.value = AppDestination.SEARCH
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSearchScopeFilter(scope: SearchScopeFilter) {
        _searchScopeFilter.value = scope
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

    // Reader Typography, Ergonomics & Theme Controls
    fun setArabicFontSize(sizeSp: Float) {
        viewModelScope.launch { preferencesRepo.updateArabicFontSize(sizeSp) }
    }

    fun setBanglaFontSize(sizeSp: Float) {
        viewModelScope.launch { preferencesRepo.updateBanglaFontSize(sizeSp) }
    }

    fun setLineHeightMultiplier(multiplier: Float) {
        viewModelScope.launch { preferencesRepo.updateLineHeightMultiplier(multiplier) }
    }

    fun setEyeCareWarmFilter(alpha: Float) {
        viewModelScope.launch { preferencesRepo.updateEyeCareWarmFilter(alpha) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { preferencesRepo.toggleKeepScreenOn(enabled) }
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
