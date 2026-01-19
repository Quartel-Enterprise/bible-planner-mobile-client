package com.quare.bibleplanner.feature.login.presentation.model

sealed interface LoginUiAction {
    data object NavigateBack : LoginUiAction

    data object CloseBottomSheet : LoginUiAction
}
