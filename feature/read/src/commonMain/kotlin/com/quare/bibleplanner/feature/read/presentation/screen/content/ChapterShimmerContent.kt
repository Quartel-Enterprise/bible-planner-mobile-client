package com.quare.bibleplanner.feature.read.presentation.screen.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox

private const val VERSE_SHIMMER_COUNT = 8
private const val CHAPTER_SHIMMER_KEY = "chapter-shimmer"
private val verseLineWidthFractions = listOf(
    listOf(1f, 0.96f, 0.58f),
    listOf(1f, 0.71f),
    listOf(1f, 0.93f, 0.82f),
    listOf(1f, 0.64f),
)
private val headerVerticalPadding = 24.dp
private val bookNameWidth = 120.dp
private val bookNameHeight = 15.dp
private val chapterNumberWidth = 72.dp
private val chapterNumberHeight = 56.dp
private val verseNumberWidth = 18.dp
private val verseLineHeight = 14.dp
private val verseLineSpacing = 14.dp
private val verseVerticalPadding = 6.dp

internal fun LazyListScope.chapterShimmerContent() {
    item(key = "$CHAPTER_SHIMMER_KEY-header") {
        ChapterHeaderShimmer()
    }
    items(
        count = VERSE_SHIMMER_COUNT,
        key = { index -> "$CHAPTER_SHIMMER_KEY-verse-$index" },
    ) { index ->
        VerseShimmerRow(
            lineWidthFractions = verseLineWidthFractions[index % verseLineWidthFractions.size],
        )
    }
}

@Composable
private fun ChapterHeaderShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = headerVerticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ShimmerBox(
            modifier = Modifier
                .width(bookNameWidth)
                .height(bookNameHeight),
        )
        ShimmerBox(
            modifier = Modifier
                .width(chapterNumberWidth)
                .height(chapterNumberHeight),
        )
    }
}

@Composable
private fun VerseShimmerRow(lineWidthFractions: List<Float>) {
    Row(
        modifier = Modifier.padding(vertical = verseVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        ShimmerBox(
            modifier = Modifier
                .width(verseNumberWidth)
                .height(verseLineHeight),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(verseLineSpacing),
        ) {
            lineWidthFractions.forEach { fraction ->
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(verseLineHeight),
                )
            }
        }
    }
}
