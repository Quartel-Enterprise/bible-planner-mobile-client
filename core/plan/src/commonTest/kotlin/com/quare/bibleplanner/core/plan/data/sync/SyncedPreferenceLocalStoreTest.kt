package com.quare.bibleplanner.core.plan.data.sync

import com.quare.bibleplanner.core.plan.data.dto.UserPreferenceDto
import com.quare.bibleplanner.core.plan.data.mapper.UserPreferenceMapper
import com.quare.bibleplanner.core.plan.fake.FakeSyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SyncedPreferenceLocalStoreTest {
    private lateinit var dao: FakeSyncedPreferenceDao
    private lateinit var store: SyncedPreferenceLocalStore

    @BeforeTest
    fun setUp() {
        dao = FakeSyncedPreferenceDao()
        store = SyncedPreferenceLocalStore(
            dao = dao,
            mapper = UserPreferenceMapper(),
        )
    }

    @Test
    fun `GIVEN a local write and a provisional default WHEN reading pending rows THEN returns only the local write`() =
        runTest {
            // Given
            dao.setLocal(
                key = "theme",
                value = "dark",
                updatedAt = 1_000L,
            )
            dao.seedProvisional(
                key = "plan_start_date",
                value = "0",
            )

            // When
            val observed = store.observePending().first()
            val snapshot = store.getPending()

            // Then
            assertEquals(listOf("theme"), observed.map(SyncedPreferenceEntity::key))
            assertEquals(observed, snapshot)
        }

    @Test
    fun `GIVEN a pushed preference WHEN marking it synced THEN clears it at its own timestamp`() = runTest {
        // When
        store.markSynced(entity(updatedAt = 1_000L))

        // Then
        assertEquals(listOf("markSynced(theme, 1000)"), dao.calls)
    }

    @Test
    fun `GIVEN no local value WHEN applying a remote preference THEN stores it as synced`() = runTest {
        // When
        store.applyRemote(
            UserPreferenceDto(
                userId = "user-1",
                key = "theme",
                value = "light",
                updatedAt = "1970-01-01T00:00:02Z",
            ),
        )

        // Then
        assertEquals(
            SyncedPreferenceEntity(
                key = "theme",
                value = "light",
                updatedAt = 2_000L,
                pendingSync = false,
            ),
            dao.rows.value["theme"],
        )
    }

    @Test
    fun `GIVEN a preference WHEN converting it THEN builds the remote row for the user`() {
        // When
        val dto = store.toDto(
            userId = "user-1",
            entity = entity(updatedAt = 1_000L),
        )

        // Then
        assertEquals(
            UserPreferenceDto(
                userId = "user-1",
                key = "theme",
                value = "dark",
                updatedAt = "1970-01-01T00:00:01Z",
            ),
            dto,
        )
    }

    @Test
    fun `GIVEN provisional defaults WHEN adopting them and clearing THEN delegates to the dao`() = runTest {
        // When
        store.adoptProvisionalDefaults(now = 7L)
        store.clearLocal()

        // Then
        assertEquals(listOf("adoptProvisional(7)", "deleteAll"), dao.calls)
    }

    private fun entity(updatedAt: Long): SyncedPreferenceEntity = SyncedPreferenceEntity(
        key = "theme",
        value = "dark",
        updatedAt = updatedAt,
        pendingSync = true,
    )
}
