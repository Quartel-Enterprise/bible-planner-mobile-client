package com.quare.bibleplanner.feature.loginsyncnudge.presentation.model

sealed interface LoginSyncNudgeUiEvent {
    data object OnLoginClick : LoginSyncNudgeUiEvent

    data object OnNotNow : LoginSyncNudgeUiEvent

    data object OnDismiss : LoginSyncNudgeUiEvent

    data class OnDontShowAgainToggled(
        val isChecked: Boolean,
    ) : LoginSyncNudgeUiEvent
}
