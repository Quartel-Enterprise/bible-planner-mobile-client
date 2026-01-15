package com.quare.bibleplanner.feature.login.presentation.model

sealed interface LoginUiAction {
    data object Dismiss : LoginUiAction
}
