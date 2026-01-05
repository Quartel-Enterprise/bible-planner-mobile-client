package com.quare.bibleplanner.core.remoteconfig.domain.usecase.impl

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.GetBooleanRemoteConfig

internal class GetBooleanRemoteConfigUseCase(
    private val remoteConfigService: RemoteConfigService,
): GetBooleanRemoteConfig {
    override suspend fun invoke(key: String): Boolean = remoteConfigService.getBoolean(key)
}
