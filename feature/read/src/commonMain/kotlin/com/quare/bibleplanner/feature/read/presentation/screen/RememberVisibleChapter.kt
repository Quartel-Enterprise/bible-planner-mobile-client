package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.screen.content.getChapterStartIndices

/**
 * Which chapter the top of the list is sitting in, offset by the [leadingItemCount] the placeholder
 * for the previous chapter takes while it loads.
 */
@Composable
internal fun rememberVisibleChapter(
    chapters: List<ReadChapterUiModel>,
    listState: LazyListState,
    leadingItemCount: Int,
    isChapterStudyBeside: Boolean,
): ReadChapterUiModel? {
    val chapterStartIndices = remember(chapters, leadingItemCount, isChapterStudyBeside) {
        getChapterStartIndices(
            chapters = chapters,
            leadingItemCount = leadingItemCount,
            isChapterStudyBeside = isChapterStudyBeside,
        )
    }
    return remember(chapters, chapterStartIndices) {
        derivedStateOf {
            chapters.indices
                .lastOrNull { index ->
                    chapterStartIndices[index] <= listState.firstVisibleItemIndex
                }?.let(chapters::get)
        }
    }.value
}
