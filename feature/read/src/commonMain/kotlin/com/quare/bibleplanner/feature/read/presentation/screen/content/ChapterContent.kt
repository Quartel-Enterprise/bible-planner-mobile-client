package com.quare.bibleplanner.feature.read.presentation.screen.content

import androidx.compose.foundation.lazy.LazyListScope
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.screen.component.ChapterEndNavigation
import com.quare.bibleplanner.feature.read.presentation.screen.component.ChapterHeader
import com.quare.bibleplanner.feature.read.presentation.screen.component.VerseRow
import org.jetbrains.compose.resources.stringResource

/**
 * One chapter as list items: its header, its verses, and the end-of-chapter controls. Vertical
 * reading calls this once per chapter into the same list, which is what makes the text continue.
 */
internal fun LazyListScope.chapterContent(
    chapter: ReadChapterUiModel,
    header: ReadHeaderUiModel,
    settings: ReaderSettingsModel,
    isPrimaryChapter: Boolean,
    focusedVerseNumber: Int?,
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
        VerseRow(
            verse = verse,
            settings = settings,
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
    item(key = "chapter-end-${chapter.chapter.bookId}-${chapter.chapter.chapterNumber}") {
        ChapterEndNavigation(
            previous = header.navigationSuggestions.previous.takeIf { isPrimaryChapter },
            next = header.navigationSuggestions.next.takeIf { isPrimaryChapter },
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
