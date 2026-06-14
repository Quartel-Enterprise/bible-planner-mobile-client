package com.quare.bibleplanner.feature.loginwarning.presentation.model

sealed interface LoginWarningUiEvent {
    data object OnLoginClick : LoginWarningUiEvent

    data object OnDismiss : LoginWarningUiEvent
}
