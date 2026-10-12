package com.quare.bibleplanner.feature.read.presentation.daycompletion

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class ReadDayCompletionTest {
    private val completedDay = PlanDayLocationModel(
        weekNumber = 1,
        dayNumber = 2,
        readingPlanType = ReadingPlanType.CHRONOLOGICAL,
    )
    private val dayReadingCompleteCommand: NavigationCommand = NavigationCommand.Navigate(
        DayReadingCompleteNavRoute(
            dayNumber = 2,
            weekNumber = 1,
            readingPlanType = "CHRONOLOGICAL",
        ),
    )
    private val dialogSettings = StudySuggestionSettingsModel(
        isEnabled = true,
        mode = StudySuggestionMode.DIALOG,
    )
    private val bannerSettings = StudySuggestionSettingsModel(
        isEnabled = true,
        mode = StudySuggestionMode.BANNER,
    )

    private lateinit var dayCompletion: ReadDayCompletion
    private lateinit var commands: List<NavigationCommand>
    private lateinit var lookedUpChapters: List<ChapterLocationModel>

    @Test
    fun `GIVEN the suggestion settings not loaded yet WHEN presenting a day THEN opens the day-complete sheet`() =
        runTest {
            // Given
            prepareScenario(studySuggestionSettings = emptyFlow())

            // When
            dayCompletion.present(completedDay)
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(dayReadingCompleteCommand),
                actual = commands,
            )
            assertNull(dayCompletion.banner.value)
        }

    @Test
    fun `GIVEN the suggestion disabled WHEN presenting a day THEN keeps quiet about the finished day`() = runTest {
        // Given
        prepareScenario(
            studySuggestionSettings = flowOf(
                StudySuggestionSettingsModel(
                    isEnabled = false,
                    mode = StudySuggestionMode.BANNER,
                ),
            ),
        )

        // When
        dayCompletion.present(completedDay)
        runCurrent()

        // Then
        assertTrue(commands.isEmpty())
        assertNull(dayCompletion.banner.value)
    }

    @Test
    fun `GIVEN the suggestion in banner mode WHEN presenting a day THEN shows the banner instead of the sheet`() =
        runTest {
            // Given
            prepareScenario(studySuggestionSettings = flowOf(bannerSettings))

            // When
            dayCompletion.present(completedDay)
            runCurrent()

            // Then
            assertEquals(
                expected = completedDay,
                actual = dayCompletion.banner.value,
            )
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN the suggestion in dialog mode WHEN presenting a day THEN opens the day-complete sheet`() = runTest {
        // Given
        prepareScenario(studySuggestionSettings = flowOf(dialogSettings))

        // When
        dayCompletion.present(completedDay)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(dayReadingCompleteCommand),
            actual = commands,
        )
        assertNull(dayCompletion.banner.value)
    }

    @Test
    fun `GIVEN a shown banner WHEN dismissing it THEN clears it`() = runTest {
        // Given
        prepareScenario(studySuggestionSettings = flowOf(bannerSettings))
        dayCompletion.present(completedDay)

        // When
        dayCompletion.dismissBanner()

        // Then
        assertNull(dayCompletion.banner.value)
    }

    @Test
    fun `GIVEN a chapter that completes a day WHEN checking it THEN looks the day up by chapter and presents it`() =
        runTest {
            // Given
            prepareScenario(
                studySuggestionSettings = flowOf(dialogSettings),
                completedDayForChapter = completedDay,
            )

            // When
            dayCompletion.checkChapter(
                bookId = BookId.GEN,
                chapterNumber = 3,
            )
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(
                    ChapterLocationModel(
                        bookId = BookId.GEN,
                        chapterNumber = 3,
                    ),
                ),
                actual = lookedUpChapters,
            )
            assertEquals(
                expected = listOf(dayReadingCompleteCommand),
                actual = commands,
            )
        }

    @Test
    fun `GIVEN a chapter that completes no day WHEN checking it THEN presents nothing`() = runTest {
        // Given
        prepareScenario(
            studySuggestionSettings = flowOf(bannerSettings),
            completedDayForChapter = null,
        )

        // When
        dayCompletion.checkChapter(
            bookId = BookId.GEN,
            chapterNumber = 3,
        )
        runCurrent()

        // Then
        assertTrue(commands.isEmpty())
        assertNull(dayCompletion.banner.value)
    }

    private fun TestScope.prepareScenario(
        studySuggestionSettings: Flow<StudySuggestionSettingsModel>,
        completedDayForChapter: PlanDayLocationModel? = null,
    ) {
        val recordedLookups = mutableListOf<ChapterLocationModel>()
        lookedUpChapters = recordedLookups
        val navigator = Navigator()
        dayCompletion = ReadDayCompletionFactory(
            navigator = navigator,
            getCompletedDayForChapter = { bookId, chapterNumber ->
                recordedLookups += ChapterLocationModel(
                    bookId = bookId,
                    chapterNumber = chapterNumber,
                )
                completedDayForChapter
            },
            observeStudySuggestionSettings = { studySuggestionSettings },
        ).create(backgroundScope)
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { command -> collected += command } }
        }
        runCurrent()
    }
}
