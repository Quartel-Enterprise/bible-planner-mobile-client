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
        val remoteSessionState = checkRemoteSessionState()
        trackEvent(
            name = AnalyticsEventNames.CURRENT_DEVICE_REVOKED,
            params = mapOf(AnalyticsParams.SERVER_SESSION_STATE to remoteSessionState.toAnalyticsValue()),
        )
        when (remoteSessionState) {
            RemoteSessionState.REVOKED -> endSession()

            RemoteSessionState.ACTIVE -> {
                intentionalLogoutMarker.unmark()
                logger.e(FalsePositiveDeviceRevocationException()) {
                    "Skipped ending session: server session still active"
                }
            }

            RemoteSessionState.UNKNOWN -> intentionalLogoutMarker.unmark()
        }
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
