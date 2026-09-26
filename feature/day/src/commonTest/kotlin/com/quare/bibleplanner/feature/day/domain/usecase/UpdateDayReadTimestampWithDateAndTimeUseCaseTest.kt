package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.date.GetFinalTimestampAfterEditionUseCase
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.day.fake.FakeDayRepository
import com.quare.bibleplanner.feature.day.fake.ReadStatusUpdate
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

internal class UpdateDayReadTimestampWithDateAndTimeUseCaseTest {
    private lateinit var useCase: UpdateDayReadTimestampWithDateAndTimeUseCase
    private lateinit var dayRepository: FakeDayRepository

    @BeforeTest
    fun setUp() {
        dayRepository = FakeDayRepository(
            day = null,
            daysWithNotesCount = 0,
        )
        useCase = UpdateDayReadTimestampWithDateAndTimeUseCase(
            getFinalTimestampAfterEdition = GetFinalTimestampAfterEditionUseCase(),
            updateDayReadTimestamp = UpdateDayReadTimestampUseCase(dayRepository),
        )
    }

    @Test
    fun `GIVEN a date and a time of day WHEN updating THEN marks the day read at that local moment`() = runTest {
        // Given
        val date = LocalDate(2026, 3, 14)
        val expectedTimestamp = date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds() +
            (9 * 60 + 30).minutes.inWholeMilliseconds

        // When
        useCase(
            weekNumber = 4,
            dayNumber = 2,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
            selectedLocalDate = date,
            eventDuration = (9 * 60 + 30).minutes,
        )

        // Then
        assertEquals(
            listOf(
                ReadStatusUpdate(
                    weekNumber = 4,
                    dayNumber = 2,
                    readingPlanType = ReadingPlanType.CHRONOLOGICAL,
                    isRead = true,
                    readTimestamp = expectedTimestamp,
                ),
            ),
            dayRepository.readStatusUpdates,
        )
    }
}
