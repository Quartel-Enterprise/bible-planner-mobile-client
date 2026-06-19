package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveIntRemoteConfig
import kotlinx.coroutines.flow.Flow

internal class ObserveIntRemoteConfigUseCase(
    private val remoteConfigService: RemoteConfigService,
) : ObserveIntRemoteConfig {
    override fun invoke(
        key: String,
        default: Int,
    ): Flow<Int> = remoteConfigService.observeInt(
        key = key,
        defaultValue = default,
    )
}
