package com.quare.bibleplanner.feature.login.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import com.quare.bibleplanner.ui.utils.presentation.UiEvent
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.NativeSignInState

sealed interface LoginUiEvent : UiEvent {
    data object DismissClick : LoginUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.LOGIN_SHEET_DISMISSED,
            params = mapOf(AnalyticsParams.METHOD to "close_button"),
        )
    }

    data object NotNowClick : LoginUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.LOGIN_SHEET_DISMISSED,
            params = mapOf(AnalyticsParams.METHOD to "not_now"),
        )
    }

    data object AddGoogleAccountConfirmClick : LoginUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.GOOGLE_ACCOUNT_ADD_CONFIRMED,
            params = emptyMap(),
        )
    }

    data object DismissAddGoogleAccountDialog : LoginUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.GOOGLE_ACCOUNT_ADD_DECLINED,
            params = emptyMap(),
        )
    }

    data class SocialLoginClick(
        val provider: LoginProvider,
        val nativeSignInState: NativeSignInState,
    ) : LoginUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.LOGIN_STARTED,
            params = mapOf(AnalyticsParams.METHOD to provider.name.lowercase()),
        )
    }

    data class SocialAuthResult(
        val provider: LoginProvider,
        val result: NativeSignInResult,
    ) : LoginUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.LOGIN,
                AnalyticsEventNames.LOGIN_CANCELLED,
                AnalyticsEventNames.LOGIN_FAILED,
            ),
        )
    }
}
