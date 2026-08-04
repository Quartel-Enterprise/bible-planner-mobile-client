package com.quare.bibleplanner.feature.read.screenshots

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.presentation.screen.ReadPortraitScreen
import com.quare.bibleplanner.ui.theme.AppTheme
import dev.lucianosantos.storescreenshots.FormFactor
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

private const val BACKGROUND = 0xFF1B1B1F

@OptIn(ExperimentalSharedTransitionApi::class)
internal abstract class ReadScreenshots(
    formFactor: FormFactor,
) : StoreScreenshotsTest(
        formFactor = formFactor,
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
            fileName = "06_read",
        ) {
            AppTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        ReadPortraitScreen(
                            platform = Platform.Android,
                            state = readUiState(locale),
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

internal class PhoneReadScreenshots : ReadScreenshots(FormFactor.Phone)

internal class Tablet7ReadScreenshots : ReadScreenshots(FormFactor.Tablet7)

internal class Tablet10ReadScreenshots : ReadScreenshots(FormFactor.Tablet10)

internal class IPhoneReadScreenshots : ReadScreenshots(FormFactor.AppleIPhone67)

internal class IPadReadScreenshots : ReadScreenshots(FormFactor.AppleIPad13)
