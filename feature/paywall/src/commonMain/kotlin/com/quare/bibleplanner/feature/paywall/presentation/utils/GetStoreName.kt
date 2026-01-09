package com.quare.bibleplanner.feature.paywall.presentation.utils

import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.getPlatform

internal fun getStoreName(): String = when (getPlatform()) {
    Platform.IOS,
    Platform.MACOS,
    -> "App Store"

    else -> "Google Play Store"
}
