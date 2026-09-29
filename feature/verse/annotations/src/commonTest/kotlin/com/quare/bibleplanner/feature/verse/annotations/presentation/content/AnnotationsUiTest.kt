package com.quare.bibleplanner.feature.verse.annotations.presentation.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.empty_title
import bibleplanner.feature.verse.annotations.generated.resources.highlight_color_section
import bibleplanner.feature.verse.annotations.generated.resources.item_count
import bibleplanner.feature.verse.annotations.generated.resources.no_results_clear
import bibleplanner.feature.verse.annotations.generated.resources.no_results_query
import bibleplanner.feature.verse.annotations.generated.resources.other_version_count
import bibleplanner.feature.verse.annotations.generated.resources.other_version_use
import bibleplanner.feature.verse.annotations.generated.resources.other_versions_empty_title
import bibleplanner.feature.verse.annotations.generated.resources.search
import bibleplanner.feature.verse.annotations.generated.resources.search_hint_short
import bibleplanner.feature.verse.annotations.generated.resources.type_notes
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.verseannotations.domain.model.VersionAnnotationCount
import com.quare.bibleplanner.feature.verse.annotations.domain.model.AnnotationEntry
import com.quare.bibleplanner.feature.verse.annotations.fixture.sampleNow
import com.quare.bibleplanner.feature.verse.annotations.fixture.samplePassage
import com.quare.bibleplanner.feature.verse.annotations.fixture.toEntry
import com.quare.bibleplanner.feature.verse.annotations.fixture.utcLocalDateTimeProvider
import com.quare.bibleplanner.feature.verse.annotations.fixture.yellow
import com.quare.bibleplanner.feature.verse.annotations.presentation.factory.AnnotationsContentFactory
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationTypeFilter
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsFilters
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiState
import com.quare.bibleplanner.ui.testing.setUiTestContent
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
internal class AnnotationsUiTest {
    private val narrowWidth = 400.dp
    private val wideWidth = 1000.dp
    private lateinit var events: MutableList<AnnotationsUiEvent>
    private lateinit var systemBackInput: DirectNavigationEventInput
    private var hasLeftTheScreen = false

    private val a21Count = VersionAnnotationCount(
        bibleVersionId = "a21",
        count = 1,
    )

    private val factory = AnnotationsContentFactory(
        currentTimestampProvider = { sampleNow },
        localDateTimeProvider = utcLocalDateTimeProvider,
    )
    private val entries = listOf(
        samplePassage(
            bookId = BookId.JHN,
            chapterNumber = 3,
            verseNumbers = listOf(16),
            highlightColor = yellow,
        ).toEntry(text = "For God so loved the world"),
        samplePassage(
            bookId = BookId.GEN,
            chapterNumber = 3,
            verseNumbers = listOf(15),
            noteText = "First promise",
        ).toEntry(text = "I will put enmity"),
    )

    @Test
    fun `GIVEN annotations WHEN showing them THEN lists each verse with its note and the count`() = runComposeUiTest {
        // Given
        prepareScenario(state = loadedState())

        // When
        waitForIdle()

        // Then
        onNodeWithText("For God so loved the world").assertIsDisplayed()
        onNodeWithText("“First promise”").assertIsDisplayed()
        onNodeWithText(getPluralString(Res.plurals.item_count, 2, 2)).assertIsDisplayed()
    }

    @Test
    fun `GIVEN annotations WHEN tapping a verse THEN asks to open it`() = runComposeUiTest {
        // Given
        prepareScenario(state = loadedState())

        // When
        onNodeWithText("For God so loved the world").performClick()

        // Then
        val event = events.single() as AnnotationsUiEvent.OnItemClick
        assertEquals(
            expected = BookId.JHN,
            actual = event.item.passage.chapter.bookId,
        )
    }

