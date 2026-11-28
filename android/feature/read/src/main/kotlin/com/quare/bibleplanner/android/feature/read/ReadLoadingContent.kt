package com.quare.bibleplanner.android.feature.read

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val contentHorizontalPadding = 20.dp

@Composable
internal fun ReadLoadingContent(modifier: Modifier = Modifier) {
    val pulse by rememberSkeletonPulse()
    LazyColumn(
        modifier = modifier.padding(horizontal = contentHorizontalPadding),
        userScrollEnabled = false,
    ) {
        chapterSkeletonContent(
            position = ChapterSkeletonPosition.LEADING,
            pulse = { pulse },
        )
    }
}
