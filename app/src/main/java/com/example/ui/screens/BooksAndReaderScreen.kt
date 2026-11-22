package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.ReaderPreferences
import com.example.data.local.entity.SectionEntity
import com.example.ui.components.RubElHizbMedallion
import com.example.ui.components.ScholarHadithCard
import com.example.ui.components.parseHexColor
import com.example.ui.components.toBanglaDigits
import com.example.ui.components.toEnglishDigits
import com.example.ui.theme.AmiriArabicFontFamily
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.launch

@Composable
fun BooksAndReaderScreen(
    books: List<BookEntity>,
    selectedBook: BookEntity?,
    selectedChapter: ChapterEntity?,
    chapters: List<ChapterEntity>,
    sections: List<SectionEntity>,
    hadiths: List<HadithEntity>,
    bookmarks: List<BookmarkEntity>,
    preferences: ReaderPreferences,
    highlightedHadithId: Int?,
    isExpandedScreen: Boolean,
    onSelectBook: (BookEntity) -> Unit,
    onSelectChapter: (BookEntity, ChapterEntity) -> Unit,
    onQuickJumpHadithNumber: (BookEntity, Int) -> Unit,
    onBackStep: () -> Boolean,
    onBookmarkClick: (HadithEntity) -> Unit,
    onTopicClick: (String) -> Unit,
    onHadithViewed: (HadithEntity) -> Unit,
    onOpenTypographySheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showJumpDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedBook != null || selectedChapter != null) {
        onBackStep()
    }

    if (isExpandedScreen && selectedBook != null) {
        Row(modifier = modifier.fillMaxSize()) {
            ChapterListPane(
                book = selectedBook,
                chapters = chapters,
                selectedChapterId = selectedChapter?.id,
                onSelectChapter = { ch -> onSelectChapter(selectedBook, ch) },
                onOpenQuickJump = { showJumpDialog = true },
                onBackToBooks = { onBackStep() },
                modifier = Modifier
                    .width(340.dp)
                    .fillMaxHeight()
            )
            VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val activeChapter = selectedChapter ?: chapters.firstOrNull()
                if (activeChapter != null) {
                    HadithChapterReaderPane(
                        book = selectedBook,
                        chapter = activeChapter,
                        sections = sections,
                        hadiths = hadiths,
                        bookmarks = bookmarks,
                        preferences = preferences,
                        highlightedHadithId = highlightedHadithId,
                        showBackButton = false,
                        onBack = {},
                        onOpenQuickJump = { showJumpDialog = true },
                        onBookmarkClick = onBookmarkClick,
                        onTopicClick = onTopicClick,
                        onHadithViewed = onHadithViewed,
                        onOpenTypographySheet = onOpenTypographySheet
                    )
                } else {
                    EmptyReaderPlaceholder("বাম পাশ থেকে একটি অধ্যায় নির্বাচন করুন")
                }
            }
        }
    } else {
        when {
            selectedBook == null -> {
                AllBooksCatalogPane(
                    books = books,
                    onSelectBook = onSelectBook,
                    modifier = modifier
                )
            }
            selectedChapter == null -> {
                ChapterListPane(
                    book = selectedBook,
                    chapters = chapters,
                    selectedChapterId = null,
                    onSelectChapter = { ch -> onSelectChapter(selectedBook, ch) },
                    onOpenQuickJump = { showJumpDialog = true },
                    onBackToBooks = { onBackStep() },
                    modifier = modifier
                )
            }
            else -> {
                HadithChapterReaderPane(
                    book = selectedBook,
                    chapter = selectedChapter,
                    sections = sections,
                    hadiths = hadiths,
                    bookmarks = bookmarks,
                    preferences = preferences,
                    highlightedHadithId = highlightedHadithId,
                    showBackButton = true,
                    onBack = { onBackStep() },
                    onOpenQuickJump = { showJumpDialog = true },
                    onBookmarkClick = onBookmarkClick,
                    onTopicClick = onTopicClick,
                    onHadithViewed = onHadithViewed,
                    onOpenTypographySheet = onOpenTypographySheet,
                    modifier = modifier
                )
            }
        }
    }

    if (showJumpDialog && selectedBook != null) {
        HadithQuickJumpDialog(
            book = selectedBook,
            onJump = { num ->
                onQuickJumpHadithNumber(selectedBook, num)
                showJumpDialog = false
            },
            onDismiss = { showJumpDialog = false }
        )
    }
}

