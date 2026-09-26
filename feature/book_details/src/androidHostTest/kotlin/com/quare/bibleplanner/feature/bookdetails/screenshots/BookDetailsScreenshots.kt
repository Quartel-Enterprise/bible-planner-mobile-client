package com.quare.bibleplanner.feature.bookdetails.screenshots

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.bookdetails.fixture.bookDetailsUiState
import com.quare.bibleplanner.feature.bookdetails.presentation.BookDetailsScreen
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme
import dev.lucianosantos.storescreenshots.DeviceMockup
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.MockupOrientation
import dev.lucianosantos.storescreenshots.ScreenshotCanvas
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.jetbrains.compose.resources.stringResource
import org.junit.Test

private const val BACKGROUND = 0xFF141C3D
private const val README_SUBDIR = "readme"

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
                state = bookDetailsUiState(bookCategoryName = stringResource(BookGroup.WisdomBooks.titleRes)),
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this@AnimatedVisibility,
                onEvent = {},
            )
        }
    }
}
