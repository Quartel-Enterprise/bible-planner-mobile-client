package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.v2.runComposeUiTest
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.change_bible_version
import bibleplanner.feature.read.generated.resources.chapter_not_downloaded_error
import bibleplanner.feature.read.generated.resources.chapter_study_card_subtitle_with_chapter
import bibleplanner.feature.read.generated.resources.chapter_study_card_title
import bibleplanner.feature.read.generated.resources.chapter_study_pill
import bibleplanner.feature.read.generated.resources.download_version
import bibleplanner.feature.read.generated.resources.listening_listen
import bibleplanner.feature.read.generated.resources.listening_pause
import bibleplanner.feature.read.generated.resources.listening_unlock
import bibleplanner.feature.read.generated.resources.manage_bible_versions
import bibleplanner.feature.read.generated.resources.mark_as_read
import bibleplanner.feature.read.generated.resources.next_chapter
import bibleplanner.feature.read.generated.resources.open_verse_note
import bibleplanner.feature.read.generated.resources.previous_chapter
import bibleplanner.feature.read.generated.resources.reader_appearance
import bibleplanner.feature.read.generated.resources.retry
import bibleplanner.feature.read.generated.resources.unknown_error_occurred
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.fixture.NoDayCompletionBanner
import com.quare.bibleplanner.feature.read.fixture.availableListeningUiState
import com.quare.bibleplanner.feature.read.fixture.hiddenListeningUiState
import com.quare.bibleplanner.feature.read.fixture.listeningPlayer
import com.quare.bibleplanner.feature.read.fixture.readUiState
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningEntrySource
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningFinishOfferUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiState
import com.quare.bibleplanner.feature.read.presentation.model.ChapterStudyEntrySource
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.feature.read.presentation.model.VerseNoteMarkPosition
import com.quare.bibleplanner.feature.read.presentation.model.VerseNoteMarkUiModel
import com.quare.bibleplanner.feature.read.presentation.screen.component.LISTENING_FINISH_OFFER_TAG
import com.quare.bibleplanner.feature.read.presentation.screen.component.LISTEN_SHORTCUT_TAG
import com.quare.bibleplanner.ui.testing.setUiTestContent
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import bibleplanner.ui.component.generated.resources.Res as ComponentRes
import bibleplanner.ui.component.generated.resources.back as backString

@OptIn(ExperimentalTestApi::class)
internal class ReadUiTest {
    private lateinit var events: MutableList<ReadUiEvent>
    private lateinit var listeningEvents: MutableList<ReadListeningUiEvent>

    private val genesisName = BookId.GEN.toBookNameResource()
    private val loadedUiState = readUiState(LOCALE)
    private val loadedChapter = (loadedUiState.content as ReadContentUiState.Success).chapters.first()
    private val firstVerse = loadedChapter.verses.first()
    private val nextSuggestion = ReadNavigationSuggestionModel(
        bookId = BookId.GEN,
        chapterNumber = CHAPTER + 1,
    )
    private val previousSuggestion = ReadNavigationSuggestionModel(
        bookId = BookId.EXO,
        chapterNumber = PREVIOUS_CHAPTER,
    )
    private val withPreviousUiState = loadedUiState.copy(
        header = loadedUiState.header.copy(
            navigationSuggestions = ReadNavigationSuggestionsModel(
                previous = previousSuggestion,
                next = nextSuggestion,
            ),
        ),
    )
    private val chapterNotFoundUiState = loadedUiState.copy(
        content = ReadContentUiState.Error.ChapterNotFound(
            errorUiEvent = ReadUiEvent.OnRetryClick,
            selectedBibleVersionName = VERSION_NAME,
            downloadStatus = DownloadStatusModel.NotStarted,
            versionSizeInBytes = null,
        ),
    )

