package com.quare.bibleplanner.feature.readingplan.screenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.feature.readingplan.fixture.READING_PLAN_LIGHT_SCREENSHOT
import com.quare.bibleplanner.feature.readingplan.fixture.READING_PLAN_SCREENSHOT
import com.quare.bibleplanner.feature.readingplan.fixture.ReadingPlanScreenshotContent
import com.quare.bibleplanner.ui.testing.screenshots.AppleScreenshotImage
import com.quare.bibleplanner.ui.testing.screenshots.AppleScreenshotSlot
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotCanvas
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

/**
 * The logo's blue, dimmed to a depth the app's own accents can still outshine. The full-strength
 * #4A6CF7 separates a dark device beautifully but is the same blue the day card, the progress ring
 * and the checks are painted in, so it out-glows the product it is framing.
 */
private const val BACKGROUND = 0xFF141C3D

/**
 * One subclass per form factor. The Play ones render the screen here, under Robolectric; the Apple
 * ones pass [appleSlot] and frame the PNG ReadingPlanAppleScreenshotCaptures rendered on the iOS
 * simulator, so the App Store shows the screen as iOS draws it.
 */
internal abstract class ReadingPlanScreenshots(
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
        "en-US" to
            (
                "Your whole Bible, one day at a time" to
                    "A plan that keeps its place, so you always know what to read next"
            ),
        "pt-BR" to
            (
                "A Bíblia inteira, um dia por vez" to
                    "Um plano que guarda o seu lugar, para você sempre saber o que ler a seguir"
            ),
        "es" to
            (
                "La Biblia entera, un día a la vez" to
                    "Un plan que guarda tu lugar, para que siempre sepas qué leer después"
            ),
    )
    private val lightBannerCopy = mapOf(
        "en-US" to (
            "Light or dark, it follows you" to
                "The same plan, in the theme you actually read in"
        ),
        "pt-BR" to (
            "Claro ou escuro, ele acompanha" to
                "O mesmo plano, no tema em que você realmente lê"
        ),
        "es" to (
            "Claro u oscuro, te acompaña" to
                "El mismo plan, en el tema en que de verdad lees"
        ),
    )

    @Test
    fun readingPlan() = bannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            subdir = outputSubdir,
            fileName = READING_PLAN_SCREENSHOT,
        ) {
            ScreenshotContent(
                theme = Theme.DARK,
                locale = locale,
                fileName = READING_PLAN_SCREENSHOT,
            )
        }
    }

    @Test
    fun readingPlanLight() = lightBannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            subdir = outputSubdir,
            fileName = READING_PLAN_LIGHT_SCREENSHOT,
            // An iOS capture runs the light screen up under the status bar, as iOS does, so the
            // clock and icons turn dark to stay readable on it.
            style = ScreenshotStyle(
                edgeToEdge = appleSlot != null,
                statusBarContentDark = appleSlot != null,
            ),
        ) {
            ScreenshotContent(
                theme = Theme.LIGHT,
                locale = locale,
                fileName = READING_PLAN_LIGHT_SCREENSHOT,
            )
        }
    }

    @Composable
    private fun ScreenshotContent(
        theme: Theme,
        locale: String,
        fileName: String,
    ) {
        if (appleSlot == null) {
            ReadingPlanScreenshotContent(
                theme = theme,
                statusBarHeight = 0.dp,
            )
        } else {
            AppleScreenshotImage(
                slot = appleSlot,
                locale = locale,
                fileName = fileName,
            )
        }
    }
}

internal class PhoneReadingPlanScreenshots : ReadingPlanScreenshots(formFactor = FormFactor.Phone)

internal class Tablet7ReadingPlanScreenshots : ReadingPlanScreenshots(formFactor = FormFactor.Tablet7)

internal class Tablet10ReadingPlanScreenshots : ReadingPlanScreenshots(formFactor = FormFactor.Tablet10)

internal class IPhone65ReadingPlanScreenshots :
    ReadingPlanScreenshots(
        appleSlot = AppleScreenshotSlot.IPHONE_6_5,
        formFactor = FormFactor.AppleIPhone65,
    )

internal class IPhone67ReadingPlanScreenshots :
    ReadingPlanScreenshots(
        appleSlot = AppleScreenshotSlot.IPHONE_6_7,
        formFactor = FormFactor.AppleIPhone67,
    )

internal class IPad13ReadingPlanScreenshots :
    ReadingPlanScreenshots(
        appleSlot = AppleScreenshotSlot.IPAD_13,
        formFactor = FormFactor.AppleIPad13,
    )

// The 11" slot: same bezel, a taller canvas, so Apple does not have to letterbox the 13" one. The
// screen inside is the same size, so it frames the 13" capture.
internal class IPad11ReadingPlanScreenshots :
    ReadingPlanScreenshots(
        appleSlot = AppleScreenshotSlot.IPAD_13,
        formFactor = FormFactor.AppleIPad13,
        outputSubdir = "ipad11",
        canvas = ScreenshotCanvas.px(1668, 2388),
    )

/** The README grid's two plan shots. See docs/store-listing-screenshots.md for the variant. */
internal class ReadmeReadingPlanScreenshots :
    StoreScreenshotsTest(
        formFactor = FormFactor.Phone,
        style = ScreenshotStyle(edgeToEdge = false),
    ) {
    @Test
    fun plan() = screenshot(
        backgroundColor = Color(BACKGROUND),
        subdir = README_SUBDIR,
        fileName = "plan",
    ) {
        ReadingPlanScreenshotContent(
            theme = Theme.DARK,
            statusBarHeight = 0.dp,
        )
    }

    @Test
    fun planLight() = screenshot(
        backgroundColor = Color(BACKGROUND),
        subdir = README_SUBDIR,
        fileName = "plan_light",
    ) {
        ReadingPlanScreenshotContent(
            theme = Theme.LIGHT,
            statusBarHeight = 0.dp,
        )
    }

    private companion object {
        const val README_SUBDIR = "readme"
    }
}
