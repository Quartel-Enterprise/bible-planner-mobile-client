package com.quare.bibleplanner.feature.profile.domain.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveBooleanRemoteConfig
import kotlinx.coroutines.flow.Flow

class ObserveShowDonateOptionUseCase(
    private val observeBooleanRemoteConfig: ObserveBooleanRemoteConfig,
) {
    operator fun invoke(): Flow<Boolean> = observeBooleanRemoteConfig("show_donate")
}