    private val firstNoteMark = VerseNoteMarkUiModel(
        noteId = "note-1",
        noteVerseNumbers = listOf(1, 2),
        position = VerseNoteMarkPosition.FIRST,
    )
    private val notedUiState = loadedUiState.copy(
        content = ReadContentUiState.Success(
            chapters = listOf(
                loadedChapter.copy(
                    verses = loadedChapter.verses.mapIndexed { index, verse ->
                        when (index) {
                            0 -> verse.copy(noteMark = firstNoteMark)
                            1 -> verse.copy(noteMark = firstNoteMark.copy(position = VerseNoteMarkPosition.LAST))
                            else -> verse
                        }
                    },
                ),
            ),
        ),
    )

    private val userEvents: List<ReadUiEvent>
        get() = events.filterNot { event ->
            event == ReadUiEvent.OnReachedStart ||
                event == ReadUiEvent.OnReachedEnd ||
                event is ReadUiEvent.OnVisibleChapterChanged
        }

    @Test
    fun `GIVEN a loaded chapter WHEN rendered THEN shows the chapter header and its verses`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = loadedUiState)

        // When
        waitForIdle()

        // Then
        onNodeWithText(getString(genesisName).uppercase()).assertIsDisplayed()
        onNodeWithText(firstVerse.text).assertIsDisplayed()
    }

    @Test
    fun `GIVEN a loaded chapter WHEN rendered THEN the chapters carry the tag the benchmarks find`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = loadedUiState)

            // When
            waitForIdle()

            // Then
            onNodeWithTag("read_chapters").assertIsDisplayed()
        }

    @Test
    fun `GIVEN a loaded chapter WHEN clicking a verse THEN emits OnVerseClick for it`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = loadedUiState)

        // When
        onNodeWithText(firstVerse.text).performClick()

        // Then
        assertEquals(
            expected = listOf<ReadUiEvent>(
                ReadUiEvent.OnVerseClick(
                    chapter = loadedChapter.chapter,
                    verseNumber = firstVerse.number,
                ),
            ),
            actual = userEvents,
        )
    }

    @Test
    fun `GIVEN a note over two verses WHEN clicking its icon THEN opens the note without selecting the verse`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = notedUiState)

            // When
            onNodeWithContentDescription(getString(Res.string.open_verse_note)).performClick()

            // Then
            assertEquals(
                expected = listOf<ReadUiEvent>(
                    ReadUiEvent.OnNoteIconClick(
                        chapter = loadedChapter.chapter,
                        noteMark = firstNoteMark,
                    ),
                ),
                actual = userEvents,
            )
        }

    @Test
    fun `GIVEN the note icon turned off WHEN rendered THEN annotated verses show no icon`() = runComposeUiTest {
        // Given
        prepareScenario(
            uiState = notedUiState.copy(
                settings = notedUiState.settings.copy(isNoteIconEnabled = false),
            ),
        )

        // When
        waitForIdle()

        // Then
        onNodeWithContentDescription(getString(Res.string.open_verse_note)).assertDoesNotExist()
        onNodeWithText(firstVerse.text).assertIsDisplayed()
    }

    @Test
    fun `GIVEN a loaded chapter WHEN clicking mark as read THEN emits ToggleReadStatus for the chapter`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = loadedUiState)

            // When
            onAllNodesWithText(getString(Res.string.mark_as_read)).onFirst().performClick()

            // Then
            assertEquals(
                expected = listOf<ReadUiEvent>(
                    ReadUiEvent.ToggleReadStatus(
                        bookId = BookId.GEN,
                        chapterNumber = CHAPTER,
                    ),
                ),
                actual = userEvents,
            )
        }

    @Test
    fun `GIVEN a loaded chapter WHEN clicking next THEN emits OnNavigationSuggestionClick for the next chapter`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = loadedUiState)

            // When
            onAllNodesWithContentDescription(getString(Res.string.next_chapter)).onFirst().performClick()

            // Then
            assertEquals(
                expected = listOf<ReadUiEvent>(ReadUiEvent.OnNavigationSuggestionClick(nextSuggestion)),
                actual = userEvents,
            )
        }

    @Test
    fun `GIVEN a chapter with a previous one WHEN clicking previous THEN emits OnNavigationSuggestionClick for it`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = withPreviousUiState)

            // When
            onAllNodesWithContentDescription(getString(Res.string.previous_chapter)).onFirst().performClick()

            // Then
            assertEquals(
                expected = listOf<ReadUiEvent>(ReadUiEvent.OnNavigationSuggestionClick(previousSuggestion)),
                actual = userEvents,
            )
        }

    @Test
    fun `GIVEN a loaded chapter WHEN clicking the top bar actions THEN emits back appearance and version events`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = loadedUiState)

            // When
            onNodeWithContentDescription(getString(ComponentRes.string.backString)).performClick()
            onNodeWithContentDescription(getString(Res.string.reader_appearance)).performClick()
            onNodeWithContentDescription(getString(Res.string.change_bible_version)).performClick()

            // Then
            assertEquals(
                expected = listOf(
                    ReadUiEvent.OnArrowBackClick,
                    ReadUiEvent.OnAppearanceClick,
                    ReadUiEvent.ManageBibleVersions,
                ),
                actual = userEvents,
            )
        }

    @Test
    fun `GIVEN a wide layout WHEN clicking the compact read pill THEN emits ToggleReadStatus for the chapter`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                uiState = loadedUiState,
                isWideLayout = true,
            )

            // When
            onAllNodesWithText(getString(Res.string.mark_as_read)).onFirst().performClick()

            // Then
            assertEquals(
                expected = listOf<ReadUiEvent>(
                    ReadUiEvent.ToggleReadStatus(
                        bookId = BookId.GEN,
                        chapterNumber = CHAPTER,
                    ),
                ),
                actual = userEvents,
            )
        }

    @Test
    fun `GIVEN a chapter still loading WHEN rendered THEN no verse is shown yet`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = loadedUiState.copy(content = ReadContentUiState.Loading))

        // When
        waitForIdle()

        // Then
        onNodeWithText(firstVerse.text).assertDoesNotExist()
        onNodeWithText(getString(genesisName).uppercase()).assertDoesNotExist()
        onNodeWithText(getString(Res.string.mark_as_read)).assertIsDisplayed()
    }

    @Test
    fun `GIVEN an unknown error WHEN clicking retry THEN emits the error event`() = runComposeUiTest {
        // Given
        prepareScenario(
            uiState = loadedUiState.copy(
                content = ReadContentUiState.Error.Unknown(errorUiEvent = ReadUiEvent.OnRetryClick),
            ),
        )

        // When
        onNodeWithText(getString(Res.string.retry)).performClick()

        // Then
        onNodeWithText(getString(Res.string.unknown_error_occurred)).assertIsDisplayed()
        assertEquals(
            expected = listOf<ReadUiEvent>(ReadUiEvent.OnRetryClick),
            actual = userEvents,
        )
    }

    @Test
    fun `GIVEN a chapter not downloaded WHEN clicking download and manage versions THEN emits their events`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = chapterNotFoundUiState)

            // When
            onNodeWithText(getString(Res.string.download_version)).performClick()
            onNodeWithText(getString(Res.string.manage_bible_versions)).performClick()

            // Then
            onNodeWithText(
                getString(
                    Res.string.chapter_not_downloaded_error,
                    getString(genesisName),
                    CHAPTER,
                    VERSION_NAME,
                ),
            ).assertIsDisplayed()
            assertEquals(
                expected = listOf(
                    ReadUiEvent.OnDownloadSelectedVersionClick,
                    ReadUiEvent.ManageBibleVersions,
                ),
                actual = userEvents,
            )
        }

    @Test
    fun `GIVEN a loaded chapter WHEN clicking the study pill THEN emits OnChapterStudyClick from the top bar`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = loadedUiState)

            // When
            onNodeWithText(getString(Res.string.chapter_study_pill)).performClick()

            // Then
            assertEquals(
                expected = listOf<ReadUiEvent>(
                    ReadUiEvent.OnChapterStudyClick(
                        bookId = BookId.GEN,
                        chapterNumber = CHAPTER,
                        source = ChapterStudyEntrySource.TOP_BAR,
                    ),
                ),
                actual = userEvents,
            )
        }

    @Test
    fun `GIVEN the study beside the text WHEN rendered THEN offers no way to open it`() = runComposeUiTest {
        // Given
        prepareScenario(
            uiState = loadedUiState.copy(isChapterStudyBeside = true),
            isWideLayout = true,
        )

        // When
        waitForIdle()

        // Then
        onNodeWithText(getString(Res.string.chapter_study_pill)).assertDoesNotExist()
        onNodeWithText(getString(Res.string.chapter_study_card_title)).assertDoesNotExist()
    }

    @Test
    fun `GIVEN a loaded chapter WHEN rendered THEN reports it as the chapter in view`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = loadedUiState)

        // When
        waitForIdle()

        // Then
        assertEquals(
            expected = listOf<ReadUiEvent>(
                ReadUiEvent.OnVisibleChapterChanged(
                    bookId = BookId.GEN,
                    chapterNumber = CHAPTER,
                ),
            ),
            actual = events.filterIsInstance<ReadUiEvent.OnVisibleChapterChanged>(),
        )
    }

    @Test
    fun `GIVEN a wide layout WHEN clicking the study pill THEN emits OnChapterStudyClick from the top bar`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                uiState = loadedUiState,
                isWideLayout = true,
            )

            // When
            onNodeWithText(getString(Res.string.chapter_study_pill)).performClick()

            // Then
            assertEquals(
                expected = listOf<ReadUiEvent>(
                    ReadUiEvent.OnChapterStudyClick(
                        bookId = BookId.GEN,
                        chapterNumber = CHAPTER,
                        source = ChapterStudyEntrySource.TOP_BAR,
                    ),
                ),
                actual = userEvents,
            )
        }

    @Test
    fun `GIVEN a loaded chapter WHEN clicking the study card THEN emits OnChapterStudyClick from the chapter end`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = loadedUiState)
            val cardTitle = getString(Res.string.chapter_study_card_title)
            onNode(hasScrollToNodeAction()).performScrollToNode(hasText(cardTitle))

            // When
            onNodeWithText(cardTitle).performClick()

            // Then
            assertEquals(
                expected = listOf<ReadUiEvent>(
                    ReadUiEvent.OnChapterStudyClick(
                        bookId = BookId.GEN,
                        chapterNumber = CHAPTER,
                        source = ChapterStudyEntrySource.CHAPTER_END,
                    ),
                ),
                actual = userEvents,
            )
        }

    @Test
    fun `GIVEN vertical reading WHEN reaching the study card THEN it names the chapter it studies`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                uiState = loadedUiState.copy(
                    settings = loadedUiState.settings.copy(isVerticalReadingEnabled = true),
                ),
            )
            val subtitle = getString(
                Res.string.chapter_study_card_subtitle_with_chapter,
                "${getString(genesisName)} $CHAPTER",
            )

            // When
            onNode(hasScrollToNodeAction()).performScrollToNode(hasText(subtitle))

            // Then
            onNodeWithText(subtitle).assertIsDisplayed()
        }

    @Test
    fun `GIVEN listening is hidden WHEN rendered THEN shows no listening entry`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = loadedUiState)

        // When
        val shortcuts = onAllNodesWithTag(LISTEN_SHORTCUT_TAG)

        // Then
        shortcuts.assertCountEquals(0)
    }

    @Test
    fun `GIVEN listening is available WHEN clicking the chapter shortcut THEN asks to listen from the shortcut`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                uiState = loadedUiState,
                listening = availableListeningUiState(),
            )

            // When
            onNodeWithTag(LISTEN_SHORTCUT_TAG).performClick()

            // Then
            assertEquals(
                expected = listOf<ReadListeningUiEvent>(
                    ReadListeningUiEvent.OnListenClick(
                        chapter = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = CHAPTER),
                        source = ListeningEntrySource.SHORTCUT,
                    ),
                ),
                actual = listeningEvents,
            )
        }

    @Test
    fun `GIVEN listening is available WHEN clicking the bottom bar button THEN asks to listen from the bottom bar`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                uiState = loadedUiState,
                listening = availableListeningUiState(),
            )
            val listen = getString(Res.string.listening_listen)

            // When
            onNodeWithContentDescription(listen).performClick()

            // Then
            assertEquals(
                expected = listOf<ReadListeningUiEvent>(
                    ReadListeningUiEvent.OnListenClick(
                        chapter = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = CHAPTER),
                        source = ListeningEntrySource.BOTTOM_BAR,
                    ),
                ),
                actual = listeningEvents,
            )
        }

    @Test
    fun `GIVEN a chapter being read aloud WHEN clicking pause in the mini player THEN toggles the playback`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                uiState = loadedUiState,
                listening = availableListeningUiState(player = listeningPlayer()),
            )
            val pause = getString(Res.string.listening_pause)

            // When
            onNodeWithContentDescription(pause).performClick()

            // Then
            assertEquals(listOf<ReadListeningUiEvent>(ReadListeningUiEvent.OnPlayPauseClick), listeningEvents)
        }

    @Test
    fun `GIVEN a chapter being read aloud WHEN clicking the mini player title THEN opens the player`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                uiState = loadedUiState,
                listening = availableListeningUiState(player = listeningPlayer()),
            )
            val reference = "${getString(genesisName)} $CHAPTER:2"

            // When
            onNodeWithText(reference).performClick()

            // Then
            assertEquals(listOf<ReadListeningUiEvent>(ReadListeningUiEvent.OnMiniPlayerClick), listeningEvents)
        }

    @Test
    fun `GIVEN the next chapter is locked WHEN clicking unlock THEN asks to unlock it`() = runComposeUiTest {
        // Given
        prepareScenario(
            uiState = loadedUiState,
            listening = availableListeningUiState(
                player = listeningPlayer(
                    status = ListeningStatusModel.NEXT_LOCKED,
                    lockedChapter = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = CHAPTER + 1),
                ),
            ),
        )
        val unlock = getString(Res.string.listening_unlock)

        // When
        onNodeWithText(unlock).performClick()

        // Then
        assertEquals(listOf<ReadListeningUiEvent>(ReadListeningUiEvent.OnUnlockNextClick), listeningEvents)
    }

    @Test
    fun `GIVEN a finished chapter offer WHEN marking it as read THEN marks the finished chapter`() = runComposeUiTest {
        // Given
        val finishedChapter = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = CHAPTER)
        prepareScenario(
            uiState = loadedUiState,
            listening = availableListeningUiState(
                player = listeningPlayer(status = ListeningStatusModel.FINISHED),
                finishOffer = ListeningFinishOfferUiModel(
                    chapter = finishedChapter,
                    playingChapter = null,
                ),
            ),
        )

        val markAsRead =
            hasText(getString(Res.string.mark_as_read)) and hasAnyAncestor(hasTestTag(LISTENING_FINISH_OFFER_TAG))

        // When
        onNode(markAsRead).performClick()

        // Then
        assertEquals(
            expected = listOf<ReadUiEvent>(
                ReadUiEvent.OnListeningMarkReadClick(
                    bookId = BookId.GEN,
                    chapterNumber = CHAPTER,
                ),
            ),
            actual = userEvents,
        )
    }

    private fun ComposeUiTest.prepareScenario(
        uiState: ReadUiState,
        isWideLayout: Boolean = false,
        listening: ReadListeningUiState = hiddenListeningUiState(),
    ) {
        events = mutableListOf()
        listeningEvents = mutableListOf()
        setUiTestContent {
            CompositionLocalProvider(LocalIsWideLayout provides isWideLayout) {
                ReadScreen(
                    platform = Platform.Android,
                    state = uiState,
                    listening = listening,
                    onEvent = { event -> events += event },
                    onListeningEvent = { event -> listeningEvents += event },
                    dayCompletionBanner = NoDayCompletionBanner,
                )
            }
        }
    }

    private companion object {
        const val CHAPTER = 1
        const val PREVIOUS_CHAPTER = 40
        const val LOCALE = "en-US"
        const val VERSION_NAME = "WEB"
    }
}
