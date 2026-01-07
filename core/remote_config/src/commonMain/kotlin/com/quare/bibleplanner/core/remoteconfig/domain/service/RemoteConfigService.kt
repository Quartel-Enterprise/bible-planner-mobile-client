package com.quare.bibleplanner.core.remoteconfig.domain.service

interface RemoteConfigService {
    suspend fun getBoolean(key: String): Boolean

    suspend fun getInt(key: String): Int

    suspend fun getString(key: String): String
}
