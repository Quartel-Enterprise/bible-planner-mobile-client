package com.quare.bibleplanner.feature.day.screenshots

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.day.fixture.DAY_SCREENSHOT
import com.quare.bibleplanner.feature.day.fixture.DayScreenshotContent
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
 * ones pass [appleSlot] and frame the PNG DayAppleScreenshotCaptures rendered on the iOS
 * simulator, so the App Store shows the screen as iOS draws it.
 */
internal abstract class DayScreenshots(
    private val outputSubdir: String? = null,
    private val appleSlot: AppleScreenshotSlot? = null,
    formFactor: FormFactor,
    canvas: ScreenshotCanvas? = null,
) : StoreScreenshotsTest(
        formFactor = formFactor,
        canvas = canvas,
        // Robolectric renders the screen without the app's window insets, so reserve the status
        // bar height instead of letting the header slide under the frame's clock. An iOS capture
        // already leaves that room itself and fills the whole screen.
        style = ScreenshotStyle(edgeToEdge = appleSlot != null),
    ) {
    private val bannerCopy = mapOf(
        "en-US" to (
            "Tick off today's reading, chapter by chapter" to
                "Open the day, read, and mark each chapter as you go"
        ),
        "pt-BR" to (
            "Marque a leitura de hoje, capítulo por capítulo" to
                "Abra o dia, leia e vá marcando cada capítulo"
        ),
        "es" to (
            "Marca la lectura de hoy, capítulo por capítulo" to
                "Abre el día, lee y ve marcando cada capítulo"
        ),
    )

    @Test
    fun day() = bannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            subdir = outputSubdir,
            fileName = DAY_SCREENSHOT,
        ) {
            if (appleSlot == null) {
                DayScreenshotContent(
                    locale = locale,
                    platform = Platform.Android,
                    statusBarHeight = 0.dp,
                )
            } else {
                AppleScreenshotImage(
                    slot = appleSlot,
                    locale = locale,
                    fileName = DAY_SCREENSHOT,
                )
            }
        }
    }
}

internal class PhoneDayScreenshots : DayScreenshots(formFactor = FormFactor.Phone)

internal class Tablet7DayScreenshots : DayScreenshots(formFactor = FormFactor.Tablet7)

internal class Tablet10DayScreenshots : DayScreenshots(formFactor = FormFactor.Tablet10)

internal class IPhone65DayScreenshots :
    DayScreenshots(
        appleSlot = AppleScreenshotSlot.IPHONE_6_5,
        formFactor = FormFactor.AppleIPhone65,
    )

internal class IPhone67DayScreenshots :
    DayScreenshots(
        appleSlot = AppleScreenshotSlot.IPHONE_6_7,
        formFactor = FormFactor.AppleIPhone67,
    )

internal class IPad13DayScreenshots :
    DayScreenshots(
        appleSlot = AppleScreenshotSlot.IPAD_13,
        formFactor = FormFactor.AppleIPad13,
    )

// The 11" slot: same bezel, a taller canvas, so Apple does not have to letterbox the 13" one. The
// screen inside is the same size, so it frames the 13" capture.
internal class IPad11DayScreenshots :
    DayScreenshots(
        appleSlot = AppleScreenshotSlot.IPAD_13,
        formFactor = FormFactor.AppleIPad13,
        outputSubdir = "ipad11",
        canvas = ScreenshotCanvas.px(1668, 2388),
    )

/** The README grid's day shot. See docs/store-listing-screenshots.md for the variant. */
internal class ReadmeDayScreenshots :
    StoreScreenshotsTest(
        formFactor = FormFactor.Phone,
        style = ScreenshotStyle(edgeToEdge = false),
    ) {
    @Test
    fun day() = screenshot(
        backgroundColor = Color(BACKGROUND),
        subdir = README_SUBDIR,
        fileName = "day",
    ) {
        DayScreenshotContent(
            locale = README_LOCALE,
            platform = Platform.Android,
            statusBarHeight = 0.dp,
        )
    }

    private companion object {
        const val README_SUBDIR = "readme"
        const val README_LOCALE = "en-US"
    }
}
