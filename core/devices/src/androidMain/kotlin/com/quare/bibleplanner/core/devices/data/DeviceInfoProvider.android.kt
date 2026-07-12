package com.quare.bibleplanner.core.devices.data

import android.os.Build
import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor

internal actual class DeviceInfoProvider actual constructor() {
    actual fun deviceName(): String = Build.MODEL?.takeIf { it.isNotBlank() }
        ?: Build.MANUFACTURER?.takeIf { it.isNotBlank() }
        ?: FALLBACK_NAME

    actual fun platform(): String = PLATFORM_ANDROID

    actual fun formFactor(): DeviceFormFactor = DeviceFormFactor.PHONE

    private companion object {
        const val PLATFORM_ANDROID = "android"
        const val FALLBACK_NAME = "Android"
    }
}
