package com.quare.bibleplanner.feature.login.presentation.model

import com.quare.bibleplanner.feature.login.domain.model.LoginProvider

data class LoginUiState(
    val enabledProviders: List<LoginProvider>,
)
