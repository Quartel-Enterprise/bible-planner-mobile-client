package com.quare.bibleplanner.feature.chat.screenshots

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.chat.fixture.CHAT_SCREENSHOT
import com.quare.bibleplanner.feature.chat.fixture.ChatScreenshotContent
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
 * ones pass [appleSlot] and frame the PNG ChatAppleScreenshotCaptures rendered on the iOS
 * simulator, so the App Store shows the screen as iOS draws it.
 */
internal abstract class ChatScreenshots(
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

    @Test
    fun chat() = bannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            subdir = outputSubdir,
            fileName = CHAT_SCREENSHOT,
        ) {
            if (appleSlot == null) {
                ChatScreenshotContent(
                    locale = locale,
                    statusBarHeight = 0.dp,
                )
            } else {
                AppleScreenshotImage(
                    slot = appleSlot,
                    locale = locale,
                    fileName = CHAT_SCREENSHOT,
                )
            }
        }
    }
}

internal class PhoneChatScreenshots : ChatScreenshots(formFactor = FormFactor.Phone)

internal class Tablet7ChatScreenshots : ChatScreenshots(formFactor = FormFactor.Tablet7)

internal class Tablet10ChatScreenshots : ChatScreenshots(formFactor = FormFactor.Tablet10)

internal class IPhone65ChatScreenshots :
    ChatScreenshots(
        appleSlot = AppleScreenshotSlot.IPHONE_6_5,
        formFactor = FormFactor.AppleIPhone65,
    )

internal class IPhone67ChatScreenshots :
    ChatScreenshots(
        appleSlot = AppleScreenshotSlot.IPHONE_6_7,
        formFactor = FormFactor.AppleIPhone67,
    )

internal class IPad13ChatScreenshots :
    ChatScreenshots(
        appleSlot = AppleScreenshotSlot.IPAD_13,
        formFactor = FormFactor.AppleIPad13,
    )

// The 11" slot: same bezel, a taller canvas, so Apple does not have to letterbox the 13" one. The
// screen inside is the same size, so it frames the 13" capture.
internal class IPad11ChatScreenshots :
    ChatScreenshots(
        appleSlot = AppleScreenshotSlot.IPAD_13,
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
        ChatScreenshotContent(
            locale = README_LOCALE,
            statusBarHeight = 0.dp,
        )
    }

    private companion object {
        const val README_SUBDIR = "readme"
        const val README_LOCALE = "en-US"
    }
}
