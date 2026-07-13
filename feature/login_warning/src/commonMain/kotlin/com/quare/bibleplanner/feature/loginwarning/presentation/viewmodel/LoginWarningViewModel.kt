package com.quare.bibleplanner.feature.loginwarning.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.loginwarning.presentation.model.LoginWarningUiAction
import com.quare.bibleplanner.feature.loginwarning.presentation.model.LoginWarningUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class LoginWarningViewModel(
    route: LoginWarningNavRoute,
    trackEvent: TrackEvent,
) : TrackedViewModel<LoginWarningUiEvent>(trackEvent) {
    val reason: LoginWarningReason = LoginWarningReason.fromKey(route.reason)

    private val _uiAction: MutableSharedFlow<LoginWarningUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<LoginWarningUiAction> = _uiAction

    init {
        track(AnalyticsEventNames.LOGIN_WARNING_SHOWN)
    }

    override fun handleEvent(event: LoginWarningUiEvent) {
        viewModelScope.launch {
            _uiAction.emit(
                when (event) {
                    LoginWarningUiEvent.OnLoginClick -> {
                        track(AnalyticsEventNames.LOGIN_WARNING_ACCEPTED)
                        LoginWarningUiAction.NavigateToLogin
                    }

                    LoginWarningUiEvent.OnDismiss -> {
                        track(AnalyticsEventNames.LOGIN_WARNING_DISMISSED)
                        LoginWarningUiAction.NavigateBack
                    }
                },
            )
        }
    }

    private fun track(name: String) {
        trackEvent(
            name = name,
            params = mapOf(AnalyticsParams.REASON to reason.key),
        )
    }
}
