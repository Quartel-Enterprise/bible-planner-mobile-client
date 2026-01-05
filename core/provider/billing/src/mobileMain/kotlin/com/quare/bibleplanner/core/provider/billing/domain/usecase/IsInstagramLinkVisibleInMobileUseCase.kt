package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.GetBooleanRemoteConfig

class IsInstagramLinkVisibleInMobileUseCase(
    private val getBooleanRemoteConfig: GetBooleanRemoteConfig,
) : IsInstagramLinkVisibleUseCase {
    override suspend fun invoke(): Boolean = getBooleanRemoteConfig("show_instagram")
}
