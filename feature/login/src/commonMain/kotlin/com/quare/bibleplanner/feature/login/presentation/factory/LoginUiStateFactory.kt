package com.quare.bibleplanner.feature.login.presentation.factory

import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.isApple
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiState

internal class LoginUiStateFactory(
    platform: Platform,
) {
    private val enabledProviders: List<LoginProvider> = if (platform.isApple()) {
        listOf(LoginProvider.APPLE, LoginProvider.GOOGLE)
    } else {
        listOf(LoginProvider.GOOGLE, LoginProvider.APPLE)
    }

    fun create(): LoginUiState = LoginUiState(
        enabledProviders = enabledProviders,
        isGoogleLoading = false,
        isAppleLoading = false,
        error = null,
    )
}
