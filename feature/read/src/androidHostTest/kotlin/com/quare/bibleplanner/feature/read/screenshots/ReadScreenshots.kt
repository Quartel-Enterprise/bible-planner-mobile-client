package com.quare.bibleplanner.feature.read.screenshots

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.fixture.READ_SCREENSHOT
import com.quare.bibleplanner.feature.read.fixture.ReadScreenshotContent
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
 * ones pass [appleSlot] and frame the PNG ReadAppleScreenshotCaptures rendered on the iOS
 * simulator, so the App Store shows the screen as iOS draws it.
 */
internal abstract class ReadScreenshots(
    private val outputSubdir: String? = null,
    private val appleSlot: AppleScreenshotSlot? = null,
    formFactor: FormFactor,
    canvas: ScreenshotCanvas? = null,
) : StoreScreenshotsTest(
        formFactor = formFactor,
        canvas = canvas,
        // An iOS capture already leaves the status bar's room itself and fills the whole screen.
        style = ScreenshotStyle(edgeToEdge = appleSlot != null),
    ) {
    private val bannerCopy = mapOf(
        "en-US" to (
            "Read the chapter right where you are" to
                "The passage of the day, without leaving the plan behind"
        ),
        "pt-BR" to (
            "Leia o capítulo ali mesmo" to
                "A passagem do dia, sem sair de perto do plano"
        ),
        "es" to (
            "Lee el capítulo allí mismo" to
                "El pasaje del día, sin alejarte del plan"
        ),
    )

    @Test
    fun read() = bannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            subdir = outputSubdir,
            fileName = READ_SCREENSHOT,
        ) {
            if (appleSlot == null) {
                ReadScreenshotContent(
                    platform = Platform.Android,
                    locale = locale,
                    areVersesHighlighted = false,
                    statusBarHeight = 0.dp,
                )
            } else {
                AppleScreenshotImage(
                    slot = appleSlot,
                    locale = locale,
                    fileName = READ_SCREENSHOT,
                )
            }
        }
    }
}

internal class PhoneReadScreenshots : ReadScreenshots(formFactor = FormFactor.Phone)

internal class Tablet7ReadScreenshots : ReadScreenshots(formFactor = FormFactor.Tablet7)

internal class Tablet10ReadScreenshots : ReadScreenshots(formFactor = FormFactor.Tablet10)

internal class IPhone65ReadScreenshots :
    ReadScreenshots(
        appleSlot = AppleScreenshotSlot.IPHONE_6_5,
        formFactor = FormFactor.AppleIPhone65,
    )

internal class IPhone67ReadScreenshots :
    ReadScreenshots(
        appleSlot = AppleScreenshotSlot.IPHONE_6_7,
        formFactor = FormFactor.AppleIPhone67,
    )

internal class IPad13ReadScreenshots :
    ReadScreenshots(
        appleSlot = AppleScreenshotSlot.IPAD_13,
        formFactor = FormFactor.AppleIPad13,
    )

// The 11" slot: same bezel, a taller canvas, so Apple does not have to letterbox the 13" one. The
// screen inside is the same size, so it frames the 13" capture.
internal class IPad11ReadScreenshots :
    ReadScreenshots(
        appleSlot = AppleScreenshotSlot.IPAD_13,
        formFactor = FormFactor.AppleIPad13,
        outputSubdir = "ipad11",
        canvas = ScreenshotCanvas.px(1668, 2388),
    )

/** The README grid's reader shots. See docs/store-listing-screenshots.md for the variant. */
internal class ReadmeReadScreenshots :
    StoreScreenshotsTest(
        formFactor = FormFactor.Phone,
        style = ScreenshotStyle(edgeToEdge = false),
    ) {
    @Test
    fun reader() = readmeScreenshot(
        fileName = "reader",
        areVersesHighlighted = false,
    )

    @Test
    fun highlights() = readmeScreenshot(
        fileName = "highlights",
        areVersesHighlighted = true,
    )

    private fun readmeScreenshot(
        fileName: String,
        areVersesHighlighted: Boolean,
    ) = screenshot(
        backgroundColor = Color(BACKGROUND),
        subdir = README_SUBDIR,
        fileName = fileName,
    ) {
        ReadScreenshotContent(
            platform = Platform.Android,
            locale = README_LOCALE,
            areVersesHighlighted = areVersesHighlighted,
            statusBarHeight = 0.dp,
        )
    }

    private companion object {
        const val README_SUBDIR = "readme"
        const val README_LOCALE = "en-US"
    }
}
