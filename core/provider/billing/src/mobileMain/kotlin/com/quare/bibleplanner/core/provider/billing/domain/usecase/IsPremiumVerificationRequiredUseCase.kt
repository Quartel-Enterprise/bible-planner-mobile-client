package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService

internal class IsPremiumVerificationRequiredUseCase(
    private val remoteConfigService: RemoteConfigService,
) {
    suspend operator fun invoke(): Boolean = remoteConfigService.getBoolean(PREMIUM_VERIFICATION_REQUIRED_KEY)

    private companion object Companion {
        const val PREMIUM_VERIFICATION_REQUIRED_KEY = "is_premium_verification_required"
    }
}
