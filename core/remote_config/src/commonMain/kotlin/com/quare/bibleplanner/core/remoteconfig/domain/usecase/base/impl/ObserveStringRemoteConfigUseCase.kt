package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveStringRemoteConfig
import kotlinx.coroutines.flow.Flow

internal class ObserveStringRemoteConfigUseCase(
    private val remoteConfigService: RemoteConfigService,
) : ObserveStringRemoteConfig {
    override fun invoke(
        key: String,
        default: String,
    ): Flow<String> = remoteConfigService.observeString(
        key = key,
        defaultValue = default,
    )
}
