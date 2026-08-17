package com.quare.bibleplanner.core.devices.data

import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIUserInterfaceIdiomPhone

internal actual class DeviceInfoProvider actual constructor() {
    actual val deviceName: String
        get() = UIDevice.currentDevice.name

    actual val platform: String
        get() = PLATFORM_IOS

    actual val formFactor: DeviceFormFactor
        get() = when (UIDevice.currentDevice.userInterfaceIdiom) {
            UIUserInterfaceIdiomPad -> DeviceFormFactor.TABLET
            UIUserInterfaceIdiomPhone -> DeviceFormFactor.PHONE
            else -> DeviceFormFactor.UNKNOWN
        }

    private companion object {
        const val PLATFORM_IOS = "ios"
    }
}
