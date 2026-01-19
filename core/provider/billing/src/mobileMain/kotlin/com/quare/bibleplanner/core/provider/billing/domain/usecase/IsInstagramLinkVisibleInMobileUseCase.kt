package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetBooleanRemoteConfig

class IsInstagramLinkVisibleInMobileUseCase(
    private val getBooleanRemoteConfig: GetBooleanRemoteConfig,
) : IsInstagramLinkVisibleUseCase {
    override suspend fun invoke(): Boolean = getBooleanRemoteConfig("show_instagram")
}
