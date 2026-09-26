package com.quare.bibleplanner.core.provider.room.dao

import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.DayEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DayDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: DayDao

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
        dao = database.dayDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN days with and without notes WHEN counting the days with notes THEN ignores blank notes`() = runTest {
        // Given
        dao.insertDays(
            listOf(
                day(
                    dayNumber = 1,
                    notes = "Loved it",
                ),
                day(
                    dayNumber = 2,
                    notes = "   ",
                ),
                day(
                    dayNumber = 3,
                    notes = null,
                ),
            ),
        )

        // When
        val count = dao.getDaysWithNotesCount()

        // Then
        assertEquals(
            expected = 1,
            actual = count,
        )
    }

    @Test
    fun `GIVEN a day WHEN reading it back THEN keeps its reading metadata`() = runTest {
        // Given
        val day = day(
            dayNumber = 1,
            notes = "Loved it",
        )
        val id = dao.insertDay(day)

        // When
        val stored = dao.getDayByWeekAndDay(
            weekNumber = 1,
            dayNumber = 1,
            readingPlanType = PLAN_TYPE,
        )

        // Then
        assertEquals(
            expected = day.copy(id = id),
            actual = stored,
        )
    }

    private fun day(
        dayNumber: Int,
        notes: String?,
    ): DayEntity = DayEntity(
        weekNumber = 1,
        dayNumber = dayNumber,
        readingPlanType = PLAN_TYPE,
        isRead = true,
        readTimestamp = 100L,
        notes = notes,
        metaUpdatedAt = 100L,
        isMetaPendingSync = true,
    )

    private companion object {
        const val PLAN_TYPE = "BOOKS"
    }
}
