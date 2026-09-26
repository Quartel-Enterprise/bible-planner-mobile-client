package com.quare.bibleplanner.core.plan.data.repository

import com.quare.bibleplanner.core.books.data.provider.BookMapsProvider
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.data.datasource.PlanLocalDataSource
import com.quare.bibleplanner.core.plan.data.mapper.ChaptersRangeMapper
import com.quare.bibleplanner.core.plan.data.mapper.ReadingPlanPreferenceMapperImpl
import com.quare.bibleplanner.core.plan.data.mapper.WeekPlanDtoToModelMapper
import com.quare.bibleplanner.core.plan.fake.FakeSyncedPreferenceDao
import com.quare.bibleplanner.core.plan.fake.InMemoryPreferencesDataStore
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class BundledPlansTest {
    private lateinit var repository: PlanRepositoryImpl

    @BeforeTest
    fun setUp() {
        repository = PlanRepositoryImpl(
            planLocalDataSource = PlanLocalDataSource(InMemoryPreferencesDataStore()),
            weekPlanDtoToModelMapper = WeekPlanDtoToModelMapper(
                bookMapsProvider = BookMapsProvider(),
                chaptersRangeMapper = ChaptersRangeMapper(),
            ),
            localDateTimeProvider = LocalDateTimeProvider { error("Unexpected call") },
            readingPlanPreferenceMapper = ReadingPlanPreferenceMapperImpl(),
            syncedPreferenceDao = FakeSyncedPreferenceDao(),
            currentTimestampProvider = CurrentTimestampProvider { error("Unexpected call") },
        )
    }

    @Test
    fun `WHEN loading each bundled plan THEN returns its 52 weeks in order`() = runTest {
        // When
        val weekNumbersByPlan = ReadingPlanType.entries.map { type -> repository.getPlans(type).map { it.number } }

        // Then
        assertEquals(List(2) { (1..52).toList() }, weekNumbersByPlan)
    }

    @Test
    fun `WHEN loading the books order plan THEN starts with Genesis and ends with Revelation`() = runTest {
        // When
        val weeks = repository.getPlans(ReadingPlanType.BOOKS)

        // Then
        assertEquals(
            BookId.GEN,
            weeks
                .first()
                .days
                .first()
                .passages
                .first()
                .bookId,
        )
        assertEquals(
            BookId.REV,
            weeks
                .last()
                .days
                .last()
                .passages
                .last()
                .bookId,
        )
    }

    @Test
    fun `WHEN loading each bundled plan THEN every day schedules at least one passage`() = runTest {
        // When
        val days = ReadingPlanType.entries.flatMap { type -> repository.getPlans(type).flatMap { it.days } }

        // Then
        assertTrue(days.all { it.passages.isNotEmpty() })
    }

    @Test
    fun `GIVEN a plan already loaded WHEN loading it again THEN reuses the cached weeks`() = runTest {
        // Given
        val first = repository.getPlans(ReadingPlanType.CHRONOLOGICAL)

        // When
        val second = repository.getPlans(ReadingPlanType.CHRONOLOGICAL)

        // Then
        assertSame(first, second)
    }
}
