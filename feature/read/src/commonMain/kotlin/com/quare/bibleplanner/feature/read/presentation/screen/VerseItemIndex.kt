package com.quare.bibleplanner.feature.read.presentation.screen

import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.VerseFocusUiModel

private const val CHAPTER_EXTRA_ITEM_COUNT = 3

internal fun findVerseItemIndex(
    chapters: List<ReadChapterUiModel>,
    leadingItemCount: Int,
    focus: VerseFocusUiModel,
): Int? {
    val chapterStartIndices = chapters.runningFold(leadingItemCount) { start, chapter ->
        start + chapter.verses.size + CHAPTER_EXTRA_ITEM_COUNT
    }
    val chapterIndex = chapters
        .indexOfFirst { chapter ->
            chapter.chapter.bookId == focus.bookId && chapter.chapter.chapterNumber == focus.chapterNumber
        }.takeIf { it >= 0 } ?: return null
    val firstVerseNumber = focus.verseNumbers.minOrNull() ?: return null
    val verseIndex = chapters[chapterIndex]
        .verses
        .indexOfFirst { it.number == firstVerseNumber }
        .takeIf { it >= 0 } ?: return null
    return chapterStartIndices[chapterIndex] + 1 + verseIndex
}
