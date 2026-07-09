package com.quare.bibleplanner.feature.more.presentation.model

import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.diagnostics_subscription_free
import bibleplanner.feature.more.generated.resources.diagnostics_subscription_pro
import bibleplanner.feature.more.generated.resources.platform_android
import bibleplanner.feature.more.generated.resources.platform_desktop
import bibleplanner.feature.more.generated.resources.platform_ios
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.platform.Platform
import org.jetbrains.compose.resources.StringResource

internal fun Platform.toStringResource(): StringResource = when (this) {
    Platform.Android -> Res.string.platform_android
    Platform.Ios -> Res.string.platform_ios
    is Platform.Desktop -> Res.string.platform_desktop
}

internal fun SubscriptionStatus.toStringResource(): StringResource = when (this) {
    SubscriptionStatus.Free -> Res.string.diagnostics_subscription_free
    is SubscriptionStatus.Pro -> Res.string.diagnostics_subscription_pro
}
