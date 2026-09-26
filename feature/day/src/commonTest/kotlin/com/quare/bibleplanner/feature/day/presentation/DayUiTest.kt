package com.quare.bibleplanner.feature.day.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.clear_notes
import bibleplanner.feature.day.generated.resources.completed_date
import bibleplanner.feature.day.generated.resources.day_ask_ai
import bibleplanner.feature.day.generated.resources.day_title_part
import bibleplanner.feature.day.generated.resources.mark_as_read
import bibleplanner.feature.day.generated.resources.mark_as_unread
import bibleplanner.feature.day.generated.resources.notes_placeholder
import bibleplanner.feature.day.generated.resources.week_title_part
import bibleplanner.ui.component.generated.resources.back
import bibleplanner.ui.component.generated.resources.edit
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.plan.domain.getGlobalDayIndex
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.day.domain.model.ChapterClickStrategy
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy
import com.quare.bibleplanner.feature.day.fixture.dayUiState
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.testing.setUiTestContent
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import bibleplanner.ui.component.generated.resources.Res as ComponentRes

@OptIn(ExperimentalTestApi::class)
internal class DayUiTest {
    private val loadedUiState = dayUiState(FIXTURE_LOCALE)
    private val readUiState = loadedUiState.copy(day = loadedUiState.day.copy(isRead = true))
    private val withoutNotesUiState = loadedUiState.copy(day = loadedUiState.day.copy(notes = null))
    private lateinit var events: MutableList<DayUiEvent>

    @Test
    fun `GIVEN a loaded day WHEN rendered THEN the header and every chapter of the day are shown`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = loadedUiState)

        // When
        waitForIdle()

        // Then
        val globalDayIndex = getGlobalDayIndex(
            weekNumber = loadedUiState.weekNumber,
            dayNumber = loadedUiState.day.number,
        )
        onNodeWithText(getString(Res.string.week_title_part, loadedUiState.weekNumber)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.day_title_part, globalDayIndex)).assertIsDisplayed()
        onAllNodesWithText(getString(BookId.GEN.toBookNameResource())).assertCountEquals(CHAPTER_COUNT)
        onNodeWithText(getString(Res.string.mark_as_read)).assertIsDisplayed()
    }

    @Test
    fun `GIVEN a loaded day WHEN clicking a chapter THEN emits OnChapterClick for that chapter`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = loadedUiState)

        // When
        onNodeWithText(SECOND_CHAPTER_NUMBER.toString()).performClick()

        // Then
        assertEquals(
            expected = listOf<DayUiEvent>(
                DayUiEvent.OnChapterClick(
                    ChapterClickStrategy.NavigateToChapter(
                        bookId = BookId.GEN,
                        isChapterRead = false,
                        chapterNumber = SECOND_CHAPTER_NUMBER,
                    ),
                ),
            ),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a loaded day WHEN toggling the checkbox of a chapter THEN emits OnChapterCheckboxClick for it`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = loadedUiState)

            // When
            onAllNodes(isToggleable())[SECOND_CHAPTER_INDEX].performClick()

            // Then
            assertEquals(
                expected = listOf<DayUiEvent>(
                    DayUiEvent.OnChapterCheckboxClick(
                        UpdateReadStatusOfPassageStrategy.Chapter(
                            passageIndex = 0,
                            chapterIndex = SECOND_CHAPTER_INDEX,
                        ),
                    ),
                ),
                actual = events,
            )
        }

    @Test
    fun `GIVEN a loaded day WHEN clicking mark as read THEN emits OnDayReadToggle`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = loadedUiState)

        // When
        onNodeWithText(getString(Res.string.mark_as_read)).performClick()

        // Then
        assertEquals(expected = listOf<DayUiEvent>(DayUiEvent.OnDayReadToggle), actual = events)
    }

    @Test
    fun `GIVEN a read day WHEN clicking edit on the completed date THEN emits OnEditDateClick`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = readUiState)
        onNode(hasScrollToNodeAction()).performScrollToNode(hasText(getString(Res.string.completed_date)))

        // When
        onNodeWithText(getString(ComponentRes.string.edit)).performClick()

        // Then
        onNodeWithText(getString(Res.string.mark_as_unread)).assertExists()
        assertEquals(expected = listOf<DayUiEvent>(DayUiEvent.OnEditDateClick), actual = events)
    }

    @Test
    fun `GIVEN a day without notes WHEN typing a note THEN emits OnNotesChanged with the typed text`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = withoutNotesUiState)
            onNode(hasScrollToNodeAction()).performScrollToNode(hasText(getString(Res.string.notes_placeholder)))

            // When
            onNodeWithText(getString(Res.string.notes_placeholder)).performTextInput(TYPED_NOTE)

            // Then
            assertEquals(
                expected = listOf(
                    DayUiEvent.OnNotesFocus,
                    DayUiEvent.OnNotesChanged(TYPED_NOTE),
                ),
                actual = events,
            )
        }

    @Test
    fun `GIVEN a day with notes WHEN clicking clear notes THEN emits OnNotesClear`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = loadedUiState)
        onNode(hasScrollToNodeAction()).performScrollToNode(
            hasContentDescription(getString(Res.string.clear_notes)),
        )

        // When
        onNodeWithContentDescription(getString(Res.string.clear_notes)).performClick()

        // Then
        assertEquals(expected = listOf<DayUiEvent>(DayUiEvent.OnNotesClear), actual = events)
    }

    @Test
    fun `GIVEN a loaded day WHEN clicking back THEN emits OnBackClick`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = loadedUiState)

        // When
        onNodeWithContentDescription(getString(ComponentRes.string.back)).performClick()

        // Then
        assertEquals(expected = listOf<DayUiEvent>(DayUiEvent.OnBackClick), actual = events)
    }

    @Test
    fun `GIVEN a loaded day WHEN clicking ask the AI THEN emits OnAskAiClick`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = loadedUiState)

        // When
        onNodeWithText(getString(Res.string.day_ask_ai)).performClick()

        // Then
        assertEquals(expected = listOf<DayUiEvent>(DayUiEvent.OnAskAiClick), actual = events)
    }

    @Test
    fun `GIVEN a day still loading WHEN rendered THEN neither the read toggle nor ask the AI are shown`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = DayUiState.Loading)

            // When
            waitForIdle()

            // Then
            onNodeWithContentDescription(getString(ComponentRes.string.back)).assertIsDisplayed()
            onNodeWithText(getString(Res.string.mark_as_read)).assertDoesNotExist()
            onNodeWithText(getString(Res.string.day_ask_ai)).assertDoesNotExist()
        }

    @OptIn(ExperimentalSharedTransitionApi::class)
    private fun ComposeUiTest.prepareScenario(uiState: DayUiState) {
        events = mutableListOf()
        setUiTestContent {
            SharedTransitionLayout {
                AnimatedContent(targetState = Unit) {
                    DayScreen(
                        platform = Platform.Android,
                        uiState = uiState,
                        snackbarHostState = remember { SnackbarHostState() },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@AnimatedContent,
                        isLandscape = false,
                        onEvent = { event -> events += event },
                        dayStudySection = { _, _, _ -> },
                    )
                }
            }
        }
    }

    private companion object {
        const val FIXTURE_LOCALE = "en-US"
        const val CHAPTER_COUNT = 3
        const val SECOND_CHAPTER_INDEX = 1
        const val SECOND_CHAPTER_NUMBER = 2
        const val TYPED_NOTE = "A new note"
    }
}
