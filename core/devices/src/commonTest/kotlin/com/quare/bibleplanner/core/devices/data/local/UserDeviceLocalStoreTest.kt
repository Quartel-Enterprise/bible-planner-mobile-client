package com.quare.bibleplanner.core.devices.data.local

import com.quare.bibleplanner.core.devices.data.dto.UserDeviceDto
import com.quare.bibleplanner.core.devices.data.mapper.UserDeviceDtoToEntityMapper
import com.quare.bibleplanner.core.provider.room.dao.UserDeviceDao
import com.quare.bibleplanner.core.provider.room.entity.UserDeviceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserDeviceLocalStoreTest {
    private val dao = FakeUserDeviceDao()
    private val localStore = UserDeviceLocalStore(dao, UserDeviceDtoToEntityMapper())

    @Test
    fun `GIVEN an unknown row WHEN applying remote THEN inserts it`() = runTest {
        // When
        localStore.applyRemote(dto(id = "row-1", name = "iPhone", updatedAt = "2026-07-11T10:00:00Z"))

        // Then
        assertEquals("iPhone", dao.rows.getValue("row-1").name)
    }

    @Test
    fun `GIVEN a non-pending older row WHEN applying a newer remote THEN overwrites the name`() = runTest {
        // Given
        dao.rows["row-1"] = entity(id = "row-1", name = "Old", updatedAt = 1_000L, pending = false)

        // When
        localStore.applyRemote(dto(id = "row-1", name = "New", updatedAt = "2026-07-11T10:00:00Z"))

        // Then
        assertEquals("New", dao.rows.getValue("row-1").name)
    }

    @Test
    fun `GIVEN a pending row WHEN applying remote THEN keeps the local name`() = runTest {
        // Given a pending rename that is newer than the remote
        dao.rows["row-1"] = entity(id = "row-1", name = "Local", updatedAt = Long.MAX_VALUE, pending = true)

        // When
        localStore.applyRemote(dto(id = "row-1", name = "Remote", updatedAt = "2026-07-11T10:00:00Z"))

        // Then
        assertEquals("Local", dao.rows.getValue("row-1").name)
    }

    @Test
    fun `GIVEN extra local rows WHEN retaining only remote ids THEN deletes the absent ones`() = runTest {
        // Given
        dao.rows["keep"] = entity(id = "keep", name = "A", updatedAt = 1L, pending = false)
        dao.rows["drop"] = entity(id = "drop", name = "B", updatedAt = 1L, pending = false)

        // When
        localStore.retainOnly(listOf("keep"))

        // Then
        assertTrue(dao.rows.containsKey("keep"))
        assertNull(dao.rows["drop"])
    }

    private fun dto(
        id: String,
        name: String,
        updatedAt: String,
    ) = UserDeviceDto(
        id = id,
        userId = "user-1",
        deviceId = "device-$id",
        name = name,
        platform = "ios",
        formFactor = "phone",
        locationCity = null,
        locationCountry = null,
        lastActiveAt = "2026-07-11T12:00:00Z",
        updatedAt = updatedAt,
    )

    private fun entity(
        id: String,
        name: String,
        updatedAt: Long,
        pending: Boolean,
    ) = UserDeviceEntity(
        id = id,
        deviceId = "device-$id",
        name = name,
        platform = "ios",
        formFactor = "phone",
        locationCity = null,
        locationCountry = null,
        lastActiveAt = 1L,
        updatedAt = updatedAt,
        isNamePendingSync = pending,
    )
}

// Faithful in-memory stand-in for the Room DAO, replicating the LWW guards expressed in its SQL.
private class FakeUserDeviceDao : UserDeviceDao {
    val rows = linkedMapOf<String, UserDeviceEntity>()

    override fun observeAll(): Flow<List<UserDeviceEntity>> = flowOf(rows.values.toList())

    override suspend fun getById(id: String): UserDeviceEntity? = rows[id]

    override suspend fun upsert(device: UserDeviceEntity) {
        rows[device.id] = device
    }

    override suspend fun renameLocal(
        id: String,
        name: String,
        now: Long,
    ) {
        rows[id]?.let { rows[id] = it.copy(name = name, updatedAt = now, isNamePendingSync = true) }
    }

    override fun getPendingFlow(): Flow<List<UserDeviceEntity>> = flowOf(rows.values.filter { it.isNamePendingSync })

    override suspend fun getPending(): List<UserDeviceEntity> = rows.values.filter { it.isNamePendingSync }

    override suspend fun markNameSynced(
        id: String,
        syncedUpdatedAt: Long,
    ) {
        rows[id]?.let { if (it.updatedAt == syncedUpdatedAt) rows[id] = it.copy(isNamePendingSync = false) }
    }

    override suspend fun applyRemoteServerFields(
        id: String,
        deviceId: String,
        platform: String,
        formFactor: String,
        locationCity: String?,
        locationCountry: String?,
        lastActiveAt: Long,
    ) {
        rows[id]?.let {
            rows[id] = it.copy(
                deviceId = deviceId,
                platform = platform,
                formFactor = formFactor,
                locationCity = locationCity,
                locationCountry = locationCountry,
                lastActiveAt = lastActiveAt,
            )
        }
    }

    override suspend fun applyRemoteName(
        id: String,
        name: String,
        remoteUpdatedAt: Long,
    ) {
        rows[id]?.let {
            if (!it.isNamePendingSync && it.updatedAt < remoteUpdatedAt) {
                rows[id] = it.copy(name = name, updatedAt = remoteUpdatedAt)
            }
        }
    }

    override suspend fun deleteById(id: String) {
        rows.remove(id)
    }

    override suspend fun deleteNotIn(ids: List<String>) {
        rows.keys.retainAll(ids.toSet())
    }

    override suspend fun deleteAll() {
        rows.clear()
    }
}
