package com.quare.bibleplanner.core.devices.data.model

import com.quare.bibleplanner.core.devices.data.dto.UserDeviceDto

internal sealed interface DeviceChange {
    data class Upserted(
        val dto: UserDeviceDto,
    ) : DeviceChange

    data class Removed(
        val id: String,
    ) : DeviceChange
}
