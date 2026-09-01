package com.quare.bibleplanner.feature.chat.screenshots

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.feature.chat.presentation.ChatScreen
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme
import com.quare.bibleplanner.ui.theme.model.Theme
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotCanvas
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Test

private val bannerCopy = mapOf(
    "en-US" to (
        "Ask anything about today's reading" to
            "The chat already knows the passage, so you can go straight to the question"
    ),
    "pt-BR" to (
        "Pergunte o que quiser sobre a leitura de hoje" to
            "O chat já conhece a passagem, então você vai direto à pergunta"
    ),
    "es" to (
        "Pregunta lo que quieras sobre la lectura de hoy" to
            "El chat ya conoce el pasaje, así que vas directo a la pregunta"
    ),
)
private const val BACKGROUND = 0xFF141C3D
private const val README_SUBDIR = "readme"
private const val README_LOCALE = "en-US"

internal abstract class ChatScreenshots(
    formFactor: FormFactor,
    private val outputSubdir: String? = null,
    canvas: ScreenshotCanvas? = null,
) : StoreScreenshotsTest(
        formFactor = formFactor,
        canvas = canvas,
        style = ScreenshotStyle(edgeToEdge = false),
    ) {
    @Test
    fun chat() = bannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            subdir = outputSubdir,
            fileName = "09_chat",
        ) {
            CompositionLocalProvider(LocalTheme provides Theme.DARK) {
                AppTheme {
                    ChatScreen(
                        uiState = chatUiState(locale),
                        scrollToBottomRequests = emptyFlow(),
                        onEvent = {},
                        onNavigateBack = {},
                    )
                }
            }
        }
    }
}

internal class PhoneChatScreenshots : ChatScreenshots(FormFactor.Phone)

internal class Tablet7ChatScreenshots : ChatScreenshots(FormFactor.Tablet7)

internal class Tablet10ChatScreenshots : ChatScreenshots(FormFactor.Tablet10)

internal class IPhone65ChatScreenshots : ChatScreenshots(FormFactor.AppleIPhone65)

internal class IPhone67ChatScreenshots : ChatScreenshots(FormFactor.AppleIPhone67)

internal class IPad13ChatScreenshots : ChatScreenshots(FormFactor.AppleIPad13)

// The 11" slot: same bezel, a taller canvas, so Apple does not have to letterbox the 13" one.
internal class IPad11ChatScreenshots :
    ChatScreenshots(
        formFactor = FormFactor.AppleIPad13,
        outputSubdir = "ipad11",
        canvas = ScreenshotCanvas.px(1668, 2388),
    )

/** The README grid's chat shot. See docs/store-listing-screenshots.md for the variant. */
internal class ReadmeChatScreenshots :
    StoreScreenshotsTest(
        formFactor = FormFactor.Phone,
        style = ScreenshotStyle(edgeToEdge = false),
    ) {
    @Test
    fun chat() = screenshot(
        backgroundColor = Color(BACKGROUND),
        subdir = README_SUBDIR,
        fileName = "chat",
    ) {
        CompositionLocalProvider(LocalTheme provides Theme.DARK) {
            AppTheme {
                ChatScreen(
                    uiState = chatUiState(README_LOCALE),
                    scrollToBottomRequests = emptyFlow(),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
    }
}
