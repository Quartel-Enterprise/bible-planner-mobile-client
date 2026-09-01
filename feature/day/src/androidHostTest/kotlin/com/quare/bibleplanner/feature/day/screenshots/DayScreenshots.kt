package com.quare.bibleplanner.feature.day.screenshots

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.day.presentation.DayScreen
import com.quare.bibleplanner.feature.daystudy.presentation.component.AiStudyEntryCard
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardQuotaUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
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
private const val BACKGROUND = 0xFF2A2A30
private const val FREE_LIMIT = 3

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

internal abstract class DayScreenshots(
    private val formFactor: FormFactor,
    private val outputSubdir: String? = null,
    canvas: ScreenshotCanvas? = null,
) : StoreScreenshotsTest(
        formFactor = formFactor,
        canvas = canvas,
        style = ScreenshotStyle(edgeToEdge = false),
    ) {
    @Test
    fun day() = bannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            subdir = outputSubdir,
            fileName = "03_day",
        ) {
            CompositionLocalProvider(LocalTheme provides Theme.DARK) {
                AppTheme { DayContent(locale, formFactor.platform) }
            }
        }
    }
}

internal class PhoneDayScreenshots : DayScreenshots(FormFactor.Phone)

internal class Tablet7DayScreenshots : DayScreenshots(FormFactor.Tablet7)

internal class Tablet10DayScreenshots : DayScreenshots(FormFactor.Tablet10)

internal class IPhone65DayScreenshots : DayScreenshots(FormFactor.AppleIPhone65)

internal class IPhone67DayScreenshots : DayScreenshots(FormFactor.AppleIPhone67)

internal class IPad13DayScreenshots : DayScreenshots(FormFactor.AppleIPad13)

// The 11" slot: same bezel, a taller canvas, so Apple does not have to letterbox the 13" one.
internal class IPad11DayScreenshots :
    DayScreenshots(
        formFactor = FormFactor.AppleIPad13,
        outputSubdir = "ipad11",
        canvas = ScreenshotCanvas.px(1668, 2388),
    )

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DayContent(
    locale: String,
    platform: Platform,
) {
    SharedTransitionLayout {
        AnimatedContent(targetState = Unit) {
            DayScreen(
                platform = platform,
                uiState = dayUiState(locale),
                snackbarHostState = SnackbarHostState(),
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedContentScope = this@AnimatedContent,
                isLandscape = false,
                onEvent = {},
                // The real card, with a state the ViewModel would otherwise have produced: the
                // section it replaces resolves a Koin ViewModel that pulls in Supabase and Room.
                // A null mode drops the status badge, so the listing shot advertises the feature
                // rather than a quota.
                dayStudySection = { _, _, sectionModifier ->
                    AiStudyEntryCard(
                        card = DayStudyCardUiModel(
                            mode = null,
                            quota = Loadable.Loaded(
                                DayStudyCardQuotaUiModel(
                                    remainingFree = FREE_LIMIT,
                                    freeLimit = FREE_LIMIT,
                                ),
                            ),
                            isPro = true,
                        ),
                        generation = null,
                        isOpening = false,
                        onClick = {},
                        modifier = sectionModifier,
                    )
                },
            )
        }
    }
}
