package com.quare.bibleplanner.core.provider.analytics

import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

internal class DesktopAnalyticsService(
    private val measurementProtocolClient: MeasurementProtocolClient,
) : AnalyticsService {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val sessionId = UUID.randomUUID().toString()
    private val userProperties = mutableMapOf<String, String?>()

    override fun setUserProperty(
        name: String,
        value: String?,
    ) {
        userProperties[name] = value
        send(
            eventName = SESSION_EVENT,
            params = emptyMap(),
        )
    }

    override fun logEvent(
        name: String,
        params: Map<String, Any>,
    ) {
        send(
            eventName = name,
            params = params,
        )
    }

    private fun send(
        eventName: String,
        params: Map<String, Any>,
    ) {
        val snapshot = userProperties.toMap()
        coroutineScope.launch {
            measurementProtocolClient.send(
                eventName = eventName,
                params = params + mapOf(
                    SESSION_ID_PARAM to sessionId,
                    ENGAGEMENT_TIME_PARAM to ENGAGEMENT_TIME_VALUE,
                ),
                userProperties = snapshot,
            )
        }
    }

    companion object {
        private const val SESSION_EVENT = "desktop_session_start"
        private const val SESSION_ID_PARAM = "session_id"
        private const val ENGAGEMENT_TIME_PARAM = "engagement_time_msec"
        private const val ENGAGEMENT_TIME_VALUE = "1"
    }
}
