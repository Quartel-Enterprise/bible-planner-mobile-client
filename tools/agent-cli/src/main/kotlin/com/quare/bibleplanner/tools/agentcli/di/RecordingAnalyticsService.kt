package com.quare.bibleplanner.tools.agentcli.di

import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService
import com.quare.bibleplanner.tools.agentcli.log.LogKind
import com.quare.bibleplanner.tools.agentcli.log.SessionLog
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

// Why: the desktop service posts to GA4; an agent's taps must land in its own log, never in the real property.
class RecordingAnalyticsService(
    private val log: SessionLog,
) : AnalyticsService {
    override fun setUserProperty(
        name: String,
        value: String?,
    ) {
        log.record(
            kind = LogKind.ANALYTICS,
            source = USER_PROPERTY,
            payload = JsonObject(mapOf(name to JsonPrimitive(value))),
        )
    }

    override fun logEvent(
        name: String,
        params: Map<String, Any>,
    ) {
        log.record(
            kind = LogKind.ANALYTICS,
            source = name,
            payload = JsonObject(params.mapValues { (_, value) -> value.toJsonPrimitive() }),
        )
    }

    override suspend fun getAppInstanceId(): String = APP_INSTANCE_ID

    private fun Any.toJsonPrimitive(): JsonPrimitive = when (this) {
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        else -> JsonPrimitive(toString())
    }

    private companion object {
        const val USER_PROPERTY = "user_property"
        const val APP_INSTANCE_ID = "agent-cli"
    }
}
