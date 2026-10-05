package com.quare.bibleplanner.feature.read.presentation.screen.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.presentation.component.VerseFlash
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiState
import com.quare.bibleplanner.feature.read.presentation.model.ChapterStudyEntrySource
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.screen.component.ChapterEndNavigationRow
import com.quare.bibleplanner.feature.read.presentation.screen.component.ChapterHeader
import com.quare.bibleplanner.feature.read.presentation.screen.component.ChapterListenShortcutPill
import com.quare.bibleplanner.feature.read.presentation.screen.component.ChapterStudyEntryCard
import com.quare.bibleplanner.feature.read.presentation.screen.component.VerseRow
import org.jetbrains.compose.resources.stringResource

private const val CHAPTER_HEADER_AND_END_ITEM_COUNT = 2

/*
 * Why: vertical reading emits every chapter into one list, so the next chapter is already below
 * and the chapter arrows are dropped.
 */
internal fun LazyListScope.chapterContent(
    chapter: ReadChapterUiModel,
    header: ReadHeaderUiModel,
    settings: ReaderSettingsModel,
    isChapterStudyBeside: Boolean,
    isListenShortcutShown: Boolean,
    focusedVerseNumber: Int?,
    verseFlash: VerseFlash,
    listening: ReadListeningUiState,
    onEvent: (ReadUiEvent) -> Unit,
    onListeningEvent: (ReadListeningUiEvent) -> Unit,
) {
    val listeningVerseNumber = listening.player
        ?.takeIf { player ->
            player.chapter.bookId == chapter.chapter.bookId &&
                player.chapter.chapterNumber == chapter.chapter.chapterNumber &&
                player.status != ListeningStatusModel.FINISHED
        }?.verseNumber
    item(key = "chapter-header-${chapter.chapter.bookId}-${chapter.chapter.chapterNumber}") {
        Column {
            ChapterHeader(
                bookName = stringResource(chapter.bookStringResource),
                chapterNumber = chapter.chapter.chapterNumber,
            )
            if (listening.isAvailable && isListenShortcutShown) {
                ChapterListenShortcutPill(
                    chapter = chapter,
                    listening = listening,
                    onListeningEvent = onListeningEvent,
                )
            }
        }
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
            isListening = verse.number == listeningVerseNumber,
            onClick = {
                onEvent(
                    ReadUiEvent.OnVerseClick(
                        chapter = chapter.chapter,
                        verseNumber = verse.number,
                    ),
                )
            },
            onNoteIconClick = { noteMark ->
                onEvent(
                    ReadUiEvent.OnNoteIconClick(
                        chapter = chapter.chapter,
                        noteMark = noteMark,
                    ),
                )
            },
        )
    }
    if (shouldListChapterStudyCard(isChapterStudyBeside)) {
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

internal fun getChapterStartIndices(
    chapters: List<ReadChapterUiModel>,
    leadingItemCount: Int,
    isChapterStudyBeside: Boolean,
): List<Int> {
    /*
     * Why: must match the items chapterContent emits, or the visible chapter and the verse scroll
     * target drift by one item per chapter.
     */
    val studyCardItemCount = if (shouldListChapterStudyCard(isChapterStudyBeside)) 1 else 0
    return chapters.runningFold(leadingItemCount) { start, chapter ->
        start + CHAPTER_HEADER_AND_END_ITEM_COUNT + chapter.verses.size + studyCardItemCount
    }
}

private fun shouldListChapterStudyCard(isChapterStudyBeside: Boolean): Boolean = !isChapterStudyBeside
