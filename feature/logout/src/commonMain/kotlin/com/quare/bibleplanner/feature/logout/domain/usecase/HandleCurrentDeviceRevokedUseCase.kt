package com.quare.bibleplanner.feature.logout.domain.usecase

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.user.domain.model.RemoteSessionState
import com.quare.bibleplanner.core.user.domain.service.IntentionalLogoutMarker
import com.quare.bibleplanner.core.user.domain.usecase.CheckRemoteSessionState

internal class HandleCurrentDeviceRevokedUseCase(
    private val checkRemoteSessionState: CheckRemoteSessionState,
    private val intentionalLogoutMarker: IntentionalLogoutMarker,
    private val trackEvent: TrackEvent,
    private val endSession: EndSession,
) : HandleCurrentDeviceRevoked {
    private val logger = Logger.withTag(LOG_TAG)

    override suspend fun invoke() {
        intentionalLogoutMarker.mark()
        val serverSessionState = checkRemoteSessionState().toAnalyticsValue()
        trackEvent(
            name = AnalyticsEventNames.CURRENT_DEVICE_REVOKED,
            params = mapOf(AnalyticsParams.SERVER_SESSION_STATE to serverSessionState),
        )
        logger.e(CurrentDeviceRevokedException(serverSessionState)) {
            "Current device revoked; ending local session"
        }
        endSession()
    }

    private fun RemoteSessionState.toAnalyticsValue(): String = when (this) {
        RemoteSessionState.REVOKED -> STATE_REVOKED
        RemoteSessionState.ACTIVE -> STATE_ACTIVE
        RemoteSessionState.UNKNOWN -> STATE_UNKNOWN
    }

    private companion object {
        const val LOG_TAG = "DeviceRevoked"
        const val STATE_REVOKED = "revoked"
        const val STATE_ACTIVE = "active"
        const val STATE_UNKNOWN = "unknown"
    }
}
