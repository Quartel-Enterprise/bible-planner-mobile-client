package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetBooleanRemoteConfig
import kotlinx.coroutines.flow.first

internal class GetBooleanRemoteConfigUseCase(
    private val remoteConfigService: RemoteConfigService,
) : GetBooleanRemoteConfig {
    override suspend fun invoke(
        key: String,
        default: Boolean,
    ): Boolean = remoteConfigService
        .observeBoolean(
            key = key,
            defaultValue = default,
        ).first()
}
