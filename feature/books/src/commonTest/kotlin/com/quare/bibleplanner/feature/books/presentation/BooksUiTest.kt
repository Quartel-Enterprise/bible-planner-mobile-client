package com.quare.bibleplanner.feature.books.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.content_description_clear_search
import bibleplanner.feature.books.generated.resources.favorites
import bibleplanner.feature.books.generated.resources.grid
import bibleplanner.feature.books.generated.resources.sort_alphabetical_descending
import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.presentation.model.BookTestament
import com.quare.bibleplanner.feature.books.fixture.booksUiState
import com.quare.bibleplanner.feature.books.fixture.exodus
import com.quare.bibleplanner.feature.books.fixture.genesis
import com.quare.bibleplanner.feature.books.fixture.joshua
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterType
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BookSortOrder
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.ui.testing.setUiTestContent
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class BooksUiTest {
    private lateinit var events: MutableList<BooksUiEvent>

    private val topBarActionButton: SemanticsMatcher =
        hasClickAction() and hasAnySibling(hasAnyDescendant(hasSetTextAction()))

    @Test
    fun `GIVEN loaded books WHEN rendered THEN shows the testament toggle the group headers and the books`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = booksUiState())

            // When
            waitForIdle()

            // Then
            onNodeWithText(getString(BookTestament.OldTestament.titleRes)).assertIsDisplayed()
            onNodeWithText(getString(BookGroup.Pentateuch.titleRes)).assertIsDisplayed()
            onNodeWithText(getString(BookGroup.HistoricalBooks.titleRes)).assertIsDisplayed()
            onNodeWithText(genesis.name).assertIsDisplayed()
            onNodeWithText(joshua.name).assertIsDisplayed()
        }

    @Test
    fun `GIVEN loaded books WHEN clicking a book THEN emits OnBookClick for it`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = booksUiState())

        // When
        onNodeWithText(exodus.name).performClick()

        // Then
        assertEquals(
            expected = listOf<BooksUiEvent>(BooksUiEvent.OnBookClick(exodus)),
            actual = events,
        )
    }

    @Test
    fun `GIVEN loaded books WHEN clicking the favorite button of a book THEN emits OnToggleFavorite for it`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = booksUiState())

            // When
            onNode(hasClickAction() and hasParent(hasText(exodus.name))).performClick()

            // Then
            assertEquals(
                expected = listOf<BooksUiEvent>(BooksUiEvent.OnToggleFavorite(exodus.id)),
                actual = events,
            )
        }

    @Test
    fun `GIVEN loaded books WHEN selecting the new testament THEN emits OnTestamentSelect`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = booksUiState())

        // When
        onNodeWithText(getString(BookTestament.NewTestament.titleRes)).performClick()

        // Then
        assertEquals(
            expected = listOf<BooksUiEvent>(BooksUiEvent.OnTestamentSelect(BookTestament.NewTestament)),
            actual = events,
        )
    }

    @Test
    fun `GIVEN books in a list WHEN selecting the grid layout THEN emits OnLayoutFormatSelect`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = booksUiState())

        // When
        onNodeWithContentDescription(getString(Res.string.grid)).performClick()

        // Then
        assertEquals(
            expected = listOf<BooksUiEvent>(BooksUiEvent.OnLayoutFormatSelect(BookLayoutFormat.Grid)),
            actual = events,
        )
    }

    @Test
    fun `GIVEN loaded books WHEN typing in the search field THEN emits OnSearchQueryChange`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = booksUiState())

        // When
        onNode(hasSetTextAction()).performTextInput(SEARCH_QUERY)

        // Then
        assertEquals(
            expected = listOf<BooksUiEvent>(BooksUiEvent.OnSearchQueryChange(SEARCH_QUERY)),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a search WHEN rendered THEN shows only the matching books without testament toggle or headers`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = searchUiState())

            // When
            waitForIdle()

            // Then
            onNodeWithText(genesis.name).assertIsDisplayed()
            onNodeWithText(exodus.name).assertDoesNotExist()
            onNodeWithText(getString(BookTestament.OldTestament.titleRes)).assertDoesNotExist()
            onNodeWithText(getString(BookGroup.Pentateuch.titleRes)).assertDoesNotExist()
        }

    @Test
    fun `GIVEN a search WHEN clicking clear search THEN emits OnSearchQueryChange with an empty query`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = searchUiState())

            // When
            onNodeWithContentDescription(getString(Res.string.content_description_clear_search)).performClick()

            // Then
            assertEquals(
                expected = listOf<BooksUiEvent>(BooksUiEvent.OnSearchQueryChange("")),
                actual = events,
            )
        }

    @Test
    fun `GIVEN loaded books WHEN clicking the sort button THEN emits OnToggleSortMenu`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = booksUiState())

        // When
        onAllNodes(topBarActionButton)[SORT_BUTTON_INDEX].performClick()

        // Then
        assertEquals(expected = listOf<BooksUiEvent>(BooksUiEvent.OnToggleSortMenu), actual = events)
    }

    @Test
    fun `GIVEN the sort menu open WHEN selecting the descending order THEN emits OnSortOrderSelect`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = booksUiState().copy(isSortMenuVisible = true))

            // When
            onNodeWithText(getString(Res.string.sort_alphabetical_descending)).performClick()

            // Then
            assertEquals(
                expected = listOf<BooksUiEvent>(
                    BooksUiEvent.OnSortOrderSelect(BookSortOrder.AlphabeticalDescending),
                ),
                actual = events,
            )
        }

    @Test
    fun `GIVEN loaded books WHEN clicking the filter button THEN emits OnToggleFilterMenu`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = booksUiState())

        // When
        onAllNodes(topBarActionButton)[FILTER_BUTTON_INDEX].performClick()

        // Then
        assertEquals(expected = listOf<BooksUiEvent>(BooksUiEvent.OnToggleFilterMenu), actual = events)
    }

    @Test
    fun `GIVEN the filter menu open WHEN selecting favorites THEN emits OnToggleFilter for favorites`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = booksUiState().copy(isFilterMenuVisible = true))

            // When
            onNodeWithText(getString(Res.string.favorites)).performClick()

            // Then
            assertEquals(
                expected = listOf<BooksUiEvent>(BooksUiEvent.OnToggleFilter(BookFilterType.Favorites)),
                actual = events,
            )
        }

    @Test
    fun `GIVEN books still loading WHEN rendered THEN shows a progress indicator instead of the books`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = BooksUiState.Loading)

            // When
            waitForIdle()

            // Then
            onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
            onNodeWithText(genesis.name).assertDoesNotExist()
            onNodeWithText(getString(BookTestament.OldTestament.titleRes)).assertDoesNotExist()
        }

    private fun searchUiState(): BooksUiState.Success = booksUiState().copy(
        filteredBooks = listOf(genesis),
        searchQuery = SEARCH_QUERY,
        shouldShowTestamentToggle = false,
    )

    @OptIn(ExperimentalSharedTransitionApi::class)
    private fun ComposeUiTest.prepareScenario(uiState: BooksUiState) {
        events = mutableListOf()
        setUiTestContent {
            SharedTransitionLayout {
                AnimatedContent(targetState = Unit) {
                    BooksScreen(
                        state = uiState,
                        isScrolled = false,
                        searchGridState = rememberLazyGridState(),
                        searchListState = rememberLazyGridState(),
                        oldTestamentGridState = rememberLazyGridState(),
                        oldTestamentListState = rememberLazyGridState(),
                        newTestamentGridState = rememberLazyGridState(),
                        newTestamentListState = rememberLazyGridState(),
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent,
                        onEvent = { event -> events += event },
                    )
                }
            }
        }
    }

    private companion object {
        const val SEARCH_QUERY = "Gen"
        const val SORT_BUTTON_INDEX = 0
        const val FILTER_BUTTON_INDEX = 1
    }
}
