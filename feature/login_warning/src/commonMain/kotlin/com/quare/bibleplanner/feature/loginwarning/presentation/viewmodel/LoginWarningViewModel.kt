package com.quare.bibleplanner.feature.loginwarning.presentation.viewmodel

import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.loginwarning.presentation.model.LoginWarningUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel

internal class LoginWarningViewModel(
    route: LoginWarningNavRoute,
    trackEvent: TrackEvent,
    private val navigator: Navigator,
) : TrackedViewModel<LoginWarningUiEvent>(trackEvent) {
    val reason: LoginWarningReason = LoginWarningReason.fromKey(route.reason)

    init {
        track(AnalyticsEventNames.LOGIN_WARNING_SHOWN)
    }

    override fun handleEvent(event: LoginWarningUiEvent) = when (event) {
        LoginWarningUiEvent.OnLoginClick -> {
            track(AnalyticsEventNames.LOGIN_WARNING_ACCEPTED)
            navigator.navigateReplacingTop(LoginNavRoute(notifyResultViaSnackbar = true))
        }

        LoginWarningUiEvent.OnDismiss -> {
            track(AnalyticsEventNames.LOGIN_WARNING_DISMISSED)
            navigator.navigateBack()
        }
    }

    private fun track(name: String) {
        trackEvent(
            name = name,
            params = mapOf(AnalyticsParams.REASON to reason.key),
        )
    }
}
