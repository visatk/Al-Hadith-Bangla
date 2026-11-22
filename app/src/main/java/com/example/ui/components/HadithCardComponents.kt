package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BookmarkCollectionEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.ReaderPreferences
import com.example.ui.theme.AmiriArabicFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HindSiliguriBanglaFontFamily
import com.example.ui.theme.ReaderThemeMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScholarHadithCard(
    hadith: HadithEntity,
    preferences: ReaderPreferences,
    isBookmarked: Boolean,
    savedCollectionName: String? = null,
    savedPersonalNote: String? = null,
    isHighlighted: Boolean = false,
    onBookmarkClick: () -> Unit,
    onTopicClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var explanationExpanded by remember { mutableStateOf(preferences.showExplanation) }

    val borderColor = if (isHighlighted) {
        GoldAccent
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isHighlighted) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("hadith_card_${hadith.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // 1. Top Metadata Bar: Rub el Hizb Medallion + Reference + Tahqiq Grade Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RubElHizbMedallion(
                        label = hadith.hadithNumber.toBanglaDigits(),
                        size = 44.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "হাদিস নং ${hadith.hadithNumber.toBanglaDigits()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = hadith.internationalNumber,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                TahqiqGradeBadge(
                    gradeBn = hadith.gradeBn,
                    gradeType = hadith.gradeType
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Sacred Arabic Text Block (Amiri Font, RTL aligned)
            if (preferences.showArabic && hadith.arabicText.isNotBlank()) {
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
                        text = hadith.arabicText.stripArabicTashkeel(preferences.showTashkeel),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = AmiriArabicFontFamily,
                            fontSize = preferences.arabicFontSizeSp.sp,
                            lineHeight = (preferences.arabicFontSizeSp * 1.72f).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Right,
                            textDirection = TextDirection.Rtl
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 3. Narrator (রাবী) Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GoldAccent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${hadith.narratorBn} হতে বর্ণিত:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // 4. Bangla Translation
            Text(
                text = hadith.banglaText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = HindSiliguriBanglaFontFamily,
                    fontSize = preferences.banglaFontSizeSp.sp,
                    lineHeight = (preferences.banglaFontSizeSp * 1.62f).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            // 5. Optional Transliteration (উচ্চারণ)
            if (preferences.showTransliteration && hadith.transliterationBn.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "বাংলা উচ্চারণ:",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = hadith.transliterationBn,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // 6. Personal Study Note (if saved in bookmarks)
            if (!savedPersonalNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "আপনার ব্যক্তিগত নোট (${savedCollectionName ?: "সংগ্রহ"}):",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = savedPersonalNote,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }

            // 7. Scholarly Commentary / Explanation (তাহকীক ও ব্যাখ্যা)
            if (hadith.explanationBn.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { explanationExpanded = !explanationExpanded }
                        .testTag("toggle_explanation_${hadith.id}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = "ব্যাখ্যা",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "সংক্ষিপ্ত ব্যাখ্যা ও শিক্ষা",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            Icon(
                                imageVector = if (explanationExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (explanationExpanded) "সংকুচিত করুন" else "প্রসারিত করুন",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AnimatedVisibility(visible = explanationExpanded) {
                            Column {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = hadith.explanationBn,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = (preferences.banglaFontSizeSp - 1.5f).coerceAtLeast(13f).sp,
                                        lineHeight = ((preferences.banglaFontSizeSp - 1.5f) * 1.55f).sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 8. Topic Tags
            val tags = remember(hadith.topicTags) {
                hadith.topicTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        AssistChip(
                            onClick = { onTopicClick?.invoke(tag) },
                            label = {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp)
                                )
                            }
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            )

            // 9. Footer Reference + Action Buttons (Minimum 48dp touch targets)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = hadith.referenceBn,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier.testTag("bookmark_button_${hadith.id}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "বুকমার্ক করুন",
                            tint = if (isBookmarked) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            copyHadithToClipboard(context, hadith)
                        },
                        modifier = Modifier.testTag("copy_button_${hadith.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "হাদিস কপি করুন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            shareHadithIntent(context, hadith)
                        },
                        modifier = Modifier.testTag("share_button_${hadith.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "হাদিস শেয়ার করুন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

fun formatHadithForShare(hadith: HadithEntity): String {
    return buildString {
        appendLine("📖 ${hadith.referenceBn}")
        appendLine("মান: ${hadith.gradeBn}")
        appendLine()
        if (hadith.arabicText.isNotBlank()) {
            appendLine(hadith.arabicText)
            appendLine()
        }
        appendLine("${hadith.narratorBn} হতে বর্ণিত:")
        appendLine(hadith.banglaText)
        if (hadith.explanationBn.isNotBlank()) {
            appendLine()
            appendLine("💡 শিক্ষা: ${hadith.explanationBn}")
        }
        appendLine()
        append("— আল হাদিস বাংলা অ্যাপ")
    }
}

fun copyHadithToClipboard(context: Context, hadith: HadithEntity) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Hadith Bangla", formatHadithForShare(hadith))
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "হাদিসটি ক্লিপবোর্ডে কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
}

fun shareHadithIntent(context: Context, hadith: HadithEntity) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, hadith.referenceBn)
        putExtra(Intent.EXTRA_TEXT, formatHadithForShare(hadith))
    }
    val chooser = Intent.createChooser(sendIntent, "হাদিস শেয়ার করুন")
    context.startActivity(chooser)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTypographyBottomSheet(
    preferences: ReaderPreferences,
    onArabicSizeChange: (Float) -> Unit,
    onBanglaSizeChange: (Float) -> Unit,
    onToggleArabic: (Boolean) -> Unit,
    onToggleTransliteration: (Boolean) -> Unit,
    onToggleExplanation: (Boolean) -> Unit,
    onToggleTashkeel: (Boolean) -> Unit,
    onThemeModeChange: (ReaderThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "পাঠ ও ফন্ট কাস্টমাইজেশন",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = "আপনার চোখের আরাম অনুযায়ী আরবি ও বাংলা ফন্ট এবং থিম নির্ধারণ করুন",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Theme selector chips
            Text(
                text = "রিডিং থিম মোড",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReaderThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = preferences.themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                        label = { Text(mode.titleBn) },
                        modifier = Modifier.testTag("theme_chip_${mode.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Arabic Font Size Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "আরবি ফন্ট সাইজ (Amiri)",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${preferences.arabicFontSizeSp.toInt().toBanglaDigits()} sp",
                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
                )
            }
            Slider(
                value = preferences.arabicFontSizeSp,
                onValueChange = onArabicSizeChange,
                valueRange = 20f..40f,
                modifier = Modifier.testTag("arabic_font_slider")
            )

            // Bangla Font Size Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "বাংলা ফন্ট সাইজ (Hind Siliguri)",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${preferences.banglaFontSizeSp.toInt().toBanglaDigits()} sp",
                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
                )
            }
            Slider(
                value = preferences.banglaFontSizeSp,
                onValueChange = onBanglaSizeChange,
                valueRange = 14f..26f,
                modifier = Modifier.testTag("bangla_font_slider")
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingSwitchRow(
                title = "আরবি ইবারত প্রদর্শন",
                checked = preferences.showArabic,
                onCheckedChange = onToggleArabic
            )
            SettingSwitchRow(
                title = "আরবি হরকত (তাজবীদ/তাশকীল) সহ",
                checked = preferences.showTashkeel,
                onCheckedChange = onToggleTashkeel
            )
            SettingSwitchRow(
                title = "বাংলা উচ্চারণ প্রদর্শন",
                checked = preferences.showTransliteration,
                onCheckedChange = onToggleTransliteration
            )
            SettingSwitchRow(
                title = "সংক্ষিপ্ত ব্যাখ্যা ও শিক্ষা প্রদর্শন",
                checked = preferences.showExplanation,
                onCheckedChange = onToggleExplanation
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookmarkSaveDialog(
    hadith: HadithEntity,
    collections: List<BookmarkCollectionEntity>,
    initialCollection: String,
    initialNote: String,
    isAlreadyBookmarked: Boolean,
    onSave: (collectionName: String, note: String) -> Unit,
    onRemove: () -> Unit,
    onCreateCollection: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCollection by remember {
        mutableStateOf(
            initialCollection.ifBlank {
                collections.firstOrNull()?.nameBn ?: "পছন্দের হাদিস"
            }
        )
    }
    var personalNote by remember { mutableStateOf(initialNote) }
    var newFolderInput by remember { mutableStateOf("") }
    var showNewFolderField by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "হাদিস সংগ্রহ ও গবেষণা নোট",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = hadith.referenceBn,
                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "ফোল্ডার নির্বাচন করুন:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    collections.forEach { col ->
                        FilterChip(
                            selected = selectedCollection == col.nameBn,
                            onClick = { selectedCollection = col.nameBn },
                            label = { Text(col.nameBn) }
                        )
                    }
                    AssistChip(
                        onClick = { showNewFolderField = !showNewFolderField },
                        label = { Text("+ নতুন ফোল্ডার") }
                    )
                }

                if (showNewFolderField) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newFolderInput,
                            onValueChange = { newFolderInput = it },
                            label = { Text("নতুন ফোল্ডারের নাম") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                if (newFolderInput.isNotBlank()) {
                                    onCreateCollection(newFolderInput.trim())
                                    selectedCollection = newFolderInput.trim()
                                    newFolderInput = ""
                                    showNewFolderField = false
                                }
                            }
                        ) {
                            Text("যুক্ত করুন")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = personalNote,
                    onValueChange = { personalNote = it },
                    label = { Text("ব্যক্তিগত দারস বা গবেষণা নোট (ঐচ্ছিক)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bookmark_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(selectedCollection, personalNote)
                    onDismiss()
                },
                modifier = Modifier.testTag("confirm_save_bookmark")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            Row {
                if (isAlreadyBookmarked) {
                    TextButton(
                        onClick = {
                            onRemove()
                            onDismiss()
                        }
                    ) {
                        Text("মুছে ফেলুন", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("বাতিল")
                }
            }
        }
    )
}
