package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.AyahEntity
import com.example.data.local.entity.QuranReadingProgress
import com.example.data.local.entity.ReaderPreferences
import com.example.data.local.entity.SurahEntity
import com.example.ui.components.IslamicGeometricDivider
import com.example.ui.components.MushafVerseEndRosette
import com.example.ui.components.RubElHizbMedallion
import com.example.ui.components.stripArabicTashkeel
import com.example.ui.components.toBanglaDigits
import com.example.ui.components.toEnglishDigits
import com.example.ui.theme.AmiriArabicFontFamily
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun QuranScreen(
    surahs: List<SurahEntity>,
    selectedSurah: SurahEntity?,
    ayahs: List<AyahEntity>,
    highlightedAyahNumber: Int?,
    quranReadingProgress: QuranReadingProgress?,
    preferences: ReaderPreferences,
    isExpandedScreen: Boolean,
    isFocusMode: Boolean,
    onToggleFocusMode: () -> Unit,
    onSelectSurah: (SurahEntity) -> Unit,
    onContinueQuranClick: (QuranReadingProgress) -> Unit,
    onPreviousSurah: () -> Unit,
    onNextSurah: () -> Unit,
    onAyahViewed: (SurahEntity, Int) -> Unit,
    onBackToSurahs: () -> Boolean,
    onQuickJumpSurahAyah: (surahId: Int, ayahNumber: Int) -> Unit,
    onOpenTypographySheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuickJumpDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedSurah != null || isFocusMode) {
        onBackToSurahs()
    }

    if (isExpandedScreen) {
        val activeSurah = selectedSurah ?: surahs.firstOrNull()
        Row(modifier = modifier.fillMaxSize()) {
            AnimatedVisibility(visible = !isFocusMode) {
                Row(modifier = Modifier.fillMaxHeight()) {
                    SurahCatalogPane(
                        surahs = surahs,
                        selectedSurahId = activeSurah?.id,
                        quranReadingProgress = quranReadingProgress,
                        onSelectSurah = onSelectSurah,
                        onContinueQuranClick = onContinueQuranClick,
                        onOpenQuickJump = { showQuickJumpDialog = true },
                        modifier = Modifier
                            .width(350.dp)
                            .fillMaxHeight()
                    )
                    VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                if (activeSurah != null) {
                    val surahIdx = surahs.indexOfFirst { it.id == activeSurah.id }
                    SurahAyahReaderPane(
                        surah = activeSurah,
                        ayahs = ayahs,
                        highlightedAyahNumber = highlightedAyahNumber,
                        preferences = preferences,
                        showBackButton = isFocusMode,
                        isFocusMode = isFocusMode,
                        hasPreviousSurah = surahIdx > 0,
                        hasNextSurah = surahIdx in 0 until surahs.lastIndex,
                        onPreviousSurah = onPreviousSurah,
                        onNextSurah = onNextSurah,
                        onAyahViewed = onAyahViewed,
                        onToggleFocusMode = onToggleFocusMode,
                        onBack = { if (isFocusMode) onToggleFocusMode() else onBackToSurahs() },
                        onOpenQuickJump = { showQuickJumpDialog = true },
                        onOpenTypographySheet = onOpenTypographySheet
                    )
                }
            }
        }
    } else {
        if (selectedSurah == null) {
            SurahCatalogPane(
                surahs = surahs,
                selectedSurahId = null,
                quranReadingProgress = quranReadingProgress,
                onSelectSurah = onSelectSurah,
                onContinueQuranClick = onContinueQuranClick,
                onOpenQuickJump = { showQuickJumpDialog = true },
                modifier = modifier
            )
        } else {
            val surahIdx = surahs.indexOfFirst { it.id == selectedSurah.id }
            SurahAyahReaderPane(
                surah = selectedSurah,
                ayahs = ayahs,
                highlightedAyahNumber = highlightedAyahNumber,
                preferences = preferences,
                showBackButton = true,
                isFocusMode = isFocusMode,
                hasPreviousSurah = surahIdx > 0,
                hasNextSurah = surahIdx in 0 until surahs.lastIndex,
                onPreviousSurah = onPreviousSurah,
                onNextSurah = onNextSurah,
                onAyahViewed = onAyahViewed,
                onToggleFocusMode = onToggleFocusMode,
                onBack = { onBackToSurahs() },
                onOpenQuickJump = { showQuickJumpDialog = true },
                onOpenTypographySheet = onOpenTypographySheet,
                modifier = modifier
            )
        }
    }

    if (showQuickJumpDialog) {
        QuranQuickJumpDialog(
            initialSurahId = selectedSurah?.id ?: 1,
            onJump = { sId, aNum ->
                onQuickJumpSurahAyah(sId, aNum)
                showQuickJumpDialog = false
            },
            onDismiss = { showQuickJumpDialog = false }
        )
    }
}

