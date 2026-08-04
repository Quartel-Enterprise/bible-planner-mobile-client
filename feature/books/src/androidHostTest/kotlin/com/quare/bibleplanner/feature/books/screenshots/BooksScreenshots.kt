package com.quare.bibleplanner.feature.books.screenshots

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.presentation.model.BookTestament
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.books.presentation.BooksScreen
import com.quare.bibleplanner.feature.books.presentation.model.BookGroupPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.ui.theme.AppTheme
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.jetbrains.compose.resources.stringResource
import org.junit.Test
import kotlin.math.roundToInt

private val bannerCopy = mapOf(
    "en-US" to (
        "Every book, always at hand" to
            "Track your progress through all 66 books of the Bible"
    ),
    "pt-BR" to (
        "Todos os livros sempre à mão" to
            "Acompanhe seu progresso nos 66 livros da Bíblia"
    ),
    "es" to (
        "Todos los libros siempre a mano" to
            "Sigue tu progreso en los 66 libros de la Biblia"
    ),
)

private const val BACKGROUND = 0xFF1B1B1F

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

@OptIn(ExperimentalSharedTransitionApi::class)
internal abstract class BooksScreenshots(
    formFactor: FormFactor,
) : StoreScreenshotsTest(
        formFactor = formFactor,
        // The screen renders without the app's window insets, so reserve the status bar height
        // instead of letting the search field slide under the frame's clock.
        style = ScreenshotStyle(edgeToEdge = false),
    ) {
    @Test
    fun books() = bannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            fileName = "05_books",
        ) {
            AppTheme {
                SharedTransitionLayout {
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

internal class PhoneBooksScreenshots : BooksScreenshots(FormFactor.Phone)

internal class Tablet7BooksScreenshots : BooksScreenshots(FormFactor.Tablet7)

internal class Tablet10BooksScreenshots : BooksScreenshots(FormFactor.Tablet10)

internal class IPhoneBooksScreenshots : BooksScreenshots(FormFactor.AppleIPhone67)

internal class IPadBooksScreenshots : BooksScreenshots(FormFactor.AppleIPad13)

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
