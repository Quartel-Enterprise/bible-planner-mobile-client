package com.quare.bibleplanner.feature.loginwarning.presentation.model

sealed interface LoginWarningUiAction {
    data object NavigateToLogin : LoginWarningUiAction

    data object NavigateBack : LoginWarningUiAction
}
