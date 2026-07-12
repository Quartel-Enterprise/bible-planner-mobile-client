package com.quare.bibleplanner.core.devices.data

import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor

internal expect class DeviceInfoProvider() {
    fun deviceName(): String

    fun platform(): String

    fun formFactor(): DeviceFormFactor
}
