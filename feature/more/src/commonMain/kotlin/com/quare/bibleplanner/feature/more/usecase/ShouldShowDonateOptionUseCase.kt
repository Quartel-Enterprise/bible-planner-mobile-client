package com.quare.bibleplanner.feature.more.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService

class ShouldShowDonateOptionUseCase(
    private val remoteConfigService: RemoteConfigService,
) {
    suspend operator fun invoke(): Boolean = remoteConfigService.getBoolean("show_donate")
}
