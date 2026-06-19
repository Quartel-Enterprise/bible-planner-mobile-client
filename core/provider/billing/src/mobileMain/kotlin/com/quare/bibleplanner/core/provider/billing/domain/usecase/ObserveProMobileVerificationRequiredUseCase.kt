package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveBooleanRemoteConfig
import kotlinx.coroutines.flow.Flow

internal class ObserveProMobileVerificationRequiredUseCase(
    private val observeBooleanRemoteConfig: ObserveBooleanRemoteConfig,
) : ObserveProVerificationRequired {
    override fun invoke(): Flow<Boolean> = observeBooleanRemoteConfig("is_premium_verification_required")
}
