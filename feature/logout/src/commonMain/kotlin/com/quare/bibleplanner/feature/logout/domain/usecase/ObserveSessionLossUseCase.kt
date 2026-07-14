package com.quare.bibleplanner.feature.logout.domain.usecase

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.user.domain.service.IntentionalLogoutMarker
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

internal class ObserveSessionLossUseCase(
    private val sessionStatus: StateFlow<SessionStatus>,
    private val intentionalLogoutMarker: IntentionalLogoutMarker,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val trackEvent: TrackEvent,
) : ObserveSessionLoss {
    private val logger = Logger.withTag(LOG_TAG)

    override suspend fun invoke() {
        var lastAuthenticated: SessionStatus.Authenticated? = null
        sessionStatus.collect { status ->
            when (status) {
                is SessionStatus.Authenticated -> lastAuthenticated = status

                is SessionStatus.NotAuthenticated -> {
                    lastAuthenticated?.let(::reportIfSpontaneous)
                    lastAuthenticated = null
                }

                SessionStatus.Initializing -> Unit

                is SessionStatus.RefreshFailure -> Unit
            }
        }
    }

    private fun reportIfSpontaneous(previous: SessionStatus.Authenticated) {
        if (intentionalLogoutMarker.consume()) return
        val source = previous.source.toAnalyticsValue()
        val isAccessTokenExpired =
            previous.session.expiresAt.toEpochMilliseconds() <= currentTimestampProvider.getCurrentTimestamp()
        trackEvent(
            name = AnalyticsEventNames.SESSION_LOST,
            params = mapOf(
                AnalyticsParams.SOURCE to source,
                AnalyticsParams.IS_ACCESS_TOKEN_EXPIRED to isAccessTokenExpired,
            ),
        )
        logger.e(
            throwable = SessionLostException(
                source = source,
                isAccessTokenExpired = isAccessTokenExpired,
            ),
        ) { "Session was cleared locally without an app-initiated logout" }
    }

    private fun SessionSource.toAnalyticsValue(): String = when (this) {
        SessionSource.AnonymousSignIn -> "anonymous_sign_in"
        SessionSource.External -> "external"
        is SessionSource.Refresh -> "refresh"
        is SessionSource.SignIn -> "sign_in"
        is SessionSource.SignUp -> "sign_up"
        SessionSource.Storage -> "storage"
        SessionSource.Unknown -> "unknown"
        is SessionSource.UserChanged -> "user_changed"
        is SessionSource.UserIdentitiesChanged -> "user_identities_changed"
    }

    private companion object {
        const val LOG_TAG = "SessionLoss"
    }
}
