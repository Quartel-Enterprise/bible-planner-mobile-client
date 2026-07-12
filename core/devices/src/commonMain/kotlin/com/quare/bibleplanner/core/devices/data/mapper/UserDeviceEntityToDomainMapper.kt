package com.quare.bibleplanner.core.devices.data.mapper

import com.quare.bibleplanner.core.devices.domain.model.DeviceModel
import com.quare.bibleplanner.core.provider.room.entity.UserDeviceEntity
import kotlin.time.Instant

internal class UserDeviceEntityToDomainMapper {
    fun map(
        entity: UserDeviceEntity,
        currentDeviceId: String,
    ): DeviceModel = DeviceModel(
        id = entity.id,
        deviceId = entity.deviceId,
        name = entity.name,
        formFactor = entity.formFactor.toDeviceFormFactor(),
        locationCity = entity.locationCity,
        locationCountry = entity.locationCountry,
        lastActiveAt = Instant.fromEpochMilliseconds(entity.lastActiveAt),
        isCurrentDevice = entity.deviceId == currentDeviceId,
    )
}
