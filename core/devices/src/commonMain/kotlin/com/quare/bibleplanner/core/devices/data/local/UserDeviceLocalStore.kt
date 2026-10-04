package com.quare.bibleplanner.core.devices.data.local

import com.quare.bibleplanner.core.devices.data.dto.UserDeviceDto
import com.quare.bibleplanner.core.devices.data.mapper.UserDeviceDtoToEntityMapper
import com.quare.bibleplanner.core.provider.room.dao.UserDeviceDao
import com.quare.bibleplanner.core.provider.room.entity.UserDeviceEntity
import kotlinx.coroutines.flow.Flow

internal class UserDeviceLocalStore(
    private val userDeviceDao: UserDeviceDao,
    private val dtoToEntityMapper: UserDeviceDtoToEntityMapper,
) {
    fun observeAll(): Flow<List<UserDeviceEntity>> = userDeviceDao.observeAll()

    fun observePending(): Flow<List<UserDeviceEntity>> = userDeviceDao.getPendingFlow()

    suspend fun getPending(): List<UserDeviceEntity> = userDeviceDao.getPending()

    suspend fun renameLocal(
        deviceRowId: String,
        name: String,
        now: Long,
    ) {
        userDeviceDao.renameLocal(id = deviceRowId, name = name, now = now)
    }

    suspend fun markNameSynced(entity: UserDeviceEntity) {
        userDeviceDao.markNameSynced(id = entity.id, syncedUpdatedAt = entity.updatedAt)
    }

    /*
     * Why: server-authoritative fields always apply; the name is Last-Write-Wins (only when the local
     * row is not pending and strictly older); an unseen row is inserted as-is.
     */
    suspend fun applyRemote(dto: UserDeviceDto) {
        val entity = dtoToEntityMapper.map(dto)
        if (userDeviceDao.getById(entity.id) == null) {
            userDeviceDao.upsert(entity)
        } else {
            userDeviceDao.applyRemoteServerFields(
                id = entity.id,
                deviceId = entity.deviceId,
                platform = entity.platform,
                formFactor = entity.formFactor,
                locationCity = entity.locationCity,
                locationCountry = entity.locationCountry,
                lastActiveAt = entity.lastActiveAt,
            )
            userDeviceDao.applyRemoteName(id = entity.id, name = entity.name, remoteUpdatedAt = entity.updatedAt)
        }
    }

    suspend fun deleteById(deviceRowId: String) {
        userDeviceDao.deleteById(deviceRowId)
    }

    suspend fun retainOnly(ids: List<String>) {
        if (ids.isEmpty()) userDeviceDao.deleteAll() else userDeviceDao.deleteNotIn(ids)
    }

    suspend fun clearLocal() {
        userDeviceDao.deleteAll()
    }
}
