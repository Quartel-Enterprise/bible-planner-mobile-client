package com.quare.bibleplanner.feature.more.domain.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.GetBooleanRemoteConfig

class ShouldShowDonateOptionUseCase(
    private val getBooleanRemoteConfig: GetBooleanRemoteConfig,
) {
    suspend operator fun invoke(): Boolean = getBooleanRemoteConfig("show_donate")
}
