package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.fake.FakePlanRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PlanStartDateUseCasesTest {
    private val now = 1_700_000_000_000L
    private val startDate = LocalDate(
        year = 2026,
        month = 1,
        day = 1,
    )

    private lateinit var planRepository: FakePlanRepository
    private lateinit var setPlanStartTime: SetPlanStartTimeUseCase

    @BeforeTest
    fun setUp() {
        planRepository = FakePlanRepository(
            plans = emptyMap(),
            startDate = startDate,
            selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL,
        )
        setPlanStartTime = SetPlanStartTimeUseCase(
            planRepository = planRepository,
            currentTimestampProvider = CurrentTimestampProvider { now },
        )
    }

    @Test
    fun `GIVEN a specific time WHEN setting the start THEN stores that time`() = runTest {
        // When
        setPlanStartTime(SetPlanStartTimeUseCase.Strategy.SpecificTime(timestamp = 42L))

        // Then
        assertEquals(listOf(42L), planRepository.startTimestamps)
    }

    @Test
    fun `GIVEN the now strategy WHEN setting the start THEN stores the current time`() = runTest {
        // When
        setPlanStartTime(SetPlanStartTimeUseCase.Strategy.Now)

        // Then
        assertEquals(listOf(now), planRepository.startTimestamps)
    }

    @Test
    fun `WHEN ensuring a default start date THEN seeds the current time as provisional`() = runTest {
        // When
        EnsureDefaultPlanStartDateUseCase(
            planRepository = planRepository,
            currentTimestampProvider = CurrentTimestampProvider { now },
        )()

        // Then
        assertEquals(listOf(now), planRepository.seededTimestamps)
    }

    @Test
    fun `GIVEN a stored start date WHEN observing it THEN emits it`() = runTest {
        // When
        val result = GetPlanStartDateFlowUseCase(planRepository)().first()

        // Then
        assertEquals(startDate, result)
    }

    @Test
    fun `GIVEN a start date WHEN computing the planned date of a day THEN offsets it by the days before it`() {
        // When
        val plannedDate = GetPlannedReadDateForDayUseCase()(
            weekNumber = 2,
            dayNumber = 3,
            startDate = startDate,
        )

        // Then
        assertEquals(
            LocalDate(
                year = 2026,
                month = 1,
                day = 10,
            ),
            plannedDate,
        )
    }
}
