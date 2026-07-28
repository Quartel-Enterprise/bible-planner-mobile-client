package com.quare.bibleplanner.core.remoteconfig.data.datasource

internal fun interface RemoteConfigProxyClient {
    suspend fun fetchParameters(): Map<String, String>?
}
