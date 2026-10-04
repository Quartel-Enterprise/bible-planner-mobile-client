package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quare.bibleplanner.ui.theme.font.displaySerifFontFamily

private val bookNameFontSize = 15.sp
private val bookNameLetterSpacing = 3.sp
private val chapterNumberFontSize = 60.sp

/*
 * Why: deliberately not a shared element with the chapter label it opens from; that label
 * is a list row and matching bounds squeezes this header to the row's width.
 */
@Composable
internal fun ChapterHeader(
    bookName: String,
    chapterNumber: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = bookName.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontFamily = displaySerifFontFamily(),
            fontSize = bookNameFontSize,
            letterSpacing = bookNameLetterSpacing,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = chapterNumber.toString(),
            style = MaterialTheme.typography.displayLarge,
            fontFamily = displaySerifFontFamily(),
            fontSize = chapterNumberFontSize,
        )
    }
}
