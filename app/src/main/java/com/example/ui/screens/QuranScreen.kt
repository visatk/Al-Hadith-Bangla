package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AyahEntity
import com.example.data.local.entity.ReaderPreferences
import com.example.data.local.entity.SurahEntity
import com.example.ui.components.IslamicGeometricDivider
import com.example.ui.components.RubElHizbMedallion
import com.example.ui.components.stripArabicTashkeel
import com.example.ui.components.toBanglaDigits
import com.example.ui.components.toEnglishDigits
import com.example.ui.theme.AmiriArabicFontFamily
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.HindSiliguriBanglaFontFamily
import kotlinx.coroutines.launch

@Composable
fun QuranScreen(
    surahs: List<SurahEntity>,
    selectedSurah: SurahEntity?,
    ayahs: List<AyahEntity>,
    highlightedAyahNumber: Int?,
    preferences: ReaderPreferences,
    isExpandedScreen: Boolean,
    onSelectSurah: (SurahEntity) -> Unit,
    onBackToSurahs: () -> Boolean,
    onQuickJumpSurahAyah: (surahId: Int, ayahNumber: Int) -> Unit,
    onOpenTypographySheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuickJumpDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedSurah != null) {
        onBackToSurahs()
    }

    if (isExpandedScreen) {
        val activeSurah = selectedSurah ?: surahs.firstOrNull()
        Row(modifier = modifier.fillMaxSize()) {
            SurahCatalogPane(
                surahs = surahs,
                selectedSurahId = activeSurah?.id,
                onSelectSurah = onSelectSurah,
                onOpenQuickJump = { showQuickJumpDialog = true },
                modifier = Modifier
                    .width(350.dp)
                    .fillMaxHeight()
            )
            VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                if (activeSurah != null) {
                    SurahAyahReaderPane(
                        surah = activeSurah,
                        ayahs = ayahs,
                        highlightedAyahNumber = highlightedAyahNumber,
                        preferences = preferences,
                        showBackButton = false,
                        onBack = {},
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
                onSelectSurah = onSelectSurah,
                onOpenQuickJump = { showQuickJumpDialog = true },
                modifier = modifier
            )
        } else {
            SurahAyahReaderPane(
                surah = selectedSurah,
                ayahs = ayahs,
                highlightedAyahNumber = highlightedAyahNumber,
                preferences = preferences,
                showBackButton = true,
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
    onSelectSurah: (SurahEntity) -> Unit,
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
                            )
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
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = AmiriArabicFontFamily,
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
    onBack: () -> Unit,
    onOpenQuickJump: () -> Unit,
    onOpenTypographySheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(ayahs, highlightedAyahNumber) {
        if (ayahs.isNotEmpty() && highlightedAyahNumber != null) {
            val idx = ayahs.indexOfFirst { it.ayahNumber == highlightedAyahNumber }
            if (idx >= 0) {
                listState.animateScrollToItem(idx + 1) // +1 for Bismillah header item
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Sticky Surah Reader Top Bar
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
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = surah.nameAr,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = AmiriArabicFontFamily,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

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
                if (ayahs.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 4.dp)
                    ) {
                        items(ayahs.size) { index ->
                            val ayah = ayahs[index]
                            AssistChip(
                                onClick = {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index + 1)
                                    }
                                },
                                label = {
                                    Text("আয়াত ${ayah.ayahNumber.toBanglaDigits()}")
                                }
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("ayah_reader_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Surah Ornamental Banner + Bismillah (except Surah At-Tawbah #9)
            item {
                SurahBismillahBanner(surah = surah)
            }

            items(ayahs, key = { it.id }) { ayah ->
                QuranAyahCard(
                    ayah = ayah,
                    preferences = preferences,
                    isHighlighted = highlightedAyahNumber == ayah.ayahNumber
                )
            }
        }
    }
}

@Composable
private fun SurahBismillahBanner(surah: SurahEntity) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldDeep),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GoldAccent.copy(alpha = 0.55f), RoundedCornerShape(22.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "سُورَةُ ${surah.nameAr}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = AmiriArabicFontFamily,
                    color = GoldLight,
                    fontSize = 26.sp
                )
            )
            Text(
                text = "সূরা ${surah.nameBn} • অর্থ: ${surah.meaningBn} • ${surah.revelationBn} • মোট আয়াত: ${surah.totalVerses.toBanglaDigits()}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.88f)
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
                        fontFamily = AmiriArabicFontFamily,
                        fontSize = 24.sp,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RubElHizbMedallion(
                        label = ayah.ayahNumber.toBanglaDigits(),
                        size = 42.dp
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
                    IconButton(onClick = { copyAyahToClipboard(context, ayah) }) {
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
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                        .border(
                            width = 1.dp,
                            color = GoldAccent.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = ayah.arabicText.stripArabicTashkeel(preferences.showTashkeel),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = AmiriArabicFontFamily,
                            fontSize = preferences.arabicFontSizeSp.sp,
                            lineHeight = (preferences.arabicFontSizeSp * 1.75f).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Right,
                            textDirection = TextDirection.Rtl
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = ayah.banglaTranslation,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = HindSiliguriBanglaFontFamily,
                    fontSize = preferences.banglaFontSizeSp.sp,
                    lineHeight = (preferences.banglaFontSizeSp * 1.62f).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
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
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Al-Quran Ayah", formatAyahForShare(ayah))
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "আয়াতটি ক্লিপবোর্ডে কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
}

private fun shareAyahIntent(context: Context, ayah: AyahEntity) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "সূরা ${ayah.surahNameBn} • আয়াত ${ayah.ayahNumber}")
        putExtra(Intent.EXTRA_TEXT, formatAyahForShare(ayah))
    }
    context.startActivity(Intent.createChooser(sendIntent, "আয়াত শেয়ার করুন"))
}
