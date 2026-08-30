package com.quare.bibleplanner.feature.read.presentation.screen.content

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val contentHorizontalPadding = 20.dp

@Composable
internal fun ReadLoadingContent(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.padding(horizontal = contentHorizontalPadding),
        userScrollEnabled = false,
    ) {
        chapterShimmerContent(ChapterShimmerPosition.LEADING)
    }
}
