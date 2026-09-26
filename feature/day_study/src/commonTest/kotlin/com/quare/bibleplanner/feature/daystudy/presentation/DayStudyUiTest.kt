package com.quare.bibleplanner.feature.daystudy.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_chat_entry_title
import bibleplanner.feature.day_study.generated.resources.ai_context_title
import bibleplanner.feature.day_study.generated.resources.ai_questions_title
import bibleplanner.feature.day_study.generated.resources.ai_study_connection_error_message
import bibleplanner.feature.day_study.generated.resources.ai_study_error
import bibleplanner.feature.day_study.generated.resources.ai_study_exhausted_subtitle
import bibleplanner.feature.day_study.generated.resources.ai_study_generate
import bibleplanner.feature.day_study.generated.resources.ai_study_generating_title
import bibleplanner.feature.day_study.generated.resources.ai_study_generation_error_title
import bibleplanner.feature.day_study.generated.resources.ai_study_retry
import bibleplanner.feature.day_study.generated.resources.ai_study_subscribe
import bibleplanner.feature.day_study.generated.resources.ai_summary_takeaways
import bibleplanner.feature.day_study.generated.resources.ai_tab_context
import bibleplanner.feature.day_study.generated.resources.ai_tab_questions
import bibleplanner.feature.day_study.generated.resources.ai_tab_summary
import bibleplanner.ui.component.generated.resources.back
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.feature.daystudy.fixture.dayStudyModel
import com.quare.bibleplanner.feature.daystudy.fixture.dayStudyUiState
import com.quare.bibleplanner.feature.daystudy.presentation.component.titleRes
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardQuotaUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationError
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationPhase
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiEvent
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiState
import com.quare.bibleplanner.ui.testing.setUiTestContent
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import bibleplanner.ui.component.generated.resources.Res as ComponentRes

@OptIn(ExperimentalTestApi::class)
internal class DayStudyUiTest {
    private val study: DayStudyModel = dayStudyModel(LOCALE)
    private val openStudyUiState: DayStudyRouteUiState = dayStudyUiState(
        locale = LOCALE,
        platform = Platform.Android,
    )
    private val cardUiState: DayStudyRouteUiState = openStudyUiState.copy(openStudy = null)
    private lateinit var events: MutableList<DayStudyRouteUiEvent>
    private lateinit var backClicks: MutableList<Unit>

