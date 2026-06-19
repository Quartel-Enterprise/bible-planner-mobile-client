package com.quare.bibleplanner.core.remoteconfig.domain.service.impl

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigDataSource
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

internal class RemoteConfigServiceImpl(
    private val dataSource: RemoteConfigDataSource,
) : RemoteConfigService {
    override fun observeBoolean(
        key: String,
        defaultValue: Boolean,
    ): Flow<Boolean> = observeValue { getBoolean(key) ?: defaultValue }

    override fun observeInt(
        key: String,
        defaultValue: Int,
    ): Flow<Int> = observeValue { getInt(key) ?: defaultValue }

    override fun observeString(
        key: String,
        defaultValue: String,
    ): Flow<String> = observeValue { getString(key) ?: defaultValue }

    private fun <T> observeValue(read: suspend RemoteConfigDataSource.() -> T): Flow<T> = callbackFlow {
        send(dataSource.read())
        val cancellable = dataSource.addConfigUpdateListener {
            launch { trySend(dataSource.read()) }
        }
        awaitClose { cancellable.cancel() }
    }.distinctUntilChanged()
}
