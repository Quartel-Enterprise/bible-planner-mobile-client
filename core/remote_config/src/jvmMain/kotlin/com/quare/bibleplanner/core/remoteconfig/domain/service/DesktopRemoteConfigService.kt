package com.quare.bibleplanner.core.remoteconfig.domain.service

import com.quare.bibleplanner.core.remoteconfig.data.datasource.RemoteConfigProxyClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val trueValues = setOf("1", "true", "t", "yes", "y", "on")
private val falseValues = setOf("0", "false", "f", "no", "n", "off", "")

internal class DesktopRemoteConfigService(
    private val proxyClient: RemoteConfigProxyClient,
    private val coroutineScope: CoroutineScope,
) : RemoteConfigDataSource {
    private val refreshInterval: Duration = 15.minutes
    private val firstFetchTimeout: Duration = 10.seconds
    private val parameters = MutableStateFlow(emptyMap<String, String>())
    private val isInitialized = CompletableDeferred<Unit>()

    init {
        coroutineScope.launch {
            while (isActive) {
                proxyClient.fetchParameters()?.let { parameters.value = it }
                isInitialized.complete(Unit)
                delay(refreshInterval)
            }
        }
    }

    override suspend fun getBoolean(key: String): Boolean? = getValue(key)?.toRemoteConfigBoolean()

    override suspend fun getInt(key: String): Int? = getValue(key)?.toIntOrNull()

    override suspend fun getString(key: String): String? = getValue(key)

    override fun addConfigUpdateListener(onUpdate: () -> Unit): Cancellable {
        val job = coroutineScope.launch {
            parameters.drop(1).collect { onUpdate() }
        }
        return Cancellable(job::cancel)
    }

    private suspend fun getValue(key: String): String? {
        withTimeoutOrNull(firstFetchTimeout) { isInitialized.await() }
        return parameters.value[key]
    }
}

private fun String.toRemoteConfigBoolean(): Boolean? = when (trim().lowercase()) {
    in trueValues -> true
    in falseValues -> false
    else -> null
}
