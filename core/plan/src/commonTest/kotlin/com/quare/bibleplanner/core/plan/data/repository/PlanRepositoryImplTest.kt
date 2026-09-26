package com.quare.bibleplanner.core.plan.data.repository

import com.quare.bibleplanner.core.books.data.provider.BookMapsProvider
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.data.datasource.PlanLocalDataSource
import com.quare.bibleplanner.core.plan.data.mapper.ChaptersRangeMapper
import com.quare.bibleplanner.core.plan.data.mapper.ReadingPlanPreferenceMapperImpl
import com.quare.bibleplanner.core.plan.data.mapper.WeekPlanDtoToModelMapper
import com.quare.bibleplanner.core.plan.fake.FakeSyncedPreferenceDao
import com.quare.bibleplanner.core.plan.fake.InMemoryPreferencesDataStore
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

internal class PlanRepositoryImplTest {
    private val now = 1_700_000_000_000L

    private lateinit var dao: FakeSyncedPreferenceDao
    private lateinit var repository: PlanRepositoryImpl

    @BeforeTest
    fun setUp() {
        dao = FakeSyncedPreferenceDao()
        repository = PlanRepositoryImpl(
            planLocalDataSource = PlanLocalDataSource(InMemoryPreferencesDataStore()),
            weekPlanDtoToModelMapper = WeekPlanDtoToModelMapper(
                bookMapsProvider = BookMapsProvider(),
                chaptersRangeMapper = ChaptersRangeMapper(),
            ),
            localDateTimeProvider = LocalDateTimeProvider { timestamp ->
                Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.UTC)
            },
            readingPlanPreferenceMapper = ReadingPlanPreferenceMapperImpl(),
            syncedPreferenceDao = dao,
            currentTimestampProvider = CurrentTimestampProvider { now },
        )
    }

    @Test
    fun `GIVEN no stored start date WHEN observing it THEN emits null`() = runTest {
        // When
        val startDate = repository.getStartPlanTimestamp().first()

        // Then
        assertNull(startDate)
    }

    @Test
    fun `GIVEN a start timestamp WHEN storing it THEN flags it pending and exposes its date`() = runTest {
        // When
        repository.setStartPlanTimestamp(START_TIMESTAMP)

        // Then
        assertEquals(
            SyncedPreferenceEntity(
                key = "plan_start_date",
                value = START_TIMESTAMP.toString(),
                updatedAt = now,
                pendingSync = true,
            ),
            dao.rows.value["plan_start_date"],
        )
        assertEquals(
            LocalDate(
                year = 2024,
                month = 3,
                day = 15,
            ),
            repository.getStartPlanTimestamp().first(),
        )
    }

    @Test
    fun `GIVEN a corrupted start date WHEN observing it THEN emits null`() = runTest {
        // Given
        dao.setLocal(
            key = "plan_start_date",
            value = "not a number",
            updatedAt = 1L,
        )

        // When
        val startDate = repository.getStartPlanTimestamp().first()

        // Then
        assertNull(startDate)
    }

    @Test
    fun `GIVEN no stored plan WHEN observing the selected plan THEN defaults to chronological`() = runTest {
        // When
        val readingPlanType = repository.getSelectedReadingPlanFlow().first()

        // Then
        assertEquals(ReadingPlanType.CHRONOLOGICAL, readingPlanType)
    }

    @Test
    fun `GIVEN a selected plan WHEN storing it THEN flags it pending and emits it`() = runTest {
        // When
        repository.setSelectedReadingPlan(ReadingPlanType.BOOKS)

        // Then
        assertEquals(
            SyncedPreferenceEntity(
                key = "selected_reading_plan",
                value = "books",
                updatedAt = now,
                pendingSync = true,
            ),
            dao.rows.value["selected_reading_plan"],
        )
        assertEquals(ReadingPlanType.BOOKS, repository.getSelectedReadingPlanFlow().first())
    }

    @Test
    fun `GIVEN a stored start date WHEN seeding the default THEN keeps the stored one`() = runTest {
        // Given
        repository.setStartPlanTimestamp(START_TIMESTAMP)

        // When
        repository.seedDefaultStartDate(0L)

        // Then
        assertEquals(
            START_TIMESTAMP.toString(),
            dao.rows.value
                .getValue("plan_start_date")
                .value,
        )
    }

    @Test
    fun `GIVEN no start date WHEN seeding the default THEN stores it as a non-pending provisional value`() = runTest {
        // When
        repository.seedDefaultStartDate(START_TIMESTAMP)

        // Then
        assertEquals(
            SyncedPreferenceEntity(
                key = "plan_start_date",
                value = START_TIMESTAMP.toString(),
                updatedAt = 0L,
                pendingSync = false,
            ),
            dao.rows.value["plan_start_date"],
        )
    }

    private companion object {
        const val START_TIMESTAMP = 1_710_504_000_000L
    }
}
