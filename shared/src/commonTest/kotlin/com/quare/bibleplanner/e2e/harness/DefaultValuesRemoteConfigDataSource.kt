package com.quare.bibleplanner.e2e.harness

import com.quare.bibleplanner.core.remoteconfig.domain.service.Cancellable
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigDataSource

internal class DefaultValuesRemoteConfigDataSource : RemoteConfigDataSource {
    override suspend fun getBoolean(key: String): Boolean? = null

    override suspend fun getInt(key: String): Int? = null

    override suspend fun getString(key: String): String? = null

    override fun addConfigUpdateListener(onUpdate: () -> Unit): Cancellable = Cancellable {}
}
