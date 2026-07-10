package com.quare.bibleplanner.core.provider.analytics

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.provider.analytics.generated.AnalyticsBuildKonfig
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

internal class MeasurementProtocolClient(
    private val httpClient: HttpClient,
    private val clientIdProvider: ClientIdProvider,
) {
    private val measurementId: String = AnalyticsBuildKonfig.GA_MEASUREMENT_ID
    private val apiSecret: String = AnalyticsBuildKonfig.GA_MEASUREMENT_API_SECRET

    suspend fun send(
        eventName: String,
        params: Map<String, Any>,
        userProperties: Map<String, String?>,
    ) {
        if (measurementId.isBlank() || apiSecret.isBlank()) return
        val payload = buildPayload(
            eventName = eventName,
            params = params,
            userProperties = userProperties,
        )
        suspendRunCatching {
            httpClient.post(COLLECT_URL) {
                url {
                    parameters.append(MEASUREMENT_ID_PARAM, measurementId)
                    parameters.append(API_SECRET_PARAM, apiSecret)
                }
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
        }.onFailure { throwable ->
            Logger.e(throwable) { "Failed to send $eventName to the Measurement Protocol" }
        }
    }

    private fun buildPayload(
        eventName: String,
        params: Map<String, Any>,
        userProperties: Map<String, String?>,
    ): JsonObject = buildJsonObject {
        put(CLIENT_ID_FIELD, clientIdProvider.getClientId())
        putJsonObject(USER_PROPERTIES_FIELD) {
            put(PLATFORM_PROPERTY, toUserProperty(PLATFORM_DESKTOP))
            userProperties.forEach { (name, value) ->
                value?.let { put(name, toUserProperty(it)) }
            }
        }
        putJsonArray(EVENTS_FIELD) {
            addJsonObject {
                put(EVENT_NAME_FIELD, eventName)
                putJsonObject(EVENT_PARAMS_FIELD) {
                    params.forEach { (key, value) ->
                        when (value) {
                            is Long -> put(key, value)
                            is Double -> put(key, value)
                            else -> put(key, value.toString())
                        }
                    }
                }
            }
        }
    }

    private fun toUserProperty(value: String): JsonObject = buildJsonObject {
        put(USER_PROPERTY_VALUE_FIELD, value)
    }

    companion object {
        private const val COLLECT_URL = "https://www.google-analytics.com/mp/collect"
        private const val MEASUREMENT_ID_PARAM = "measurement_id"
        private const val API_SECRET_PARAM = "api_secret"
        private const val CLIENT_ID_FIELD = "client_id"
        private const val USER_PROPERTIES_FIELD = "user_properties"
        private const val USER_PROPERTY_VALUE_FIELD = "value"
        private const val EVENTS_FIELD = "events"
        private const val EVENT_NAME_FIELD = "name"
        private const val EVENT_PARAMS_FIELD = "params"
        private const val PLATFORM_PROPERTY = "platform"
        private const val PLATFORM_DESKTOP = "desktop"
    }
}
