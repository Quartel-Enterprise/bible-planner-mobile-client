package com.quare.bibleplanner.feature.read.presentation.screen

import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.VerseFocusUiModel
import com.quare.bibleplanner.feature.read.presentation.screen.content.getChapterStartIndices

internal fun findVerseItemIndex(
    chapters: List<ReadChapterUiModel>,
    leadingItemCount: Int,
    isChapterStudyBeside: Boolean,
    focus: VerseFocusUiModel,
): Int? {
    val chapterStartIndices = getChapterStartIndices(
        chapters = chapters,
        leadingItemCount = leadingItemCount,
        isChapterStudyBeside = isChapterStudyBeside,
    )
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
