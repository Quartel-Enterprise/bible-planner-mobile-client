package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveBooleanRemoteConfig
import kotlinx.coroutines.flow.Flow

internal class ObserveBooleanRemoteConfigUseCase(
    private val remoteConfigService: RemoteConfigService,
) : ObserveBooleanRemoteConfig {
    override fun invoke(
        key: String,
        default: Boolean,
    ): Flow<Boolean> = remoteConfigService.observeBoolean(
        key = key,
        defaultValue = default,
    )
}
