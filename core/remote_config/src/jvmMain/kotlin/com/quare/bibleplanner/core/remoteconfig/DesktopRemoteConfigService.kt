package com.quare.bibleplanner.core.remoteconfig

import com.quare.bibleplanner.core.remoteconfig.domain.service.Cancellable
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigDataSource

class DesktopRemoteConfigService : RemoteConfigDataSource {
    override suspend fun getBoolean(key: String): Boolean? = null

    override suspend fun getInt(key: String): Int? = null

    override suspend fun getString(key: String): String? = null

    override fun addConfigUpdateListener(onUpdate: () -> Unit): Cancellable = Cancellable { }
}
