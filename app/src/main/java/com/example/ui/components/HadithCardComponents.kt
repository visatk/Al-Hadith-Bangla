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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BookmarkCollectionEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.ReaderPreferences
import com.example.ui.theme.ArabicFontStyle
import com.example.ui.theme.BanglaFontStyle
import com.example.ui.theme.GoldAccent
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
    onOpenInReader: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var explanationExpanded by remember(hadith.id, preferences.showExplanation) {
        mutableStateOf(preferences.showExplanation)
    }

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

            // 2. Sacred Arabic Text Block (Amiri Font, RTL aligned, uses ar / ar_diacless columns, text-selectable)
            val resolvedArabicText = remember(hadith.arabicText, hadith.arabicTextDiacless, preferences.showTashkeel) {
                if (preferences.showTashkeel) {
                    hadith.arabicText.ifBlank { hadith.arabicTextDiacless }
                } else {
                    hadith.arabicTextDiacless.ifBlank { hadith.arabicText.stripArabicTashkeel(false) }
                }
            }
            if (preferences.showArabic && resolvedArabicText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .border(
                            width = 1.dp,
                            color = GoldAccent.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    androidx.compose.foundation.text.selection.SelectionContainer {
                        Text(
                            text = resolvedArabicText,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = preferences.arabicFontStyle.fontFamily,
                                fontSize = preferences.arabicFontSizeSp.sp,
                                lineHeight = (preferences.arabicFontSizeSp * (preferences.lineHeightMultiplier + 0.14f)).sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Right,
                                textDirection = TextDirection.Rtl
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 3. Narrator (রাবী) Badge
            if (hadith.narratorBn.isNotBlank()) {
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
            }

            // 4. Bangla Translation (Selectable for scholarly study, ergonomic line spacing)
            androidx.compose.foundation.text.selection.SelectionContainer {
                Text(
                    text = hadith.banglaText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = preferences.banglaFontStyle.fontFamily,
                        fontSize = preferences.banglaFontSizeSp.sp,
                        lineHeight = (preferences.banglaFontSizeSp * preferences.lineHeightMultiplier).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            // 5. Personal Study Note (if saved in bookmarks)
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

            // 6. Scholarly Commentary / Explanation (তাহকীক ও ব্যাখ্যা)
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
                                        lineHeight = ((preferences.banglaFontSizeSp - 1.5f) * preferences.lineHeightMultiplier).sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 7. Topic Tags
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

            // 8. Footer Reference + Action Buttons (Minimum 48dp touch targets)
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
                    if (onOpenInReader != null) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOpenInReader()
                            },
                            modifier = Modifier.testTag("open_in_reader_button_${hadith.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "অধ্যায়ে পড়ুন",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBookmarkClick()
                        },
                        modifier = Modifier.testTag("bookmark_button_${hadith.id}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) "বুকমার্ক সম্পাদনা করুন" else "বুকমার্ক করুন",
                            tint = if (isBookmarked) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
        if (hadith.narratorBn.isNotBlank()) {
            appendLine("${hadith.narratorBn} হতে বর্ণিত:")
        }
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
    runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: error("Clipboard unavailable")
        val clip = ClipData.newPlainText("Hadith Bangla", formatHadithForShare(hadith))
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "হাদিসটি ক্লিপবোর্ডে কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, "কপি করা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
    }
}

fun shareHadithIntent(context: Context, hadith: HadithEntity) {
    runCatching {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, hadith.referenceBn)
            putExtra(Intent.EXTRA_TEXT, formatHadithForShare(hadith))
        }
        val chooser = Intent.createChooser(sendIntent, "হাদিস শেয়ার করুন")
        context.startActivity(chooser)
    }.onFailure {
        Toast.makeText(context, "শেয়ার করার অ্যাপ পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReaderTypographyBottomSheet(
    preferences: ReaderPreferences,
    onArabicSizeChange: (Float) -> Unit,
    onBanglaSizeChange: (Float) -> Unit,
    onLineHeightChange: (Float) -> Unit,
    onEyeCareFilterChange: (Float) -> Unit,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    onToggleArabic: (Boolean) -> Unit,
    onToggleExplanation: (Boolean) -> Unit,
    onToggleTashkeel: (Boolean) -> Unit,
    onThemeModeChange: (ReaderThemeMode) -> Unit,
    onArabicFontStyleChange: (ArabicFontStyle) -> Unit = {},
    onBanglaFontStyleChange: (BanglaFontStyle) -> Unit = {},
    onApplyPreset: ((
        arabicSizeSp: Float,
        banglaSizeSp: Float,
        lineHeight: Float,
        warmFilter: Float,
        themeMode: ReaderThemeMode,
        arabicFontStyle: ArabicFontStyle,
        banglaFontStyle: BanglaFontStyle
    ) -> Unit)? = null,
    onImportSqliteDatabase: ((android.net.Uri) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val dbFilePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onImportSqliteDatabase?.invoke(uri)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "পাঠের আরাম, ফন্ট ও থিম স্টুডিও",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = "মনোযোগ ধরে রাখতে ও চোখের আরামের জন্য আরবি-বাংলা ফন্ট, লাইন স্পেসিং ও থিম কাস্টমাইজ করুন",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Live Typography Preview Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = GoldAccent.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("typography_live_preview_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "লাইভ প্রিভিউ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${preferences.arabicFontStyle.titleBn} • ${preferences.banglaFontStyle.titleBn}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    if (preferences.showArabic) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val previewArabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ"
                            .stripArabicTashkeel(preferences.showTashkeel)
                        Text(
                            text = previewArabic,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = preferences.arabicFontStyle.fontFamily,
                                fontSize = preferences.arabicFontSizeSp.sp,
                                lineHeight = (preferences.arabicFontSizeSp * (preferences.lineHeightMultiplier + 0.14f)).sp,
                                textAlign = TextAlign.Right,
                                textDirection = TextDirection.Rtl,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "নিশ্চয়ই সকল আমল নিয়তের উপর নির্ভরশীল এবং প্রত্যেকে তাই পাবে যা সে নিয়ত করে।",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = preferences.banglaFontStyle.fontFamily,
                            fontSize = preferences.banglaFontSizeSp.sp,
                            lineHeight = (preferences.banglaFontSizeSp * preferences.lineHeightMultiplier).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // One-Tap Parametric Reading Presets
            if (onApplyPreset != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "দ্রুত রিডিং প্রিসেট (এক ট্যাপে সেটআপ)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistChip(
                        onClick = {
                            onApplyPreset(
                                26f,
                                17f,
                                1.65f,
                                0f,
                                ReaderThemeMode.MANUSCRIPT_LIGHT,
                                ArabicFontStyle.AMIRI,
                                BanglaFontStyle.HIND_SILIGURI
                            )
                        },
                        label = { Text("প্রামাণিক দারস") },
                        modifier = Modifier.testTag("preset_standard_dars")
                    )
                    AssistChip(
                        onClick = {
                            onApplyPreset(
                                32f,
                                20f,
                                1.80f,
                                0.04f,
                                ReaderThemeMode.SEPIA_SCHOLAR,
                                ArabicFontStyle.SCHEHERAZADE,
                                BanglaFontStyle.NOTO_SERIF_BENGALI
                            )
                        },
                        label = { Text("বড় অক্ষর (স্বস্তিদায়ক)") },
                        modifier = Modifier.testTag("preset_comfort_large")
                    )
                    AssistChip(
                        onClick = {
                            onApplyPreset(
                                28f,
                                18f,
                                1.75f,
                                0.10f,
                                ReaderThemeMode.TAHAJJUD_DARK,
                                ArabicFontStyle.SCHEHERAZADE,
                                BanglaFontStyle.HIND_SILIGURI
                            )
                        },
                        label = { Text("তাহাজ্জুদ নাইট") },
                        modifier = Modifier.testTag("preset_tahajjud_night")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Theme selector cards with live color swatches
            Text(
                text = "রিডিং থিম ও কালার প্যালেট",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReaderThemeMode.entries.forEach { mode ->
                    val isSelected = preferences.themeMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onThemeModeChange(mode) },
                        leadingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy((-4).dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(mode.bgPreview)
                                        .border(1.dp, mode.primaryPreview, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(mode.primaryPreview)
                                        .border(1.dp, mode.accentPreview, CircleShape)
                                )
                            }
                        },
                        label = { Text(mode.titleBn) },
                        modifier = Modifier.testTag("theme_chip_${mode.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Arabic Font Family Selector
            Text(
                text = "আরবি লিপি (Arabic Font Family)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ArabicFontStyle.entries.forEach { style ->
                    FilterChip(
                        selected = preferences.arabicFontStyle == style,
                        onClick = { onArabicFontStyleChange(style) },
                        label = {
                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = style.titleBn,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = "بِسْمِ اللَّهِ • ${style.subtitleBn}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = style.fontFamily,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("arabic_font_chip_${style.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bangla Font Family Selector
            Text(
                text = "বাংলা ফন্ট শৈলী (Bangla Typography)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BanglaFontStyle.entries.forEach { style ->
                    FilterChip(
                        selected = preferences.banglaFontStyle == style,
                        onClick = { onBanglaFontStyleChange(style) },
                        label = {
                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = style.titleBn,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = style.fontFamily
                                    )
                                )
                                Text(
                                    text = style.subtitleBn,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = style.fontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("bangla_font_chip_${style.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Arabic Font Size Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "আরবি ফন্ট সাইজ (${preferences.arabicFontStyle.titleBn})",
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
                    text = "বাংলা ফন্ট সাইজ (${preferences.banglaFontStyle.titleBn})",
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

            // Line Spacing Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "লাইন স্পেসিং (স্বস্তিদায়ক পাঠের দূরত্ব)",
                    style = MaterialTheme.typography.titleSmall
                )
                val pct = ((preferences.lineHeightMultiplier * 100).toInt()).toBanglaDigits()
                Text(
                    text = "$pct%",
                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
                )
            }
            Slider(
                value = preferences.lineHeightMultiplier,
                onValueChange = onLineHeightChange,
                valueRange = 1.4f..2.1f,
                modifier = Modifier.testTag("line_height_slider")
            )

            // Eye-Care Warm Light Filter Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "চোখের আরাম ফিল্টার (নীল আলো হ্রাস)",
                    style = MaterialTheme.typography.titleSmall
                )
                val warmPct = ((preferences.eyeCareWarmFilter / 0.28f) * 100).toInt().coerceIn(0, 100).toBanglaDigits()
                Text(
                    text = "$warmPct%",
                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
                )
            }
            Slider(
                value = preferences.eyeCareWarmFilter,
                onValueChange = onEyeCareFilterChange,
                valueRange = 0f..0.28f,
                modifier = Modifier.testTag("eye_care_warm_slider")
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingSwitchRow(
                title = "পড়ার সময় স্ক্রিন চালু রাখুন",
                checked = preferences.keepScreenOnWhileReading,
                onCheckedChange = onToggleKeepScreenOn
            )
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
                title = "সংক্ষিপ্ত ব্যাখ্যা ও শিক্ষা প্রদর্শন",
                checked = preferences.showExplanation,
                onCheckedChange = onToggleExplanation
            )

            if (onImportSqliteDatabase != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                androidx.compose.material3.OutlinedButton(
                    onClick = { dbFilePicker.launch("*/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_import_sqlite_button")
                ) {
                    Text("নতুন হাদিস ডেটাবেস (.sqlite / .db) যুক্ত করুন")
                }
            }

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
