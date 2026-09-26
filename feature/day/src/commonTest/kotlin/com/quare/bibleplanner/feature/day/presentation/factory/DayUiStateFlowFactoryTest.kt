package com.quare.bibleplanner.feature.day.presentation.factory

import com.quare.bibleplanner.core.books.domain.usecase.GetBooksFlowUseCase
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.feature.day.domain.EditDaySelectableDates
import com.quare.bibleplanner.feature.day.domain.mapper.LocalDateTimeToDateMapper
import com.quare.bibleplanner.feature.day.domain.usecase.CalculateAllChaptersReadStatusUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.ConvertTimestampToDatePickerInitialDateUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.GetDayDetailsUseCase
import com.quare.bibleplanner.feature.day.fake.FakeBooksRepository
import com.quare.bibleplanner.feature.day.fake.FakeDayRepository
import com.quare.bibleplanner.feature.day.fake.FakePlanRepository
import com.quare.bibleplanner.feature.day.presentation.mapper.ReadDateFormatter
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.feature.day.presentation.model.PickerType
import com.quare.bibleplanner.ui.utils.MonthPresentationMapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

internal class DayUiStateFlowFactoryTest {
    private val planDay = DayModel(
        number = 1,
        passages = listOf(
            PassageModel(
                bookId = BookId.GEN,
                chapters = listOf(chapter(1), chapter(2)),
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
    private val readTimestamp = 5 * 60 * 60 * 1_000L + 7 * 60 * 1_000L
    private val readGenesis = BookDataModel(
        id = BookId.GEN,
        chapters = listOf(
            bookChapter(
                number = 1,
                isRead = true,
                readUpdatedAt = 1_000L,
            ),
            bookChapter(
                number = 2,
                isRead = true,
                readUpdatedAt = readTimestamp,
            ),
        ),
        isRead = true,
    )
    private val readObadiah = BookDataModel(
        id = BookId.OBA,
        chapters = listOf(
            bookChapter(
                number = 1,
                isRead = true,
                readUpdatedAt = 3_000L,
            ),
        ),
        isRead = true,
    )
    private lateinit var factory: DayUiStateFlowFactory

    @Test
    fun `GIVEN a day missing from the plan WHEN creating the state THEN stays loading`() = runTest {
        // Given
        prepareScenario(books = listOf(readGenesis, readObadiah))

        // When
        val state = factory
            .createUiState(
                weekNumber = 9,
                dayNumber = 1,
                readingPlanType = ReadingPlanType.BOOKS,
                currentState = null,
            ).first()

        // Then
        assertEquals(DayUiState.Loading, state)
    }

    @Test
    fun `GIVEN a read day without a timestamp WHEN creating the state THEN dates it by the last chapter read`() =
        runTest {
            // Given
            prepareScenario(books = listOf(readGenesis, readObadiah))

            // When
            val state = factory
                .createUiState(
                    weekNumber = 1,
                    dayNumber = 1,
                    readingPlanType = ReadingPlanType.BOOKS,
                    currentState = null,
                ).first()

            // Then
            assertIs<DayUiState.Loaded>(state)
            assertEquals("05", state.formattedReadDate?.hour)
            assertEquals("07", state.formattedReadDate?.minute)
            assertEquals(5, state.datePickerUiState.initialHour)
            assertEquals(7, state.datePickerUiState.initialMinute)
            assertEquals(3, state.completedPassagesCount)
            assertEquals(3, state.totalPassagesCount)
            assertEquals(mapOf((0 to 0) to true, (0 to 1) to true), state.chapterReadStatus)
            assertEquals(
                DayNavRoute(
                    dayNumber = 1,
                    weekNumber = 1,
                    readingPlanType = ReadingPlanType.BOOKS.name,
                ),
                state.dayRoute,
            )
        }

    @Test
    fun `GIVEN a fresh state WHEN creating it THEN the date picker starts hidden with no selection`() = runTest {
        // Given
        prepareScenario(books = listOf(readGenesis, readObadiah))

        // When
        val state = factory
            .createUiState(
                weekNumber = 1,
                dayNumber = 1,
                readingPlanType = ReadingPlanType.BOOKS,
                currentState = null,
            ).first()

        // Then
        assertIs<DayUiState.Loaded>(state)
        assertNull(state.datePickerUiState.visiblePicker)
        assertNull(state.datePickerUiState.selectedDateMillis)
        assertNull(state.datePickerUiState.selectedLocalDate)
        assertIs<EditDaySelectableDates>(state.datePickerUiState.selectableDates)
    }

    @Test
    fun `GIVEN a stored read timestamp WHEN creating the state THEN formats it instead of the derived one`() = runTest {
        // Given
        val storedTimestamp = 22 * 60 * 60 * 1_000L + 30 * 60 * 1_000L
        prepareScenario(
            books = listOf(readGenesis, readObadiah),
            storedDay = planDay.copy(readTimestamp = storedTimestamp),
        )

        // When
        val state = factory
            .createUiState(
                weekNumber = 1,
                dayNumber = 1,
                readingPlanType = ReadingPlanType.BOOKS,
                currentState = null,
            ).first()

        // Then
        assertIs<DayUiState.Loaded>(state)
        assertEquals("22", state.formattedReadDate?.hour)
        assertEquals("30", state.formattedReadDate?.minute)
    }

    @Test
    fun `GIVEN a partially read day WHEN creating the state THEN has no read date and counts only read items`() =
        runTest {
            // Given
            prepareScenario(
                books = listOf(
                    readGenesis.copy(
                        chapters = listOf(
                            bookChapter(
                                number = 1,
                                isRead = true,
                                readUpdatedAt = 1_000L,
                            ),
                            bookChapter(
                                number = 2,
                                isRead = false,
                                readUpdatedAt = null,
                            ),
                        ),
                        isRead = false,
                    ),
                    readObadiah,
                ),
            )

            // When
            val state = factory
                .createUiState(
                    weekNumber = 1,
                    dayNumber = 1,
                    readingPlanType = ReadingPlanType.BOOKS,
                    currentState = null,
                ).first()

            // Then
            assertIs<DayUiState.Loaded>(state)
            assertNull(state.formattedReadDate)
            assertEquals(2, state.completedPassagesCount)
            assertEquals(3, state.totalPassagesCount)
            assertEquals(mapOf((0 to 0) to true, (0 to 1) to false), state.chapterReadStatus)
        }

    @Test
    fun `GIVEN books missing from the library WHEN creating the state THEN nothing counts as read`() = runTest {
        // Given
        prepareScenario(books = emptyList())

        // When
        val state = factory
            .createUiState(
                weekNumber = 1,
                dayNumber = 1,
                readingPlanType = ReadingPlanType.BOOKS,
                currentState = null,
            ).first()

        // Then
        assertIs<DayUiState.Loaded>(state)
        assertEquals(0, state.completedPassagesCount)
        assertEquals(3, state.totalPassagesCount)
        assertNull(state.formattedReadDate)
    }

    @Test
    fun `GIVEN a passage from a book not in the library WHEN creating the state THEN has no read date`() = runTest {
        // Given
        prepareScenario(
            books = listOf(readGenesis, readObadiah),
            storedDay = planDay.copy(
                passages = planDay.passages + PassageModel(
                    bookId = BookId.REV,
                    chapters = listOf(
                        chapter(
                            bookId = BookId.REV,
                            number = 1,
                        ),
                    ),
                    isRead = true,
                    chapterRanges = "1",
                ),
            ),
        )

        // When
        val state = factory
            .createUiState(
                weekNumber = 1,
                dayNumber = 1,
                readingPlanType = ReadingPlanType.BOOKS,
                currentState = null,
            ).first()

        // Then
        assertIs<DayUiState.Loaded>(state)
        assertEquals(3, state.completedPassagesCount)
        assertEquals(4, state.totalPassagesCount)
        assertNull(state.formattedReadDate)
    }

    @Test
    fun `GIVEN an open date picker WHEN the day updates THEN keeps the selection and refreshes the initial values`() =
        runTest {
            // Given
            prepareScenario(books = listOf(readGenesis, readObadiah))
            val initialState = factory
                .createUiState(
                    weekNumber = 1,
                    dayNumber = 1,
                    readingPlanType = ReadingPlanType.BOOKS,
                    currentState = null,
                ).first()
            assertIs<DayUiState.Loaded>(initialState)
            val openPickerState = initialState.copy(
                datePickerUiState = initialState.datePickerUiState.copy(
                    visiblePicker = PickerType.TIME,
                    selectedDateMillis = 123L,
                    selectedLocalDate = LocalDate(2026, 2, 3),
                    initialHour = 0,
                    initialMinute = 0,
                ),
            )

            // When
            val state = factory
                .createUiState(
                    weekNumber = 1,
                    dayNumber = 1,
                    readingPlanType = ReadingPlanType.BOOKS,
                    currentState = openPickerState,
                ).first()

            // Then
            assertIs<DayUiState.Loaded>(state)
            assertEquals(PickerType.TIME, state.datePickerUiState.visiblePicker)
            assertEquals(123L, state.datePickerUiState.selectedDateMillis)
            assertEquals(LocalDate(2026, 2, 3), state.datePickerUiState.selectedLocalDate)
            assertEquals(5, state.datePickerUiState.initialHour)
            assertEquals(7, state.datePickerUiState.initialMinute)
        }

    private fun chapter(
        number: Int,
        bookId: BookId = BookId.GEN,
    ): ChapterModel = ChapterModel(
        number = number,
        startVerse = null,
        endVerse = null,
        bookId = bookId,
    )

    private fun bookChapter(
        number: Int,
        isRead: Boolean,
        readUpdatedAt: Long?,
    ): BookChapterModel = BookChapterModel(
        number = number,
        verses = listOf(
            VerseModel(
                number = 1,
                isRead = isRead,
            ),
        ),
        isRead = isRead,
        readUpdatedAt = readUpdatedAt,
    )

    private fun prepareScenario(
        books: List<BookDataModel>,
        storedDay: DayModel? = null,
    ) {
        val booksRepository = FakeBooksRepository(books)
        val localDateTimeProvider = { timestamp: Long ->
            Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.UTC)
        }
        factory = DayUiStateFlowFactory(
            getDayDetails = GetDayDetailsUseCase(
                getPlansByWeekUseCase = GetPlansByWeekUseCase(
                    planRepository = FakePlanRepository(
                        plans = mapOf(
                            ReadingPlanType.BOOKS to listOf(
                                WeekPlanModel(
                                    number = 1,
                                    days = listOf(storedDay ?: planDay),
                                ),
                            ),
                        ),
                        startDate = null,
                    ),
                    booksRepository = booksRepository,
                    getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
                    currentTimestampProvider = { 0L },
                    localDateTimeProvider = localDateTimeProvider,
                ),
                dayRepository = FakeDayRepository(
                    day = storedDay,
                    daysWithNotesCount = 0,
                ),
            ),
            getBooks = GetBooksFlowUseCase(booksRepository),
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
    }
}
