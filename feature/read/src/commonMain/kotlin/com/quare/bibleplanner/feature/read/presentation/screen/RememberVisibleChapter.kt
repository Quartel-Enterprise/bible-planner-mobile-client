package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel

private const val CHAPTER_EXTRA_ITEM_COUNT = 2

/**
 * Which chapter the top of the list is sitting in. Each chapter lays out its header, one item per
 * verse and its end-of-chapter row, so the boundaries are a running total of those.
 */
@Composable
internal fun rememberVisibleChapter(
    chapters: List<ReadChapterUiModel>,
    listState: LazyListState,
): ReadChapterUiModel? {
    val chapterStartIndices = remember(chapters) {
        chapters.runningFold(0) { start, chapter -> start + chapter.verses.size + CHAPTER_EXTRA_ITEM_COUNT }
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
