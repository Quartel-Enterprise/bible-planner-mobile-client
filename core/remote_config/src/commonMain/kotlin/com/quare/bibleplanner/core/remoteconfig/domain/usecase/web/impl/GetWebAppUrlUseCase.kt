package com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.impl

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetStringRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.GetWebAppUrl

internal class GetWebAppUrlUseCase(
    private val getStringRemoteConfig: GetStringRemoteConfig,
) : GetWebAppUrl {
    override suspend fun invoke(): String = getStringRemoteConfig(MORE_WEB_APP_URL).ifBlank { WEB_APP_URL_DEFAULT }

    companion object {
        private const val MORE_WEB_APP_URL = "more_web_app_url"
        private const val WEB_APP_URL_DEFAULT = "https://web.bibleplanner.app/"
    }
}
