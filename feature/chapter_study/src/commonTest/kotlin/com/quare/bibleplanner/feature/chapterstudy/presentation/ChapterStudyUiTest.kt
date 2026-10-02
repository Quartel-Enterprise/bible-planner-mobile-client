package com.quare.bibleplanner.feature.chapterstudy.presentation

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import bibleplanner.feature.chapter_study.generated.resources.Res
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_chat
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_phase_context
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_phase_questions
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_phase_reading
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_phase_summary
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_section_context
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_section_cross_references
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_section_key_verse
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_section_outline
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_section_people_and_places
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_section_reflection
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_section_summary
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_share
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_title
import bibleplanner.ui.component.generated.resources.ai_disclaimer
import bibleplanner.ui.component.generated.resources.ai_study_connection_error_message
import bibleplanner.ui.component.generated.resources.ai_study_error
import bibleplanner.ui.component.generated.resources.ai_study_generating_title
import bibleplanner.ui.component.generated.resources.ai_study_retry
import bibleplanner.ui.component.generated.resources.back
import com.quare.bibleplanner.core.books.util.getVerseReferenceLabel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyModel
import com.quare.bibleplanner.core.chapterstudy.testing.createChapterStudy
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyContentUiState
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiEvent
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiState
import com.quare.bibleplanner.ui.testing.setUiTestContent
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import bibleplanner.ui.component.generated.resources.Res as ComponentRes

@OptIn(ExperimentalTestApi::class)
internal class ChapterStudyUiTest {
    private val study: ChapterStudyModel = createChapterStudy()
    private val loadedContent = ChapterStudyContentUiState.Loaded(
        study = study,
        keyVerseText = KEY_VERSE_TEXT,
    )
    private lateinit var events: MutableList<ChapterStudyUiEvent>
    private lateinit var backClicks: MutableList<Unit>

    @Test
    fun `GIVEN a study WHEN rendered THEN shows every section and the chapter it is about`() = runComposeUiTest {
        // Given
        prepareScenario(content = loadedContent)

        // When
        waitForIdle()

        // Then
        onNodeWithText(getString(Res.string.chapter_study_title)).assertIsDisplayed()
        onNodeWithText(chapterLabel()).assertIsDisplayed()
        onNodeWithText(getString(Res.string.chapter_study_section_summary)).assertIsDisplayed()
        onNodeWithText(study.summary).assertIsDisplayed()
        onNodeWithText(getString(Res.string.chapter_study_section_context)).assertIsDisplayed()
        onNodeWithText(study.context).assertIsDisplayed()
        onNodeWithText(getString(Res.string.chapter_study_section_outline)).performScrollTo().assertIsDisplayed()
        onNodeWithText(study.outline.first().title).performScrollTo().assertIsDisplayed()
        onNodeWithText("1-7").assertIsDisplayed()
        onNodeWithText(getString(Res.string.chapter_study_section_people_and_places))
            .performScrollTo()
            .assertIsDisplayed()
        study.peopleAndPlaces.forEach { name -> onNodeWithText(name).performScrollTo().assertIsDisplayed() }
        onNodeWithText(getString(Res.string.chapter_study_section_key_verse, "3:15"))
            .performScrollTo()
            .assertIsDisplayed()
        onNodeWithText(KEY_VERSE_TEXT).performScrollTo().assertIsDisplayed()
        onNodeWithText(getString(Res.string.chapter_study_section_cross_references))
            .performScrollTo()
            .assertIsDisplayed()
        onNodeWithText(crossReferenceLabel()).performScrollTo().assertIsDisplayed()
        onNodeWithText(getString(Res.string.chapter_study_section_reflection)).performScrollTo().assertIsDisplayed()
        onNodeWithText(study.reflectionQuestions.first()).performScrollTo().assertIsDisplayed()
        onNodeWithText(getString(ComponentRes.string.ai_disclaimer)).performScrollTo().assertIsDisplayed()
        assertEquals(expected = emptyList<ChapterStudyUiEvent>(), actual = events)
    }

