package com.quare.bibleplanner.feature.daystudy.screenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_tab_questions
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.daystudy.fixture.DAY_STUDY_CONTEXT_SCREENSHOT
import com.quare.bibleplanner.feature.daystudy.fixture.DAY_STUDY_QUESTIONS_SCREENSHOT
import com.quare.bibleplanner.feature.daystudy.fixture.DAY_STUDY_SCREENSHOT
import com.quare.bibleplanner.feature.daystudy.fixture.DayStudyScreenshotContent
import com.quare.bibleplanner.feature.daystudy.fixture.firstQuestion
import com.quare.bibleplanner.ui.testing.screenshots.AppleScreenshotImage
import com.quare.bibleplanner.ui.testing.screenshots.AppleScreenshotSlot
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotCanvas
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Assume.assumeTrue
import org.junit.Test

private const val BACKGROUND = 0xFF141C3D

/**
 * One subclass per form factor. The Play ones render the screen here, under Robolectric; the Apple
 * ones pass [appleSlot] and frame the PNG DayStudyAppleScreenshotCaptures rendered on the iOS
 * simulator, so the App Store shows the screen as iOS draws it.
 */
internal abstract class DayStudyScreenshots(
    private val isWide: Boolean,
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
            "Understand what you just read" to
                "An overview, the historical context, and the questions the passage raises"
        ),
        "pt-BR" to (
            "Entenda o que você acabou de ler" to
                "Uma visão geral, o contexto histórico e as perguntas que a passagem levanta"
        ),
        "es" to (
            "Entiende lo que acabas de leer" to
                "Una visión general, el contexto histórico y las preguntas que plantea el pasaje"
        ),
    )
    private val contextBannerCopy = mapOf(
        "en-US" to (
            "The world the passage happened in" to
                "Who was writing, when, and what the first readers already knew"
        ),
        "pt-BR" to (
            "O mundo em que a passagem aconteceu" to
                "Quem escreveu, quando, e o que os primeiros leitores já sabiam"
        ),
        "es" to (
            "El mundo en que ocurrió el pasaje" to
                "Quién escribía, cuándo, y qué sabían ya los primeros lectores"
        ),
    )
    private val questionsBannerCopy = mapOf(
        "en-US" to (
            "The questions the passage raises" to
                "Straight answers to what readers most often ask about this reading"
        ),
        "pt-BR" to (
            "As perguntas que a passagem levanta" to
                "Respostas diretas ao que os leitores mais perguntam sobre esta leitura"
        ),
        "es" to (
            "Las preguntas que plantea el pasaje" to
                "Respuestas directas a lo que más preguntan los lectores sobre esta lectura"
        ),
    )

    @Test
    fun dayStudy() = bannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            subdir = outputSubdir,
            fileName = DAY_STUDY_SCREENSHOT,
        ) {
            ScreenshotContent(
                locale = locale,
                fileName = DAY_STUDY_SCREENSHOT,
            )
        }
    }

    /**
     * Play caps a store listing at eight screenshots per device and the App Store at ten. The chat
     * screen takes the eighth Play slot, so the context tab — the thinnest of the study's three,
     * half a screen of white under three short facts — is the one that fills an Apple slot only.
     */
    @Test
    fun dayStudyContext() {
        assumeTrue(appleSlot != null)
        captureContext()
    }

    private fun captureContext() = contextBannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            subdir = outputSubdir,
            fileName = DAY_STUDY_CONTEXT_SCREENSHOT,
        ) {
            ScreenshotContent(
                locale = locale,
                fileName = DAY_STUDY_CONTEXT_SCREENSHOT,
            )
        }
    }

    @Test
    fun dayStudyQuestions() = questionsBannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            subdir = outputSubdir,
            fileName = DAY_STUDY_QUESTIONS_SCREENSHOT,
            // Opening the first question shows that the tab answers them, not just lists them. An
            // iOS capture was opened on the simulator already, so only the Play shots click.
            beforeCapture = { rule ->
                if (appleSlot == null) {
                    rule.onNodeWithText(runBlocking { getString(Res.string.ai_tab_questions) }).performClick()
                    rule.onNodeWithText(firstQuestion(locale)).performClick()
                }
            },
        ) {
            ScreenshotContent(
                locale = locale,
                fileName = DAY_STUDY_QUESTIONS_SCREENSHOT,
            )
        }
    }

    @Composable
    private fun ScreenshotContent(
        locale: String,
        fileName: String,
    ) {
        if (appleSlot == null) {
            DayStudyScreenshotContent(
                locale = locale,
                platform = Platform.Android,
                isWide = isWide,
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

internal class PhoneDayStudyScreenshots :
    DayStudyScreenshots(
        isWide = false,
        formFactor = FormFactor.Phone,
    )

internal class Tablet7DayStudyScreenshots :
    DayStudyScreenshots(
        isWide = false,
        formFactor = FormFactor.Tablet7,
    )

internal class Tablet10DayStudyScreenshots :
    DayStudyScreenshots(
        isWide = true,
        formFactor = FormFactor.Tablet10,
    )

internal class IPhone65DayStudyScreenshots :
    DayStudyScreenshots(
        isWide = false,
        appleSlot = AppleScreenshotSlot.IPHONE_6_5,
        formFactor = FormFactor.AppleIPhone65,
    )

internal class IPhone67DayStudyScreenshots :
    DayStudyScreenshots(
        isWide = false,
        appleSlot = AppleScreenshotSlot.IPHONE_6_7,
        formFactor = FormFactor.AppleIPhone67,
    )

internal class IPad13DayStudyScreenshots :
    DayStudyScreenshots(
        isWide = true,
        appleSlot = AppleScreenshotSlot.IPAD_13,
        formFactor = FormFactor.AppleIPad13,
    )

// The 11" slot: same bezel, a taller canvas, so Apple does not have to letterbox the 13" one. The
// screen inside is the same size, so it frames the 13" capture.
internal class IPad11DayStudyScreenshots :
    DayStudyScreenshots(
        isWide = true,
        outputSubdir = "ipad11",
        appleSlot = AppleScreenshotSlot.IPAD_13,
        formFactor = FormFactor.AppleIPad13,
        canvas = ScreenshotCanvas.px(1668, 2388),
    )

/** The README grid's study shot. See docs/store-listing-screenshots.md for the variant. */
internal class ReadmeDayStudyScreenshots :
    StoreScreenshotsTest(
        formFactor = FormFactor.Phone,
        style = ScreenshotStyle(edgeToEdge = false),
    ) {
    @Test
    fun study() = screenshot(
        backgroundColor = Color(BACKGROUND),
        subdir = README_SUBDIR,
        fileName = "study",
    ) {
        DayStudyScreenshotContent(
            locale = README_LOCALE,
            platform = Platform.Android,
            isWide = false,
            statusBarHeight = 0.dp,
        )
    }

    private companion object {
        const val README_SUBDIR = "readme"
        const val README_LOCALE = "en-US"
    }
}
