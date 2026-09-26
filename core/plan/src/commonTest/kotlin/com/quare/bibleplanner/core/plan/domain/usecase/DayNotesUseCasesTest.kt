package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.fake.FakeDayRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DayNotesUseCasesTest {
    private lateinit var dayRepository: FakeDayRepository

    @BeforeTest
    fun setUp() {
        dayRepository = FakeDayRepository(daysWithNotesCount = 4)
    }

    @Test
    fun `GIVEN new notes WHEN updating a day THEN stores them for that day and plan`() = runTest {
        // When
        UpdateDayNotesUseCase(dayRepository)(
            weekNumber = 3,
            dayNumber = 5,
            readingPlanType = ReadingPlanType.BOOKS,
            notes = "Reflection",
        )

        // Then
        assertEquals(listOf("updateDayNotes(3, 5, BOOKS, Reflection)"), dayRepository.calls)
    }

    @Test
    fun `GIVEN a day with notes WHEN deleting them THEN clears the notes of that day`() = runTest {
        // When
        DeleteDayNotesUseCase(UpdateDayNotesUseCase(dayRepository))(
            weekNumber = 3,
            dayNumber = 5,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
        )

        // Then
        assertEquals(listOf("updateDayNotes(3, 5, CHRONOLOGICAL, null)"), dayRepository.calls)
    }

    @Test
    fun `GIVEN days with notes WHEN counting them THEN returns the stored count`() = runTest {
        // When
        val count = GetDaysWithNotesCountUseCase(dayRepository)()

        // Then
        assertEquals(4, count)
    }
}
