package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.GetBooleanRemoteConfig

internal class IsPremiumVerificationRequiredUseCase(
    private val getBooleanRemoteConfig: GetBooleanRemoteConfig,
) {
    suspend operator fun invoke(): Boolean = getBooleanRemoteConfig("is_premium_verification_required")
}
