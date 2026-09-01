package com.quare.bibleplanner.core.devices.data

import android.os.Build
import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor

internal actual class DeviceInfoProvider actual constructor() {
    actual val deviceName: String
        get() = Build.MODEL?.takeIf { it.isNotBlank() }
            ?: Build.MANUFACTURER?.takeIf { it.isNotBlank() }
            ?: FALLBACK_NAME

    actual val platform: String
        get() = PLATFORM_ANDROID

    actual val formFactor: DeviceFormFactor
        get() = DeviceFormFactor.PHONE

    private companion object {
        const val PLATFORM_ANDROID = "android"
        const val FALLBACK_NAME = "Android"
    }
}
