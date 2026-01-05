package com.quare.bibleplanner.core.remoteconfig.domain.service

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.CompletableDeferred

internal class AndroidRemoteConfigService(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
) : RemoteConfigService {
    private val isInitialized = CompletableDeferred<Unit>()

    init {
        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener {
                isInitialized.complete(Unit)
            }
    }

    override suspend fun getBoolean(key: String): Boolean = getWithAwait { getBoolean(key) }

    override suspend fun getInt(key: String): Int = getWithAwait { getLong(key).toInt() }

    private suspend fun <T> getWithAwait(call: FirebaseRemoteConfig.() -> T) : T {
        isInitialized.await()
        return call(firebaseRemoteConfig)
    }
}
