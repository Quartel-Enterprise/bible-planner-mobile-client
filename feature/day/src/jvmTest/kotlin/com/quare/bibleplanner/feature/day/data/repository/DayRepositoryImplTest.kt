package com.quare.bibleplanner.feature.day.data.repository

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.provider.room.entity.DayEntity
import com.quare.bibleplanner.feature.day.data.datasource.DayLocalDataSource
import com.quare.bibleplanner.feature.day.data.mapper.DayEntityToModelMapper
import com.quare.bibleplanner.feature.day.fake.FakePlanRepository
import com.quare.bibleplanner.feature.day.fake.InMemoryBibleDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
internal class DayRepositoryImplTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val now = 1_750_000_000_000L
    private val planDay = DayModel(
        number = 3,
        passages = listOf(
            PassageModel(
                bookId = BookId.GEN,
                chapters = emptyList(),
                isRead = false,
                chapterRanges = null,
            ),
        ),
        isRead = true,
        totalVerses = 10,
        readVerses = 10,
        readTimestamp = 5L,
        plannedReadDate = null,
        notes = "plan notes",
        isToday = false,
    )
    private lateinit var repository: DayRepositoryImpl
    private lateinit var database: InMemoryBibleDatabase

    @BeforeTest
    fun setUp() {
        database = InMemoryBibleDatabase(testDispatcher)
        repository = DayRepositoryImpl(
            dayLocalDataSource = DayLocalDataSource(
                dayDao = database.database.dayDao(),
                currentTimestampProvider = { now },
            ),
            planRepository = FakePlanRepository(
                plans = mapOf(
                    ReadingPlanType.BOOKS to listOf(
                        WeekPlanModel(
                            number = 2,
                            days = listOf(planDay),
                        ),
                    ),
                ),
                startDate = null,
            ),
            dayEntityToModelMapper = DayEntityToModelMapper(),
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN no stored day WHEN reading it THEN returns the plan day unread without timestamp or notes`() =
        runTest(testDispatcher) {
            // When
            val day = repository.getDayByWeekAndDay(
                weekNumber = 2,
                dayNumber = 3,
                readingPlanType = ReadingPlanType.BOOKS,
            )

            // Then
            assertEquals(
                planDay.copy(
                    isRead = false,
                    readTimestamp = null,
                    notes = null,
                ),
                day,
            )
        }

    @Test
    fun `GIVEN a location outside the plan WHEN reading it THEN returns null`() = runTest(testDispatcher) {
        // When
        val days = listOf(
            repository.getDayByWeekAndDay(
                weekNumber = 5,
                dayNumber = 3,
                readingPlanType = ReadingPlanType.BOOKS,
            ),
            repository.getDayByWeekAndDay(
                weekNumber = 2,
                dayNumber = 9,
                readingPlanType = ReadingPlanType.BOOKS,
            ),
            repository
                .getDayByWeekAndDayFlow(
                    weekNumber = 2,
                    dayNumber = 3,
                    readingPlanType = ReadingPlanType.CHRONOLOGICAL,
                ).first(),
        )

        // Then
        assertEquals(listOf<DayModel?>(null, null, null), days)
    }

    @Test
    fun `GIVEN a new day WHEN marking it read THEN stores it pending sync and exposes the timestamp`() =
        runTest(testDispatcher) {
            // When
            repository.updateDayReadStatus(
                weekNumber = 2,
                dayNumber = 3,
                readingPlanType = ReadingPlanType.BOOKS,
                isRead = true,
                readTimestamp = 99L,
            )

            // Then
            val day = repository
                .getDayByWeekAndDayFlow(
                    weekNumber = 2,
                    dayNumber = 3,
                    readingPlanType = ReadingPlanType.BOOKS,
                ).first()
            assertEquals(true, day?.isRead)
            assertEquals(99L, day?.readTimestamp)
            assertNull(day?.notes)
            val entity = storedEntity()
            assertEquals(now, entity?.metaUpdatedAt)
            assertEquals(true, entity?.isMetaPendingSync)
        }

    @Test
    fun `GIVEN a stored day with notes WHEN changing its read status THEN keeps the notes`() = runTest(testDispatcher) {
        // Given
        repository.updateDayNotes(
            weekNumber = 2,
            dayNumber = 3,
            readingPlanType = ReadingPlanType.BOOKS,
            notes = "keep me",
        )

        // When
        repository.updateDayReadStatus(
            weekNumber = 2,
            dayNumber = 3,
            readingPlanType = ReadingPlanType.BOOKS,
            isRead = true,
            readTimestamp = 7L,
        )

        // Then
        val day = repository.getDayByWeekAndDay(
            weekNumber = 2,
            dayNumber = 3,
            readingPlanType = ReadingPlanType.BOOKS,
        )
        assertEquals("keep me", day?.notes)
        assertEquals(7L, day?.readTimestamp)
        assertEquals(true, day?.isRead)
    }

    @Test
    fun `GIVEN a new day WHEN saving notes THEN stores them unread and pending sync`() = runTest(testDispatcher) {
        // When
        repository.updateDayNotes(
            weekNumber = 2,
            dayNumber = 3,
            readingPlanType = ReadingPlanType.BOOKS,
            notes = "first thoughts",
        )

        // Then
        val entity = storedEntity()
        assertEquals("first thoughts", entity?.notes)
        assertEquals(false, entity?.isRead)
        assertEquals(true, entity?.isMetaPendingSync)
    }

    @Test
    fun `GIVEN a read day WHEN editing its notes THEN keeps the read status`() = runTest(testDispatcher) {
        // Given
        repository.updateDayReadStatus(
            weekNumber = 2,
            dayNumber = 3,
            readingPlanType = ReadingPlanType.BOOKS,
            isRead = true,
            readTimestamp = 11L,
        )

        // When
        repository.updateDayNotes(
            weekNumber = 2,
            dayNumber = 3,
            readingPlanType = ReadingPlanType.BOOKS,
            notes = "edited",
        )

        // Then
        val entity = storedEntity()
        assertEquals("edited", entity?.notes)
        assertEquals(true, entity?.isRead)
        assertEquals(11L, entity?.readTimestamp)
    }

    @Test
    fun `GIVEN days with and without notes WHEN counting THEN counts only the ones with notes`() =
        runTest(testDispatcher) {
            // Given
            repository.updateDayNotes(
                weekNumber = 2,
                dayNumber = 3,
                readingPlanType = ReadingPlanType.BOOKS,
                notes = "note",
            )
            repository.updateDayReadStatus(
                weekNumber = 2,
                dayNumber = 4,
                readingPlanType = ReadingPlanType.BOOKS,
                isRead = true,
                readTimestamp = 1L,
            )

            // When
            val count = repository.getDaysWithNotesCount()

            // Then
            assertEquals(1, count)
        }

    private suspend fun storedEntity(): DayEntity? = database.database.dayDao().getDayByWeekAndDay(
        weekNumber = 2,
        dayNumber = 3,
        readingPlanType = ReadingPlanType.BOOKS.name,
    )
}
