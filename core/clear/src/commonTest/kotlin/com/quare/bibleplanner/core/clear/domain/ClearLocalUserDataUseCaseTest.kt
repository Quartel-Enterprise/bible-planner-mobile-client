package com.quare.bibleplanner.core.clear.domain

import com.quare.bibleplanner.core.books.domain.usecase.ClearLocalReadingDataUseCase
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.core.plan.domain.usecase.EnsureDefaultPlanStartDateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ClearLocalUserDataUseCaseTest {
    private val now = 1_700_000_000_000L

    private lateinit var calls: MutableList<String>
    private lateinit var useCase: ClearLocalUserDataUseCase

    @BeforeTest
    fun setUp() {
        calls = mutableListOf()
        useCase = ClearLocalUserDataUseCase(
            clearLocalReadingData = ClearLocalReadingDataUseCase(RecordingBookDao(calls)),
            clearAllSyncedLocalData = { calls += "clearAllSyncedLocalData" },
            clearDayStudyLocalData = { calls += "clearDayStudyLocalData" },
            clearChatLocalData = { calls += "clearChatLocalData" },
            ensureDefaultPlanStartDate = EnsureDefaultPlanStartDateUseCase(
                planRepository = SeedRecordingPlanRepository(calls),
                currentTimestampProvider = CurrentTimestampProvider { now },
            ),
        )
    }

    @Test
    fun `WHEN clearing the local user data THEN wipes every local dataset once`() = runTest {
        // When
        useCase()

        // Then
        assertEquals(
            setOf(
                "resetAllBooksProgress",
                "clearAllSyncedLocalData",
                "clearDayStudyLocalData",
                "clearChatLocalData",
                "seedDefaultStartDate($now)",
            ),
            calls.toSet(),
        )
        assertEquals(5, calls.size)
    }

    @Test
    fun `WHEN clearing the local user data THEN re-seeds the start date only after wiping the synced data`() = runTest {
        // When
        useCase()

        // Then
        assertTrue(calls.indexOf("clearAllSyncedLocalData") < calls.indexOf("seedDefaultStartDate($now)"))
    }
}

private class RecordingBookDao(
    private val calls: MutableList<String>,
) : ThrowingBookDao() {
    override suspend fun resetAllBooksProgress() {
        calls += "resetAllBooksProgress"
    }
}

private class SeedRecordingPlanRepository(
    private val calls: MutableList<String>,
) : PlanRepository {
    override suspend fun getPlans(readingPlanType: ReadingPlanType): List<WeekPlanModel> = error("Unexpected call")

    override suspend fun setStartPlanTimestamp(timestamp: Long) {
        error("Unexpected call")
    }

    override fun getStartPlanTimestamp(): Flow<LocalDate?> = error("Unexpected call")

    override fun getSelectedReadingPlanFlow(): Flow<ReadingPlanType> = error("Unexpected call")

    override suspend fun setSelectedReadingPlan(readingPlanType: ReadingPlanType) {
        error("Unexpected call")
    }

    override suspend fun seedDefaultStartDate(timestamp: Long) {
        calls += "seedDefaultStartDate($timestamp)"
    }
}
