package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.ReaderPreferences
import com.example.ui.components.ScholarHadithCard
import com.example.ui.components.toBanglaDigits

@Composable
fun SearchScreen(
    query: String,
    selectedBookId: Int?,
    selectedGrade: String?,
    books: List<BookEntity>,
    results: List<HadithEntity>,
    bookmarks: List<BookmarkEntity>,
    preferences: ReaderPreferences,
    onQueryChange: (String) -> Unit,
    onBookFilterChange: (Int?) -> Unit,
    onGradeFilterChange: (String?) -> Unit,
    onBookmarkClick: (HadithEntity) -> Unit,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bookmarkMap = remember(bookmarks) { bookmarks.associateBy { it.hadithId } }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "হাদিস অনুসন্ধান ও তাহকীক ফিল্টার",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "বাংলা অনুবাদ, আরবি ইবারত, রাবীর নাম, বিষয় বা হাদিস নম্বর দিয়ে খুঁজুন",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("যেমন: নিয়ত, সালাত, উমর, ১, আখলাক…") },
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

                // Book Filter Row
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

                // Grade Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedGrade == null,
                            onClick = { onGradeFilterChange(null) },
                            label = { Text("সকল মান") }
                        )
                        FilterChip(
                            selected = selectedGrade == "SAHIH",
                            onClick = {
                                onGradeFilterChange(if (selectedGrade == "SAHIH") null else "SAHIH")
                            },
                            label = { Text("সহীহ") }
                        )
                        FilterChip(
                            selected = selectedGrade == "HASAN",
                            onClick = {
                                onGradeFilterChange(if (selectedGrade == "HASAN") null else "HASAN")
                            },
                            label = { Text("হাসান") }
                        )
                    }

                    Text(
                        text = "${results.size.toBanglaDigits()} টি ফলাফল",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "কোনো হাদিস পাওয়া যায়নি",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "ভিন্ন শব্দ বা ফিল্টার পরিবর্তন করে পুনরায় চেষ্টা করুন",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("search_results_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(results, key = { it.id }) { hadith ->
                    val bm = bookmarkMap[hadith.id]
                    ScholarHadithCard(
                        hadith = hadith,
                        preferences = preferences,
                        isBookmarked = bm != null,
                        savedCollectionName = bm?.collectionName,
                        savedPersonalNote = bm?.personalNote,
                        onBookmarkClick = { onBookmarkClick(hadith) },
                        onTopicClick = onTopicClick
                    )
                }
            }
        }
    }
}
