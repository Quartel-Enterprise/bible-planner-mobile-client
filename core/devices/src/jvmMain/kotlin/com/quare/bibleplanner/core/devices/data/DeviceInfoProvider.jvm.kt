package com.quare.bibleplanner.core.devices.data

import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor
import java.net.InetAddress

internal actual class DeviceInfoProvider actual constructor() {
    actual fun deviceName(): String = runCatching { InetAddress.getLocalHost().hostName }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: System.getProperty("user.name")?.takeIf { it.isNotBlank() }
        ?: FALLBACK_NAME

    actual fun platform(): String = PLATFORM_DESKTOP

    actual fun formFactor(): DeviceFormFactor = DeviceFormFactor.COMPUTER

    private companion object {
        const val PLATFORM_DESKTOP = "desktop"
        const val FALLBACK_NAME = "Computer"
    }
}
