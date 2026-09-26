package com.quare.bibleplanner.feature.readingplan.presentation.viewmodel

import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import com.quare.bibleplanner.core.model.plan.PlansModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.review.domain.model.ReviewTrigger
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage
import com.quare.bibleplanner.feature.readingplan.domain.tracker.BibleProgressMilestoneTracker
import com.quare.bibleplanner.feature.readingplan.domain.tracker.ReadingStreakMilestoneTracker
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.FindFirstWeekWithUnreadBookUseCase
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.ResolvePlanStatusUseCase
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.day
import com.quare.bibleplanner.feature.readingplan.fake.FakeBooksRepository
import com.quare.bibleplanner.feature.readingplan.presentation.factory.ReadingPlanStateFactory
import com.quare.bibleplanner.feature.readingplan.presentation.mapper.DeleteProgressMapper
import com.quare.bibleplanner.feature.readingplan.presentation.mapper.WeeksPlanPresentationMapper
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOption
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiAction
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class ReadingPlanViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val daysPerWeek = 7
    private val versesPerBook = 100
    private lateinit var viewModel: ReadingPlanViewModel
    private lateinit var plans: MutableSharedFlow<PlansModel>
    private lateinit var selectedPlan: MutableStateFlow<ReadingPlanType>
    private lateinit var books: MutableStateFlow<List<BookDataModel>>
    private lateinit var commands: List<NavigationCommand>
    private lateinit var actions: List<ReadingPlanUiAction>
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>
    private lateinit var reviewTriggers: List<ReviewTrigger>
    private lateinit var selectedPlans: List<ReadingPlanType>
    private lateinit var dayReadUpdates: List<List<Any>>
    private var loginNudges = 0

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN the plans are not loaded yet WHEN observing the state THEN it is loading the chronological plan`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(initialPlans = null)

            // When
            val state = viewModel.uiState.value

            // Then
            assertIs<ReadingPlanUiState.Loading>(state)
            assertEquals(
                expected = ReadingPlanType.CHRONOLOGICAL,
                actual = state.selectedReadingPlan,
            )
        }

    @Test
    fun `GIVEN the plans are not loaded yet WHEN another plan is selected THEN keeps loading with that plan`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(initialPlans = null)

            // When
            selectedPlan.value = ReadingPlanType.BOOKS

            // Then
            val state = assertIs<ReadingPlanUiState.Loading>(viewModel.uiState.value)
            assertEquals(
                expected = ReadingPlanType.BOOKS,
                actual = state.selectedReadingPlan,
            )
        }

    @Test
    fun `GIVEN the plans are not loaded yet WHEN the progress changes THEN keeps loading`() = runTest(testDispatcher) {
        // Given
        prepareScenario(initialPlans = null)

        // When
        books.value = booksWithReadVerses(10)

        // Then
        assertIs<ReadingPlanUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun `GIVEN plans with read days WHEN they load THEN expands and scrolls to the first unread week`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(initialPlans = plans(readDays = 9))

            // When
            val state = loaded()

            // Then
            assertEquals(
                expected = listOf(1, 2, 3),
                actual = state.weekPlans.map { it.weekPlan.number },
            )
            assertEquals(
                expected = listOf(2),
                actual = expandedWeekNumbers(),
            )
            assertEquals(
                expected = 2,
                actual = state.scrollToWeekNumber,
            )
            assertTrue(state.scrollToWeekIsAutomatic)
            assertEquals(
                expected = 9,
                actual = state.readDaysCount,
            )
            assertEquals(
                expected = 21,
                actual = state.totalDaysCount,
            )
        }

    @Test
    fun `GIVEN a completed plan WHEN it loads THEN does not scroll anywhere`() = runTest(testDispatcher) {
        // Given
        prepareScenario(initialPlans = plans(readDays = 21))

        // When
        val state = loaded()

        // Then
        assertEquals(
            expected = 0,
            actual = state.scrollToWeekNumber,
        )
        assertTrue(expandedWeekNumbers().isEmpty())
    }

    @Test
    fun `GIVEN the first load happened WHEN the plans change THEN does not scroll again`() = runTest(testDispatcher) {
        // Given
        prepareScenario(initialPlans = plans(readDays = 0))
        viewModel.onEvent(ReadingPlanUiEvent.OnScrollToWeekCompleted)

        // When
        plans.emit(plans(readDays = 9))

        // Then
        assertEquals(
            expected = 0,
            actual = loaded().scrollToWeekNumber,
        )
        assertEquals(
            expected = listOf(1),
            actual = expandedWeekNumbers(),
        )
    }

    @Test
    fun `GIVEN progress in the plan WHEN all the progress is reset THEN only the first week stays expanded`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(initialPlans = plans(readDays = 9))
            viewModel.onEvent(ReadingPlanUiEvent.OnWeekExpandClick(weekNumber = 3))

            // When
            plans.emit(plans(readDays = 0))

            // Then
            assertEquals(
                expected = listOf(1),
                actual = expandedWeekNumbers(),
            )
        }

    @Test
    fun `GIVEN the loaded plans WHEN the books plan is selected THEN shows the books order weeks`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            selectedPlan.value = ReadingPlanType.BOOKS

            // Then
            val state = loaded()
            assertEquals(
                expected = ReadingPlanType.BOOKS,
                actual = state.selectedReadingPlan,
            )
            assertEquals(
                expected = 2,
                actual = state.weekPlans.size,
            )
        }

    @Test
    fun `GIVEN the loaded plans WHEN the bible progress grows THEN shows the progress and its motivation`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            books.value = booksWithReadVerses(10)

            // Then
            val state = loaded()
            assertEquals(
                expected = 10f,
                actual = state.progress,
            )
            assertEquals(
                expected = PlanMotivationMessage.OverallProgress.EarlyStart,
                actual = state.motivationMessage,
            )
        }

    @Test
    fun `GIVEN the bible progress below a milestone WHEN it crosses the milestone THEN asks for a review`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            books.value = booksWithReadVerses(30)

            // Then
            assertEquals(
                expected = listOf(ReviewTrigger.PROGRESS_MILESTONE),
                actual = reviewTriggers,
            )
            assertTrue(trackedEvents.contains("bible_progress_milestone" to mapOf<String, Any>("percent" to 25)))
        }

    @Test
    fun `GIVEN a six day streak WHEN the seventh day is read THEN asks for a review`() = runTest(testDispatcher) {
        // Given
        prepareScenario(initialPlans = plans(readDays = 6))

        // When
        plans.emit(plans(readDays = 7))

        // Then
        assertEquals(
            expected = listOf(ReviewTrigger.STREAK_MILESTONE),
            actual = reviewTriggers,
        )
    }

    @Test
    fun `GIVEN the chronological plan WHEN picking the books plan THEN selects it and tracks the change`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            viewModel.onEvent(ReadingPlanUiEvent.OnOrderMenuClick)

            // When
            viewModel.onEvent(ReadingPlanUiEvent.OnPlanClick(ReadingPlanType.BOOKS))

            // Then
            assertEquals(
                expected = listOf(ReadingPlanType.BOOKS),
                actual = selectedPlans,
            )
            assertTrue(trackedEvents.contains("plan_selected" to mapOf<String, Any>("plan_type" to "books")))
            assertFalse(loaded().isShowingOrderMenu)
        }

    @Test
    fun `GIVEN the chronological plan WHEN picking it again THEN does not track a plan change`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(ReadingPlanUiEvent.OnPlanClick(ReadingPlanType.CHRONOLOGICAL))

            // Then
            assertEquals(
                expected = listOf(ReadingPlanType.CHRONOLOGICAL),
                actual = selectedPlans,
            )
            assertTrue(trackedEvents.none { (name, _) -> name == "plan_selected" })
        }

    @Test
    fun `GIVEN a collapsed week WHEN tapping it THEN expands it and tracks the expansion`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ReadingPlanUiEvent.OnWeekExpandClick(weekNumber = 2))

        // Then
        assertEquals(
            expected = listOf(1, 2),
            actual = expandedWeekNumbers(),
        )
        assertEquals(
            expected = listOf("plan_week_toggled" to mapOf<String, Any>("week_number" to 2, "is_expanded" to true)),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN an expanded week WHEN tapping it THEN collapses it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ReadingPlanUiEvent.OnWeekExpandClick(weekNumber = 1))

        // Then
        assertTrue(expandedWeekNumbers().isEmpty())
        assertEquals(
            expected = listOf("plan_week_toggled" to mapOf<String, Any>("week_number" to 1, "is_expanded" to false)),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN an unread day WHEN marking it read THEN shows it read at once and persists it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(
                ReadingPlanUiEvent.OnDayReadClick(
                    dayNumber = 1,
                    weekNumber = 1,
                ),
            )

            // Then
            assertTrue(isDayRead(weekNumber = 1, dayNumber = 1))
            assertEquals(
                expected = listOf(listOf<Any>(1, 1, true, ReadingPlanType.CHRONOLOGICAL)),
                actual = dayReadUpdates,
            )
            assertEquals(
                expected = 1,
                actual = loginNudges,
            )
            assertEquals(
                expected = listOf(
                    "day_read_toggled" to mapOf<String, Any>(
                        "plan_type" to "chronological",
                        "week_number" to 1,
                        "day_number" to 1,
                        "is_read" to true,
                        "source" to "plan_list",
                    ),
                ),
                actual = trackedEvents,
            )
        }

    @Test
    fun `GIVEN a day just marked read WHEN a stale emission arrives THEN keeps showing it read`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            viewModel.onEvent(
                ReadingPlanUiEvent.OnDayReadClick(
                    dayNumber = 1,
                    weekNumber = 1,
                ),
            )

            // When
            plans.emit(plans(readDays = 0))

            // Then
            assertTrue(isDayRead(weekNumber = 1, dayNumber = 1))
        }

    @Test
    fun `GIVEN a confirmed read day WHEN it is later unmarked elsewhere THEN shows it unread`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            viewModel.onEvent(
                ReadingPlanUiEvent.OnDayReadClick(
                    dayNumber = 1,
                    weekNumber = 1,
                ),
            )
            plans.emit(plans(readDays = 1))

            // When
            plans.emit(plans(readDays = 0))

            // Then
            assertFalse(isDayRead(weekNumber = 1, dayNumber = 1))
        }

    @Test
    fun `GIVEN a read day WHEN unmarking it THEN shows it unread and persists it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(initialPlans = plans(readDays = 3))

        // When
        viewModel.onEvent(
            ReadingPlanUiEvent.OnDayReadClick(
                dayNumber = 3,
                weekNumber = 1,
            ),
        )

        // Then
        assertFalse(isDayRead(weekNumber = 1, dayNumber = 3))
        assertEquals(
            expected = listOf(listOf<Any>(1, 3, false, ReadingPlanType.CHRONOLOGICAL)),
            actual = dayReadUpdates,
        )
    }

    @Test
    fun `GIVEN the last unread day of the current week WHEN marking it read THEN focuses the next week`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(initialPlans = plans(readDays = 6))

            // When
            viewModel.onEvent(
                ReadingPlanUiEvent.OnDayReadClick(
                    dayNumber = 7,
                    weekNumber = 1,
                ),
            )

            // Then
            assertEquals(
                expected = listOf(2),
                actual = expandedWeekNumbers(),
            )
        }

    @Test
    fun `GIVEN more unread days in the current week WHEN marking one read THEN keeps the week expanded`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(initialPlans = plans(readDays = 2))

            // When
            viewModel.onEvent(
                ReadingPlanUiEvent.OnDayReadClick(
                    dayNumber = 3,
                    weekNumber = 1,
                ),
            )

            // Then
            assertEquals(
                expected = listOf(1),
                actual = expandedWeekNumbers(),
            )
        }

    @Test
    fun `GIVEN the books plan WHEN marking a day read THEN persists it for the books plan`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        selectedPlan.value = ReadingPlanType.BOOKS

        // When
        viewModel.onEvent(
            ReadingPlanUiEvent.OnDayReadClick(
                dayNumber = 2,
                weekNumber = 2,
            ),
        )

        // Then
        assertTrue(isDayRead(weekNumber = 2, dayNumber = 2))
        assertEquals(
            expected = listOf(listOf<Any>(2, 2, true, ReadingPlanType.BOOKS)),
            actual = dayReadUpdates,
        )
    }

    @Test
    fun `GIVEN the plans are loading WHEN marking a day read THEN ignores it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(initialPlans = null)

        // When
        viewModel.onEvent(
            ReadingPlanUiEvent.OnDayReadClick(
                dayNumber = 1,
                weekNumber = 1,
            ),
        )

        // Then
        assertTrue(dayReadUpdates.isEmpty())
    }

    @Test
    fun `GIVEN the loaded plans WHEN marking a day outside the plan THEN ignores it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(
            ReadingPlanUiEvent.OnDayReadClick(
                dayNumber = 1,
                weekNumber = 99,
            ),
        )

        // Then
        assertTrue(dayReadUpdates.isEmpty())
    }

    @Test
    fun `GIVEN the loaded plans WHEN tapping a day THEN opens that day of the selected plan`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(
                ReadingPlanUiEvent.OnDayClick(
                    dayNumber = 4,
                    weekNumber = 2,
                ),
            )

            // Then
            assertEquals(
                expected = listOf<NavigationCommand>(
                    NavigationCommand.Navigate(
                        DayNavRoute(
                            dayNumber = 4,
                            weekNumber = 2,
                            readingPlanType = "CHRONOLOGICAL",
                        ),
                    ),
                ),
                actual = commands,
            )
        }

    @Test
    fun `GIVEN the loaded plans WHEN tapping edit plan THEN opens the start date editor`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ReadingPlanUiEvent.OnEditPlanClick)

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(NavigationCommand.Navigate(EditPlanStartDateNavRoute)),
            actual = commands,
        )
    }

    @Test
    fun `GIVEN the loaded plans WHEN opening and dismissing the menus THEN toggles their visibility`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            viewModel.onEvent(ReadingPlanUiEvent.OnOverflowClick)
            viewModel.onEvent(ReadingPlanUiEvent.OnOrderMenuClick)
            val openedState = loaded()

            // When
            viewModel.onEvent(ReadingPlanUiEvent.OnOverflowDismiss)
            viewModel.onEvent(ReadingPlanUiEvent.OnOrderMenuDismiss)

            // Then
            assertTrue(openedState.isShowingMenu)
            assertTrue(openedState.isShowingOrderMenu)
            assertFalse(loaded().isShowingMenu)
            assertFalse(loaded().isShowingOrderMenu)
        }

    @Test
    fun `GIVEN the plans are loading WHEN opening the menus THEN shows them`() = runTest(testDispatcher) {
        // Given
        prepareScenario(initialPlans = null)

        // When
        viewModel.onEvent(ReadingPlanUiEvent.OnOverflowClick)
        viewModel.onEvent(ReadingPlanUiEvent.OnOrderMenuClick)

        // Then
        val state = assertIs<ReadingPlanUiState.Loading>(viewModel.uiState.value)
        assertTrue(state.isShowingMenu)
        assertTrue(state.isShowingOrderMenu)
    }

    @Test
    fun `GIVEN the open overflow menu WHEN picking edit start day THEN closes the menu and opens the editor`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            viewModel.onEvent(ReadingPlanUiEvent.OnOverflowClick)

            // When
            viewModel.onEvent(ReadingPlanUiEvent.OnOverflowOptionClick(OverflowOption.EDIT_START_DAY))

            // Then
            assertFalse(loaded().isShowingMenu)
            assertEquals(
                expected = listOf<NavigationCommand>(NavigationCommand.Navigate(EditPlanStartDateNavRoute)),
                actual = commands,
            )
        }

    @Test
    fun `GIVEN read days WHEN picking delete progress THEN opens the delete confirmation`() = runTest(testDispatcher) {
        // Given
        prepareScenario(initialPlans = plans(readDays = 1))

        // When
        viewModel.onEvent(ReadingPlanUiEvent.OnOverflowOptionClick(OverflowOption.DELETE_PROGRESS))

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(NavigationCommand.Navigate(DeleteAllProgressNavRoute)),
            actual = commands,
        )
    }

    @Test
    fun `GIVEN no read days WHEN picking delete progress THEN tells there is nothing to delete`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(initialPlans = plans(readDays = 0))

            // When
            viewModel.onEvent(ReadingPlanUiEvent.OnOverflowOptionClick(OverflowOption.DELETE_PROGRESS))

            // Then
            assertEquals(
                expected = listOf<ReadingPlanUiAction>(ReadingPlanUiAction.ShowNoProgressToDelete),
                actual = actions,
            )
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN the plans are loading WHEN picking delete progress THEN does nothing`() = runTest(testDispatcher) {
        // Given
        prepareScenario(initialPlans = null)

        // When
        viewModel.onEvent(ReadingPlanUiEvent.OnOverflowOptionClick(OverflowOption.DELETE_PROGRESS))

        // Then
        assertTrue(actions.isEmpty())
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN the collapsed upcoming group WHEN toggling it THEN expands it and tracks the toggle`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(ReadingPlanUiEvent.OnToggleUpcomingExpanded)

            // Then
            assertTrue(loaded().upcomingExpanded)
            assertEquals(
                expected = listOf(
                    "plan_group_toggled" to mapOf<String, Any>("group" to "upcoming", "is_expanded" to true),
                ),
                actual = trackedEvents,
            )
        }

    @Test
    fun `GIVEN the collapsed completed group WHEN toggling it THEN expands it and tracks the toggle`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(ReadingPlanUiEvent.OnToggleCompletedExpanded)

            // Then
            assertTrue(loaded().completedExpanded)
            assertEquals(
                expected = listOf(
                    "plan_group_toggled" to mapOf<String, Any>("group" to "completed", "is_expanded" to true),
                ),
                actual = trackedEvents,
            )
        }

    @Test
    fun `GIVEN the plans are loading WHEN toggling the groups THEN tracks nothing`() = runTest(testDispatcher) {
        // Given
        prepareScenario(initialPlans = null)

        // When
        viewModel.onEvent(ReadingPlanUiEvent.OnToggleUpcomingExpanded)
        viewModel.onEvent(ReadingPlanUiEvent.OnToggleCompletedExpanded)

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a next day to read WHEN going to the active row THEN scrolls and flashes that day`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(initialPlans = plans(readDays = 9))

            // When
            viewModel.onEvent(ReadingPlanUiEvent.OnGoToActiveRowClick)

            // Then
            val state = loaded()
            assertEquals(
                expected = 2,
                actual = state.scrollToWeekNumber,
            )
            assertFalse(state.scrollToWeekIsAutomatic)
            assertEquals(
                expected = 10,
                actual = state.flashTargetGlobalIndex,
            )
        }

    @Test
    fun `GIVEN today in an upcoming week WHEN skipping to today THEN expands the upcoming group and that week`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(initialPlans = plans(readDays = 0, todayGlobalIndex = 15))

            // When
            viewModel.onEvent(ReadingPlanUiEvent.OnSkipToTodayClick)

            // Then
            val state = loaded()
            assertTrue(state.upcomingExpanded)
            assertFalse(state.completedExpanded)
            assertEquals(
                expected = listOf(1, 3),
                actual = expandedWeekNumbers(),
            )
            assertEquals(
                expected = 15,
                actual = state.flashTargetGlobalIndex,
            )
        }

    @Test
    fun `GIVEN today in a completed week WHEN skipping to today THEN expands the completed group`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(initialPlans = plans(readDays = 9, todayGlobalIndex = 3))

            // When
            viewModel.onEvent(ReadingPlanUiEvent.OnSkipToTodayClick)

            // Then
            assertTrue(loaded().completedExpanded)
            assertFalse(loaded().upcomingExpanded)
        }

    @Test
    fun `GIVEN no day planned for today WHEN skipping to today THEN nothing changes`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        val stateBefore = loaded()

        // When
        viewModel.onEvent(ReadingPlanUiEvent.OnSkipToTodayClick)

        // Then
        assertEquals(
            expected = stateBefore,
            actual = loaded(),
        )
    }

    @Test
    fun `GIVEN a flashing day WHEN the flash completes THEN clears the flash target`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        viewModel.onEvent(ReadingPlanUiEvent.OnGoToActiveRowClick)

        // When
        viewModel.onEvent(ReadingPlanUiEvent.OnFlashCompleted)

        // Then
        assertEquals(
            expected = 0,
            actual = loaded().flashTargetGlobalIndex,
        )
    }

    @Test
    fun `GIVEN the loaded plans WHEN the list scroll changes THEN mirrors it in the state`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ReadingPlanUiEvent.OnScrollToTopClick)
        viewModel.onEvent(ReadingPlanUiEvent.OnScrollStateChange(isScrolledDown = true))
        viewModel.onEvent(ReadingPlanUiEvent.OnActiveRowVisibilityChange(isActiveRowVisible = false))

        // Then
        val state = loaded()
        assertTrue(state.scrollToTop)
        assertTrue(state.isScrolledDown)
        assertFalse(state.isActiveRowVisible)
    }

    @Test
    fun `GIVEN pending scrolls WHEN they complete THEN clears them`() = runTest(testDispatcher) {
        // Given
        prepareScenario(initialPlans = plans(readDays = 9))
        viewModel.onEvent(ReadingPlanUiEvent.OnScrollToTopClick)

        // When
        viewModel.onEvent(ReadingPlanUiEvent.OnScrollToTopCompleted)
        viewModel.onEvent(ReadingPlanUiEvent.OnScrollToWeekCompleted)

        // Then
        val state = loaded()
        assertFalse(state.scrollToTop)
        assertEquals(
            expected = 0,
            actual = state.scrollToWeekNumber,
        )
        assertFalse(state.scrollToWeekIsAutomatic)
    }

    @Test
    fun `GIVEN the plans are loading WHEN the list scroll changes THEN mirrors it in the loading state`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(initialPlans = null)

            // When
            viewModel.onEvent(ReadingPlanUiEvent.OnScrollToTopClick)
            viewModel.onEvent(ReadingPlanUiEvent.OnScrollStateChange(isScrolledDown = true))
            viewModel.onEvent(ReadingPlanUiEvent.OnActiveRowVisibilityChange(isActiveRowVisible = false))

            // Then
            val state = assertIs<ReadingPlanUiState.Loading>(viewModel.uiState.value)
            assertTrue(state.scrollToTop)
            assertTrue(state.isScrolledDown)
            assertFalse(state.isActiveRowVisible)
        }

    @Test
    fun `GIVEN the plans are loading WHEN pending scrolls complete THEN clears them in the loading state`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(initialPlans = null)
            viewModel.onEvent(ReadingPlanUiEvent.OnScrollToTopClick)

            // When
            viewModel.onEvent(ReadingPlanUiEvent.OnScrollToTopCompleted)
            viewModel.onEvent(ReadingPlanUiEvent.OnScrollToWeekCompleted)
            viewModel.onEvent(ReadingPlanUiEvent.OnFlashCompleted)

            // Then
            val state = assertIs<ReadingPlanUiState.Loading>(viewModel.uiState.value)
            assertFalse(state.scrollToTop)
            assertEquals(
                expected = 0,
                actual = state.scrollToWeekNumber,
            )
        }

    private fun loaded(): ReadingPlanUiState.Loaded = assertIs<ReadingPlanUiState.Loaded>(viewModel.uiState.value)

    private fun expandedWeekNumbers(): List<Int> = loaded()
        .weekPlans
        .filter { it.isExpanded }
        .map { it.weekPlan.number }

    private fun isDayRead(
        weekNumber: Int,
        dayNumber: Int,
    ): Boolean = loaded()
        .weekPlans
        .first { it.weekPlan.number == weekNumber }
        .weekPlan
        .days
        .first { it.number == dayNumber }
        .isRead

    private fun weeks(
        count: Int,
        readDays: Int,
        todayGlobalIndex: Int?,
    ): List<WeekPlanModel> = (1..count).map { weekNumber ->
        WeekPlanModel(
            number = weekNumber,
            days = (1..daysPerWeek).map { dayNumber ->
                val globalIndex = (weekNumber - 1) * daysPerWeek + dayNumber
                day(
                    number = dayNumber,
                    isRead = globalIndex <= readDays,
                    isToday = globalIndex == todayGlobalIndex,
                )
            },
        )
    }

    private fun plans(
        readDays: Int,
        todayGlobalIndex: Int? = null,
    ): PlansModel = PlansModel(
        chronologicalOrder = weeks(
            count = 3,
            readDays = readDays,
            todayGlobalIndex = todayGlobalIndex,
        ),
        booksOrder = weeks(
            count = 2,
            readDays = 0,
            todayGlobalIndex = null,
        ),
    )

    private fun booksWithReadVerses(readVerses: Int): List<BookDataModel> = listOf(
        BookDataModel(
            id = BookId.GEN,
            chapters = listOf(
                BookChapterModel(
                    number = 1,
                    verses = (1..versesPerBook).map { number ->
                        VerseModel(
                            number = number,
                            isRead = number <= readVerses,
                        )
                    },
                    isRead = false,
                    readUpdatedAt = null,
                ),
            ),
            isRead = false,
            isFavorite = false,
        ),
    )

    private fun TestScope.prepareScenario(initialPlans: PlansModel? = plans(readDays = 0)) {
        val navigator = Navigator()
        val collectedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        val collectedTriggers = mutableListOf<ReviewTrigger>()
        val collectedPlans = mutableListOf<ReadingPlanType>()
        val collectedUpdates = mutableListOf<List<Any>>()
        trackedEvents = collectedEvents
        reviewTriggers = collectedTriggers
        selectedPlans = collectedPlans
        dayReadUpdates = collectedUpdates
        loginNudges = 0
        plans = MutableSharedFlow(replay = 1)
        initialPlans?.let(plans::tryEmit)
        selectedPlan = MutableStateFlow(ReadingPlanType.CHRONOLOGICAL)
        books = MutableStateFlow(booksWithReadVerses(0))
        val trackEvent = TrackEvent { name, params -> collectedEvents += name to params }
        viewModel = ReadingPlanViewModel(
            setSelectedReadingPlan = { type ->
                collectedPlans += type
                selectedPlan.value = type
            },
            getPlanMotivationMessage = { _, progress ->
                if (progress > 0f) {
                    PlanMotivationMessage.OverallProgress.EarlyStart
                } else {
                    PlanMotivationMessage.OverallProgress.Zero
                }
            },
            resolvePlanStatus = ResolvePlanStatusUseCase(),
            weeksPlanPresentationMapper = WeeksPlanPresentationMapper(
                localDateTimeProvider = {
                    LocalDateTime(
                        year = 2026,
                        month = 1,
                        day = 1,
                        hour = 0,
                        minute = 0,
                    )
                },
                currentTimestampProvider = { 0L },
            ),
            deleteProgressMapper = DeleteProgressMapper(),
            updateDayReadStatus = { weekNumber, dayNumber, isRead, readingPlanType ->
                collectedUpdates += listOf<Any>(weekNumber, dayNumber, isRead, readingPlanType)
            },
            requestLoginNudgeIfNeeded = { loginNudges++ },
            navigator = navigator,
            factory = ReadingPlanStateFactory(),
            observePlansByWeek = { plans },
            getSelectedReadingPlanFlow = { selectedPlan },
            calculateBibleProgress = CalculateBibleProgressUseCase(FakeBooksRepository(books)),
            findFirstWeekWithUnreadBook = FindFirstWeekWithUnreadBookUseCase(),
            requestReviewIfNeeded = { trigger -> collectedTriggers += trigger },
            trackEvent = trackEvent,
            bibleProgressMilestoneTracker = BibleProgressMilestoneTracker(trackEvent),
            readingStreakMilestoneTracker = ReadingStreakMilestoneTracker(trackEvent),
        )
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        actions = mutableListOf<ReadingPlanUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
        runCurrent()
    }
}
