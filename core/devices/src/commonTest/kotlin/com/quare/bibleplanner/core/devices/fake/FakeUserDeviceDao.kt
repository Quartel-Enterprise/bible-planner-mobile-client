package com.quare.bibleplanner.core.devices.fake

import com.quare.bibleplanner.core.provider.room.dao.UserDeviceDao
import com.quare.bibleplanner.core.provider.room.entity.UserDeviceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeUserDeviceDao(
    initialRows: List<UserDeviceEntity> = emptyList(),
) : UserDeviceDao {
    val rows = MutableStateFlow(initialRows)

    override fun observeAll(): Flow<List<UserDeviceEntity>> = rows

    override suspend fun getById(id: String): UserDeviceEntity? = rows.value.find { it.id == id }

    override suspend fun upsert(device: UserDeviceEntity) {
        rows.value = rows.value.filterNot { it.id == device.id } + device
    }

    override suspend fun renameLocal(
        id: String,
        name: String,
        now: Long,
    ) {
        updateRow(id) { row ->
            row.copy(
                name = name,
                updatedAt = now,
                isNamePendingSync = true,
            )
        }
    }

    override fun getPendingFlow(): Flow<List<UserDeviceEntity>> =
        rows.map { current -> current.filter { it.isNamePendingSync } }

    override suspend fun getPending(): List<UserDeviceEntity> = rows.value.filter { it.isNamePendingSync }

    override suspend fun markNameSynced(
        id: String,
        syncedUpdatedAt: Long,
    ) {
        updateRow(id) { row ->
            if (row.updatedAt == syncedUpdatedAt) row.copy(isNamePendingSync = false) else row
        }
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
        updateRow(id) { row ->
            row.copy(
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
        updateRow(id) { row ->
            if (!row.isNamePendingSync && row.updatedAt < remoteUpdatedAt) {
                row.copy(
                    name = name,
                    updatedAt = remoteUpdatedAt,
                )
            } else {
                row
            }
        }
    }

    override suspend fun deleteById(id: String) {
        rows.value = rows.value.filterNot { it.id == id }
    }

    override suspend fun deleteNotIn(ids: List<String>) {
        rows.value = rows.value.filter { it.id in ids }
    }

    override suspend fun deleteAll() {
        rows.value = emptyList()
    }

    private fun updateRow(
        id: String,
        transform: (UserDeviceEntity) -> UserDeviceEntity,
    ) {
        rows.value = rows.value.map { row -> if (row.id == id) transform(row) else row }
    }
}
