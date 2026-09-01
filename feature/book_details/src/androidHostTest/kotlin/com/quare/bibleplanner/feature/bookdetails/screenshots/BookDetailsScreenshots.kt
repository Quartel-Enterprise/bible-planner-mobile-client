package com.quare.bibleplanner.feature.bookdetails.screenshots

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.bookdetails.presentation.BookDetailsScreen
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiState
import com.quare.bibleplanner.feature.bookdetails.presentation.utils.toSynopsisResource
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme
import com.quare.bibleplanner.ui.theme.model.Theme
import dev.lucianosantos.storescreenshots.DeviceMockup
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.MockupOrientation
import dev.lucianosantos.storescreenshots.ScreenshotCanvas
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.jetbrains.compose.resources.stringResource
import org.junit.Test

/**
 * Psalms, half read, with the synopsis open. The book is picked for its 150 chapters: on the
 * landscape shot the grid fills its whole column, where a short book leaves the bottom half of the
 * tablet empty.
 */
private const val BACKGROUND = 0xFF141C3D
private const val README_SUBDIR = "readme"
private const val BOOK_CHAPTERS = 150
private const val READ_CHAPTERS = 76

/** The README grid's book shot. See docs/store-listing-screenshots.md for the variant. */
internal class ReadmeBookDetailsScreenshots :
    StoreScreenshotsTest(
        formFactor = FormFactor.Phone,
        style = ScreenshotStyle(edgeToEdge = false),
    ) {
    @Test
    fun book() = screenshot(
        backgroundColor = Color(BACKGROUND),
        subdir = README_SUBDIR,
        fileName = "book",
    ) {
        CompositionLocalProvider(LocalTheme provides Theme.DARK) {
            AppTheme { BookDetailsContent() }
        }
    }
}

/**
 * The one landscape shot: the same screen splits into two columns above 600dp, which is the whole
 * argument of the README's multiplatform section, and a portrait frame cannot show it.
 */
internal class ReadmeBookDetailsWideScreenshots :
    StoreScreenshotsTest(
        formFactor = FormFactor.Tablet10,
        canvas = ScreenshotCanvas.dp(1280, 880),
        style = ScreenshotStyle(
            edgeToEdge = false,
            mockupFrame = { content ->
                DeviceMockup(
                    formFactor = FormFactor.Tablet10,
                    orientation = MockupOrientation.Landscape,
                    edgeToEdge = false,
                    content = content,
                )
            },
        ),
    ) {
    @Test
    fun bookWide() = screenshot(
        backgroundColor = Color(BACKGROUND),
        subdir = README_SUBDIR,
        fileName = "wide_book",
    ) {
        CompositionLocalProvider(LocalTheme provides Theme.DARK) {
            AppTheme { BookDetailsContent() }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun BookDetailsContent() {
    SharedTransitionLayout {
        AnimatedVisibility(visible = true) {
            BookDetailsScreen(
                platform = Platform.Android,
                state = bookDetailsUiState(),
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this@AnimatedVisibility,
                onEvent = {},
            )
        }
    }
}

@Composable
private fun bookDetailsUiState(): BookDetailsUiState.Success = BookDetailsUiState.Success(
    id = BookId.PSA,
    nameStringResource = BookId.PSA.toBookNameResource(),
    synopsisStringResource = BookId.PSA.toSynopsisResource(),
    chapters = (1..BOOK_CHAPTERS).map { number ->
        BookChapterModel(
            number = number,
            verses = emptyList(),
            isRead = number <= READ_CHAPTERS,
            readUpdatedAt = null,
        )
    },
    progress = READ_CHAPTERS.toFloat() / BOOK_CHAPTERS,
    readChaptersCount = READ_CHAPTERS,
    totalChaptersCount = BOOK_CHAPTERS,
    areAllChaptersRead = false,
    isFavorite = true,
    bookGroup = BookGroup.WisdomBooks,
    bookCategoryName = stringResource(BookGroup.WisdomBooks.titleRes),
    isSynopsisExpanded = true,
)
