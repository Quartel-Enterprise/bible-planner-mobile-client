package com.quare.bibleplanner.core.remoteconfig.data.datasource

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.remoteconfig.data.dto.RemoteConfigResponseDto
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.functions.Functions
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

internal class SupabaseRemoteConfigProxyClient(
    private val functions: Functions,
    private val json: Json,
) : RemoteConfigProxyClient {
    override suspend fun fetchParameters(): Map<String, String>? = suspendRunCatching {
        val response = functions.invoke(FUNCTION_NAME)
        json.decodeFromString(RemoteConfigResponseDto.serializer(), response.bodyAsText()).parameters
    }.onFailure { throwable ->
        Logger.e(throwable) { "Failed to fetch the remote config from $FUNCTION_NAME" }
    }.getOrNull()

    private companion object {
        const val FUNCTION_NAME = "get-remote-config"
    }
}
