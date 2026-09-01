package com.quare.bibleplanner.feature.read.screenshots

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.presentation.screen.ReadNarrowScreen
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme
import com.quare.bibleplanner.ui.theme.model.Theme
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotCanvas
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

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
private const val BACKGROUND = 0xFF2A2A30

/**
 * Screens branch on this — the back arrow is a chevron on Apple and a left arrow elsewhere — and
 * everything here renders under Robolectric, which is Android whatever device the frame draws. So
 * the platform has to follow the form factor, or the Apple shots ship Android chrome.
 */
private val FormFactor.platform: Platform
    get() = when (this) {
        FormFactor.AppleIPhone65,
        FormFactor.AppleIPhone67,
        FormFactor.AppleIPad13,
        -> Platform.Ios

        else -> Platform.Android
    }

internal abstract class ReadScreenshots(
    private val formFactor: FormFactor,
    private val outputSubdir: String? = null,
    canvas: ScreenshotCanvas? = null,
) : StoreScreenshotsTest(
        formFactor = formFactor,
        canvas = canvas,
        style = ScreenshotStyle(edgeToEdge = false),
    ) {
    @Test
    fun read() = bannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            subdir = outputSubdir,
            fileName = "06_read",
        ) {
            CompositionLocalProvider(LocalTheme provides Theme.DARK) {
                AppTheme {
                    ReadNarrowScreen(
                        platform = formFactor.platform,
                        state = readUiState(locale),
                        onEvent = {},
                    )
                }
            }
        }
    }
}

internal class PhoneReadScreenshots : ReadScreenshots(FormFactor.Phone)

internal class Tablet7ReadScreenshots : ReadScreenshots(FormFactor.Tablet7)

internal class Tablet10ReadScreenshots : ReadScreenshots(FormFactor.Tablet10)

internal class IPhone65ReadScreenshots : ReadScreenshots(FormFactor.AppleIPhone65)

internal class IPhone67ReadScreenshots : ReadScreenshots(FormFactor.AppleIPhone67)

internal class IPad13ReadScreenshots : ReadScreenshots(FormFactor.AppleIPad13)

// The 11" slot: same bezel, a taller canvas, so Apple does not have to letterbox the 13" one.
internal class IPad11ReadScreenshots :
    ReadScreenshots(
        formFactor = FormFactor.AppleIPad13,
        outputSubdir = "ipad11",
        canvas = ScreenshotCanvas.px(1668, 2388),
    )
