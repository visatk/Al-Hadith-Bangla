package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.ReadingProgressEntity
import com.example.data.local.entity.SqliteDatabaseFileReport
import com.example.ui.components.IslamicGeometricDivider
import com.example.ui.components.RubElHizbMedallion
import com.example.ui.components.TahqiqGradeBadge
import com.example.ui.components.parseHexColor
import com.example.ui.components.toBanglaDigits
import com.example.ui.theme.AmiriArabicFontFamily
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    books: List<BookEntity>,
    allHadiths: List<HadithEntity>,
    readingProgress: ReadingProgressEntity?,
    sqliteReports: List<SqliteDatabaseFileReport>,
    onBookClick: (BookEntity) -> Unit,
    onHadithClick: (HadithEntity) -> Unit,
    onContinueReadingClick: (ReadingProgressEntity) -> Unit,
    onSearchBarClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onSqliteStudioClick: () -> Unit,
    onTypographySettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val featuredHadith = remember(allHadiths) {
        allHadiths.firstOrNull { it.id == 1 } ?: allHadiths.firstOrNull()
    }

    val popularTopics = remember {
        listOf("ঈমান", "সালাত", "ইলম", "ইখলাস", "আখলাক", "তাকওয়া", "পবিত্রতা", "প্রতিবেশী")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_lazy_column"),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Atmospheric Islamic Arch Hero Banner + Search Trigger
        item {
            HeroSanctuaryHeader(
                sqliteCount = sqliteReports.size,
                totalBooks = books.size,
                totalIndexedHadiths = allHadiths.size,
                onSearchBarClick = onSearchBarClick,
                onSqliteStudioClick = onSqliteStudioClick,
                onTypographySettingsClick = onTypographySettingsClick
            )
        }

        // 2. Continue Reading Card (সর্বশেষ পঠিত) or Quick Welcome Guide
        item {
            if (readingProgress != null) {
                ContinueReadingCard(
                    progress = readingProgress,
                    onClick = { onContinueReadingClick(readingProgress) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // 3. Daily Featured Hadith Showcase (আজকের নির্বাচিত হাদিস)
        if (featuredHadith != null) {
            item {
                FeaturedHadithShowcaseCard(
                    hadith = featuredHadith,
                    onReadFullClick = { onHadithClick(featuredHadith) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // 4. Topic Explorer Chips (বিষয়ভিত্তিক হাদিস অন্বেষণ)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "বিষয়ভিত্তিক হাদিস অন্বেষণ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "ট্যাপ করে খুঁজুন",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    popularTopics.forEach { topic ->
                        AssistChip(
                            onClick = { onTopicClick(topic) },
                            label = {
                                Text(
                                    text = topic,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("topic_chip_$topic")
                        )
                    }
                }
            }
        }

        // 5. Primary Hadith Books Collection Header
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                IslamicGeometricDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "বিশুদ্ধ হাদিস গ্রন্থসমূহ",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "সিহাহ সিত্তাহ ও নির্বাচিত হাদিস সংকলন (.sqlite চালিত)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSqliteStudioClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "SQLite উৎস",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${books.size.toBanglaDigits()} টি গ্রন্থ",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }

        // 6. Books Grid (2 columns or responsive cards)
        val chunkedBooks = books.chunked(2)
        items(chunkedBooks.size) { rowIndex ->
            val pair = chunkedBooks[rowIndex]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { book ->
                    HadithBookGridCard(
                        book = book,
                        onClick = { onBookClick(book) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HeroSanctuaryHeader(
    sqliteCount: Int,
    totalBooks: Int,
    totalIndexedHadiths: Int,
    onSearchBarClick: () -> Unit,
    onSqliteStudioClick: () -> Unit,
    onTypographySettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(EmeraldDeep)
    ) {
        // Generated Islamic Arch Hero Illustration
        Image(
            painter = painterResource(id = R.drawable.img_hero_islamic_arch_1795332332976),
            contentDescription = "ইসলামিক আর্চ ব্যানার",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
        )

        // Rich Emerald-to-Obsidian Gradient Overlay for crisp text contrast
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            EmeraldDeep.copy(alpha = 0.82f),
                            EmeraldDeep.copy(alpha = 0.90f),
                            EmeraldDeep.copy(alpha = 0.97f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Top bar with Arabic Basmala & Typography button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onSqliteStudioClick() }
                        .testTag("hero_sqlite_status_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(GoldLight)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${sqliteCount.toBanglaDigits()}টি .sqlite সক্রিয় • ${totalBooks.toBanglaDigits()}টি গ্রন্থ",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onTypographySettingsClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .testTag("home_typography_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "ফন্ট ও থিম সেটিংস",
                        tint = GoldLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = AmiriArabicFontFamily,
                    fontSize = 24.sp,
                    color = GoldLight
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "আল হাদিস বাংলা",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "রাসূলুল্লাহ (ﷺ)-এর বিশুদ্ধ সুন্নাহ, তাহকীক ও সংক্ষিপ্ত ব্যাখ্যা সংকলন",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.85f)
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Elevated Search Bar Trigger
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onSearchBarClick() }
                    .testTag("home_search_trigger")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "অনুসন্ধান",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "হাদিস নম্বর, শব্দ, রাবী বা বিষয় লিখে খুঁজুন…",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${totalIndexedHadiths.toBanglaDigits()}+",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueReadingCard(
    progress: ReadingProgressEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("continue_reading_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "সর্বশেষ পঠিত",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "সর্বশেষ পঠিত হাদিস থেকে শুরু করুন",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "${progress.bookTitleBn} • ${progress.chapterTitleBn}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "হাদিস নং ${progress.hadithNumber.toBanglaDigits()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun FeaturedHadithShowcaseCard(
    hadith: HadithEntity,
    onReadFullClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = GoldAccent.copy(alpha = 0.45f),
                shape = RoundedCornerShape(22.dp)
            )
            .clip(RoundedCornerShape(22.dp))
            .clickable { onReadFullClick() }
            .testTag("featured_daily_hadith_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "আজকের নির্বাচিত হাদিস",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                TahqiqGradeBadge(
                    gradeBn = hadith.gradeBn,
                    gradeType = hadith.gradeType
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = hadith.arabicText,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = AmiriArabicFontFamily,
                    fontSize = 23.sp,
                    lineHeight = 38.sp,
                    textAlign = TextAlign.Right,
                    textDirection = TextDirection.Rtl,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = hadith.banglaText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${hadith.narratorBn} • ${hadith.internationalNumber}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ব্যাখ্যাসহ পড়ুন",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HadithBookGridCard(
    book: BookEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = remember(book.accentHex) { parseHexColor(book.accentHex) }

    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("book_card_${book.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RubElHizbMedallion(
                    label = book.bookCode,
                    size = 44.dp,
                    primaryColor = accentColor,
                    accentColor = GoldAccent
                )
                Text(
                    text = book.titleAr,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = AmiriArabicFontFamily,
                        fontSize = 19.sp,
                        color = accentColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = book.titleBn,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = book.authorBn,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.10f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${book.chapterCount.toBanglaDigits()} অধ্যায়",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = accentColor,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "${book.hadithCount.toBanglaDigits()} হাদিস",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = accentColor,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}
