package com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.impl

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.GetBooleanRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.IsMoreWebAppEnabled

internal class IsMoreWebAppEnabledUseCase(
    private val getBooleanRemoteConfig: GetBooleanRemoteConfig,
) : IsMoreWebAppEnabled {
    override suspend fun invoke(): Boolean = getBooleanRemoteConfig(MORE_WEB_APP_ENABLED_KEY)

    companion object Companion {
        private const val MORE_WEB_APP_ENABLED_KEY = "more_web_app_enabled"
    }
}
