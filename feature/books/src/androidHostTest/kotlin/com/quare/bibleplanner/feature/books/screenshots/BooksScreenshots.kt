package com.quare.bibleplanner.feature.books.screenshots

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.books.fixture.BOOKS_SCREENSHOT
import com.quare.bibleplanner.feature.books.fixture.BooksScreenshotContent
import com.quare.bibleplanner.ui.testing.screenshots.AppleScreenshotImage
import com.quare.bibleplanner.ui.testing.screenshots.AppleScreenshotSlot
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotCanvas
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

private const val BACKGROUND = 0xFF141C3D

/**
 * One subclass per form factor. The Play ones render the screen here, under Robolectric; the Apple
 * ones pass [appleSlot] and frame the PNG BooksAppleScreenshotCaptures rendered on the iOS
 * simulator, so the App Store shows the screen as iOS draws it.
 */
internal abstract class BooksScreenshots(
    private val outputSubdir: String? = null,
    private val appleSlot: AppleScreenshotSlot? = null,
    formFactor: FormFactor,
    canvas: ScreenshotCanvas? = null,
) : StoreScreenshotsTest(
        formFactor = formFactor,
        canvas = canvas,
        // The screen renders without the app's window insets, so reserve the status bar height
        // instead of letting the search field slide under the frame's clock. An iOS capture
        // already leaves that room itself and fills the whole screen.
        style = ScreenshotStyle(edgeToEdge = appleSlot != null),
    ) {
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

    @Test
    fun books() = bannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            subdir = outputSubdir,
            fileName = BOOKS_SCREENSHOT,
        ) {
            if (appleSlot == null) {
                BooksScreenshotContent(statusBarHeight = 0.dp)
            } else {
                AppleScreenshotImage(
                    slot = appleSlot,
                    locale = locale,
                    fileName = BOOKS_SCREENSHOT,
                )
            }
        }
    }
}

internal class PhoneBooksScreenshots : BooksScreenshots(formFactor = FormFactor.Phone)

internal class Tablet7BooksScreenshots : BooksScreenshots(formFactor = FormFactor.Tablet7)

internal class Tablet10BooksScreenshots : BooksScreenshots(formFactor = FormFactor.Tablet10)

internal class IPhone65BooksScreenshots :
    BooksScreenshots(
        appleSlot = AppleScreenshotSlot.IPHONE_6_5,
        formFactor = FormFactor.AppleIPhone65,
    )

internal class IPhone67BooksScreenshots :
    BooksScreenshots(
        appleSlot = AppleScreenshotSlot.IPHONE_6_7,
        formFactor = FormFactor.AppleIPhone67,
    )

internal class IPad13BooksScreenshots :
    BooksScreenshots(
        appleSlot = AppleScreenshotSlot.IPAD_13,
        formFactor = FormFactor.AppleIPad13,
    )

// The 11" slot: same bezel, a taller canvas, so Apple does not have to letterbox the 13" one. The
// screen inside is the same size, so it frames the 13" capture.
internal class IPad11BooksScreenshots :
    BooksScreenshots(
        appleSlot = AppleScreenshotSlot.IPAD_13,
        formFactor = FormFactor.AppleIPad13,
        outputSubdir = "ipad11",
        canvas = ScreenshotCanvas.px(1668, 2388),
    )

/** The README grid's books shot. See docs/store-listing-screenshots.md for the variant. */
internal class ReadmeBooksScreenshots :
    StoreScreenshotsTest(
        formFactor = FormFactor.Phone,
        style = ScreenshotStyle(edgeToEdge = false),
    ) {
    @Test
    fun books() = screenshot(
        backgroundColor = Color(BACKGROUND),
        subdir = README_SUBDIR,
        fileName = "books",
    ) {
        BooksScreenshotContent(statusBarHeight = 0.dp)
    }

    private companion object {
        const val README_SUBDIR = "readme"
    }
}
