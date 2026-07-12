package com.quare.bibleplanner.core.devices.data

import com.quare.bibleplanner.core.devices.data.dto.RegisterDeviceRequest
import com.quare.bibleplanner.core.devices.data.dto.RevokeDeviceRequest
import com.quare.bibleplanner.core.devices.data.dto.UserDeviceDto
import com.quare.bibleplanner.core.devices.data.model.DeviceChange
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

internal class UserDevicesRemoteStore(
    private val supabaseClient: SupabaseClient,
    private val realtime: Realtime,
    private val functions: Functions,
    private val json: Json,
) {
    suspend fun fetch(userId: String): List<UserDeviceDto> = supabaseClient
        .from(TABLE)
        .select {
            filter { eq("user_id", userId) }
        }.decodeList()

    fun observeChanges(userId: String): Flow<DeviceChange> = flow {
        val channel = realtime.channel("user_devices_$userId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = TABLE
            filter("user_id", FilterOperator.EQ, userId)
        }
        channel.subscribe()
        try {
            changes.collect { action ->
                val change = when (action) {
                    is PostgresAction.Insert -> DeviceChange.Upserted(action.decodeRecord<UserDeviceDto>())
                    is PostgresAction.Update -> DeviceChange.Upserted(action.decodeRecord<UserDeviceDto>())
                    is PostgresAction.Delete -> action.oldRecord.rowId()?.let(DeviceChange::Removed)
                    else -> null
                }
                if (change != null) emit(change)
            }
        } finally {
            withContext(NonCancellable) {
                realtime.removeChannel(channel)
            }
        }
    }

    suspend fun rename(
        deviceRowId: String,
        name: String,
        updatedAt: String,
    ) {
        supabaseClient.from(TABLE).update(
            {
                set("name", name)
                set("updated_at", updatedAt)
            },
        ) {
            filter { eq("id", deviceRowId) }
        }
    }

    suspend fun signOutDevice(deviceRowId: String) {
        functions.invoke(FUNCTION_REVOKE) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(RevokeDeviceRequest.serializer(), RevokeDeviceRequest(deviceRowId)))
        }
    }

    // RLS scopes the delete to the caller's own rows, so filtering by device_id is enough.
    suspend fun deleteOwnDevice(deviceId: String) {
        supabaseClient.from(TABLE).delete {
            filter { eq("device_id", deviceId) }
        }
    }

    suspend fun registerCurrentDevice(request: RegisterDeviceRequest) {
        functions.invoke(FUNCTION_REGISTER) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(RegisterDeviceRequest.serializer(), request))
        }
    }

    private fun JsonObject.rowId(): String? = (get("id") as? JsonPrimitive)?.contentOrNull

    private companion object {
        const val TABLE = "user_devices"
        const val FUNCTION_REGISTER = "register-device"
        const val FUNCTION_REVOKE = "revoke-device-session"
    }
}
