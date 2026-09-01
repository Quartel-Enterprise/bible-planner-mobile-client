package com.quare.bibleplanner.core.devices.data

import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor
import java.net.InetAddress

internal actual class DeviceInfoProvider actual constructor() {
    actual val deviceName: String
        get() = runCatching { InetAddress.getLocalHost().hostName }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: System.getProperty("user.name")?.takeIf { it.isNotBlank() }
            ?: FALLBACK_NAME

    actual val platform: String
        get() = PLATFORM_DESKTOP

    actual val formFactor: DeviceFormFactor
        get() = DeviceFormFactor.COMPUTER

    private companion object {
        const val PLATFORM_DESKTOP = "desktop"
        const val FALLBACK_NAME = "Computer"
    }
}
