package com.quare.bibleplanner.core.provider.room.dao

import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SyncedPreferenceDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: SyncedPreferenceDao

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
        dao = database.syncedPreferenceDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN no value WHEN applying a remote one THEN stores it as synced`() = runTest {
        // When
        dao.applyRemote(
            key = KEY,
            value = "dark",
            remoteUpdatedAt = 10L,
        )

        // Then
        assertEquals(
            expected = "dark",
            actual = dao.observeValue(KEY).first(),
        )
        assertEquals(
            expected = emptyList(),
            actual = dao.getPending(),
        )
    }

    @Test
    fun `GIVEN an older synced value WHEN applying a newer remote one THEN overwrites it`() = runTest {
        // Given
        dao.applyRemote(
            key = KEY,
            value = "dark",
            remoteUpdatedAt = 10L,
        )

        // When
        dao.applyRemote(
            key = KEY,
            value = "light",
            remoteUpdatedAt = 20L,
        )

        // Then
        assertEquals(
            expected = "light",
            actual = dao.observeValue(KEY).first(),
        )
    }

    @Test
    fun `GIVEN a pending local value WHEN applying a remote one THEN keeps the local value`() = runTest {
        // Given
        dao.setLocal(
            key = KEY,
            value = "dark",
            updatedAt = 10L,
        )

        // When
        dao.applyRemote(
            key = KEY,
            value = "light",
            remoteUpdatedAt = 20L,
        )

        // Then
        assertEquals(
            expected = listOf(
                SyncedPreferenceEntity(
                    key = KEY,
                    value = "dark",
                    updatedAt = 10L,
                    pendingSync = true,
                ),
            ),
            actual = dao.getPendingFlow().first(),
        )
    }

    @Test
    fun `GIVEN a provisional default WHEN adopting it THEN it becomes a pending change`() = runTest {
        // Given
        dao.seedProvisional(
            key = KEY,
            value = "system",
        )

        // When
        dao.adoptProvisional(now = 50L)

        // Then
        assertEquals(
            expected = listOf(
                SyncedPreferenceEntity(
                    key = KEY,
                    value = "system",
                    updatedAt = 50L,
                    pendingSync = true,
                ),
            ),
            actual = dao.getPending(),
        )
    }

    private companion object {
        const val KEY = "theme"
    }
}
