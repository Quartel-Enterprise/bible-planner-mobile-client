package com.quare.bibleplanner.feature.loginsyncnudge.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.loginnudge.domain.usecase.DismissLoginNudgePermanently
import com.quare.bibleplanner.core.loginnudge.domain.usecase.SnoozeLoginNudge
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.model.LoginSyncNudgeUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LoginSyncNudgeViewModel(
    private val snoozeLoginNudge: SnoozeLoginNudge,
    private val dismissLoginNudgePermanently: DismissLoginNudgePermanently,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<LoginSyncNudgeUiEvent>(trackEvent) {
    private val _dontShowAgain: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val dontShowAgain: StateFlow<Boolean> = _dontShowAgain

    override fun handleEvent(event: LoginSyncNudgeUiEvent) {
        when (event) {
            is LoginSyncNudgeUiEvent.OnDontShowAgainToggled -> _dontShowAgain.update { event.isChecked }

            LoginSyncNudgeUiEvent.OnLoginClick -> {
                close(isLogin = true) {
                    navigator.navigateReplacingTop(LoginNavRoute(notifyResultViaSnackbar = true))
                }
            }

            LoginSyncNudgeUiEvent.OnNotNow,
            LoginSyncNudgeUiEvent.OnDismiss,
            -> close(isLogin = false) { navigator.navigateBack() }
        }
    }

    private fun close(
        isLogin: Boolean,
        navigate: () -> Unit,
    ) {
        viewModelScope.launch {
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
            navigate()
        }
    }
}
