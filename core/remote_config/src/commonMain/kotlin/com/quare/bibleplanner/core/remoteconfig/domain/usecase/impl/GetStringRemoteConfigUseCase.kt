package com.quare.bibleplanner.core.remoteconfig.domain.usecase.impl

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.GetStringRemoteConfig

internal class GetStringRemoteConfigUseCase(
    private val remoteConfigService: RemoteConfigService,
) : GetStringRemoteConfig {
    override suspend fun invoke(key: String): String = remoteConfigService.getString(key)
}
