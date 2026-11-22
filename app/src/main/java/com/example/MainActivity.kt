package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storage
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.HadithEntity
import com.example.ui.components.BookmarkSaveDialog
import com.example.ui.components.ReaderTypographyBottomSheet
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.BooksAndReaderScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SqliteArchitectureScreen
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
    val books by viewModel.books.collectAsStateWithLifecycle()
    val allHadiths by viewModel.allHadiths.collectAsStateWithLifecycle()
    val selectedBook by viewModel.selectedBook.collectAsStateWithLifecycle()
    val selectedChapter by viewModel.selectedChapter.collectAsStateWithLifecycle()
    val chapters by viewModel.chaptersForSelectedBook.collectAsStateWithLifecycle()
    val sections by viewModel.sectionsForSelectedChapter.collectAsStateWithLifecycle()
    val chapterHadiths by viewModel.hadithsForSelectedChapter.collectAsStateWithLifecycle()
    val highlightedHadithId by viewModel.highlightedHadithId.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchBookFilter by viewModel.searchBookFilter.collectAsStateWithLifecycle()
    val searchGradeFilter by viewModel.searchGradeFilter.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    val bookmarkedHadiths by viewModel.bookmarkedHadiths.collectAsStateWithLifecycle()
    val rawBookmarks by viewModel.rawBookmarks.collectAsStateWithLifecycle()
    val bookmarkCollections by viewModel.bookmarkCollections.collectAsStateWithLifecycle()
    val readingProgress by viewModel.readingProgress.collectAsStateWithLifecycle()
    val readerPreferences by viewModel.readerPreferences.collectAsStateWithLifecycle()
    val sqliteReports by viewModel.sqliteReports.collectAsStateWithLifecycle()
    val isSyncingSqlite by viewModel.isSyncingSqlite.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    var showTypographySheet by remember { mutableStateOf(false) }
    var activeBookmarkHadith by remember { mutableStateOf<HadithEntity?>(null) }

    // BackHandler for secondary tabs returning to HOME
    BackHandler(enabled = currentDestination != AppDestination.HOME && !(currentDestination == AppDestination.BOOKS && (selectedBook != null || selectedChapter != null))) {
        viewModel.navigateToDestination(AppDestination.HOME)
    }

    val navItems = remember {
        listOf(
            NavDestinationMeta(AppDestination.HOME, Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
            NavDestinationMeta(AppDestination.BOOKS, Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_books"),
            NavDestinationMeta(AppDestination.SEARCH, Icons.Filled.Search, Icons.Outlined.Search, "nav_search"),
            NavDestinationMeta(AppDestination.BOOKMARKS, Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, "nav_bookmarks"),
            NavDestinationMeta(AppDestination.SQLITE_STUDIO, Icons.Filled.Storage, Icons.Outlined.Storage, "nav_sqlite")
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 700.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!isExpandedScreen) {
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
                                label = { Text(item.destination.titleBn) },
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
                if (isExpandedScreen) {
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Dismissible Status Notification Banner
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

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (currentDestination) {
                            AppDestination.HOME -> {
                                HomeScreen(
                                    books = books,
                                    allHadiths = allHadiths,
                                    readingProgress = readingProgress,
                                    sqliteReports = sqliteReports,
                                    onBookClick = { book -> viewModel.openBook(book) },
                                    onHadithClick = { hadith -> viewModel.openHadithFromAnywhere(hadith) },
                                    onContinueReadingClick = { progress ->
                                        val targetHadith = allHadiths.firstOrNull { it.id == progress.hadithId }
                                        if (targetHadith != null) {
                                            viewModel.openHadithFromAnywhere(targetHadith)
                                        }
                                    },
                                    onSearchBarClick = {
                                        viewModel.navigateToDestination(AppDestination.SEARCH)
                                    },
                                    onTopicClick = { topic ->
                                        viewModel.openTopicSearch(topic)
                                    },
                                    onSqliteStudioClick = {
                                        viewModel.navigateToDestination(AppDestination.SQLITE_STUDIO)
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
                                    onSelectBook = { book -> viewModel.openBook(book) },
                                    onSelectChapter = { book, chapter ->
                                        viewModel.openChapter(book, chapter)
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

                            AppDestination.SEARCH -> {
                                SearchScreen(
                                    query = searchQuery,
                                    selectedBookId = searchBookFilter,
                                    selectedGrade = searchGradeFilter,
                                    books = books,
                                    results = searchResults,
                                    bookmarks = rawBookmarks,
                                    preferences = readerPreferences,
                                    onQueryChange = viewModel::updateSearchQuery,
                                    onBookFilterChange = viewModel::updateSearchBookFilter,
                                    onGradeFilterChange = viewModel::updateSearchGradeFilter,
                                    onBookmarkClick = { hadith ->
                                        activeBookmarkHadith = hadith
                                    },
                                    onTopicClick = { topic ->
                                        viewModel.openTopicSearch(topic)
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

                            AppDestination.SQLITE_STUDIO -> {
                                SqliteArchitectureScreen(
                                    sqliteReports = sqliteReports,
                                    isSyncing = isSyncingSqlite,
                                    totalBooksCount = books.size,
                                    totalHadithsCount = allHadiths.size,
                                    onResyncClick = { viewModel.refreshSqliteDatabases() },
                                    onImportSqliteUri = { uri ->
                                        viewModel.importExternalSqliteDatabase(uri)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Typography & Reading Mode Bottom Sheet
    if (showTypographySheet) {
        ReaderTypographyBottomSheet(
            preferences = readerPreferences,
            onArabicSizeChange = viewModel::setArabicFontSize,
            onBanglaSizeChange = viewModel::setBanglaFontSize,
            onToggleArabic = viewModel::setShowArabic,
            onToggleTransliteration = viewModel::setShowTransliteration,
            onToggleExplanation = viewModel::setShowExplanation,
            onToggleTashkeel = viewModel::setShowTashkeel,
            onThemeModeChange = viewModel::setThemeMode,
            onDismiss = { showTypographySheet = false }
        )
    }

    // Bookmark & Personal Study Note Dialog
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
