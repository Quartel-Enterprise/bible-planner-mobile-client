package com.quare.bibleplanner.feature.daystudy.screenshots

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_tab_context
import bibleplanner.feature.day_study.generated.resources.ai_tab_questions
import com.quare.bibleplanner.feature.daystudy.presentation.DayStudyScreen
import com.quare.bibleplanner.ui.theme.AppTheme
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Test

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

private const val BACKGROUND = 0xFF1B1B1F

internal abstract class DayStudyScreenshots(
    formFactor: FormFactor,
    private val isWide: Boolean,
) : StoreScreenshotsTest(
        formFactor = formFactor,
        style = ScreenshotStyle(edgeToEdge = false),
    ) {
    @Test
    fun dayStudy() = bannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            fileName = "04_day_study",
        ) {
            AppTheme {
                // The wide layout is a bare pane with no Scaffold, so without a Surface the
                // frame's bezel shows through behind the text.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    DayStudyScreen(
                        uiState = dayStudyUiState(locale),
                        isWide = isWide,
                        snackbarHostState = SnackbarHostState(),
                        onCardClick = {},
                        onRetryClick = {},
                        onNavigateBack = {},
                    )
                }
            }
        }
    }

    @Test
    fun dayStudyContext() = contextBannerCopy.forEach { (locale, copy) ->
        val (title, description) = copy
        screenshot(
            locales = listOf(locale),
            title = title,
            description = description,
            backgroundColor = Color(BACKGROUND),
            fileName = "07_day_study_context",
            // The tab label is resolved rather than hard-coded so the click keeps working in
            // every locale, and keeps working if the wording changes.
            beforeCapture = { rule ->
                rule.onNodeWithText(runBlocking { getString(Res.string.ai_tab_context) }).performClick()
            },
        ) {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    DayStudyScreen(
                        uiState = dayStudyUiState(locale),
                        isWide = isWide,
                        snackbarHostState = SnackbarHostState(),
                        onCardClick = {},
                        onRetryClick = {},
                        onNavigateBack = {},
                    )
                }
            }
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
            fileName = "08_day_study_questions",
            // Opening the first question shows that the tab answers them, not just lists them.
            beforeCapture = { rule ->
                rule.onNodeWithText(runBlocking { getString(Res.string.ai_tab_questions) }).performClick()
                rule.onNodeWithText(firstQuestion(locale)).performClick()
            },
        ) {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    DayStudyScreen(
                        uiState = dayStudyUiState(locale),
                        isWide = isWide,
                        snackbarHostState = SnackbarHostState(),
                        onCardClick = {},
                        onRetryClick = {},
                        onNavigateBack = {},
                    )
                }
            }
        }
    }
}

internal class PhoneDayStudyScreenshots :
    DayStudyScreenshots(
        formFactor = FormFactor.Phone,
        isWide = false,
    )

internal class Tablet7DayStudyScreenshots :
    DayStudyScreenshots(
        formFactor = FormFactor.Tablet7,
        isWide = false,
    )

internal class Tablet10DayStudyScreenshots :
    DayStudyScreenshots(
        formFactor = FormFactor.Tablet10,
        isWide = true,
    )

internal class IPhone65DayStudyScreenshots :
    DayStudyScreenshots(
        formFactor = FormFactor.AppleIPhone65,
        isWide = false,
    )

internal class IPhone67DayStudyScreenshots :
    DayStudyScreenshots(
        formFactor = FormFactor.AppleIPhone67,
        isWide = false,
    )

internal class IPadDayStudyScreenshots :
    DayStudyScreenshots(
        formFactor = FormFactor.AppleIPad13,
        isWide = true,
    )
