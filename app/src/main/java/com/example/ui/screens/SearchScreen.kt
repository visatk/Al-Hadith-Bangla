package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.entity.AyahEntity
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.ReaderPreferences
import com.example.ui.components.ScholarHadithCard
import com.example.ui.components.toBanglaDigits
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.SearchScopeFilter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    query: String,
    scopeFilter: SearchScopeFilter,
    selectedBookId: Int?,
    selectedGrade: String?,
    books: List<BookEntity>,
    hadithResults: List<HadithEntity>,
    quranResults: List<AyahEntity>,
    bookmarks: List<BookmarkEntity>,
    preferences: ReaderPreferences,
    onQueryChange: (String) -> Unit,
    onScopeFilterChange: (SearchScopeFilter) -> Unit,
    onBookFilterChange: (Int?) -> Unit,
    onGradeFilterChange: (String?) -> Unit,
    onBookmarkClick: (HadithEntity) -> Unit,
    onTopicClick: (String) -> Unit,
    onAyahClick: (AyahEntity) -> Unit,
    onOpenHadithInReader: (HadithEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val bookmarkMap = remember(bookmarks) { bookmarks.associateBy { it.hadithId } }
    val totalCount = hadithResults.size + quranResults.size
    val isInitialEmptyState = query.isBlank() && selectedBookId == null && selectedGrade == null
    val quickSuggestions = remember {
        listOf("সালাত", "ঈমান", "পিতা-মাতা", "তাকওয়া", "২:২৫৫", "ইখলাস", "দুআ", "যাকাত")
    }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "হাদিস ও আল-কুরআন সমন্বিত অনুসন্ধান",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "বাংলা অনুবাদ, আরবি ইবারত, রাবীর নাম, সূরা:আয়াত (যেমন 2:255) বা হাদিস নম্বর দিয়ে খুঁজুন",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("যেমন: সালাত, নিয়ত, পিতা-মাতা, 2:255, তাকওয়া…") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "অনুসন্ধান"
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = { onQueryChange("") },
                                modifier = Modifier.testTag("clear_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "মুছুন"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("global_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 1. Unified Scope Filter (সব / শুধু হাদিস / শুধু আল-কুরআন)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(SearchScopeFilter.entries) { scope ->
                            FilterChip(
                                selected = scopeFilter == scope,
                                onClick = { onScopeFilterChange(scope) },
                                label = { Text(scope.labelBn) }
                            )
                        }
                    }

                    Text(
                        text = "${totalCount.toBanglaDigits()} টি ফলাফল",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // 2. Book & Tahqiq Grade Filters (when Hadith search is active)
                if (scopeFilter != SearchScopeFilter.QURAN_ONLY) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedBookId == null,
                                onClick = { onBookFilterChange(null) },
                                label = { Text("সব গ্রন্থ") }
                            )
                        }
                        items(books, key = { it.id }) { book ->
                            FilterChip(
                                selected = selectedBookId == book.id,
                                onClick = {
                                    onBookFilterChange(if (selectedBookId == book.id) null else book.id)
                                },
                                label = { Text(book.titleBn) }
                            )
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedGrade == null,
                                onClick = { onGradeFilterChange(null) },
                                label = { Text("সকল মান") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedGrade == "SAHIH",
                                onClick = {
                                    onGradeFilterChange(if (selectedGrade == "SAHIH") null else "SAHIH")
                                },
                                label = { Text("সহীহ") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedGrade == "HASAN",
                                onClick = {
                                    onGradeFilterChange(if (selectedGrade == "HASAN") null else "HASAN")
                                },
                                label = { Text("হাসান") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedGrade == "DAIF",
                                onClick = {
                                    onGradeFilterChange(if (selectedGrade == "DAIF") null else "DAIF")
                                },
                                label = { Text("যঈফ") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedGrade == "MAWDU",
                                onClick = {
                                    onGradeFilterChange(if (selectedGrade == "MAWDU") null else "MAWDU")
                                },
                                label = { Text("জাল/মাওযূ") }
                            )
                        }
                    }
                }
            }
        }

        if (hadithResults.isEmpty() && quranResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = 560.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_scholar_empty_ornament_1795432153586),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 148.dp, height = 110.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .border(
                                width = 1.dp,
                                color = GoldAccent.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(22.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isInitialEmptyState) {
                            "হাদিস ও আল-কুরআন থেকে মুহূর্তেই খুঁজুন"
                        } else {
                            "কোনো হাদিস বা আয়াত পাওয়া যায়নি"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isInitialEmptyState) {
                            "নিচের জনপ্রিয় বিষয়গুলোতে ট্যাপ করুন অথবা উপরে শব্দ বা সূরা:আয়াত লিখুন"
                        } else {
                            "ভিন্ন শব্দ বা ফিল্টার পরিবর্তন করে পুনরায় চেষ্টা করুন"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center
                    )
                    if (isInitialEmptyState) {
                        Spacer(modifier = Modifier.height(16.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickSuggestions.forEach { suggestion ->
                                AssistChip(
                                    onClick = { onQueryChange(suggestion) },
                                    label = { Text(suggestion) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("search_suggestion_$suggestion")
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("search_results_list"),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (quranResults.isNotEmpty()) {
                    item(key = "quran_results_header") {
                        Text(
                            text = "আল-কুরআনের আয়াত ফলাফল (${quranResults.size.toBanglaDigits()} টি)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 780.dp)
                                .semantics { heading() }
                        )
                    }
                    items(quranResults, key = { "ayah_${it.id}" }, contentType = { "search_ayah_card" }) { ayah ->
                        QuranAyahCard(
                            ayah = ayah,
                            preferences = preferences,
                            onAyahClick = { onAyahClick(ayah) },
                            modifier = Modifier.widthIn(max = 780.dp)
                        )
                    }
                }

                if (hadithResults.isNotEmpty()) {
                    item(key = "hadith_results_header") {
                        Text(
                            text = "হাদিস ফলাফল (${hadithResults.size.toBanglaDigits()} টি)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 780.dp)
                                .semantics { heading() }
                        )
                    }
                    items(hadithResults, key = { "hadith_${it.id}" }, contentType = { "search_hadith_card" }) { hadith ->
                        val bm = bookmarkMap[hadith.id]
                        ScholarHadithCard(
                            hadith = hadith,
                            preferences = preferences,
                            isBookmarked = bm != null,
                            savedCollectionName = bm?.collectionName,
                            savedPersonalNote = bm?.personalNote,
                            onBookmarkClick = { onBookmarkClick(hadith) },
                            onTopicClick = onTopicClick,
                            onOpenInReader = { onOpenHadithInReader(hadith) },
                            modifier = Modifier.widthIn(max = 780.dp)
                        )
                    }
                }
            }
        }
    }
}
