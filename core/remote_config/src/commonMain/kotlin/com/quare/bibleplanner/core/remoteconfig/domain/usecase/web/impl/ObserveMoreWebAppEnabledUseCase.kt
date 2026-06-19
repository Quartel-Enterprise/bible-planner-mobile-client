package com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.impl

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveBooleanRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.ObserveMoreWebAppEnabled
import kotlinx.coroutines.flow.Flow

internal class ObserveMoreWebAppEnabledUseCase(
    private val observeBooleanRemoteConfig: ObserveBooleanRemoteConfig,
) : ObserveMoreWebAppEnabled {
    override fun invoke(): Flow<Boolean> = observeBooleanRemoteConfig(MORE_WEB_APP_ENABLED_KEY)

    companion object {
        private const val MORE_WEB_APP_ENABLED_KEY = "more_web_app_enabled"
    }
}
