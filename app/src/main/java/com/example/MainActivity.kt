package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.HadithEntity
import com.example.ui.components.BookmarkSaveDialog
import com.example.ui.components.ReaderTypographyBottomSheet
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.BooksAndReaderScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.AlHadithBanglaTheme
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.HadithViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: HadithViewModel = viewModel()
            val readerPreferences by viewModel.readerPreferences.collectAsStateWithLifecycle()

            LaunchedEffect(readerPreferences.keepScreenOnWhileReading) {
                if (readerPreferences.keepScreenOnWhileReading) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            AlHadithBanglaTheme(themeMode = readerPreferences.themeMode) {
                AlHadithAppRoot(viewModel = viewModel)
            }
        }
    }
}

private data class NavDestinationMeta(
    val destination: AppDestination,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

@Composable
fun AlHadithAppRoot(viewModel: HadithViewModel) {
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val isFocusMode by viewModel.isFocusMode.collectAsStateWithLifecycle()
    val books by viewModel.books.collectAsStateWithLifecycle()
    val allHadiths by viewModel.allHadiths.collectAsStateWithLifecycle()
    val totalIndexedHadiths by viewModel.totalIndexedHadiths.collectAsStateWithLifecycle()
    val totalIndexedAyahs by viewModel.totalIndexedAyahs.collectAsStateWithLifecycle()

    val selectedBook by viewModel.selectedBook.collectAsStateWithLifecycle()
    val selectedChapter by viewModel.selectedChapter.collectAsStateWithLifecycle()
    val chapters by viewModel.chaptersForSelectedBook.collectAsStateWithLifecycle()
    val sections by viewModel.sectionsForSelectedChapter.collectAsStateWithLifecycle()
    val chapterHadiths by viewModel.hadithsForSelectedChapter.collectAsStateWithLifecycle()
    val highlightedHadithId by viewModel.highlightedHadithId.collectAsStateWithLifecycle()

    // Al-Quran State
    val surahs by viewModel.surahs.collectAsStateWithLifecycle()
    val selectedSurah by viewModel.selectedSurah.collectAsStateWithLifecycle()
    val surahAyahs by viewModel.ayahsForSelectedSurah.collectAsStateWithLifecycle()
    val highlightedAyahNumber by viewModel.highlightedAyahNumber.collectAsStateWithLifecycle()

    // Unified Search State
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchScopeFilter by viewModel.searchScopeFilter.collectAsStateWithLifecycle()
    val searchBookFilter by viewModel.searchBookFilter.collectAsStateWithLifecycle()
    val searchGradeFilter by viewModel.searchGradeFilter.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val quranSearchResults by viewModel.quranSearchResults.collectAsStateWithLifecycle()

    val bookmarkedHadiths by viewModel.bookmarkedHadiths.collectAsStateWithLifecycle()
    val rawBookmarks by viewModel.rawBookmarks.collectAsStateWithLifecycle()
    val bookmarkCollections by viewModel.bookmarkCollections.collectAsStateWithLifecycle()
    val readingProgress by viewModel.readingProgress.collectAsStateWithLifecycle()
    val readerPreferences by viewModel.readerPreferences.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    var showTypographySheet by remember { mutableStateOf(false) }
    var activeBookmarkHadith by remember { mutableStateOf<HadithEntity?>(null) }

    val isInsideSubScreen = (currentDestination == AppDestination.BOOKS && (selectedBook != null || selectedChapter != null)) ||
        (currentDestination == AppDestination.QURAN && selectedSurah != null) ||
        isFocusMode

    BackHandler(enabled = currentDestination != AppDestination.HOME && !isInsideSubScreen) {
        viewModel.navigateToDestination(AppDestination.HOME)
    }

    val navItems = remember {
        listOf(
            NavDestinationMeta(AppDestination.HOME, Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
            NavDestinationMeta(AppDestination.BOOKS, Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_books"),
            NavDestinationMeta(AppDestination.QURAN, Icons.Filled.AutoStories, Icons.Outlined.AutoStories, "nav_quran"),
            NavDestinationMeta(AppDestination.SEARCH, Icons.Filled.Search, Icons.Outlined.Search, "nav_search"),
            NavDestinationMeta(AppDestination.BOOKMARKS, Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, "nav_bookmarks")
        )
    }

    val warmFilterAlpha = readerPreferences.eyeCareWarmFilter.coerceIn(0f, 0.30f)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                if (warmFilterAlpha > 0.005f) {
                    drawRect(color = Color(0xFFFFA000).copy(alpha = warmFilterAlpha))
                }
            }
    ) {
        val isCompactScreen = maxWidth < 600.dp
        val isExpandedScreen = maxWidth >= 600.dp
        val isLargeExpandedScreen = maxWidth >= 1040.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                AnimatedVisibility(visible = isCompactScreen && !isFocusMode) {
                    NavigationBar {
                        navItems.forEach { item ->
                            val selected = currentDestination == item.destination
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.navigateToDestination(item.destination) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.destination.titleBn
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.destination.titleBn,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedVisibility(visible = isExpandedScreen && !isLargeExpandedScreen && !isFocusMode) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        navItems.forEach { item ->
                            val selected = currentDestination == item.destination
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.navigateToDestination(item.destination) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.destination.titleBn
                                    )
                                },
                                label = { Text(item.destination.titleBn) },
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = isLargeExpandedScreen && !isFocusMode) {
                    androidx.compose.material3.PermanentDrawerSheet(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(end = 4.dp)
                    ) {
                        Text(
                            text = "আল হাদিস ও কুরআন",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
                        )
                        navItems.forEach { item ->
                            val selected = currentDestination == item.destination
                            androidx.compose.material3.NavigationDrawerItem(
                                selected = selected,
                                onClick = { viewModel.navigateToDestination(item.destination) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.destination.titleBn
                                    )
                                },
                                label = { Text(item.destination.titleBn) },
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 2.dp)
                                    .testTag(item.tag)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    AnimatedVisibility(visible = statusBannerMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = statusBannerMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.clearStatusBanner() }) {
                                    Text("ঠিক আছে")
                                }
                            }
                        }
                    }

                    androidx.compose.animation.Crossfade(
                        targetState = currentDestination,
                        label = "destination_crossfade",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) { destination ->
                        when (destination) {
                            AppDestination.HOME -> {
                                HomeScreen(
                                    books = books,
                                    allHadiths = allHadiths,
                                    totalIndexedHadiths = totalIndexedHadiths,
                                    totalSurahs = surahs.size,
                                    totalAyahs = totalIndexedAyahs,
                                    readingProgress = readingProgress,
                                    onBookClick = { book -> viewModel.openBook(book) },
                                    onHadithClick = { hadith -> viewModel.openHadithFromAnywhere(hadith) },
                                    onOpenQuranClick = {
                                        viewModel.navigateToDestination(AppDestination.QURAN)
                                    },
                                    onContinueReadingClick = { progress ->
                                        val book = books.firstOrNull { it.id == progress.bookId }
                                        if (book != null) {
                                            viewModel.jumpToHadithNumberInBook(book, progress.hadithNumber)
                                        }
                                    },
                                    onSearchBarClick = {
                                        viewModel.navigateToDestination(AppDestination.SEARCH)
                                    },
                                    onTopicClick = { topic ->
                                        viewModel.openTopicSearch(topic)
                                    },
                                    onTypographySettingsClick = {
                                        showTypographySheet = true
                                    }
                                )
                            }

                            AppDestination.BOOKS -> {
                                BooksAndReaderScreen(
                                    books = books,
                                    selectedBook = selectedBook,
                                    selectedChapter = selectedChapter,
                                    chapters = chapters,
                                    sections = sections,
                                    hadiths = chapterHadiths,
                                    bookmarks = rawBookmarks,
                                    preferences = readerPreferences,
                                    highlightedHadithId = highlightedHadithId,
                                    isExpandedScreen = isExpandedScreen,
                                    isFocusMode = isFocusMode,
                                    onToggleFocusMode = { viewModel.toggleFocusMode() },
                                    onSelectBook = { book -> viewModel.openBook(book) },
                                    onSelectChapter = { book, chapter ->
                                        viewModel.openChapter(book, chapter)
                                    },
                                    onQuickJumpHadithNumber = { book, hadithNum ->
                                        viewModel.jumpToHadithNumberInBook(book, hadithNum)
                                    },
                                    onBackStep = { viewModel.navigateBackInBooks() },
                                    onBookmarkClick = { hadith ->
                                        activeBookmarkHadith = hadith
                                    },
                                    onTopicClick = { topic ->
                                        viewModel.openTopicSearch(topic)
                                    },
                                    onHadithViewed = { hadith ->
                                        viewModel.markHadithAsRead(hadith)
                                    },
                                    onOpenTypographySheet = {
                                        showTypographySheet = true
                                    }
                                )
                            }

                            AppDestination.QURAN -> {
                                QuranScreen(
                                    surahs = surahs,
                                    selectedSurah = selectedSurah,
                                    ayahs = surahAyahs,
                                    highlightedAyahNumber = highlightedAyahNumber,
                                    preferences = readerPreferences,
                                    isExpandedScreen = isExpandedScreen,
                                    isFocusMode = isFocusMode,
                                    onToggleFocusMode = { viewModel.toggleFocusMode() },
                                    onSelectSurah = { surah -> viewModel.openSurah(surah) },
                                    onBackToSurahs = { viewModel.navigateBackInQuran() },
                                    onQuickJumpSurahAyah = { sId, aNum ->
                                        viewModel.jumpToSurahAndAyah(sId, aNum)
                                    },
                                    onOpenTypographySheet = {
                                        showTypographySheet = true
                                    }
                                )
                            }

                            AppDestination.SEARCH -> {
                                SearchScreen(
                                    query = searchQuery,
                                    scopeFilter = searchScopeFilter,
                                    selectedBookId = searchBookFilter,
                                    selectedGrade = searchGradeFilter,
                                    books = books,
                                    hadithResults = searchResults,
                                    quranResults = quranSearchResults,
                                    bookmarks = rawBookmarks,
                                    preferences = readerPreferences,
                                    onQueryChange = viewModel::updateSearchQuery,
                                    onScopeFilterChange = viewModel::updateSearchScopeFilter,
                                    onBookFilterChange = viewModel::updateSearchBookFilter,
                                    onGradeFilterChange = viewModel::updateSearchGradeFilter,
                                    onBookmarkClick = { hadith ->
                                        activeBookmarkHadith = hadith
                                    },
                                    onTopicClick = { topic ->
                                        viewModel.openTopicSearch(topic)
                                    },
                                    onAyahClick = { ayah ->
                                        viewModel.openAyahFromSearch(ayah)
                                    }
                                )
                            }

                            AppDestination.BOOKMARKS -> {
                                BookmarksScreen(
                                    bookmarkedHadiths = bookmarkedHadiths,
                                    collections = bookmarkCollections,
                                    preferences = readerPreferences,
                                    onEditBookmark = { hadith ->
                                        activeBookmarkHadith = hadith
                                    },
                                    onCreateCollection = { name, color ->
                                        viewModel.createNewCollection(name, color)
                                    },
                                    onTopicClick = { topic ->
                                        viewModel.openTopicSearch(topic)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTypographySheet) {
        ReaderTypographyBottomSheet(
            preferences = readerPreferences,
            onArabicSizeChange = viewModel::setArabicFontSize,
            onBanglaSizeChange = viewModel::setBanglaFontSize,
            onLineHeightChange = viewModel::setLineHeightMultiplier,
            onEyeCareFilterChange = viewModel::setEyeCareWarmFilter,
            onToggleKeepScreenOn = viewModel::setKeepScreenOn,
            onToggleArabic = viewModel::setShowArabic,
            onToggleExplanation = viewModel::setShowExplanation,
            onToggleTashkeel = viewModel::setShowTashkeel,
            onThemeModeChange = viewModel::setThemeMode,
            onImportSqliteDatabase = { uri ->
                viewModel.importExternalSqliteDatabase(uri)
            },
            onDismiss = { showTypographySheet = false }
        )
    }

    val dialogHadith = activeBookmarkHadith
    if (dialogHadith != null) {
        val existing = rawBookmarks.firstOrNull { it.hadithId == dialogHadith.id }
        BookmarkSaveDialog(
            hadith = dialogHadith,
            collections = bookmarkCollections,
            initialCollection = existing?.collectionName.orEmpty(),
            initialNote = existing?.personalNote.orEmpty(),
            isAlreadyBookmarked = existing != null,
            onSave = { collectionName, note ->
                viewModel.saveHadithBookmark(dialogHadith, collectionName, note)
            },
            onRemove = {
                viewModel.removeHadithBookmark(dialogHadith.id)
            },
            onCreateCollection = { folderName ->
                viewModel.createNewCollection(folderName, "#0D6E52")
            },
            onDismiss = { activeBookmarkHadith = null }
        )
    }
}
