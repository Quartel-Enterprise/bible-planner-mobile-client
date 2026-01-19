package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig

internal class GetIntRemoteConfigUseCase(
    private val remoteConfigService: RemoteConfigService,
) : GetIntRemoteConfig {
    override suspend fun invoke(key: String): Int = remoteConfigService.getInt(key)
}
