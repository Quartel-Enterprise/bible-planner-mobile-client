package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService

class IsInstagramLinkVisibleInMobileUseCase(private val remoteConfigService: RemoteConfigService): IsInstagramLinkVisibleUseCase {
    override suspend fun invoke(): Boolean = remoteConfigService.getBoolean("show_instagram")
}
