package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GradeDaifBg
import com.example.ui.theme.GradeDaifDarkBg
import com.example.ui.theme.GradeDaifDarkText
import com.example.ui.theme.GradeDaifText
import com.example.ui.theme.GradeHasanBg
import com.example.ui.theme.GradeHasanDarkBg
import com.example.ui.theme.GradeHasanDarkText
import com.example.ui.theme.GradeHasanText
import com.example.ui.theme.GradeMawduBg
import com.example.ui.theme.GradeMawduDarkBg
import com.example.ui.theme.GradeMawduDarkText
import com.example.ui.theme.GradeMawduText
import com.example.ui.theme.GradeSahihBg
import com.example.ui.theme.GradeSahihDarkBg
import com.example.ui.theme.GradeSahihDarkText
import com.example.ui.theme.GradeSahihText

/**
 * Custom 8-pointed Rub el Hizb Islamic Geometric Medallion drawn via `Modifier.drawWithCache`
 * so `Stroke`, `Size`, and `Offset` calculations are cached across recompositions and scrolls
 * (following Android Compose Graphics Performance guidelines).
 */
@Composable
fun RubElHizbMedallion(
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = GoldAccent
) {
    Box(
        modifier = modifier
            .size(size)
            .drawWithCache {
                val side = this.size.minDimension * 0.64f
                val topLeft = Offset((this.size.width - side) / 2f, (this.size.height - side) / 2f)
                val rectSize = Size(side, side)
                val stroke = Stroke(width = 1.6.dp.toPx())
                val fillAlphaColor = primaryColor.copy(alpha = 0.14f)
                val innerCircleColor = primaryColor.copy(alpha = 0.08f)
                val innerRadius = side * 0.42f

                onDrawBehind {
                    rotate(degrees = 0f) {
                        drawRect(color = fillAlphaColor, topLeft = topLeft, size = rectSize)
                        drawRect(color = accentColor, topLeft = topLeft, size = rectSize, style = stroke)
                    }
                    rotate(degrees = 45f) {
                        drawRect(color = fillAlphaColor, topLeft = topLeft, size = rectSize)
                        drawRect(color = accentColor, topLeft = topLeft, size = rectSize, style = stroke)
                    }
                    drawCircle(color = innerCircleColor, radius = innerRadius)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (label.length >= 4) 11.sp else 13.sp,
                color = primaryColor
            )
        )
    }
}

/**
 * Color-coded Tahqiq (হাদিসের মান: সহীহ, হাসান, যঈফ, জাল/মাওযূ) authenticity badge derived from grade_id.
 */
@Composable
fun TahqiqGradeBadge(
    gradeBn: String,
    gradeType: String,
    modifier: Modifier = Modifier
) {
    val upper = gradeType.uppercase()
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgColor: Color
    val textColor: Color
    val icon = when (upper) {
        "HASAN" -> {
            bgColor = if (isDarkTheme) GradeHasanDarkBg else GradeHasanBg
            textColor = if (isDarkTheme) GradeHasanDarkText else GradeHasanText
            Icons.Default.Verified
        }
        "DAIF" -> {
            bgColor = if (isDarkTheme) GradeDaifDarkBg else GradeDaifBg
            textColor = if (isDarkTheme) GradeDaifDarkText else GradeDaifText
            Icons.Default.WarningAmber
        }
        "MAWDU" -> {
            bgColor = if (isDarkTheme) GradeMawduDarkBg else GradeMawduBg
            textColor = if (isDarkTheme) GradeMawduDarkText else GradeMawduText
            Icons.Default.ErrorOutline
        }
        else -> {
            bgColor = if (isDarkTheme) GradeSahihDarkBg else GradeSahihBg
            textColor = if (isDarkTheme) GradeSahihDarkText else GradeSahihText
            Icons.Default.Verified
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = if (isDarkTheme) 0.42f else 0.32f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .semantics(mergeDescendants = true) { contentDescription = "তাহকীক মান: $gradeBn" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = gradeBn,
            style = MaterialTheme.typography.labelLarge.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        )
    }
}

/**
 * Decorative Islamic Arch Header Divider drawn with `Modifier.drawWithCache`
 * to cache the `Path` object and reuse `Path.rewind()` across layout passes without heap allocations.
 */
@Composable
fun IslamicGeometricDivider(
    modifier: Modifier = Modifier,
    tint: Color = GoldAccent.copy(alpha = 0.45f)
) {
    val diamondPath = androidx.compose.runtime.remember { Path() }
    Box(
        modifier = modifier.drawWithCache {
            val midY = size.height / 2f
            val centerX = size.width / 2f
            val diamondRadius = 6.dp.toPx()
            val strokePx = 1.dp.toPx()
            val dotRadius = 2.5.dp.toPx()
            val sideInset = 16.dp.toPx()

            diamondPath.rewind()
            diamondPath.moveTo(centerX, midY - diamondRadius)
            diamondPath.lineTo(centerX + diamondRadius, midY)
            diamondPath.lineTo(centerX, midY + diamondRadius)
            diamondPath.lineTo(centerX - diamondRadius, midY)
            diamondPath.close()

            onDrawBehind {
                drawLine(
                    color = tint,
                    start = Offset(sideInset, midY),
                    end = Offset(centerX - diamondRadius * 3f, midY),
                    strokeWidth = strokePx
                )
                drawLine(
                    color = tint,
                    start = Offset(centerX + diamondRadius * 3f, midY),
                    end = Offset(size.width - sideInset, midY),
                    strokeWidth = strokePx
                )
                drawPath(path = diamondPath, color = tint)
                drawCircle(
                    color = tint,
                    radius = dotRadius,
                    center = Offset(centerX - diamondRadius * 2f, midY)
                )
                drawCircle(
                    color = tint,
                    radius = dotRadius,
                    center = Offset(centerX + diamondRadius * 2f, midY)
                )
            }
        }
    )
}

/**
 * Helper to convert English digits to Bangla digits (০-৯) for scholarly Bengali presentation.
 */
fun Int.toBanglaDigits(): String = this.toString().toBanglaDigits()

fun String.toBanglaDigits(): String {
    val banglaNums = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val sb = StringBuilder(this.length)
    for (i in 0 until this.length) {
        val ch = this[i]
        if (ch in '0'..'9') {
            sb.append(banglaNums[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

/**
 * Helper to convert Bangla digits (০-৯) to English digits (0-9) for numeric searches and quick-jump inputs.
 */
fun String.toEnglishDigits(): String {
    val sb = StringBuilder(this.length)
    for (i in 0 until this.length) {
        val ch = this[i]
        when (ch) {
            in '০'..'৯' -> sb.append((ch - '০' + '0'.code).toChar())
            else -> sb.append(ch)
        }
    }
    return sb.toString()
}

private val ARABIC_TASHKEEL_REGEX = Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")

/**
 * Helper to strip Arabic Harakat/Tashkeel when the user toggles "হরকত ছাড়া আরবি"
 * and no precomputed `ar_diacless` column is available.
 */
fun String.stripArabicTashkeel(keepTashkeel: Boolean): String {
    if (keepTashkeel) return this
    return this.replace(ARABIC_TASHKEEL_REGEX, "")
}

/**
 * Safe hex color parser for book emblems.
 */
fun parseHexColor(hex: String, fallback: Color = Color(0xFF0B5D48)): Color {
    return runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(fallback)
}