    @Test
    fun `GIVEN an open study WHEN rendered THEN the summary tab shows the chapter summaries and takeaways`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = openStudyUiState)

            // When
            waitForIdle()

            // Then
            onNodeWithText(getString(Res.string.ai_tab_summary)).assertIsSelected()
            onNodeWithText(study.chapterSummaries.first().title).assertIsDisplayed()
            onNodeWithText(study.chapterSummaries.first().body).assertIsDisplayed()
            onNodeWithText(getString(Res.string.ai_summary_takeaways))
                .performScrollTo()
                .assertIsDisplayed()
            assertEquals(expected = emptyList<DayStudyRouteUiEvent>(), actual = events)
        }

    @Test
    fun `GIVEN an open study WHEN clicking the context tab THEN shows the historical context and its facts`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = openStudyUiState)

            // When
            onNodeWithText(getString(Res.string.ai_tab_context)).performClick()

            // Then
            onNodeWithText(getString(Res.string.ai_tab_context)).assertIsSelected()
            onNodeWithText(getString(Res.string.ai_context_title)).assertIsDisplayed()
            study.context.facts.forEach { fact ->
                onNodeWithText(fact.label).assertIsDisplayed()
                onNodeWithText(fact.value).assertIsDisplayed()
            }
            onNodeWithText(study.chapterSummaries.first().title).assertIsNotDisplayed()
        }

    @Test
    fun `GIVEN an open study WHEN clicking the questions tab THEN lists the common questions`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = openStudyUiState)

        // When
        onNodeWithText(getString(Res.string.ai_tab_questions)).performClick()

        // Then
        onNodeWithText(getString(Res.string.ai_tab_questions)).assertIsSelected()
        onNodeWithText(getString(Res.string.ai_questions_title)).assertIsDisplayed()
        study.commonQuestions.forEach { question ->
            onNodeWithText(question.question).assertIsDisplayed()
            onNodeWithText(question.answer).assertDoesNotExist()
        }
    }

    @Test
    fun `GIVEN the questions tab WHEN clicking a question THEN reveals its answer`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = openStudyUiState)
        onNodeWithText(getString(Res.string.ai_tab_questions)).performClick()

        // When
        onNodeWithText(study.commonQuestions.first().question).performClick()

        // Then
        onNodeWithText(study.commonQuestions.first().answer).assertIsDisplayed()
    }

    @Test
    fun `GIVEN the questions tab WHEN clicking ask about this reading THEN emits OnAskAiClick`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = openStudyUiState)
        onNodeWithText(getString(Res.string.ai_tab_questions)).performClick()

        // When
        onNodeWithText(getString(Res.string.ai_chat_entry_title))
            .performScrollTo()
            .performClick()

        // Then
        assertEquals(
            expected = listOf<DayStudyRouteUiEvent>(DayStudyRouteUiEvent.OnAskAiClick),
            actual = events,
        )
    }

    @Test
    fun `GIVEN an open study WHEN clicking back THEN navigates back`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = openStudyUiState)

        // When
        onNodeWithContentDescription(getString(ComponentRes.string.back)).performClick()

        // Then
        assertEquals(expected = listOf(Unit), actual = backClicks)
    }

    @Test
    fun `GIVEN a study being generated WHEN rendered THEN shows the generating title and every phase`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                uiState = cardUiState.copy(
                    generation = DayStudyGenerationUiModel(currentPhaseIndex = GENERATING_PHASE_INDEX),
                ),
            )

            // When
            waitForIdle()

            // Then
            onNodeWithText(getString(Res.string.ai_study_generating_title)).assertIsDisplayed()
            DayStudyGenerationPhase.entries.forEach { phase ->
                onNodeWithText(getString(phase.titleRes)).assertIsDisplayed()
            }
            onNodeWithText(getString(Res.string.ai_tab_summary)).assertDoesNotExist()
        }

    @Test
    fun `GIVEN a generic generation error WHEN clicking try again THEN emits OnRetryClick`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = cardUiState.copy(generationError = DayStudyGenerationError.GENERIC))

        // When
        onNodeWithText(getString(Res.string.ai_study_retry)).performClick()

        // Then
        onNodeWithText(getString(Res.string.ai_study_generation_error_title)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.ai_study_error)).assertIsDisplayed()
        assertEquals(
            expected = listOf<DayStudyRouteUiEvent>(DayStudyRouteUiEvent.OnRetryClick),
            actual = events,
        )
    }

    @Test
    fun `GIVEN an offline generation error WHEN rendered THEN shows the connection error message`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = cardUiState.copy(generationError = DayStudyGenerationError.OFFLINE))

        // When
        waitForIdle()

        // Then
        onNodeWithText(getString(Res.string.ai_study_connection_error_message)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.ai_study_error)).assertDoesNotExist()
    }

    @Test
    fun `GIVEN a locked card WHEN clicking subscribe THEN shows the exhausted quota and emits OnCardClick`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                uiState = cardUiState.copy(
                    card = loadedCard(
                        mode = DayStudyCardMode.LOCKED,
                        remainingFree = 0,
                        isPro = false,
                    ),
                ),
            )

            // When
            onNodeWithText(getString(Res.string.ai_study_subscribe)).performClick()

            // Then
            onNodeWithText(getString(Res.string.ai_study_exhausted_subtitle, FREE_LIMIT)).assertIsDisplayed()
            assertEquals(
                expected = listOf<DayStudyRouteUiEvent>(DayStudyRouteUiEvent.OnCardClick),
                actual = events,
            )
        }

    @Test
    fun `GIVEN a card ready to generate WHEN clicking generate THEN emits OnCardClick`() = runComposeUiTest {
        // Given
        prepareScenario(
            uiState = cardUiState.copy(
                card = loadedCard(
                    mode = DayStudyCardMode.GENERATE,
                    remainingFree = FREE_LIMIT,
                    isPro = false,
                ),
            ),
        )

        // When
        onNodeWithText(getString(Res.string.ai_study_generate)).performClick()

        // Then
        assertEquals(
            expected = listOf<DayStudyRouteUiEvent>(DayStudyRouteUiEvent.OnCardClick),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a generated study not opened yet WHEN rendered THEN emits OnCardClick to open it`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = cardUiState)

        // When
        waitForIdle()

        // Then
        assertEquals(
            expected = listOf<DayStudyRouteUiEvent>(DayStudyRouteUiEvent.OnCardClick),
            actual = events,
        )
    }

    @Test
    fun `GIVEN the card still loading WHEN rendered THEN neither the hero nor the study is shown`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = cardUiState.copy(card = Loadable.Loading))

        // When
        waitForIdle()

        // Then
        onNodeWithText(getString(Res.string.ai_study_generate)).assertDoesNotExist()
        onNodeWithText(getString(Res.string.ai_tab_summary)).assertDoesNotExist()
        assertEquals(expected = emptyList<DayStudyRouteUiEvent>(), actual = events)
    }

    private fun loadedCard(
        mode: DayStudyCardMode,
        remainingFree: Int,
        isPro: Boolean,
    ): Loadable<DayStudyCardUiModel> = Loadable.Loaded(
        DayStudyCardUiModel(
            mode = mode,
            quota = Loadable.Loaded(
                DayStudyCardQuotaUiModel(
                    remainingFree = remainingFree,
                    freeLimit = FREE_LIMIT,
                ),
            ),
            isPro = isPro,
        ),
    )

    private fun ComposeUiTest.prepareScenario(uiState: DayStudyRouteUiState) {
        events = mutableListOf()
        backClicks = mutableListOf()
        setUiTestContent {
            DayStudyScreen(
                uiState = uiState,
                isWide = false,
                snackbarHostState = remember { SnackbarHostState() },
                onCardClick = { events += DayStudyRouteUiEvent.OnCardClick },
                onRetryClick = { events += DayStudyRouteUiEvent.OnRetryClick },
                onAskAiClick = { events += DayStudyRouteUiEvent.OnAskAiClick },
                onNavigateBack = { backClicks += Unit },
            )
        }
    }

    private companion object {
        const val LOCALE = "en-US"
        const val FREE_LIMIT = 3
        const val GENERATING_PHASE_INDEX = 1
    }
}
