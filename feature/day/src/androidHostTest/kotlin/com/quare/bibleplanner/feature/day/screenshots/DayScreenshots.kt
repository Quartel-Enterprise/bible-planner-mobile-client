package com.quare.bibleplanner.feature.day.screenshots

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.day.presentation.DayScreen
import com.quare.bibleplanner.feature.daystudy.presentation.component.AiStudyEntryCard
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardQuotaUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
import com.quare.bibleplanner.ui.theme.AppTheme
import dev.lucianosantos.storescreenshots.FormFactor
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

private const val BACKGROUND = 0xFF1B1B1F
private const val FREE_LIMIT = 3

internal abstract class DayScreenshots(
    formFactor: FormFactor,
) : StoreScreenshotsTest(
        formFactor = formFactor,
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
            fileName = "03_day",
        ) {
            AppTheme { DayContent(locale) }
        }
    }
}

internal class PhoneDayScreenshots : DayScreenshots(FormFactor.Phone)

internal class Tablet7DayScreenshots : DayScreenshots(FormFactor.Tablet7)

internal class Tablet10DayScreenshots : DayScreenshots(FormFactor.Tablet10)

internal class IPhone65DayScreenshots : DayScreenshots(FormFactor.AppleIPhone65)

internal class IPhone67DayScreenshots : DayScreenshots(FormFactor.AppleIPhone67)

internal class IPadDayScreenshots : DayScreenshots(FormFactor.AppleIPad13)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DayContent(locale: String) {
    SharedTransitionLayout {
        AnimatedContent(targetState = Unit) {
            DayScreen(
                platform = Platform.Android,
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
