package com.quare.bibleplanner.core.devices.data

import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor

internal actual class DeviceInfoProvider actual constructor() {
    actual val deviceName: String
        get() = DEVICE_NAME

    actual val platform: String
        get() = PLATFORM_WEB

    actual val formFactor: DeviceFormFactor
        get() = DeviceFormFactor.COMPUTER

    private companion object {
        const val PLATFORM_WEB = "web"
        const val DEVICE_NAME = "Web browser"
    }
}
