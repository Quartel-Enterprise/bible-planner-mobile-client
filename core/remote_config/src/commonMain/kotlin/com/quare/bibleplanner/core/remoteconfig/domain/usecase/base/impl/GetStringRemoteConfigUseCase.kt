package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetStringRemoteConfig

internal class GetStringRemoteConfigUseCase(
    private val remoteConfigService: RemoteConfigService,
) : GetStringRemoteConfig {
    override suspend fun invoke(key: String): String = remoteConfigService.getString(key)
}
