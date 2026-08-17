package com.quare.bibleplanner.core.devices.data

import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor

internal expect class DeviceInfoProvider() {
    val deviceName: String

    val platform: String

    val formFactor: DeviceFormFactor
}
