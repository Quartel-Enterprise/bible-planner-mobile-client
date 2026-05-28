package com.quare.bibleplanner.feature.login.presentation.factory

import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiState

internal class LoginUiStateFactory {
    fun create(): LoginUiState = LoginUiState(
        enabledProviders = listOf(LoginProvider.GOOGLE),
        isGoogleLoading = false,
        isErrorVisible = false,
    )
}
