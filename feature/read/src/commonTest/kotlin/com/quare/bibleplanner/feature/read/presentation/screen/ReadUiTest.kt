package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.change_bible_version
import bibleplanner.feature.read.generated.resources.chapter_not_downloaded_error
import bibleplanner.feature.read.generated.resources.download_version
import bibleplanner.feature.read.generated.resources.manage_bible_versions
import bibleplanner.feature.read.generated.resources.mark_as_read
import bibleplanner.feature.read.generated.resources.next_chapter
import bibleplanner.feature.read.generated.resources.previous_chapter
import bibleplanner.feature.read.generated.resources.reader_appearance
import bibleplanner.feature.read.generated.resources.retry
import bibleplanner.feature.read.generated.resources.unknown_error_occurred
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.fixture.readUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
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

    private val userEvents: List<ReadUiEvent>
        get() = events.filterNot { event ->
            event == ReadUiEvent.OnReachedStart || event == ReadUiEvent.OnReachedEnd
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
            onNodeWithText("${getString(genesisName)} $CHAPTER").assertIsDisplayed()
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

    private fun ComposeUiTest.prepareScenario(
        uiState: ReadUiState,
        isWideLayout: Boolean = false,
    ) {
        events = mutableListOf()
        setUiTestContent {
            CompositionLocalProvider(LocalIsWideLayout provides isWideLayout) {
                ReadScreen(
                    platform = Platform.Android,
                    state = uiState,
                    onEvent = { event -> events += event },
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