    @Test
    fun `GIVEN annotations WHEN tapping a type THEN asks to filter by it`() = runComposeUiTest {
        // Given
        prepareScenario(state = loadedState())

        // When
        onNodeWithText(getString(Res.string.type_notes)).performClick()

        // Then
        assertEquals(
            expected = listOf<AnnotationsUiEvent>(AnnotationsUiEvent.OnTypeFilterClick(AnnotationTypeFilter.NOTES)),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a phone WHEN tapping the magnifying glass THEN asks to open the search`() = runComposeUiTest {
        // Given
        prepareScenario(state = loadedState())

        // When
        onNodeWithContentDescription(getString(Res.string.search)).performClick()

        // Then
        assertEquals(
            expected = listOf<AnnotationsUiEvent>(AnnotationsUiEvent.OnSearchClick),
            actual = events,
        )
    }

    @Test
    fun `GIVEN an open search WHEN typing THEN sends the query`() = runComposeUiTest {
        // Given
        prepareScenario(state = loadedState().copy(isSearchOpen = true))

        // When
        onNode(hasSetTextAction()).performTextInput("love")

        // Then
        assertEquals(
            expected = listOf<AnnotationsUiEvent>(AnnotationsUiEvent.OnSearchQueryChange("love")),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a search without matches WHEN clearing the filters THEN asks to reset them`() = runComposeUiTest {
        // Given
        prepareScenario(
            state = loadedState(
                filters = AnnotationsContentFactory.noFilters.copy(query = "zzz"),
            ).copy(
                isSearchOpen = true,
                searchQuery = "zzz",
            ),
        )
        onNodeWithText(getString(Res.string.no_results_query, "zzz")).assertIsDisplayed()

        // When
        onNodeWithText(getString(Res.string.no_results_clear)).performClick()

        // Then
        assertEquals(
            expected = listOf<AnnotationsUiEvent>(AnnotationsUiEvent.OnClearFiltersClick),
            actual = events,
        )
    }

    @Test
    fun `GIVEN an open search on a phone WHEN going back through the system THEN closes it and stays`() =
        runComposeUiTest {
            // Given
            prepareScenario(state = loadedState().copy(isSearchOpen = true))

            // When
            runOnIdle { systemBackInput.backCompleted() }

            // Then
            assertEquals(
                expected = listOf<AnnotationsUiEvent>(AnnotationsUiEvent.OnSearchCloseClick),
                actual = events,
            )
            assertFalse(hasLeftTheScreen)
        }

    @Test
    fun `GIVEN a closed search WHEN going back through the system THEN leaves the screen`() = runComposeUiTest {
        // Given
        prepareScenario(state = loadedState())

        // When
        runOnIdle { systemBackInput.backCompleted() }

        // Then
        assertTrue(events.isEmpty())
        assertTrue(hasLeftTheScreen)
    }

    @Test
    fun `GIVEN a wide window with the search open WHEN going back through the system THEN leaves the screen`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                state = loadedState().copy(isSearchOpen = true),
                width = wideWidth,
            )

            // When
            runOnIdle { systemBackInput.backCompleted() }

            // Then
            assertTrue(events.isEmpty())
            assertTrue(hasLeftTheScreen)
        }

    @Test
    fun `GIVEN nothing marked WHEN showing the screen THEN explains how to mark verses`() = runComposeUiTest {
        // Given
        prepareScenario(state = loadedState(entries = emptyList()))

        // When
        waitForIdle()

        // Then
        onNodeWithText(getString(Res.string.empty_title)).assertIsDisplayed()
    }

