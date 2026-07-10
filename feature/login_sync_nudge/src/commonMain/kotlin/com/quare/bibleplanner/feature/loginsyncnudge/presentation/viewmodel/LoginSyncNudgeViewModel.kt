package com.quare.bibleplanner.feature.loginsyncnudge.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.loginnudge.domain.usecase.DismissLoginNudgePermanently
import com.quare.bibleplanner.core.loginnudge.domain.usecase.SnoozeLoginNudge
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.model.LoginSyncNudgeUiAction
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.model.LoginSyncNudgeUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LoginSyncNudgeViewModel(
    private val snoozeLoginNudge: SnoozeLoginNudge,
    private val dismissLoginNudgePermanently: DismissLoginNudgePermanently,
    private val trackEvent: TrackEvent,
) : ViewModel() {
    private val _dontShowAgain: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val dontShowAgain: StateFlow<Boolean> = _dontShowAgain

    private val _uiAction: MutableSharedFlow<LoginSyncNudgeUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<LoginSyncNudgeUiAction> = _uiAction

    fun onEvent(event: LoginSyncNudgeUiEvent) {
        when (event) {
            is LoginSyncNudgeUiEvent.OnDontShowAgainToggled -> _dontShowAgain.update { event.isChecked }

            LoginSyncNudgeUiEvent.OnLoginClick -> {
                close(
                    isLogin = true,
                    action = LoginSyncNudgeUiAction.NavigateToLogin,
                )
            }

            LoginSyncNudgeUiEvent.OnNotNow,
            LoginSyncNudgeUiEvent.OnDismiss,
            -> {
                close(
                    isLogin = false,
                    action = LoginSyncNudgeUiAction.Dismiss,
                )
            }
        }
    }

    private fun close(
        isLogin: Boolean,
        action: LoginSyncNudgeUiAction,
    ) {
        viewModelScope.launch {
            if (isLogin) {
                trackEvent(
                    name = AnalyticsEventNames.LOGIN_NUDGE_ACCEPTED,
                    params = emptyMap(),
                )
            }
            when {
                _dontShowAgain.value -> {
                    dismissLoginNudgePermanently()
                    trackEvent(
                        name = AnalyticsEventNames.LOGIN_NUDGE_DISABLED,
                        params = emptyMap(),
                    )
                }

                !isLogin -> {
                    snoozeLoginNudge()
                    trackEvent(
                        name = AnalyticsEventNames.LOGIN_NUDGE_SNOOZED,
                        params = emptyMap(),
                    )
                }
            }
            _uiAction.emit(action)
        }
    }
}
