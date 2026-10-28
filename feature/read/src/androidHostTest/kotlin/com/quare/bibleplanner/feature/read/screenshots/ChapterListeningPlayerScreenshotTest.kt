package com.quare.bibleplanner.feature.read.screenshots

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordTargetModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.feature.read.fixture.chapterListeningPlayerUiState
import com.quare.bibleplanner.feature.read.fixture.listeningPlayer
import com.quare.bibleplanner.feature.read.presentation.screen.content.ChapterListeningPlayerContent
import com.quare.bibleplanner.ui.screenshots.testing.ScreenshotTest
import com.quare.bibleplanner.ui.screenshots.testing.ScreenshotVariant
import org.junit.Test

internal class ChapterListeningPlayerScreenshotTest : ScreenshotTest() {
    private val longVerse = "God said, \"Let the earth yield grass, herbs yielding seeds, and fruit trees bearing " +
        "fruit after their kind, with their seeds in it, on the earth;\" and it was so."

    @Test
    fun wordHighlight() {
        val word = longVerse.lastIndexOf("earth")
        snapshot(
            name = "word_highlight",
            variants = ScreenshotVariant.all,
        ) {
            ChapterListeningPlayerContent(
                uiState = chapterListeningPlayerUiState(
                    player = listeningPlayer(
                        verseText = longVerse,
                        spokenWord = ListeningWordTargetModel.Verse(word until word + "earth".length),
                    ),
                    isWordHighlightEnabled = true,
                ),
                onEvent = {},
            )
        }
    }

    @Test
    fun nextChapterLocked() {
        snapshot(name = "next_chapter_locked") {
            ChapterListeningPlayerContent(
                uiState = chapterListeningPlayerUiState(
                    player = listeningPlayer(
                        status = ListeningStatusModel.NEXT_LOCKED,
                        lockedChapter = ChapterLocationModel(
                            bookId = BookId.GEN,
                            chapterNumber = 2,
                        ),
                    ),
                    isWordHighlightEnabled = true,
                ),
                onEvent = {},
            )
        }
    }

    @Test
    fun needsConnection() {
        snapshot(name = "needs_connection") {
            ChapterListeningPlayerContent(
                uiState = chapterListeningPlayerUiState(
                    player = listeningPlayer(status = ListeningStatusModel.NEEDS_CONNECTION),
                    isWordHighlightEnabled = true,
                ),
                onEvent = {},
            )
        }
    }

    @Test
    fun wordHighlightOff() {
        snapshot(name = "word_highlight_off") {
            ChapterListeningPlayerContent(
                uiState = chapterListeningPlayerUiState(
                    player = listeningPlayer(verseText = longVerse),
                    isWordHighlightEnabled = false,
                ),
                onEvent = {},
            )
        }
    }
}
