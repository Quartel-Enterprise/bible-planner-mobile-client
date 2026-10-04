package com.quare.bibleplanner.feature.books.fixture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.presentation.model.BookTestament
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.feature.books.presentation.BooksScreen
import com.quare.bibleplanner.feature.books.presentation.model.BookGroupPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

internal const val BOOKS_SCREENSHOT = "05_books"

private val pentateuch = listOf(
    SampleBook(
        id = BookId.GEN,
        totalChapters = 50,
        chaptersRead = 50,
        isFavorite = true,
    ),
    SampleBook(
        id = BookId.EXO,
        totalChapters = 40,
        chaptersRead = 40,
        isFavorite = false,
    ),
    SampleBook(
        id = BookId.LEV,
        totalChapters = 27,
        chaptersRead = 18,
        isFavorite = false,
    ),
    SampleBook(
        id = BookId.NUM,
        totalChapters = 36,
        chaptersRead = 9,
        isFavorite = false,
    ),
    SampleBook(
        id = BookId.DEU,
        totalChapters = 34,
        chaptersRead = 0,
        isFavorite = false,
    ),
)
private val historicalBooks = listOf(
    SampleBook(
        id = BookId.JOS,
        totalChapters = 24,
        chaptersRead = 12,
        isFavorite = true,
    ),
    SampleBook(
        id = BookId.JDG,
        totalChapters = 21,
        chaptersRead = 0,
        isFavorite = false,
    ),
    SampleBook(
        id = BookId.RUT,
        totalChapters = 4,
        chaptersRead = 4,
        isFavorite = false,
    ),
)

/**
 * The books screen as the store screenshots show it, on every platform that renders them: the
 * Robolectric generators for Play and the iOS simulator captures for the App Store.
 *
 * [statusBarHeight] is the room the frame's status bar takes at the top. The Play frames reserve
 * it themselves, so they pass zero; an iOS capture fills the whole screen and passes its slot's.
 */
@Composable
internal fun BooksScreenshotContent(statusBarHeight: Dp) {
    CompositionLocalProvider(LocalTheme provides Theme.DARK) {
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                SharedTransitionLayout(modifier = Modifier.padding(top = statusBarHeight)) {
                    AnimatedVisibility(visible = true) {
                        BooksScreen(
                            state = rememberBooksUiState(),
                            isScrolled = false,
                            searchGridState = rememberLazyGridState(),
                            searchListState = rememberLazyGridState(),
                            oldTestamentGridState = rememberLazyGridState(),
                            oldTestamentListState = rememberLazyGridState(),
                            newTestamentGridState = rememberLazyGridState(),
                            newTestamentListState = rememberLazyGridState(),
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@AnimatedVisibility,
                            onEvent = {},
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberBooksUiState(): BooksUiState.Success {
    val pentateuchBooks = pentateuch.map { it.toPresentationModel() }
    val historical = historicalBooks.map { it.toPresentationModel() }
    return BooksUiState.Success(
        books = pentateuchBooks + historical,
        filteredBooks = pentateuchBooks + historical,
        selectedTestament = BookTestament.OldTestament,
        searchQuery = "",
        groupsInTestament = listOf(
            BookGroupPresentationModel(
                group = BookGroup.Pentateuch,
                books = pentateuchBooks,
            ),
            BookGroupPresentationModel(
                group = BookGroup.HistoricalBooks,
                books = historical,
            ),
        ),
        filterOptions = emptyList(),
        shouldShowTestamentToggle = true,
        isFilterMenuVisible = false,
        isSortMenuVisible = false,
        sortOrder = null,
        layoutFormat = BookLayoutFormat.Grid,
    )
}

@Composable
private fun SampleBook.toPresentationModel(): BookPresentationModel {
    val progress = chaptersRead.toFloat() / totalChapters
    return BookPresentationModel(
        id = id,
        name = stringResource(id.toBookNameResource()),
        chapterProgressText = "$chaptersRead/$totalChapters",
        progress = progress,
        percentageText = "${(progress * 100).roundToInt()}%",
        isCompleted = chaptersRead == totalChapters,
        isFavorite = isFavorite,
    )
}
