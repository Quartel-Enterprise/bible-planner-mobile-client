package com.quare.bibleplanner.core.plan.data.sync

import com.quare.bibleplanner.core.plan.data.dto.DayMetaDto
import com.quare.bibleplanner.core.plan.data.mapper.DayMetaMapper
import com.quare.bibleplanner.core.plan.fake.ThrowingDayDao
import com.quare.bibleplanner.core.provider.room.entity.DayEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DayMetaLocalStoreTest {
    private val pendingDay = DayEntity(
        id = 1,
        weekNumber = 2,
        dayNumber = 3,
        readingPlanType = "BOOKS",
        isRead = true,
        readTimestamp = 500L,
        notes = "Great chapter",
        metaUpdatedAt = 1_000L,
        isMetaPendingSync = true,
    )
    private val remoteMeta = DayMetaDto(
        userId = "user-1",
        weekNumber = 2,
        dayNumber = 3,
        planType = "BOOKS",
        readTimestamp = 900L,
        notes = "Remote note",
        updatedAt = "1970-01-01T00:00:02Z",
    )

    private lateinit var dayDao: RecordingDayDao
    private lateinit var store: DayMetaLocalStore

    @Test
    fun `GIVEN pending days WHEN reading them THEN returns the dao rows for both the flow and the snapshot`() =
        runTest {
            // Given
            prepareScenario()

            // When
            val observed = store.observePending().first()
            val snapshot = store.getPending()

            // Then
            assertEquals(listOf(pendingDay), observed)
            assertEquals(listOf(pendingDay), snapshot)
        }

    @Test
    fun `GIVEN a pushed day WHEN marking it synced THEN clears it at the pushed timestamp`() = runTest {
        // Given
        prepareScenario()

        // When
        store.markSynced(pendingDay)

        // Then
        assertEquals(listOf("markDayMetaSynced(2, 3, BOOKS, 1000)"), dayDao.calls)
    }

    @Test
    fun `GIVEN a day without meta timestamp WHEN marking it synced THEN does nothing`() = runTest {
        // Given
        prepareScenario()

        // When
        store.markSynced(pendingDay.copy(metaUpdatedAt = null))

        // Then
        assertTrue(dayDao.calls.isEmpty())
    }

    @Test
    fun `GIVEN an existing day WHEN applying remote meta THEN updates it in place`() = runTest {
        // Given
        prepareScenario(
            changedRows = 1,
            existingDay = pendingDay,
        )

        // When
        store.applyRemote(remoteMeta)

        // Then
        assertEquals(listOf("applyRemoteDayMeta(2, 3, BOOKS, 900, Remote note, 2000)"), dayDao.calls)
        assertTrue(dayDao.insertedDays.isEmpty())
    }

    @Test
    fun `GIVEN a stale remote meta for an existing day WHEN applying it THEN keeps the local day`() = runTest {
        // Given
        prepareScenario(
            changedRows = 0,
            existingDay = pendingDay,
        )

        // When
        store.applyRemote(remoteMeta)

        // Then
        assertTrue(dayDao.insertedDays.isEmpty())
    }

    @Test
    fun `GIVEN a day missing locally WHEN applying remote meta THEN inserts it as already synced`() = runTest {
        // Given
        prepareScenario(
            changedRows = 0,
            existingDay = null,
        )

        // When
        store.applyRemote(remoteMeta)

        // Then
        assertEquals(
            listOf(
                DayEntity(
                    weekNumber = 2,
                    dayNumber = 3,
                    readingPlanType = "BOOKS",
                    isRead = false,
                    readTimestamp = 900L,
                    notes = "Remote note",
                    metaUpdatedAt = 2_000L,
                    isMetaPendingSync = false,
                ),
            ),
            dayDao.insertedDays,
        )
    }

    @Test
    fun `GIVEN a pending day WHEN converting it THEN builds the remote row for the user`() {
        // Given
        prepareScenario()

        // When
        val dto = store.toDto(
            userId = "user-1",
            entity = pendingDay,
        )

        // Then
        assertEquals(
            DayMetaDto(
                userId = "user-1",
                weekNumber = 2,
                dayNumber = 3,
                planType = "BOOKS",
                readTimestamp = 500L,
                notes = "Great chapter",
                updatedAt = "1970-01-01T00:00:01Z",
            ),
            dto,
        )
    }

    @Test
    fun `GIVEN a first launch WHEN seeding and clearing THEN delegates to the day dao`() = runTest {
        // Given
        prepareScenario()

        // When
        store.seed(now = 5L)
        store.clearLocal()

        // Then
        assertEquals(listOf("markLegacyDayMetaPending(5)", "clearAllDayMetaSync"), dayDao.calls)
    }

    private fun prepareScenario(
        changedRows: Int = 0,
        existingDay: DayEntity? = null,
    ) {
        dayDao = RecordingDayDao(
            pending = listOf(pendingDay),
            changedRows = changedRows,
            existingDay = existingDay,
        )
        store = DayMetaLocalStore(
            dayDao = dayDao,
            dayMetaMapper = DayMetaMapper(),
        )
    }
}

private class RecordingDayDao(
    private val pending: List<DayEntity>,
    private val changedRows: Int,
    private val existingDay: DayEntity?,
) : ThrowingDayDao() {
    val calls = mutableListOf<String>()
    val insertedDays = mutableListOf<DayEntity>()

    override fun getPendingDayMetaSyncFlow(): Flow<List<DayEntity>> = flowOf(pending)

    override suspend fun getPendingDayMetaSync(): List<DayEntity> = pending

    override suspend fun markDayMetaSynced(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
        syncedUpdatedAt: Long,
    ) {
        calls += "markDayMetaSynced($weekNumber, $dayNumber, $readingPlanType, $syncedUpdatedAt)"
    }

    override suspend fun applyRemoteDayMeta(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
        readTimestamp: Long?,
        notes: String?,
        remoteUpdatedAt: Long,
    ): Int {
        calls +=
            "applyRemoteDayMeta($weekNumber, $dayNumber, $readingPlanType, $readTimestamp, $notes, $remoteUpdatedAt)"
        return changedRows
    }

    override suspend fun getDayByWeekAndDay(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
    ): DayEntity? = existingDay

    override suspend fun insertDay(day: DayEntity): Long {
        insertedDays += day
        return 1L
    }

    override suspend fun markLegacyDayMetaPending(now: Long) {
        calls += "markLegacyDayMetaPending($now)"
    }

    override suspend fun clearAllDayMetaSync() {
        calls += "clearAllDayMetaSync"
    }
}
