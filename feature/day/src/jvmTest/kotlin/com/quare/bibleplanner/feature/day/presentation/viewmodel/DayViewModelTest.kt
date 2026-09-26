package com.quare.bibleplanner.feature.day.presentation.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.failed_to_toggle_chapter_message
import bibleplanner.feature.day.generated.resources.nothing_to_delete_message
import com.quare.bibleplanner.core.books.domain.usecase.GetBooksFlowUseCase
import com.quare.bibleplanner.core.date.GetFinalTimestampAfterEditionUseCase
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.model.route.ChatEntrySource
import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.DeleteDayNotesUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetDaysWithNotesCountUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetMaxFreeNotesAmountUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.TrackReadingCompletionEventsUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.UpdateDayNotesUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.UpdateDayReadStatusUseCase
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.feature.day.domain.EditDaySelectableDates
import com.quare.bibleplanner.feature.day.domain.mapper.LocalDateTimeToDateMapper
import com.quare.bibleplanner.feature.day.domain.model.ChapterClickStrategy
import com.quare.bibleplanner.feature.day.domain.model.DayUseCases
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy
import com.quare.bibleplanner.feature.day.domain.usecase.CalculateAllChaptersReadStatusUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.ConvertTimestampToDatePickerInitialDateUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.ConvertUtcDateToLocalDateUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.GetDayDetailsUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.IsChapterReadStatusUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.ShouldBlockAddNotesUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.ToggleChapterReadStatusUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.UpdateChapterReadStatusUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.UpdateDayReadTimestampUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.UpdateDayReadTimestampWithDateAndTimeUseCase
import com.quare.bibleplanner.feature.day.fake.FakeDayRepository
import com.quare.bibleplanner.feature.day.fake.FakeDayStudyGenerationCoordinator
import com.quare.bibleplanner.feature.day.fake.FakePlanRepository
import com.quare.bibleplanner.feature.day.fake.InMemoryBibleDatabase
import com.quare.bibleplanner.feature.day.fake.ReadStatusUpdate
import com.quare.bibleplanner.feature.day.presentation.factory.DayUiStateFlowFactory
import com.quare.bibleplanner.feature.day.presentation.mapper.DeleteRouteNotesMapper
import com.quare.bibleplanner.feature.day.presentation.mapper.ReadDateFormatter
import com.quare.bibleplanner.feature.day.presentation.model.DayUiAction
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.feature.day.presentation.model.PickerType
import com.quare.bibleplanner.ui.utils.MonthPresentationMapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
internal class DayViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val notesDebounce = 2.seconds
    private val blockedNotesCleanupDelay = 500.milliseconds
    private val now = 1_750_000_000_000L
    private val dayRoute = DayNavRoute(
        dayNumber = 1,
        weekNumber = 1,
        readingPlanType = ReadingPlanType.BOOKS.name,
    )
    private val planDay = DayModel(
        number = 1,
        passages = listOf(
            PassageModel(
                bookId = BookId.GEN,
                chapters = listOf(chapter(number = 1), chapter(number = 2)),
                isRead = false,
                chapterRanges = "1-2",
            ),
            PassageModel(
                bookId = BookId.OBA,
                chapters = emptyList(),
                isRead = false,
                chapterRanges = null,
            ),
        ),
        isRead = false,
        totalVerses = 0,
        readVerses = 0,
        readTimestamp = null,
        plannedReadDate = null,
        notes = null,
        isToday = false,
    )
    private lateinit var viewModel: DayViewModel
    private lateinit var viewModelStore: ViewModelStore
    private lateinit var database: InMemoryBibleDatabase
    private lateinit var dayRepository: FakeDayRepository
    private lateinit var coordinator: FakeDayStudyGenerationCoordinator
    private lateinit var trackedEvents: MutableStateFlow<List<Pair<String, Map<String, Any>>>>
    private lateinit var actions: MutableStateFlow<List<DayUiAction>>
    private lateinit var commands: MutableStateFlow<List<NavigationCommand>>
    private lateinit var loginNudgeRequested: CompletableDeferred<Unit>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        viewModelStore.clear()
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a planned day WHEN opening it THEN loads the day with its passages`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val state = awaitLoaded()

        // Then
        assertEquals(planDay.passages.map(PassageModel::bookId), state.day.passages.map(PassageModel::bookId))
        assertEquals(dayRoute, state.dayRoute)
        assertEquals(0, state.completedPassagesCount)
        assertEquals(3, state.totalPassagesCount)
        assertEquals(Platform.Android, viewModel.platform)
    }

    @Test
    fun `GIVEN an unread day WHEN toggling it THEN marks every passage read and asks for the login nudge`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            awaitLoaded()

            // When
            viewModel.onEvent(DayUiEvent.OnDayReadToggle)
            runCurrent()
            loginNudgeRequested.await()

            // Then
            assertEquals(
                ReadStatusUpdate(
                    weekNumber = 1,
                    dayNumber = 1,
                    readingPlanType = ReadingPlanType.BOOKS,
                    isRead = true,
                    readTimestamp = now,
                ),
                dayRepository.readStatusUpdates.single(),
            )
            assertEquals(
                mapOf<String, Any>(
                    "plan_type" to "books",
                    "week_number" to 1,
                    "day_number" to 1,
                    "is_read" to true,
                    "source" to "day_screen",
                ),
                trackedParams(AnalyticsEventNames.DAY_READ_TOGGLED),
            )
            assertTrue(
                database.booksRepository
                    .getBooksFlow()
                    .first()
                    .all { book -> book.isRead },
            )
        }

    @Test
    fun `GIVEN an unread chapter WHEN checking it THEN reads it and tracks the chapter toggle`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            awaitLoaded()

            // When
            viewModel.onEvent(
                DayUiEvent.OnChapterCheckboxClick(
                    UpdateReadStatusOfPassageStrategy.Chapter(
                        passageIndex = 0,
                        chapterIndex = 1,
                    ),
                ),
            )
            runCurrent()

            // Then
            assertEquals(
                mapOf<String, Any>(
                    "plan_type" to "books",
                    "week_number" to 1,
                    "day_number" to 1,
                    "book_id" to "gen",
                    "chapter_number" to 2,
                    "is_read" to true,
                    "source" to "day_screen",
                ),
                awaitTrackedParams(AnalyticsEventNames.CHAPTER_READ_TOGGLED),
            )
            val state = viewModel.uiState.first { state ->
                state is DayUiState.Loaded && state.chapterReadStatus[0 to 1] == true
            }
            assertIs<DayUiState.Loaded>(state)
            assertEquals(1, state.completedPassagesCount)
        }

    @Test
    fun `GIVEN an unread whole book passage WHEN checking it THEN reads the book and tracks the book toggle`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            awaitLoaded()

            // When
            viewModel.onEvent(
                DayUiEvent.OnChapterCheckboxClick(UpdateReadStatusOfPassageStrategy.EntireBook(passageIndex = 1)),
            )
            runCurrent()

            // Then
            assertEquals(
                mapOf<String, Any>(
                    "book_id" to "oba",
                    "is_read" to true,
                ),
                awaitTrackedParams(AnalyticsEventNames.BOOK_READ_TOGGLED),
            )
        }

    @Test
    fun `GIVEN a chapter outside the passage WHEN checking it THEN shows the toggle failure message`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            awaitLoaded()

            // When
            viewModel.onEvent(
                DayUiEvent.OnChapterCheckboxClick(
                    UpdateReadStatusOfPassageStrategy.Chapter(
                        passageIndex = 0,
                        chapterIndex = 7,
                    ),
                ),
            )
            runCurrent()

            // Then
            assertEquals(
                listOf(DayUiAction.ShowSnackBar(Res.string.failed_to_toggle_chapter_message)),
                actions.first(List<DayUiAction>::isNotEmpty),
            )
            assertTrue(dayRepository.readStatusUpdates.isEmpty())
        }

    @Test
    fun `GIVEN a passage outside the day WHEN checking it THEN does nothing`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        awaitLoaded()

        // When
        viewModel.onEvent(
            DayUiEvent.OnChapterCheckboxClick(UpdateReadStatusOfPassageStrategy.EntireBook(passageIndex = 4)),
        )
        runCurrent()

        // Then
        assertTrue(actions.value.isEmpty())
        assertTrue(dayRepository.readStatusUpdates.isEmpty())
    }

    @Test
    fun `GIVEN the day is still loading WHEN toggling it THEN ignores the toggle`() = runTest(testDispatcher) {
        // Given
        prepareScenario(plannedDay = null)

        // When
        viewModel.onEvent(DayUiEvent.OnDayReadToggle)
        runCurrent()
        viewModel.onEvent(DayUiEvent.OnNotesFocus)
        runCurrent()
        viewModel.onEvent(DayUiEvent.OnNotesClear)
        runCurrent()
        viewModel.onEvent(
            DayUiEvent.OnEditReadDate(
                hour = 1,
                minute = 2,
            ),
        )
        runCurrent()
        viewModel.onEvent(DayUiEvent.OnDayStudyNavigate)
        runCurrent()

        // Then
        assertEquals(DayUiState.Loading, viewModel.uiState.value)
        assertTrue(dayRepository.readStatusUpdates.isEmpty())
        assertTrue(actions.value.isEmpty())
        assertTrue(commands.value.isEmpty())
    }

    @Test
    fun `GIVEN a loaded day WHEN opening and closing the pickers THEN shows the matching picker`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            awaitLoaded()

            // When
            viewModel.onEvent(DayUiEvent.OnEditDateClick)
            runCurrent()
            val afterEditClick = loadedState().datePickerUiState.visiblePicker
            viewModel.onEvent(DayUiEvent.OnShowTimePicker)
            runCurrent()
            val afterShowTime = loadedState().datePickerUiState.visiblePicker
            viewModel.onEvent(DayUiEvent.OnDismissPicker)
            runCurrent()

            // Then
            assertEquals(PickerType.DATE, afterEditClick)
            assertEquals(PickerType.TIME, afterShowTime)
            assertNull(loadedState().datePickerUiState.visiblePicker)
        }

    @Test
    fun `GIVEN the date picker WHEN selecting a date THEN keeps it and moves to the time picker`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            awaitLoaded()
            val utcMillis = LocalDate(2026, 2, 3).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

            // When
            viewModel.onEvent(DayUiEvent.OnDateSelected(utcMillis))
            runCurrent()

            // Then
            val datePicker = loadedState().datePickerUiState
            assertEquals(utcMillis, datePicker.selectedDateMillis)
            assertEquals(LocalDate(2026, 2, 3), datePicker.selectedLocalDate)
            assertEquals(PickerType.TIME, datePicker.visiblePicker)
        }

    @Test
    fun `GIVEN a selected date WHEN confirming the time THEN saves the read moment and resets the picker`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            awaitLoaded()
            viewModel.onEvent(
                DayUiEvent.OnDateSelected(LocalDate(2026, 2, 3).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()),
            )
            runCurrent()

            // When
            viewModel.onEvent(
                DayUiEvent.OnEditReadDate(
                    hour = 14,
                    minute = 30,
                ),
            )
            runCurrent()

            // Then
            val expectedTimestamp = LocalDate(2026, 2, 3)
                .atStartOfDayIn(TimeZone.currentSystemDefault())
                .toEpochMilliseconds() + (14 * 60 + 30).minutes.inWholeMilliseconds
            assertEquals(
                ReadStatusUpdate(
                    weekNumber = 1,
                    dayNumber = 1,
                    readingPlanType = ReadingPlanType.BOOKS,
                    isRead = true,
                    readTimestamp = expectedTimestamp,
                ),
                dayRepository.readStatusUpdates.single(),
            )
            val datePicker = loadedState().datePickerUiState
            assertNull(datePicker.visiblePicker)
            assertNull(datePicker.selectedDateMillis)
            assertNull(datePicker.selectedLocalDate)
            assertEquals(
                mapOf<String, Any>(
                    "plan_type" to "books",
                    "week_number" to 1,
                    "day_number" to 1,
                ),
                trackedParams(AnalyticsEventNames.READ_DATE_EDITED),
            )
        }

    @Test
    fun `GIVEN no selected date WHEN confirming the time THEN saves nothing`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        awaitLoaded()

        // When
        viewModel.onEvent(
            DayUiEvent.OnEditReadDate(
                hour = 14,
                minute = 30,
            ),
        )
        runCurrent()

        // Then
        assertTrue(dayRepository.readStatusUpdates.isEmpty())
    }

    @Test
    fun `GIVEN typed notes WHEN the debounce elapses THEN saves only the last text and tracks its length`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            awaitLoaded()
            viewModel.onEvent(DayUiEvent.OnNotesChanged("Draft"))
            runCurrent()

            // When
            viewModel.onEvent(DayUiEvent.OnNotesChanged("Final note"))
            runCurrent()
            advanceTimeBy(notesDebounce + 1.milliseconds)

            // Then
            assertEquals(listOf<String?>("Final note"), dayRepository.notesUpdates)
            assertEquals(
                mapOf<String, Any>(
                    "plan_type" to "books",
                    "week_number" to 1,
                    "day_number" to 1,
                    "note_length" to 10,
                ),
                trackedParams(AnalyticsEventNames.NOTE_SAVED),
            )
            assertEquals("Final note", loadedState().day.notes)
        }

    @Test
    fun `GIVEN blank notes WHEN the debounce elapses THEN clears them without tracking`() = runTest(testDispatcher) {
        // Given
        prepareScenario(storedNotes = "old")
        awaitLoaded()

        // When
        viewModel.onEvent(DayUiEvent.OnNotesChanged("   "))
        runCurrent()
        advanceTimeBy(notesDebounce + 1.milliseconds)

        // Then
        assertEquals(listOf<String?>(null), dayRepository.notesUpdates)
        assertTrue(trackedEvents.value.none { (name, _) -> name == AnalyticsEventNames.NOTE_SAVED })
    }

    @Test
    fun `GIVEN saved notes WHEN the screen is closed THEN saves nothing again`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        awaitLoaded()
        viewModel.onEvent(DayUiEvent.OnNotesChanged("Saved"))
        runCurrent()
        advanceTimeBy(notesDebounce + 1.milliseconds)

        // When
        viewModelStore.clear()
        advanceUntilIdle()

        // Then
        assertEquals(listOf<String?>("Saved"), dayRepository.notesUpdates)
    }

    @Test
    fun `GIVEN a day with notes WHEN clearing them THEN opens the delete confirmation`() = runTest(testDispatcher) {
        // Given
        prepareScenario(storedNotes = "Remember this")
        awaitLoaded()

        // When
        viewModel.onEvent(DayUiEvent.OnNotesClear)
        runCurrent()

        // Then
        assertEquals(
            listOf(
                NavigationCommand.Navigate(
                    DeleteNotesRoute(
                        readingPlanType = "BOOKS",
                        week = 1,
                        day = 1,
                    ),
                ),
            ),
            commands.value,
        )
    }

    @Test
    fun `GIVEN a day without notes WHEN clearing them THEN says there is nothing to delete`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            awaitLoaded()

            // When
            viewModel.onEvent(DayUiEvent.OnNotesClear)
            runCurrent()

            // Then
            assertEquals(listOf(DayUiAction.ShowSnackBar(Res.string.nothing_to_delete_message)), actions.value)
            assertTrue(commands.value.isEmpty())
        }

    @Test
    fun `GIVEN a free user at the notes limit WHEN focusing empty notes THEN blocks them and shows the warning`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                isFreeUser = true,
                daysWithNotes = 3,
            )
            awaitLoaded()

            // When
            viewModel.onEvent(DayUiEvent.OnNotesFocus)
            runCurrent()

            // Then
            assertEquals(listOf(DayUiAction.ClearFocus), actions.value)
            assertEquals(listOf(NavigationCommand.Navigate(AddNotesFreeWarningNavRoute(3))), commands.value)
            assertEquals(
                mapOf<String, Any>("max_free_notes" to 3),
                trackedParams(AnalyticsEventNames.NOTES_LIMIT_REACHED),
            )
        }

    @Test
    fun `GIVEN notes typed before the limit check WHEN blocked THEN deletes them afterwards`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                isFreeUser = true,
                daysWithNotes = 3,
            )
            awaitLoaded()
            viewModel.onEvent(DayUiEvent.OnNotesFocus)
            runCurrent()

            // When
            viewModel.onEvent(DayUiEvent.OnNotesChanged("x"))
            runCurrent()
            advanceTimeBy(blockedNotesCleanupDelay + 1.milliseconds)

            // Then
            assertEquals(listOf<String?>(null), dayRepository.notesUpdates)
        }

    @Test
    fun `GIVEN nothing typed WHEN the blocked notes cleanup runs THEN deletes nothing`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            isFreeUser = true,
            daysWithNotes = 3,
        )
        awaitLoaded()

        // When
        viewModel.onEvent(DayUiEvent.OnNotesFocus)
        runCurrent()
        advanceTimeBy(blockedNotesCleanupDelay + 1.milliseconds)

        // Then
        assertTrue(dayRepository.notesUpdates.isEmpty())
    }

    @Test
    fun `GIVEN a pro user WHEN focusing empty notes THEN lets them write`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            isFreeUser = false,
            daysWithNotes = 3,
        )
        awaitLoaded()

        // When
        viewModel.onEvent(DayUiEvent.OnNotesFocus)
        runCurrent()
        advanceUntilIdle()

        // Then
        assertTrue(actions.value.isEmpty())
        assertTrue(commands.value.isEmpty())
    }

    @Test
    fun `GIVEN existing notes WHEN focusing them THEN skips the limit check`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            storedNotes = "Already here",
            isFreeUser = true,
            daysWithNotes = 3,
        )
        awaitLoaded()

        // When
        viewModel.onEvent(DayUiEvent.OnNotesFocus)
        runCurrent()

        // Then
        assertTrue(actions.value.isEmpty())
        assertTrue(commands.value.isEmpty())
    }

    @Test
    fun `GIVEN a loaded day WHEN going back THEN navigates back and tracks it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        awaitLoaded()

        // When
        viewModel.onEvent(DayUiEvent.OnBackClick)
        runCurrent()

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands.value)
        assertEquals(emptyMap(), trackedParams(AnalyticsEventNames.DAY_BACK_CLICKED))
    }

    @Test
    fun `GIVEN a chapter WHEN clicking it THEN opens the reader on that chapter`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        awaitLoaded()

        // When
        viewModel.onEvent(
            DayUiEvent.OnChapterClick(
                ChapterClickStrategy.NavigateToChapter(
                    bookId = BookId.GEN,
                    isChapterRead = true,
                    chapterNumber = 2,
                ),
            ),
        )
        runCurrent()

        // Then
        assertEquals(
            listOf(
                NavigationCommand.Navigate(
                    ReadNavRoute(
                        bookId = "GEN",
                        chapterNumber = 2,
                        isChapterRead = true,
                        isFromBookDetails = false,
                    ),
                ),
            ),
            commands.value,
        )
        assertEquals(mapOf<String, Any>("source" to "day_screen"), trackedParams(AnalyticsEventNames.CHAPTER_CLICKED))
    }

    @Test
    fun `GIVEN a whole book passage WHEN clicking it THEN opens the reader on its first chapter`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            awaitLoaded()

            // When
            viewModel.onEvent(
                DayUiEvent.OnChapterClick(
                    ChapterClickStrategy.NavigateToFirstChapterOfTheBook(
                        bookId = BookId.OBA,
                        isChapterRead = false,
                    ),
                ),
            )
            runCurrent()

            // Then
            assertEquals(
                listOf(
                    NavigationCommand.Navigate(
                        ReadNavRoute(
                            bookId = "OBA",
                            chapterNumber = 1,
                            isChapterRead = false,
                            isFromBookDetails = false,
                        ),
                    ),
                ),
                commands.value,
            )
        }

    @Test
    fun `GIVEN a loaded day WHEN asking the AI THEN opens the chat for this day`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        awaitLoaded()

        // When
        viewModel.onEvent(DayUiEvent.OnAskAiClick)
        runCurrent()

        // Then
        assertEquals(
            listOf(
                NavigationCommand.Navigate(
                    ChatNavRoute(
                        source = ChatEntrySource.DAY_FAB,
                        dayNumber = 1,
                        weekNumber = 1,
                        readingPlanType = "BOOKS",
                    ),
                ),
            ),
            commands.value,
        )
        assertEquals(
            mapOf<String, Any>("source" to "day_fab"),
            trackedParams(AnalyticsEventNames.AI_CHAT_ENTRY_CLICKED),
        )
    }

    @Test
    fun `GIVEN the study card WHEN subscribing or signing in THEN opens the paywall and the login warning`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            awaitLoaded()

            // When
            viewModel.onEvent(DayUiEvent.OnDayStudySubscribeClick)
            runCurrent()
            viewModel.onEvent(DayUiEvent.OnDayStudyLoginRequired)
            runCurrent()

            // Then
            assertEquals(
                listOf(
                    NavigationCommand.Navigate(PaywallNavRoute(PaywallEntrySource.DAY_STUDY)),
                    NavigationCommand.Navigate(LoginWarningNavRoute(LoginWarningReason.DayStudy.key)),
                ),
                commands.value,
            )
        }

    @Test
    fun `GIVEN a study message WHEN it arrives THEN shows it as a snackbar`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        awaitLoaded()

        // When
        viewModel.onEvent(DayUiEvent.OnDayStudyMessage("Study ready"))
        runCurrent()

        // Then
        assertEquals(listOf(DayUiAction.ShowSnackBarText("Study ready")), actions.value)
    }

    @Test
    fun `GIVEN a loaded day WHEN opening the study THEN navigates to the day study`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        awaitLoaded()

        // When
        viewModel.onEvent(DayUiEvent.OnDayStudyNavigate)
        runCurrent()

        // Then
        assertEquals(listOf(NavigationCommand.Navigate(studyRoute())), commands.value)
    }

    @Test
    fun `GIVEN a wide window WHEN the day loads and updates THEN opens the study companion only once`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            viewModel.onEvent(DayUiEvent.OnWidthClassChanged(isWide = true))
            runCurrent()
            awaitLoaded()

            // When
            viewModel.onEvent(DayUiEvent.OnNotesChanged("update"))
            runCurrent()
            viewModel.onEvent(DayUiEvent.OnWidthClassChanged(isWide = true))
            runCurrent()

            // Then
            assertEquals(listOf(NavigationCommand.Navigate(studyRoute())), commands.value)
        }

    @Test
    fun `GIVEN a narrow window WHEN widening it again THEN reopens the study companion`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        awaitLoaded()
        viewModel.onEvent(DayUiEvent.OnWidthClassChanged(isWide = true))
        runCurrent()
        viewModel.onEvent(DayUiEvent.OnWidthClassChanged(isWide = false))
        runCurrent()

        // When
        viewModel.onEvent(DayUiEvent.OnWidthClassChanged(isWide = true))
        runCurrent()

        // Then
        assertEquals(
            listOf(NavigationCommand.Navigate(studyRoute()), NavigationCommand.Navigate(studyRoute())),
            commands.value,
        )
    }

    @Test
    fun `GIVEN a pending study reopen for this day WHEN it loads THEN consumes it and opens the study`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(pendingOpenKey = "BOOKS-1-1")

            // When
            awaitLoaded()

            // Then
            assertEquals(listOf("BOOKS-1-1"), coordinator.consumedKeys)
            assertEquals(listOf(NavigationCommand.Navigate(studyRoute())), commands.value)
        }

    @Test
    fun `GIVEN a pending study reopen for another day WHEN it loads THEN leaves it alone`() = runTest(testDispatcher) {
        // Given
        prepareScenario(pendingOpenKey = "BOOKS-9-9")

        // When
        awaitLoaded()

        // Then
        assertTrue(coordinator.consumedKeys.isEmpty())
        assertTrue(commands.value.isEmpty())
    }

    private suspend fun TestScope.awaitLoaded(): DayUiState.Loaded {
        val state = viewModel.uiState.first { it is DayUiState.Loaded }
        runCurrent()
        assertIs<DayUiState.Loaded>(state)
        return state
    }

    private fun loadedState(): DayUiState.Loaded {
        val state = viewModel.uiState.value
        assertIs<DayUiState.Loaded>(state)
        return state
    }

    private fun trackedParams(name: String): Map<String, Any>? =
        trackedEvents.value.lastOrNull { (eventName, _) -> eventName == name }?.second

    private suspend fun awaitTrackedParams(name: String): Map<String, Any> {
        val events = trackedEvents.first { events -> events.any { (eventName, _) -> eventName == name } }
        return events.last { (eventName, _) -> eventName == name }.second
    }

    private fun studyRoute(): DayStudyNavRoute = DayStudyNavRoute(
        dayNumber = 1,
        weekNumber = 1,
        readingPlanType = ReadingPlanType.BOOKS.name,
    )

    private fun chapter(number: Int): ChapterModel = ChapterModel(
        number = number,
        startVerse = null,
        endVerse = null,
        bookId = BookId.GEN,
    )

    private suspend fun TestScope.prepareScenario(
        plannedDay: DayModel? = planDay,
        storedNotes: String? = null,
        isFreeUser: Boolean = false,
        daysWithNotes: Int = 0,
        pendingOpenKey: String? = null,
    ) {
        database = InMemoryBibleDatabase(testDispatcher)
        database.insertBook(
            bookId = BookId.GEN,
            versesPerChapter = listOf(2, 2),
        )
        database.insertBook(
            bookId = BookId.OBA,
            versesPerChapter = listOf(2),
        )
        trackedEvents = MutableStateFlow(emptyList())
        actions = MutableStateFlow(emptyList())
        commands = MutableStateFlow(emptyList())
        loginNudgeRequested = CompletableDeferred()
        coordinator = FakeDayStudyGenerationCoordinator(pendingOpenKey)
        dayRepository = FakeDayRepository(
            day = plannedDay?.copy(notes = storedNotes),
            daysWithNotesCount = daysWithNotes,
        )
        val navigator = Navigator()
        val trackEvent = { name: String, params: Map<String, Any> ->
            trackedEvents.update { events -> events + (name to params) }
        }
        val localDateTimeProvider = { timestamp: Long ->
            Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.UTC)
        }
        val getPlansByWeek = GetPlansByWeekUseCase(
            planRepository = FakePlanRepository(
                plans = mapOf(
                    ReadingPlanType.BOOKS to listOfNotNull(
                        plannedDay?.let { day ->
                            WeekPlanModel(
                                number = 1,
                                days = listOf(day),
                            )
                        },
                    ),
                ),
                startDate = null,
            ),
            booksRepository = database.booksRepository,
            getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
            currentTimestampProvider = { now },
            localDateTimeProvider = localDateTimeProvider,
        )
        val updatePassageReadStatus = database.updatePassageReadStatus(
            currentTimestampProvider = { now },
            trackEvent = trackEvent,
        )
        val getMaxFreeNotesAmount = GetMaxFreeNotesAmountUseCase(FixedIntRemoteConfig(value = 3))
        val updateDayNotes = UpdateDayNotesUseCase(dayRepository)
        val getBooks = GetBooksFlowUseCase(database.booksRepository)
        val useCases = DayUseCases(
            updateDayReadStatus = UpdateDayReadStatusUseCase(
                dayRepository = dayRepository,
                updatePassageReadStatus = updatePassageReadStatus,
                getPlansByWeekUseCase = getPlansByWeek,
                currentTimestampProvider = { now },
                trackReadingCompletionEvents = TrackReadingCompletionEventsUseCase(trackEvent),
            ),
            toggleChapterReadStatus = ToggleChapterReadStatusUseCase(
                calculateChapterReadStatus = IsChapterReadStatusUseCase(getBooks),
                updateChapterReadStatus = UpdateChapterReadStatusUseCase(
                    dayRepository = dayRepository,
                    markPassagesRead = updatePassageReadStatus,
                    getPlansByWeek = getPlansByWeek,
                    currentTimestampProvider = { now },
                ),
            ),
            convertUtcDateToLocalDate = ConvertUtcDateToLocalDateUseCase(),
            updateDayReadTimestampWithDateAndTime = UpdateDayReadTimestampWithDateAndTimeUseCase(
                getFinalTimestampAfterEdition = GetFinalTimestampAfterEditionUseCase(),
                updateDayReadTimestamp = UpdateDayReadTimestampUseCase(dayRepository),
            ),
            updateDayNotes = updateDayNotes,
            shouldBlockAddNotes = ShouldBlockAddNotesUseCase(
                getDaysWithNotesCount = GetDaysWithNotesCountUseCase(dayRepository),
                getMaxFreeNotesAmount = getMaxFreeNotesAmount,
                isFreeUser = { isFreeUser },
            ),
            getMaxFreeNotesAmount = getMaxFreeNotesAmount,
            deleteDayNotes = DeleteDayNotesUseCase(updateDayNotes),
        )
        val dayUiStateFlowFactory = DayUiStateFlowFactory(
            getDayDetails = GetDayDetailsUseCase(
                getPlansByWeekUseCase = getPlansByWeek,
                dayRepository = dayRepository,
            ),
            getBooks = getBooks,
            readDateFormatter = ReadDateFormatter(
                localDateTimeToDateMapper = LocalDateTimeToDateMapper(),
                monthPresentationMapper = MonthPresentationMapper(),
                localDateTimeProvider = localDateTimeProvider,
            ),
            editDaySelectableDates = EditDaySelectableDates(),
            convertTimestampToDatePickerInitialDate = ConvertTimestampToDatePickerInitialDateUseCase(),
            calculateAllChaptersReadStatus = CalculateAllChaptersReadStatusUseCase(),
            localDateTimeProvider = localDateTimeProvider,
        )
        viewModelStore = ViewModelStore()
        viewModel = ViewModelProvider.create(
            store = viewModelStore,
            factory = viewModelFactory {
                initializer {
                    DayViewModel(
                        useCases = useCases,
                        dayUiStateFlowFactory = dayUiStateFlowFactory,
                        deleteRouteNotesMapper = DeleteRouteNotesMapper(),
                        requestLoginNudgeIfNeeded = { loginNudgeRequested.complete(Unit) },
                        applicationScope = ApplicationScope(backgroundScope),
                        generationCoordinator = coordinator,
                        navigator = navigator,
                        platform = Platform.Android,
                        route = dayRoute,
                        trackEvent = trackEvent,
                    )
                }
            },
        )[DayViewModel::class]
        backgroundScope.launch {
            viewModel.uiAction.collect { action -> actions.update { collected -> collected + action } }
        }
        backgroundScope.launch {
            navigator.commands.collect { command -> commands.update { collected -> collected + command } }
        }
    }
}

private class FixedIntRemoteConfig(
    private val value: Int,
) : GetIntRemoteConfig {
    override suspend fun invoke(
        key: String,
        default: Int,
    ): Int = value
}
