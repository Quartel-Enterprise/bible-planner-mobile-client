package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveBooleanRemoteConfig
import kotlinx.coroutines.flow.Flow

class ObserveInstagramLinkVisibleInMobileUseCase(
    private val observeBooleanRemoteConfig: ObserveBooleanRemoteConfig,
) : ObserveInstagramLinkVisible {
    override fun invoke(): Flow<Boolean> = observeBooleanRemoteConfig("show_instagram")
}
