package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetBooleanRemoteConfig

internal class IsProMobileVerificationRequiredUseCase(
    private val getBooleanRemoteConfig: GetBooleanRemoteConfig,
) : IsProVerificationRequiredUseCase {
    override suspend operator fun invoke(): Boolean = getBooleanRemoteConfig("is_premium_verification_required")
}
