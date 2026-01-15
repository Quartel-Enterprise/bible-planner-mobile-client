package com.quare.bibleplanner.feature.login.presentation.model

sealed interface LoginUiEvent {
    data object Dismiss : LoginUiEvent
}
