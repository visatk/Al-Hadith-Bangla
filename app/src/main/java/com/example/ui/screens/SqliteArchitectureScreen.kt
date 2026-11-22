package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SqliteDatabaseFileReport
import com.example.ui.components.toBanglaDigits
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GradeSahihBg
import com.example.ui.theme.GradeSahihText

/**
 * Dedicated SQLite Data Source & Architecture Hub screen where the developer/user can see
 * the exact `app/src/main/assets/databases/` folder configuration, inspect all discovered `.sqlite`
 * databases, copy the SQLite schema, or import additional `.sqlite` / `.db` files at runtime.
 */
@Composable
fun SqliteArchitectureScreen(
    sqliteReports: List<SqliteDatabaseFileReport>,
    isSyncing: Boolean,
    totalBooksCount: Int,
    totalHadithsCount: Int,
    totalAyahsCount: Int = 6236,
    onResyncClick: () -> Unit,
    onImportSqliteUri: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sqlitePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportSqliteUri(uri)
        }
    }

    val standardSchemaSql = """
CREATE TABLE books (
    id INTEGER PRIMARY KEY,
    title_bn TEXT NOT NULL,
    title_ar TEXT NOT NULL,
    title_en TEXT,
    author_bn TEXT,
    hadith_count INTEGER DEFAULT 0,
    chapter_count INTEGER DEFAULT 0,
    book_code TEXT,
    accent_hex TEXT,
    description_bn TEXT
);

CREATE TABLE chapters (
    id INTEGER PRIMARY KEY,
    book_id INTEGER NOT NULL,
    chapter_number INTEGER NOT NULL,
    title_bn TEXT NOT NULL,
    title_ar TEXT,
    hadith_range TEXT,
    hadith_count INTEGER DEFAULT 0
);

CREATE TABLE sections (
    id INTEGER PRIMARY KEY,
    book_id INTEGER NOT NULL,
    chapter_id INTEGER NOT NULL,
    section_number TEXT,
    title_bn TEXT NOT NULL,
    preface_bn TEXT
);

CREATE TABLE hadiths (
    id INTEGER PRIMARY KEY,
    book_id INTEGER NOT NULL,
    chapter_id INTEGER NOT NULL,
    section_id INTEGER,
    hadith_number INTEGER NOT NULL,
    international_number TEXT,
    arabic_text TEXT NOT NULL,
    bangla_text TEXT NOT NULL,
    transliteration_bn TEXT,
    narrator_bn TEXT NOT NULL,
    explanation_bn TEXT,
    grade_bn TEXT NOT NULL,
    grade_type TEXT NOT NULL,
    reference_bn TEXT,
    topic_tags TEXT
);
    """.trimIndent()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("sqlite_architecture_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Dedicated Folder Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SQLite Data Source Architecture",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "মডুলার .sqlite ও Room হাইব্রিড ইঞ্জিন",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dedicated Data Source Folder Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = GoldAccent.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ডেটাবেস আপলোড ফোল্ডার (Data Source):",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "app/src/main/assets/databases/bangla-hadiths/\napp/src/main/assets/databases/quran_bn.json",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "এই ফোল্ডারের সকল .sqlite হাদিস গ্রন্থ (chapter, section, hadith স্কিমা) এবং quran_bn.json (১১৪ সূরা, ৬,২৩৬ আয়াত) স্বয়ংক্রিয়ভাবে স্ক্যান ও Room-এ ইনডেক্স করা হয়েছে।",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons: Import .sqlite at runtime & Resync
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                sqlitePickerLauncher.launch(arrayOf("*/*"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_sqlite_file_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(".sqlite ইম্পোর্ট করুন")
                        }

                        OutlinedButton(
                            onClick = onResyncClick,
                            enabled = !isSyncing,
                            modifier = Modifier.testTag("resync_sqlite_button")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সিঙ্ক")
                        }
                    }
                }
            }
        }

        // 2. Live Indexed Summary Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "হাদিস গ্রন্থ",
                    value = totalBooksCount.toBanglaDigits(),
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "ইনডেক্সড হাদিস",
                    value = totalHadithsCount.toBanglaDigits(),
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "কুরআন আয়াত",
                    value = totalAyahsCount.toBanglaDigits(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Discovered .sqlite Database Files List
        item {
            Text(
                text = "শনাক্তকৃত .sqlite ডেটাবেস ফাইলসমূহ",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(sqliteReports, key = { it.relativePath }) { report ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = report.fileName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = report.relativePath,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GradeSahihBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = GradeSahihText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${report.fileSizeKb.toInt().toBanglaDigits()} KB",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = GradeSahihText,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )

                    Text(
                        text = "টেবিলসমূহ: ${report.detectedTables.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.statusNoteBn,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // 4. SQLite Schema Reference Card for uploading future databases
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                ),
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
                                text = "সমর্থিত SQLite টেবিল স্কিমা (DDL)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "নতুন .sqlite ডেটাবেস তৈরির জন্য এই স্ট্যান্ডার্ড স্কিমা ব্যবহার করুন",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                copySchemaToClipboard(context, standardSchemaSql)
                            },
                            modifier = Modifier.testTag("copy_sql_schema_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("কপি SQL")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = standardSchemaSql,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

private fun copySchemaToClipboard(context: Context, sql: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Al Hadith SQLite Schema", sql))
    Toast.makeText(context, "SQLite স্কিমা কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
}
