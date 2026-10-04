package com.quare.bibleplanner.feature.books.screenshots

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.books.fixture.booksUiState
import com.quare.bibleplanner.feature.books.fixture.exodus
import com.quare.bibleplanner.feature.books.presentation.BooksScreen
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.ui.screenshots.testing.ScreenshotTest
import com.quare.bibleplanner.ui.screenshots.testing.ScreenshotVariant
import org.junit.Test

internal class BooksScreenshotTest : ScreenshotTest() {
    @Test
    fun loading() {
        snapshot(name = "loading") {
            BooksContent(uiState = BooksUiState.Loading)
        }
    }

    @Test
    fun list() {
        snapshot(
            name = "list",
            variants = ScreenshotVariant.all,
        ) {
            BooksContent(uiState = booksUiState())
        }
    }

    @Test
    fun grid() {
        snapshot(
            name = "grid",
            variants = ScreenshotVariant.all,
        ) {
            BooksContent(uiState = booksUiState().copy(layoutFormat = BookLayoutFormat.Grid))
        }
    }

    @Test
    fun filterMenu() {
        snapshot(name = "filter_menu") {
            BooksContent(uiState = booksUiState().copy(isFilterMenuVisible = true))
        }
    }

    @Test
    fun searching() {
        snapshot(name = "searching") {
            BooksContent(
                uiState = booksUiState().copy(
                    searchQuery = SEARCH_QUERY,
                    filteredBooks = listOf(exodus),
                ),
            )
        }
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    private fun BooksContent(uiState: BooksUiState) {
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
                    onEvent = {},
                )
            }
        }
    }

    private companion object {
        const val SEARCH_QUERY = "Ex"
    }
}
