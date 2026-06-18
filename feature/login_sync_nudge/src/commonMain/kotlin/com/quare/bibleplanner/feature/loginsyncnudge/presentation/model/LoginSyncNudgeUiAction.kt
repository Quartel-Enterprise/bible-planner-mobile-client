package com.quare.bibleplanner.feature.loginsyncnudge.presentation.model

sealed interface LoginSyncNudgeUiAction {
    data object NavigateToLogin : LoginSyncNudgeUiAction

    data object Dismiss : LoginSyncNudgeUiAction
}