@Composable
private fun AllBooksCatalogPane(
    books: List<BookEntity>,
    onSelectBook: (BookEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterQuery by remember { mutableStateOf("") }
    val filteredBooks = remember(books, filterQuery) {
        if (filterQuery.isBlank()) books
        else books.filter {
            it.titleBn.contains(filterQuery, ignoreCase = true) ||
                it.titleEn.contains(filterQuery, ignoreCase = true) ||
                it.authorBn.contains(filterQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("books_catalog_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "হাদিস গ্রন্থসমূহ",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "অধ্যায় ও পরিচ্ছেদ অনুযায়ী হাদিস অধ্যয়ন করতে যেকোনো গ্রন্থ নির্বাচন করুন",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = { Text("গ্রন্থ বা ইমামের নাম দিয়ে খুঁজুন…") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_filter_input")
                )
            }
        }

        items(filteredBooks, key = { it.id }) { book ->
            val accentColor = remember(book.accentHex) { parseHexColor(book.accentHex) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSelectBook(book) }
                    .testTag("catalog_book_item_${book.id}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RubElHizbMedallion(
                            label = book.bookCode,
                            size = 52.dp,
                            primaryColor = accentColor
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = book.titleBn,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = book.titleAr,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = AmiriArabicFontFamily,
                                        fontSize = 20.sp,
                                        color = accentColor
                                    )
                                )
                            }
                            Text(
                                text = book.authorBn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = book.descriptionBn,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = accentColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${book.chapterCount.toBanglaDigits()} টি অধ্যায়",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = accentColor,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "মোট ${book.hadithCount.toBanglaDigits()} হাদিস",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = book.sourceFile,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterListPane(
    book: BookEntity,
    chapters: List<ChapterEntity>,
    selectedChapterId: Int?,
    onSelectChapter: (ChapterEntity) -> Unit,
    onOpenQuickJump: () -> Unit,
    onBackToBooks: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chapterQuery by androidx.compose.runtime.saveable.rememberSaveable(book.id) { mutableStateOf("") }
    val accentColor = remember(book.accentHex) { parseHexColor(book.accentHex) }
    val filteredChapters = remember(chapters, chapterQuery) {
        val norm = chapterQuery.trim().toEnglishDigits()
        if (norm.isBlank()) chapters
        else chapters.filter {
            it.titleBn.contains(norm, ignoreCase = true) ||
                it.titleAr.contains(norm, ignoreCase = true) ||
                it.chapterNumber.toString() == norm
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToBooks,
                        modifier = Modifier.testTag("back_to_books_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "গ্রন্থ তালিকায় ফিরুন"
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = book.titleBn,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${book.authorBn} • ${chapters.size.toBanglaDigits()} টি অধ্যায় • ${book.hadithCount.toBanglaDigits()} হাদিস",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    AssistChip(
                        onClick = onOpenQuickJump,
                        label = { Text("হাদিস জাম্প") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FindInPage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("book_quick_jump_button")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = chapterQuery,
                    onValueChange = { chapterQuery = it },
                    placeholder = { Text("অধ্যায়ের নাম বা নম্বর খুঁজুন…") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chapter_search_input")
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("chapter_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredChapters, key = { it.id }, contentType = { "chapter_card" }) { chapter ->
                val isSelected = selectedChapterId == chapter.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectChapter(chapter) }
                        .testTag("chapter_item_${chapter.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            accentColor.copy(alpha = 0.10f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RubElHizbMedallion(
                            label = chapter.chapterNumber.toBanglaDigits(),
                            size = 42.dp,
                            primaryColor = accentColor
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = chapter.titleBn,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (chapter.titleAr.isNotBlank()) {
                                    Text(
                                        text = chapter.titleAr,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = AmiriArabicFontFamily,
                                            color = accentColor
                                        )
                                    )
                                }
                                if (chapter.hadithRange.isNotBlank()) {
                                    Text(
                                        text = "হাদিস: ${chapter.hadithRange.toBanglaDigits()}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "অধ্যায় খুলুন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HadithChapterReaderPane(
    book: BookEntity,
    chapter: ChapterEntity,
    sections: List<SectionEntity>,
    hadiths: List<HadithEntity>,
    bookmarks: List<BookmarkEntity>,
    preferences: ReaderPreferences,
    highlightedHadithId: Int?,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onOpenQuickJump: () -> Unit,
    onBookmarkClick: (HadithEntity) -> Unit,
    onTopicClick: (String) -> Unit,
    onHadithViewed: (HadithEntity) -> Unit,
    onOpenTypographySheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val sectionMap = remember(sections) { sections.associateBy { it.id } }
    val bookmarkMap = remember(bookmarks) { bookmarks.associateBy { it.hadithId } }
    val showScrollToTop by remember {
        androidx.compose.runtime.derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }

    LaunchedEffect(hadiths, highlightedHadithId) {
        if (hadiths.isNotEmpty()) {
            val targetHadith = hadiths.firstOrNull { it.id == highlightedHadithId } ?: hadiths.first()
            onHadithViewed(targetHadith)
            val idx = hadiths.indexOfFirst { it.id == highlightedHadithId }
            if (idx >= 0) {
                listState.animateScrollToItem(idx)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showBackButton) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("reader_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "অধ্যায় তালিকায় ফিরুন"
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${book.titleBn} • অধ্যায় ${chapter.chapterNumber.toBanglaDigits()}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = chapter.titleBn,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = onOpenQuickJump,
                            modifier = Modifier.testTag("reader_quick_jump_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FindInPage,
                                contentDescription = "হাদিস নম্বর জাম্প",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onOpenTypographySheet,
                            modifier = Modifier.testTag("reader_typography_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "ফন্ট ও থিম সেটিংস",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (hadiths.size > 1) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 4.dp)
                        ) {
                            items(hadiths.size, key = { hadiths[it].id }, contentType = { "hadith_jump_chip" }) { index ->
                                val h = hadiths[index]
                                AssistChip(
                                    onClick = {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(index)
                                        }
                                        onHadithViewed(h)
                                    },
                                    label = {
                                        Text("হাদিস ${h.hadithNumber.toBanglaDigits()}")
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (hadiths.isEmpty()) {
                EmptyReaderPlaceholder("এই অধ্যায়ে এখনো কোনো হাদিস যুক্ত করা হয়নি")
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("hadith_reader_lazy_column"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        count = hadiths.size,
                        key = { hadiths[it].id },
                        contentType = { "hadith_reader_item" }
                    ) { index ->
                        val hadith = hadiths[index]
                        val prevSectionId = if (index > 0) hadiths[index - 1].sectionId else null
                        val section = if (hadith.sectionId != prevSectionId) sectionMap[hadith.sectionId] else null
                        val bookmark = bookmarkMap[hadith.id]

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (section != null && (section.titleBn.isNotBlank() || section.prefaceBn.isNotBlank())) {
                                SectionPrefaceBanner(section = section)
                            }

                            ScholarHadithCard(
                                hadith = hadith,
                                preferences = preferences,
                                isBookmarked = bookmark != null,
                                savedCollectionName = bookmark?.collectionName,
                                savedPersonalNote = bookmark?.personalNote,
                                isHighlighted = highlightedHadithId == hadith.id,
                                onBookmarkClick = { onBookmarkClick(hadith) },
                                onTopicClick = onTopicClick
                            )
                        }
                    }
                }
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = showScrollToTop,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            androidx.compose.material3.SmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch { listState.animateScrollToItem(0) }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag("hadith_scroll_to_top_fab")
            ) {
                Text(
                    text = "↑ শীর্ষে",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun HadithQuickJumpDialog(
    book: BookEntity,
    onJump: (hadithNumber: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var numberInput by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${book.titleBn} • হাদিস জাম্প",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "এই গ্রন্থের মোট ${book.hadithCount.toBanglaDigits()} টি হাদিসের মধ্যে যেকোনো হাদিস নম্বরে সরাসরি যেতে নিচে নম্বর লিখুন:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                OutlinedTextField(
                    value = numberInput,
                    onValueChange = { numberInput = it },
                    label = { Text("হাদিস নম্বর (১ - ${book.hadithCount.toBanglaDigits()})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val num = numberInput.trim().toEnglishDigits().toIntOrNull() ?: 1
                    onJump(num.coerceAtLeast(1))
                }
            ) {
                Text("হাদিসে যান")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
private fun SectionPrefaceBanner(
    section: SectionEntity,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = GoldAccent.copy(alpha = 0.35f),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(GoldAccent)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                val heading = if (section.titleBn.startsWith(section.sectionNumber)) {
                    section.titleBn
                } else {
                    "${section.sectionNumber} ${section.titleBn}".trim()
                }
                Text(
                    text = heading,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
                if (section.prefaceBn.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = section.prefaceBn,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyReaderPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
