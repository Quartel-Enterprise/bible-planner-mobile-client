package com.quare.bibleplanner.core.remoteconfig

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService

class DesktopRemoteConfigService : RemoteConfigService {
    override suspend fun getBoolean(key: String): Boolean = true

    override suspend fun getInt(key: String): Int = 0

    override suspend fun getString(key: String): String = ""
}
