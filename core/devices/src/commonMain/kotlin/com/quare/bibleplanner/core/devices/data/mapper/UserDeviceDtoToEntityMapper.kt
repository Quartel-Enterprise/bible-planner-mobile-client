package com.quare.bibleplanner.core.devices.data.mapper

import com.quare.bibleplanner.core.devices.data.dto.UserDeviceDto
import com.quare.bibleplanner.core.provider.room.entity.UserDeviceEntity
import kotlin.time.Instant

internal class UserDeviceDtoToEntityMapper {
    fun map(dto: UserDeviceDto): UserDeviceEntity = UserDeviceEntity(
        id = dto.id,
        deviceId = dto.deviceId,
        name = dto.name,
        platform = dto.platform,
        formFactor = dto.formFactor,
        locationCity = dto.locationCity,
        locationCountry = dto.locationCountry,
        lastActiveAt = Instant.parse(dto.lastActiveAt).toEpochMilliseconds(),
        updatedAt = Instant.parse(dto.updatedAt).toEpochMilliseconds(),
        isNamePendingSync = false,
    )
}
