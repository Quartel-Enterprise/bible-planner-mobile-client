package com.quare.bibleplanner.feature.login.presentation.model

import org.jetbrains.compose.resources.StringResource

sealed interface LoginUiAction {
    data object NavigateBack : LoginUiAction

    data object CloseBottomSheet : LoginUiAction

    data class NotifyLoginResult(
        val message: StringResource,
    ) : LoginUiAction
}