    @Test
    fun `GIVEN a wide window WHEN showing annotations THEN keeps the search field and the filter panel in view`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                state = loadedState(),
                width = wideWidth,
            )

            // When
            waitForIdle()

            // Then
            onNodeWithText(getString(Res.string.search_hint_short)).assertExists()
            onNodeWithText(getString(Res.string.highlight_color_section)).assertIsDisplayed()
            assertTrue(events.isEmpty())
        }

    @Test
    fun `GIVEN nothing marked here but marks in another version WHEN showing the screen THEN points to that version`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                state = loadedState(
                    entries = emptyList(),
                    otherVersionCounts = listOf(a21Count),
                ),
            )

            // When
            waitForIdle()

            // Then
            onNodeWithText(getString(Res.string.other_versions_empty_title)).assertIsDisplayed()
            onNodeWithText(getPluralString(Res.plurals.other_version_count, 1, 1, "A21")).assertIsDisplayed()
            onNodeWithText(getString(Res.string.empty_title)).assertDoesNotExist()
        }

    @Test
    fun `GIVEN nothing marked here but marks in another version WHEN tapping to use it THEN asks to select it`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                state = loadedState(
                    entries = emptyList(),
                    otherVersionCounts = listOf(a21Count),
                ),
            )

            // When
            onNodeWithText(getString(Res.string.other_version_use, "A21")).performClick()

            // Then
            assertEquals(
                expected = listOf<AnnotationsUiEvent>(
                    AnnotationsUiEvent.OnUseVersionClick(
                        bibleVersionId = "a21",
                        isEmptyState = true,
                    ),
                ),
                actual = events,
            )
        }

    @Test
    fun `GIVEN nothing marked anywhere WHEN showing the screen THEN offers no other version`() = runComposeUiTest {
        // Given
        prepareScenario(state = loadedState(entries = emptyList()))

        // When
        waitForIdle()

        // Then
        onNodeWithText(getString(Res.string.other_version_use, "A21")).assertDoesNotExist()
    }

    @Test
    fun `GIVEN marks here and in another version WHEN tapping to use it above the list THEN asks to select it`() =
        runComposeUiTest {
            // Given
            prepareScenario(state = loadedState(otherVersionCounts = listOf(a21Count)))
            onNodeWithText(getPluralString(Res.plurals.other_version_count, 1, 1, "A21")).assertIsDisplayed()
            onNodeWithText("For God so loved the world").assertIsDisplayed()

            // When
            onNodeWithText(getString(Res.string.other_version_use, "A21")).performClick()

            // Then
            assertEquals(
                expected = listOf<AnnotationsUiEvent>(
                    AnnotationsUiEvent.OnUseVersionClick(
                        bibleVersionId = "a21",
                        isEmptyState = false,
                    ),
                ),
                actual = events,
            )
        }

    private fun loadedState(
        entries: List<AnnotationEntry> = this.entries,
        otherVersionCounts: List<VersionAnnotationCount> = emptyList(),
        filters: AnnotationsFilters = AnnotationsContentFactory.noFilters,
    ): AnnotationsUiState = AnnotationsUiState(
        content = Loadable.Loaded(
            factory.create(
                entries = entries,
                otherVersionCounts = otherVersionCounts,
                filters = filters,
            ),
        ),
        openMenuItemKey = null,
        openFilterMenu = null,
        pendingRemoval = null,
        isCustomRangePickerOpen = false,
        isSearchOpen = false,
        searchQuery = filters.query,
    )

    private fun ComposeUiTest.prepareScenario(
        state: AnnotationsUiState,
        width: Dp = narrowWidth,
    ) {
        events = mutableListOf()
        hasLeftTheScreen = false
        systemBackInput = DirectNavigationEventInput()
        val dispatcherOwner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher = NavigationEventDispatcher(
                onBackCompletedFallback = { hasLeftTheScreen = true },
            ).apply { addInput(systemBackInput) }
        }
        setUiTestContent {
            CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides dispatcherOwner) {
                Box(
                    modifier = Modifier
                        .wrapContentWidth(
                            align = Alignment.Start,
                            unbounded = true,
                        ).requiredWidth(width)
                        .fillMaxHeight(),
                ) {
                    AnnotationsScreen(
                        platform = Platform.Android,
                        state = state,
                        onEvent = { event -> events += event },
                    )
                }
            }
        }
    }
}
