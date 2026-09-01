package com.quare.bibleplanner.feature.readingplan.screenshots

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.feature.readingplan.presentation.content.ReadingPlanScreen
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme
import com.quare.bibleplanner.ui.theme.model.Theme
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
private const val README_SUBDIR = "readme"
private val bannerCopy = mapOf(
    "en-US" to
        ("Your whole Bible, one day at a time" to "A plan that keeps its place, so you always know what to read next"),
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

internal abstract class ReadingPlanScreenshots(
    formFactor: FormFactor,
    private val outputSubdir: String? = null,
    canvas: ScreenshotCanvas? = null,
) : StoreScreenshotsTest(
        formFactor = formFactor,
        canvas = canvas,
        // The screen renders without the app's window insets, so reserve the status bar height
        // instead of letting the header slide under the frame's clock.
        style = ScreenshotStyle(edgeToEdge = false),
    ) {
    @Test
    fun readingPlan() = bannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            subdir = outputSubdir,
            fileName = "01_reading_plan",
        ) {
            CompositionLocalProvider(LocalTheme provides Theme.DARK) {
                AppTheme { ReadingPlanContent() }
            }
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
            fileName = "02_reading_plan_light",
        ) {
            CompositionLocalProvider(LocalTheme provides Theme.LIGHT) {
                AppTheme { ReadingPlanContent() }
            }
        }
    }
}

internal class PhoneReadingPlanScreenshots : ReadingPlanScreenshots(FormFactor.Phone)

internal class Tablet7ReadingPlanScreenshots : ReadingPlanScreenshots(FormFactor.Tablet7)

internal class Tablet10ReadingPlanScreenshots : ReadingPlanScreenshots(FormFactor.Tablet10)

internal class IPhone65ReadingPlanScreenshots : ReadingPlanScreenshots(FormFactor.AppleIPhone65)

internal class IPhone67ReadingPlanScreenshots : ReadingPlanScreenshots(FormFactor.AppleIPhone67)

internal class IPad13ReadingPlanScreenshots : ReadingPlanScreenshots(FormFactor.AppleIPad13)

// The 11" slot: same bezel, a taller canvas, so Apple does not have to letterbox the 13" one.
internal class IPad11ReadingPlanScreenshots :
    ReadingPlanScreenshots(
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
        CompositionLocalProvider(LocalTheme provides Theme.DARK) {
            AppTheme { ReadingPlanContent() }
        }
    }

    @Test
    fun planLight() = screenshot(
        backgroundColor = Color(BACKGROUND),
        subdir = README_SUBDIR,
        fileName = "plan_light",
    ) {
        CompositionLocalProvider(LocalTheme provides Theme.LIGHT) {
            AppTheme { ReadingPlanContent() }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ReadingPlanContent() {
    // The screen paints no background of its own — in the app that comes from the Scaffold in
    // MainRoot — so without a Surface the frame's bezel shows through behind the cards.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        SharedTransitionLayout {
            AnimatedContent(targetState = Unit) {
                ReadingPlanScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@AnimatedContent,
                    uiState = readingPlanUiState(),
                    onEvent = {},
                    lazyListState = rememberLazyListState(),
                )
            }
        }
    }
}
