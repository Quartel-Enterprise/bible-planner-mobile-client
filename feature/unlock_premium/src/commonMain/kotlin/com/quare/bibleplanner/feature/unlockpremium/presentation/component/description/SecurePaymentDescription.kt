package com.quare.bibleplanner.feature.unlockpremium.presentation.component.description

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import bibleplanner.feature.unlock_premium.generated.resources.Res
import bibleplanner.feature.unlock_premium.generated.resources.footer_payment_info
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.getPlatform
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SecurePaymentDescription() {
    val platform = remember { getPlatform() }
    val store = when (platform) {
        Platform.IOS,
        Platform.MACOS,
        -> "App Store"

        Platform.ANDROID,
        Platform.LINUX,
        Platform.WINDOWS,
        -> "Google Play Store"
    }
    BaseTextDescription(
        text = stringResource(Res.string.footer_payment_info, store),
    )
}
