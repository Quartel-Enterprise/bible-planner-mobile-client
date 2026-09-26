package com.quare.bibleplanner.feature.bookdetails.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.v2.runComposeUiTest
import bibleplanner.feature.book_details.generated.resources.Res
import bibleplanner.feature.book_details.generated.resources.chapters
import bibleplanner.feature.book_details.generated.resources.mark_all_as_read
import bibleplanner.feature.book_details.generated.resources.mark_all_as_unread
import bibleplanner.feature.book_details.generated.resources.reading_progress
import bibleplanner.ui.component.generated.resources.back
import bibleplanner.ui.component.generated.resources.favorite
import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.bookdetails.fixture.bookDetailsUiState
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiEvent
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiState
import com.quare.bibleplanner.feature.bookdetails.presentation.utils.toSynopsisResource
import com.quare.bibleplanner.ui.testing.setUiTestContent
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import bibleplanner.ui.component.generated.resources.Res as ComponentRes

@OptIn(ExperimentalTestApi::class)
internal class BookDetailsUiTest {
    private lateinit var events: MutableList<BookDetailsUiEvent>

    @Test
    fun `GIVEN a loaded book WHEN rendered THEN shows its name progress and synopsis`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = bookDetailsUiState(bookCategoryName = getString(BookGroup.WisdomBooks.titleRes)))

        // When
        waitForIdle()

        // Then
        onNodeWithText(getString(BookId.PSA.toBookNameResource())).assertIsDisplayed()
        onNodeWithText(getString(Res.string.reading_progress)).assertIsDisplayed()
        onNodeWithText(getString(BookGroup.WisdomBooks.titleRes)).assertIsDisplayed()
        onNodeWithText(
            text = getString(BookId.PSA.toSynopsisResource()),
            substring = true,
        ).assertIsDisplayed()
    }

    @Test
    fun `GIVEN a loaded book WHEN clicking a chapter THEN emits OnChapterClick for it`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = bookDetailsUiState(bookCategoryName = getString(BookGroup.WisdomBooks.titleRes)))
        val chapterMatcher = hasText(CLICKED_CHAPTER.toString()) and hasClickAction()

        // When
        onNode(hasScrollToNodeAction()).performScrollToNode(chapterMatcher)
        onNode(chapterMatcher).performClick()

        // Then
        assertEquals(
            expected = listOf<BookDetailsUiEvent>(BookDetailsUiEvent.OnChapterClick(CLICKED_CHAPTER)),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a partly read book WHEN clicking mark all as read THEN emits OnToggleAllChapters`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = bookDetailsUiState(bookCategoryName = getString(BookGroup.WisdomBooks.titleRes)))
        val markAllMatcher = hasText(getString(Res.string.mark_all_as_read))

        // When
        onNode(hasScrollToNodeAction()).performScrollToNode(markAllMatcher)
        onNode(markAllMatcher).performClick()

        // Then
        assertEquals(
            expected = listOf<BookDetailsUiEvent>(BookDetailsUiEvent.OnToggleAllChapters),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a fully read book WHEN rendered THEN offers to mark all as unread`() = runComposeUiTest {
        // Given
        prepareScenario(
            uiState = bookDetailsUiState(
                bookCategoryName = getString(BookGroup.WisdomBooks.titleRes),
            ).copy(areAllChaptersRead = true),
        )
        val markAllUnreadMatcher = hasText(getString(Res.string.mark_all_as_unread))

        // When
        onNode(hasScrollToNodeAction()).performScrollToNode(markAllUnreadMatcher)

        // Then
        onNode(markAllUnreadMatcher).assertIsDisplayed()
        onNodeWithText(getString(Res.string.mark_all_as_read)).assertDoesNotExist()
    }

    @Test
    fun `GIVEN a loaded book WHEN clicking favorite THEN emits OnToggleFavorite`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = bookDetailsUiState(bookCategoryName = getString(BookGroup.WisdomBooks.titleRes)))

        // When
        onNodeWithContentDescription(getString(ComponentRes.string.favorite)).performClick()

        // Then
        assertEquals(
            expected = listOf<BookDetailsUiEvent>(BookDetailsUiEvent.OnToggleFavorite),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a loaded book WHEN clicking back THEN emits OnBackClick`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = bookDetailsUiState(bookCategoryName = getString(BookGroup.WisdomBooks.titleRes)))

        // When
        onNodeWithContentDescription(getString(ComponentRes.string.back)).performClick()

        // Then
        assertEquals(
            expected = listOf<BookDetailsUiEvent>(BookDetailsUiEvent.OnBackClick),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a book still loading WHEN rendered THEN shows only the back button`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = BookDetailsUiState.Loading)

        // When
        waitForIdle()

        // Then
        onNodeWithContentDescription(getString(ComponentRes.string.back)).assertIsDisplayed()
        onNodeWithContentDescription(getString(ComponentRes.string.favorite)).assertDoesNotExist()
        onNodeWithText(getString(BookId.PSA.toBookNameResource())).assertDoesNotExist()
        onNodeWithText(getString(Res.string.chapters)).assertDoesNotExist()
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    private fun ComposeUiTest.prepareScenario(uiState: BookDetailsUiState) {
        events = mutableListOf()
        setUiTestContent {
            SharedTransitionLayout {
                AnimatedContent(targetState = Unit) {
                    BookDetailsScreen(
                        platform = Platform.Android,
                        state = uiState,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent,
                        onEvent = { event -> events += event },
                    )
                }
            }
        }
    }

    private companion object {
        const val CLICKED_CHAPTER = 3
    }
}
