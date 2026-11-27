package com.quare.bibleplanner.feature.login.presentation.model

sealed interface LoginUiAction {
    data class NotifyLoginResult(
        val message: LoginResultMessage,
    ) : LoginUiAction
}
