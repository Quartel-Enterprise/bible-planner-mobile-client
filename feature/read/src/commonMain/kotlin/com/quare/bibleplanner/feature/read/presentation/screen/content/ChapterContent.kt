package com.quare.bibleplanner.feature.read.presentation.screen.content

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.presentation.component.VerseFlash
import com.quare.bibleplanner.feature.read.presentation.model.ChapterStudyEntrySource
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.screen.component.ChapterEndNavigationRow
import com.quare.bibleplanner.feature.read.presentation.screen.component.ChapterHeader
import com.quare.bibleplanner.feature.read.presentation.screen.component.ChapterStudyEntryCard
import com.quare.bibleplanner.feature.read.presentation.screen.component.VerseRow
import org.jetbrains.compose.resources.stringResource

/**
 * One chapter as list items: its header, its verses, and the end-of-chapter controls. Vertical
 * reading calls this once per chapter into the same list, which is what makes the text continue, and
 * drops the chapter arrows: the next chapter is already below, so the only decision left is the read
 * status.
 */
internal fun LazyListScope.chapterContent(
    chapter: ReadChapterUiModel,
    header: ReadHeaderUiModel,
    settings: ReaderSettingsModel,
    isChapterStudyBeside: Boolean,
    focusedVerseNumber: Int?,
    verseFlash: VerseFlash,
    onEvent: (ReadUiEvent) -> Unit,
) {
    item(key = "chapter-header-${chapter.chapter.bookId}-${chapter.chapter.chapterNumber}") {
        ChapterHeader(
            bookName = stringResource(chapter.bookStringResource),
            chapterNumber = chapter.chapter.chapterNumber,
        )
    }
    items(
        count = chapter.verses.size,
        key = { index ->
            "verse-${chapter.chapter.bookId}-${chapter.chapter.chapterNumber}-${chapter.verses[index].number}"
        },
    ) { index ->
        val verse = chapter.verses[index]
        val flashFocus = verseFlash.focus
        val isFlashing = flashFocus != null &&
            flashFocus.bookId == chapter.chapter.bookId &&
            flashFocus.chapterNumber == chapter.chapter.chapterNumber &&
            verse.number in flashFocus.verseNumbers
        VerseRow(
            verse = verse,
            settings = settings,
            flashAlpha = verseFlash.alpha.takeIf { isFlashing },
            isDimmed = focusedVerseNumber != null && focusedVerseNumber != verse.number,
            onClick = {
                onEvent(
                    ReadUiEvent.OnVerseClick(
                        chapter = chapter.chapter,
                        verseNumber = verse.number,
                    ),
                )
            },
        )
    }
    if (!isChapterStudyBeside) {
        item(key = "chapter-study-${chapter.chapter.bookId}-${chapter.chapter.chapterNumber}") {
            val bookName = stringResource(chapter.bookStringResource)
            ChapterStudyEntryCard(
                chapterLabel = "$bookName ${chapter.chapter.chapterNumber}".takeIf {
                    settings.isVerticalReadingEnabled
                },
                onClick = {
                    onEvent(
                        ReadUiEvent.OnChapterStudyClick(
                            bookId = chapter.chapter.bookId,
                            chapterNumber = chapter.chapter.chapterNumber,
                            source = ChapterStudyEntrySource.CHAPTER_END,
                        ),
                    )
                },
                modifier = Modifier.padding(top = 28.dp),
            )
        }
    }
    item(key = "chapter-end-${chapter.chapter.bookId}-${chapter.chapter.chapterNumber}") {
        ChapterEndNavigationRow(
            suggestions = header.navigationSuggestions.takeIf { !settings.isVerticalReadingEnabled },
            isRead = chapter.isRead,
            onReadClick = {
                onEvent(
                    ReadUiEvent.ToggleReadStatus(
                        bookId = chapter.chapter.bookId,
                        chapterNumber = chapter.chapter.chapterNumber,
                    ),
                )
            },
            onEvent = onEvent,
        )
    }
}
