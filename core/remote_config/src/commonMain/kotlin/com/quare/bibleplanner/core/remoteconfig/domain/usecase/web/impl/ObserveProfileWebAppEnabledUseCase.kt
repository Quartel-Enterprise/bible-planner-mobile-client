package com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.impl

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveBooleanRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.ObserveProfileWebAppEnabled
import kotlinx.coroutines.flow.Flow

internal class ObserveProfileWebAppEnabledUseCase(
    private val observeBooleanRemoteConfig: ObserveBooleanRemoteConfig,
) : ObserveProfileWebAppEnabled {
    override fun invoke(): Flow<Boolean> = observeBooleanRemoteConfig(PROFILE_WEB_APP_ENABLED_KEY)

    companion object {
        private const val PROFILE_WEB_APP_ENABLED_KEY = "profile_web_app_enabled"
    }
}
