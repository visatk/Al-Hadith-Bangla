package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.local.entity.BookmarkCollectionEntity
import com.example.data.local.entity.BookmarkedHadithWithDetail
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.ReaderPreferences
import com.example.ui.components.ScholarHadithCard
import com.example.ui.components.toBanglaDigits
import com.example.ui.theme.GoldAccent

@Composable
fun BookmarksScreen(
    bookmarkedHadiths: List<BookmarkedHadithWithDetail>,
    collections: List<BookmarkCollectionEntity>,
    preferences: ReaderPreferences,
    onEditBookmark: (HadithEntity) -> Unit,
    onCreateCollection: (String, String) -> Unit,
    onTopicClick: (String) -> Unit,
    onOpenHadithInReader: (HadithEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var bookmarkFilterQuery by remember { mutableStateOf("") }
    var showCreateFolderDialog by remember { mutableStateOf(false) }

    val filteredBookmarks = remember(bookmarkedHadiths, selectedFolder, bookmarkFilterQuery) {
        val byFolder = if (selectedFolder == null) bookmarkedHadiths
        else bookmarkedHadiths.filter { it.collectionName == selectedFolder }
        val q = bookmarkFilterQuery.trim()
        if (q.isBlank()) byFolder
        else byFolder.filter { item ->
            item.hadith.banglaText.contains(q, ignoreCase = true) ||
                item.hadith.referenceBn.contains(q, ignoreCase = true) ||
                item.personalNote.contains(q, ignoreCase = true) ||
                item.collectionName.contains(q, ignoreCase = true)
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
                            text = "সংরক্ষিত হাদিস ও দারস নোট",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.semantics { heading() }
                        )
                        Text(
                            text = "ফোল্ডার অনুযায়ী আপনার প্রিয় হাদিস ও ব্যক্তিগত গবেষণা নোটসমূহ",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    AssistChip(
                        onClick = { showCreateFolderDialog = true },
                        label = { Text("নতুন ফোল্ডার") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CreateNewFolder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("create_collection_button")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (bookmarkedHadiths.isNotEmpty()) {
                    OutlinedTextField(
                        value = bookmarkFilterQuery,
                        onValueChange = { bookmarkFilterQuery = it },
                        placeholder = { Text("সংরক্ষিত হাদিস বা নোট খুঁজুন…") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (bookmarkFilterQuery.isNotEmpty()) {
                                IconButton(onClick = { bookmarkFilterQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "মুছুন")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bookmarks_filter_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedFolder == null,
                            onClick = { selectedFolder = null },
                            label = {
                                Text("সব ফোল্ডার (${bookmarkedHadiths.size.toBanglaDigits()})")
                            }
                        )
                    }
                    items(collections, key = { it.id }) { folder ->
                        val count = bookmarkedHadiths.count { it.collectionName == folder.nameBn }
                        FilterChip(
                            selected = selectedFolder == folder.nameBn,
                            onClick = {
                                selectedFolder = if (selectedFolder == folder.nameBn) null else folder.nameBn
                            },
                            label = {
                                Text("${folder.nameBn} (${count.toBanglaDigits()})")
                            }
                        )
                    }
                }
            }
        }

        if (filteredBookmarks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                        text = "এই ফোল্ডারে এখনো কোনো হাদিস সংরক্ষিত নেই",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "যেকোনো হাদিস কার্ডের নিচে বুকমার্ক আইকনে ট্যাপ করে ফোল্ডার ও ব্যক্তিগত নোটসহ সংরক্ষণ করতে পারবেন।",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("bookmarks_lazy_column"),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredBookmarks, key = { it.hadith.id }) { item ->
                    ScholarHadithCard(
                        hadith = item.hadith,
                        preferences = preferences,
                        isBookmarked = true,
                        savedCollectionName = item.collectionName,
                        savedPersonalNote = item.personalNote,
                        onBookmarkClick = { onEditBookmark(item.hadith) },
                        onTopicClick = onTopicClick,
                        onOpenInReader = { onOpenHadithInReader(item.hadith) },
                        modifier = Modifier.widthIn(max = 780.dp)
                    )
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        var folderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("নতুন হাদিস সংগ্রহ ফোল্ডার") },
            text = {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("ফোল্ডারের নাম (যেমন: জুমআর বয়ান)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (folderName.isNotBlank()) {
                            onCreateCollection(folderName.trim(), "#0D6E52")
                            showCreateFolderDialog = false
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("তৈরি করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
