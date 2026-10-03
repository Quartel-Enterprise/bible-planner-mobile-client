package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel

private const val CHAPTER_EXTRA_ITEM_COUNT = 3

// Why: boundaries mirror chapterContent's items per chapter (verses + CHAPTER_EXTRA_ITEM_COUNT),
// offset by leadingItemCount for the previous chapter's loading placeholder.
@Composable
internal fun rememberVisibleChapter(
    chapters: List<ReadChapterUiModel>,
    listState: LazyListState,
    leadingItemCount: Int,
): ReadChapterUiModel? {
    val chapterStartIndices = remember(chapters, leadingItemCount) {
        chapters.runningFold(leadingItemCount) { start, chapter ->
            start + chapter.verses.size + CHAPTER_EXTRA_ITEM_COUNT
        }
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
