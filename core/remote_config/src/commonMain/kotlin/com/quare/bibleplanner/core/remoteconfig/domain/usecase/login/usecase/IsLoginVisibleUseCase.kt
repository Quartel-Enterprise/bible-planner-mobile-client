package com.quare.bibleplanner.core.remoteconfig.domain.usecase.login.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.login.IsLoginVisible

class IsLoginVisibleUseCase(
    private val remoteConfigService: RemoteConfigService,
) : IsLoginVisible {
    override suspend fun invoke(): Boolean = remoteConfigService.getBoolean("is_login_visible")
}