@Composable
private fun SurahCatalogPane(
    surahs: List<SurahEntity>,
    selectedSurahId: Int?,
    quranReadingProgress: QuranReadingProgress?,
    onSelectSurah: (SurahEntity) -> Unit,
    onContinueQuranClick: (QuranReadingProgress) -> Unit,
    onOpenQuickJump: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var revelationFilter by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }

    val filteredSurahs = remember(surahs, searchQuery, revelationFilter) {
        val normQuery = searchQuery.trim().toEnglishDigits()
        surahs.filter { surah ->
            val matchesRev = revelationFilter == null || surah.revelationType.equals(revelationFilter, ignoreCase = true)
            val matchesQuery = normQuery.isBlank() ||
                surah.nameBn.contains(normQuery, ignoreCase = true) ||
                surah.meaningBn.contains(normQuery, ignoreCase = true) ||
                surah.transliterationEn.contains(normQuery, ignoreCase = true) ||
                surah.nameAr.contains(normQuery, ignoreCase = true) ||
                surah.id.toString() == normQuery
            matchesRev && matchesQuery
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "আল-কুরআনুল কারীম",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.semantics { heading() }
                        )
                        Text(
                            text = "১১৪ টি সূরা • ৬,২৩৬ টি আয়াত ও বাংলা অনুবাদ",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    AssistChip(
                        onClick = onOpenQuickJump,
                        label = { Text("সূরা:আয়াত জাম্প") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FindInPage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("quran_quick_jump_button")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("সূরা নম্বর, বাংলা বা আরবি নাম দিয়ে খুঁজুন…") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("surah_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = revelationFilter == null,
                        onClick = { revelationFilter = null },
                        label = { Text("সব সূরা (${surahs.size.toBanglaDigits()})") }
                    )
                    FilterChip(
                        selected = revelationFilter == "meccan",
                        onClick = {
                            revelationFilter = if (revelationFilter == "meccan") null else "meccan"
                        },
                        label = { Text("মাক্কী") }
                    )
                    FilterChip(
                        selected = revelationFilter == "medinan",
                        onClick = {
                            revelationFilter = if (revelationFilter == "medinan") null else "medinan"
                        },
                        label = { Text("মাদানী") }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("surah_catalog_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (quranReadingProgress != null && searchQuery.isBlank() && revelationFilter == null) {
                item(key = "quran_resume_banner") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onContinueQuranClick(quranReadingProgress) }
                            .semantics(mergeDescendants = true) {}
                            .testTag("quran_catalog_resume_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "সর্বশেষ তিলাওয়াত থেকে শুরু করুন",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Text(
                                        text = "সূরা ${quranReadingProgress.surahNameBn} • আয়াত ${quranReadingProgress.ayahNumber.toBanglaDigits()}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "পড়ুন",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            items(filteredSurahs, key = { it.id }) { surah ->
                val isSelected = selectedSurahId == surah.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) GoldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSelectSurah(surah) }
                        .semantics(mergeDescendants = true) {}
                        .testTag("surah_item_${surah.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
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
                            label = surah.id.toBanglaDigits(),
                            size = 46.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "সূরা ${surah.nameBn}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "${surah.meaningBn} • ${surah.revelationBn} • ${surah.totalVerses.toBanglaDigits()} আয়াত",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Text(
                            text = surah.nameAr,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahAyahReaderPane(
    surah: SurahEntity,
    ayahs: List<AyahEntity>,
    highlightedAyahNumber: Int?,
    preferences: ReaderPreferences,
    showBackButton: Boolean,
    isFocusMode: Boolean,
    hasPreviousSurah: Boolean,
    hasNextSurah: Boolean,
    onPreviousSurah: () -> Unit,
    onNextSurah: () -> Unit,
    onAyahViewed: (SurahEntity, Int) -> Unit,
    onToggleFocusMode: () -> Unit,
    onBack: () -> Unit,
    onOpenQuickJump: () -> Unit,
    onOpenTypographySheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 4 }
    }
    val surahProgress by remember(ayahs.size) {
        derivedStateOf {
            if (ayahs.isEmpty()) 0f
            else ((listState.firstVisibleItemIndex).coerceAtLeast(1).toFloat() / ayahs.size.toFloat()).coerceIn(0.03f, 1f)
        }
    }

    LaunchedEffect(surah.id) {
        if (highlightedAyahNumber == null) {
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(ayahs, highlightedAyahNumber) {
        if (ayahs.isNotEmpty() && highlightedAyahNumber != null) {
            val idx = ayahs.indexOfFirst { it.ayahNumber == highlightedAyahNumber }
            if (idx >= 0) {
                listState.animateScrollToItem(idx + 1) // +1 for Bismillah header item
            }
        }
    }

    // Persist reading position as the user scrolls through ayahs
    LaunchedEffect(surah.id, ayahs) {
        if (ayahs.isNotEmpty()) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .distinctUntilChanged()
                .collect { firstIndex ->
                    val ayahIdx = (firstIndex - 1).coerceIn(0, ayahs.lastIndex)
                    onAyahViewed(surah, ayahs[ayahIdx].ayahNumber)
                }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sticky Surah Reader Top Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (showBackButton) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.testTag("quran_reader_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "সূরা তালিকায় ফিরুন"
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "সূরা নং ${surah.id.toBanglaDigits()} • ${surah.revelationBn} • ${surah.totalVerses.toBanglaDigits()} আয়াত",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "সূরা ${surah.nameBn} (${surah.meaningBn})",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.semantics { heading() }
                                )
                            }

                            Text(
                                text = surah.nameAr,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = preferences.arabicFontStyle.fontFamily,
                                    fontSize = 22.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )

                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggleFocusMode()
                                },
                                modifier = Modifier.testTag("quran_focus_mode_button")
                            ) {
                                Icon(
                                    imageVector = if (isFocusMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = if (isFocusMode) "মনোযোগ মোড বন্ধ করুন" else "মনোযোগ মোড চালু করুন",
                                    tint = if (isFocusMode) GoldAccent else MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = onOpenQuickJump,
                                modifier = Modifier.testTag("surah_reader_jump_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FindInPage,
                                    contentDescription = "আয়াত জাম্প",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = onOpenTypographySheet,
                                modifier = Modifier.testTag("quran_typography_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "ফন্ট সেটিংস",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Quick Jump-to-Ayah Horizontal Chips
                        AnimatedVisibility(visible = !isFocusMode && ayahs.size > 1) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(top = 4.dp)
                            ) {
                                items(
                                    count = ayahs.size,
                                    key = { ayahs[it].id },
                                    contentType = { "ayah_jump_chip" }
                                ) { index ->
                                    val ayah = ayahs[index]
                                    AssistChip(
                                        onClick = {
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(index + 1)
                                            }
                                            onAyahViewed(surah, ayah.ayahNumber)
                                        },
                                        label = {
                                            Text("আয়াত ${ayah.ayahNumber.toBanglaDigits()}")
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (ayahs.size > 1) {
                        LinearProgressIndicator(
                            progress = { surahProgress },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                        )
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("ayah_reader_lazy_column"),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Surah Ornamental Banner + Bismillah (except Surah At-Tawbah #9)
                item(key = "surah_header_${surah.id}") {
                    Box(modifier = Modifier.widthIn(max = 780.dp)) {
                        SurahBismillahBanner(
                            surah = surah,
                            preferences = preferences
                        )
                    }
                }

                items(ayahs, key = { it.id }, contentType = { "quran_ayah_card" }) { ayah ->
                    QuranAyahCard(
                        ayah = ayah,
                        preferences = preferences,
                        isHighlighted = highlightedAyahNumber == ayah.ayahNumber,
                        modifier = Modifier.widthIn(max = 780.dp)
                    )
                }

                if (hasPreviousSurah || hasNextSurah) {
                    item(key = "surah_nav_footer") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 780.dp)
                                .padding(top = 6.dp, bottom = 28.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (hasPreviousSurah) {
                                OutlinedButton(
                                    onClick = onPreviousSurah,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("previous_surah_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("পূর্ববর্তী সূরা")
                                }
                            }
                            if (hasNextSurah) {
                                Button(
                                    onClick = onNextSurah,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("next_surah_button")
                                ) {
                                    Text("পরবর্তী সূরা")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showScrollToTop,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch { listState.animateScrollToItem(0) }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag("quran_scroll_to_top_fab")
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
private fun SurahBismillahBanner(
    surah: SurahEntity,
    preferences: ReaderPreferences
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldDeep),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GoldAccent.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_quran_sanctuary_banner_1795432153972),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                EmeraldDeep.copy(alpha = 0.85f),
                                EmeraldDeep.copy(alpha = 0.92f),
                                EmeraldDeep.copy(alpha = 0.96f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "سُورَةُ ${surah.nameAr}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = preferences.arabicFontStyle.fontFamily,
                        color = GoldLight,
                        fontSize = 27.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "সূরা ${surah.nameBn} • অর্থ: ${surah.meaningBn} • ${surah.revelationBn} • মোট আয়াত: ${surah.totalVerses.toBanglaDigits()}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.90f)
                    ),
                    textAlign = TextAlign.Center
                )

                if (surah.id != 9 && surah.id != 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    IslamicGeometricDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = preferences.arabicFontStyle.fontFamily,
                            fontSize = 25.sp,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun QuranAyahCard(
    ayah: AyahEntity,
    preferences: ReaderPreferences,
    isHighlighted: Boolean = false,
    onAyahClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val borderColor = if (isHighlighted) {
        GoldAccent
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isHighlighted) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .then(if (onAyahClick != null) Modifier.clickable { onAyahClick() } else Modifier)
            .testTag("ayah_card_${ayah.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    MushafVerseEndRosette(
                        ayahNumberBn = ayah.ayahNumber.toBanglaDigits(),
                        size = 44.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "সূরা ${ayah.surahNameBn} • আয়াত ${ayah.ayahNumber.toBanglaDigits()}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "আল-কুরআন ${ayah.surahId.toBanglaDigits()}:${ayah.ayahNumber.toBanglaDigits()}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Row {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        copyAyahToClipboard(context, ayah)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "আয়াত কপি করুন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { shareAyahIntent(context, ayah) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "আয়াত শেয়ার করুন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (preferences.showArabic && ayah.arabicText.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .border(
                            width = 1.dp,
                            color = GoldAccent.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = ayah.arabicText.stripArabicTashkeel(preferences.showTashkeel),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = preferences.arabicFontStyle.fontFamily,
                                fontSize = preferences.arabicFontSizeSp.sp,
                                lineHeight = (preferences.arabicFontSizeSp * (preferences.lineHeightMultiplier + 0.12f)).sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Right,
                                textDirection = TextDirection.Rtl
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SelectionContainer {
                Text(
                    text = ayah.banglaTranslation,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = preferences.banglaFontStyle.fontFamily,
                        fontSize = preferences.banglaFontSizeSp.sp,
                        lineHeight = (preferences.banglaFontSizeSp * preferences.lineHeightMultiplier).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
private fun QuranQuickJumpDialog(
    initialSurahId: Int,
    onJump: (surahId: Int, ayahNumber: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var surahInput by remember { mutableStateOf(initialSurahId.toString()) }
    var ayahInput by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "সূরা ও আয়াত নম্বর জাম্প",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "যেকোনো সূরা (১-১১৪) এবং আয়াত নম্বরে সরাসরি যেতে নিচে নম্বর লিখুন (যেমন: আয়াতুল কুরসীর জন্য সূরা ২, আয়াত ২৫৫):",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                OutlinedTextField(
                    value = surahInput,
                    onValueChange = { surahInput = it },
                    label = { Text("সূরা নম্বর (১ - ১১৪)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ayahInput,
                    onValueChange = { ayahInput = it },
                    label = { Text("আয়াত নম্বর") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sId = surahInput.trim().toEnglishDigits().toIntOrNull() ?: initialSurahId
                    val aNum = ayahInput.trim().toEnglishDigits().toIntOrNull() ?: 1
                    onJump(sId.coerceIn(1, 114), aNum.coerceAtLeast(1))
                }
            ) {
                Text("যান")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

fun formatAyahForShare(ayah: AyahEntity): String {
    return buildString {
        appendLine("🕋 আল-কুরআনুল কারীম")
        appendLine("সূরা ${ayah.surahNameBn} (${ayah.surahNameAr}) • আয়াত ${ayah.ayahNumber.toBanglaDigits()} [${ayah.surahId}:${ayah.ayahNumber}]")
        appendLine()
        appendLine(ayah.arabicText)
        appendLine()
        appendLine("বাংলা অনুবাদ:")
        appendLine(ayah.banglaTranslation)
        appendLine()
        append("— আল হাদিস ও আল-কুরআন বাংলা অ্যাপ")
    }
}

private fun copyAyahToClipboard(context: Context, ayah: AyahEntity) {
    runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: error("Clipboard unavailable")
        val clip = ClipData.newPlainText("Al-Quran Ayah", formatAyahForShare(ayah))
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "আয়াতটি ক্লিপবোর্ডে কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, "কপি করা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
    }
}

private fun shareAyahIntent(context: Context, ayah: AyahEntity) {
    runCatching {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "সূরা ${ayah.surahNameBn} • আয়াত ${ayah.ayahNumber}")
            putExtra(Intent.EXTRA_TEXT, formatAyahForShare(ayah))
        }
        context.startActivity(Intent.createChooser(sendIntent, "আয়াত শেয়ার করুন"))
    }.onFailure {
        Toast.makeText(context, "শেয়ার করার অ্যাপ পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
    }
}