    @Test
    fun `GIVEN a study with only a summary and a context WHEN rendered THEN leaves the empty sections out`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                content = ChapterStudyContentUiState.Loaded(
                    study = study.copy(
                        outline = emptyList(),
                        peopleAndPlaces = emptyList(),
                        keyVerse = null,
                        crossReferences = emptyList(),
                        reflectionQuestions = emptyList(),
                    ),
                    keyVerseText = null,
                ),
            )

            // When
            waitForIdle()

            // Then
            onNodeWithText(study.summary).assertIsDisplayed()
            onNodeWithText(getString(Res.string.chapter_study_section_outline)).assertDoesNotExist()
            onNodeWithText(getString(Res.string.chapter_study_section_people_and_places)).assertDoesNotExist()
            onNodeWithText(getString(Res.string.chapter_study_share)).assertDoesNotExist()
            onNodeWithText(getString(Res.string.chapter_study_section_cross_references)).assertDoesNotExist()
            onNodeWithText(getString(Res.string.chapter_study_section_reflection)).assertDoesNotExist()
        }

    @Test
    fun `GIVEN a study WHEN clicking an outline section THEN emits OnOutlineSectionClick`() = runComposeUiTest {
        // Given
        prepareScenario(content = loadedContent)

        // When
        onNodeWithText(study.outline.first().title).performScrollTo().performClick()

        // Then
        assertEquals(
            expected = listOf<ChapterStudyUiEvent>(ChapterStudyUiEvent.OnOutlineSectionClick(study.outline.first())),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a study WHEN clicking share on the key verse THEN emits OnShareKeyVerseClick`() = runComposeUiTest {
        // Given
        prepareScenario(content = loadedContent)

        // When
        onNodeWithText(getString(Res.string.chapter_study_share)).performScrollTo().performClick()

        // Then
        assertEquals(
            expected = listOf<ChapterStudyUiEvent>(ChapterStudyUiEvent.OnShareKeyVerseClick),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a study WHEN clicking a cross reference THEN emits OnCrossReferenceClick`() = runComposeUiTest {
        // Given
        prepareScenario(content = loadedContent)

        // When
        onNodeWithText(crossReferenceLabel()).performScrollTo().performClick()

        // Then
        assertEquals(
            expected = listOf<ChapterStudyUiEvent>(
                ChapterStudyUiEvent.OnCrossReferenceClick(study.crossReferences.first()),
            ),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a study WHEN clicking chat THEN emits OnAskAiClick`() = runComposeUiTest {
        // Given
        prepareScenario(content = loadedContent)

        // When
        onNodeWithContentDescription(getString(Res.string.chapter_study_chat)).performClick()

        // Then
        assertEquals(expected = listOf<ChapterStudyUiEvent>(ChapterStudyUiEvent.OnAskAiClick), actual = events)
    }

    @Test
    fun `GIVEN a study being generated WHEN rendered THEN shows the steps and no chat button`() = runComposeUiTest {
        // Given
        prepareScenario(content = ChapterStudyContentUiState.Generating(currentPhaseIndex = 1))

        // When
        waitForIdle()

        // Then
        onNodeWithText(getString(ComponentRes.string.ai_study_generating_title)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.chapter_study_phase_reading, chapterLabel())).assertIsDisplayed()
        onNodeWithText(getString(Res.string.chapter_study_phase_summary)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.chapter_study_phase_context)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.chapter_study_phase_questions)).assertIsDisplayed()
        onNodeWithContentDescription(getString(Res.string.chapter_study_chat)).assertDoesNotExist()
    }

    @Test
    fun `GIVEN a failed generation WHEN clicking retry THEN emits OnRetryClick`() = runComposeUiTest {
        // Given
        prepareScenario(content = ChapterStudyContentUiState.Failed(isOffline = false))

        // When
        onNodeWithText(getString(ComponentRes.string.ai_study_retry)).performClick()

        // Then
        onNodeWithText(getString(ComponentRes.string.ai_study_error)).assertIsDisplayed()
        assertEquals(expected = listOf<ChapterStudyUiEvent>(ChapterStudyUiEvent.OnRetryClick), actual = events)
    }

    @Test
    fun `GIVEN a generation that failed offline WHEN rendered THEN blames the connection`() = runComposeUiTest {
        // Given
        prepareScenario(content = ChapterStudyContentUiState.Failed(isOffline = true))

        // When
        waitForIdle()

        // Then
        onNodeWithText(getString(ComponentRes.string.ai_study_connection_error_message)).assertIsDisplayed()
        onNodeWithText(getString(ComponentRes.string.ai_study_error)).assertDoesNotExist()
    }

    @Test
    fun `GIVEN the study still loading WHEN clicking back THEN calls the back callback`() = runComposeUiTest {
        // Given
        prepareScenario(content = ChapterStudyContentUiState.Loading)

        // When
        onNodeWithContentDescription(getString(ComponentRes.string.back)).performClick()

        // Then
        assertEquals(expected = listOf(Unit), actual = backClicks)
        onNodeWithText(study.summary).assertDoesNotExist()
        assertEquals(expected = emptyList<ChapterStudyUiEvent>(), actual = events)
    }

    private suspend fun chapterLabel(): String = getVerseReferenceLabel(
        bookId = BookId.GEN,
        chapterNumber = CHAPTER_NUMBER,
        verseNumbers = emptyList(),
    )

    private suspend fun crossReferenceLabel(): String {
        val reference = study.crossReferences.first()
        return getVerseReferenceLabel(
            bookId = reference.bookId,
            chapterNumber = reference.chapterNumber,
            verseNumbers = (reference.startVerse..reference.endVerse).toList(),
        )
    }

    private fun ComposeUiTest.prepareScenario(content: ChapterStudyContentUiState) {
        events = mutableListOf()
        backClicks = mutableListOf()
        setUiTestContent {
            ChapterStudyScreen(
                uiState = ChapterStudyUiState(
                    bookId = BookId.GEN,
                    chapterNumber = CHAPTER_NUMBER,
                    platform = Platform.Android,
                    content = content,
                ),
                onEvent = { events += it },
                onNavigateBack = { backClicks += Unit },
            )
        }
    }

    private companion object {
        const val CHAPTER_NUMBER = 3
        const val KEY_VERSE_TEXT = "I will put hostility between you and the woman."
    }
}
