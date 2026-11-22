package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GradeDaifBg
import com.example.ui.theme.GradeDaifText
import com.example.ui.theme.GradeHasanBg
import com.example.ui.theme.GradeHasanText
import com.example.ui.theme.GradeSahihBg
import com.example.ui.theme.GradeSahihText

/**
 * Custom Canvas-drawn 8-pointed Rub el Hizb Islamic Geometric Medallion for Hadith & Chapter numbers.
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
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val side = this.size.minDimension * 0.64f
            val topLeft = Offset((this.size.width - side) / 2f, (this.size.height - side) / 2f)

            // Draw two overlapping rotated squares (Rub el Hizb 8-pointed star)
            for (angle in listOf(0f, 45f)) {
                rotate(degrees = angle) {
                    drawRect(
                        color = primaryColor.copy(alpha = 0.14f),
                        topLeft = topLeft,
                        size = Size(side, side)
                    )
                    drawRect(
                        color = accentColor,
                        topLeft = topLeft,
                        size = Size(side, side),
                        style = Stroke(width = 1.6.dp.toPx())
                    )
                }
            }
            drawCircle(
                color = primaryColor.copy(alpha = 0.08f),
                radius = side * 0.42f
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = primaryColor
            )
        )
    }
}

/**
 * Color-coded Tahqiq (হাদিসের মান: সহীহ, হাসান, যঈফ) authenticity badge.
 */
@Composable
fun TahqiqGradeBadge(
    gradeBn: String,
    gradeType: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (gradeType.uppercase()) {
        "HASAN" -> GradeHasanBg to GradeHasanText
        "DAIF" -> GradeDaifBg to GradeDaifText
        else -> GradeSahihBg to GradeSahihText
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.28f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = gradeBn,
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
 * Decorative Islamic Arch Header Divider drawn with Jetpack Compose Canvas.
 */
@Composable
fun IslamicGeometricDivider(
    modifier: Modifier = Modifier,
    tint: Color = GoldAccent.copy(alpha = 0.45f)
) {
    Canvas(modifier = modifier) {
        val midY = size.height / 2f
        val centerX = size.width / 2f
        val diamondRadius = 6.dp.toPx()

        drawLine(
            color = tint,
            start = Offset(16.dp.toPx(), midY),
            end = Offset(centerX - diamondRadius * 3f, midY),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = tint,
            start = Offset(centerX + diamondRadius * 3f, midY),
            end = Offset(size.width - 16.dp.toPx(), midY),
            strokeWidth = 1.dp.toPx()
        )

        val diamondPath = Path().apply {
            moveTo(centerX, midY - diamondRadius)
            lineTo(centerX + diamondRadius, midY)
            lineTo(centerX, midY + diamondRadius)
            lineTo(centerX - diamondRadius, midY)
            close()
        }
        drawPath(path = diamondPath, color = tint)
        drawCircle(
            color = tint,
            radius = 2.5.dp.toPx(),
            center = Offset(centerX - diamondRadius * 2f, midY)
        )
        drawCircle(
            color = tint,
            radius = 2.5.dp.toPx(),
            center = Offset(centerX + diamondRadius * 2f, midY)
        )
    }
}

/**
 * Helper to convert English digits to Bangla digits (০-৯) for scholarly Bengali presentation.
 */
fun Int.toBanglaDigits(): String = this.toString().toBanglaDigits()

fun String.toBanglaDigits(): String {
    val banglaNums = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val sb = StringBuilder(this.length)
    for (ch in this) {
        if (ch in '0'..'9') {
            sb.append(banglaNums[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

/**
 * Helper to strip Arabic Harakat/Tashkeel when the user toggles "হরকত ছাড়া আরবি".
 */
fun String.stripArabicTashkeel(keepTashkeel: Boolean): String {
    if (keepTashkeel) return this
    val tashkeelRegex = Regex("[\\u064B-\\u065F\\u0670]")
    return this.replace(tashkeelRegex, "")
}

/**
 * Safe hex color parser for book emblems.
 */
fun parseHexColor(hex: String, fallback: Color = Color(0xFF0B5D48)): Color {
    return runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(fallback)
}
