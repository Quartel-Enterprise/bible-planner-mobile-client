package com.quare.bibleplanner.core.devices.data

import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIUserInterfaceIdiomPhone

internal actual class DeviceInfoProvider actual constructor() {
    actual fun deviceName(): String = UIDevice.currentDevice.name

    actual fun platform(): String = PLATFORM_IOS

    actual fun formFactor(): DeviceFormFactor = when (UIDevice.currentDevice.userInterfaceIdiom) {
        UIUserInterfaceIdiomPad -> DeviceFormFactor.TABLET
        UIUserInterfaceIdiomPhone -> DeviceFormFactor.PHONE
        else -> DeviceFormFactor.UNKNOWN
    }

    private companion object {
        const val PLATFORM_IOS = "ios"
    }
}
