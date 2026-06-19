package com.quare.bibleplanner.core.remoteconfig.domain.service

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.CompletableDeferred

internal class AndroidRemoteConfigService(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
) : RemoteConfigDataSource {
    private val isInitialized = CompletableDeferred<Unit>()

    init {
        firebaseRemoteConfig
            .setConfigSettingsAsync(
                remoteConfigSettings {
                    minimumFetchIntervalInSeconds = MINIMUM_FETCH_INTERVAL_IN_SECONDS
                },
            ).addOnCompleteListener {
                firebaseRemoteConfig
                    .fetchAndActivate()
                    .addOnCompleteListener {
                        isInitialized.complete(Unit)
                    }
            }
    }

    override suspend fun getBoolean(key: String): Boolean? = getRemoteValue(key)?.asBoolean()

    override suspend fun getInt(key: String): Int? = getRemoteValue(key)?.asLong()?.toInt()

    override suspend fun getString(key: String): String? = getRemoteValue(key)?.asString()

    private suspend fun getRemoteValue(key: String): FirebaseRemoteConfigValue? = getWithAwait {
        getValue(key).takeIf { it.source != FirebaseRemoteConfig.VALUE_SOURCE_STATIC }
    }

    private suspend fun <T> getWithAwait(call: FirebaseRemoteConfig.() -> T): T {
        isInitialized.await()
        return call(firebaseRemoteConfig)
    }

    override fun addConfigUpdateListener(onUpdate: () -> Unit): Cancellable {
        val registration = firebaseRemoteConfig.addOnConfigUpdateListener(
            RemoteConfigUpdateListener(
                firebaseRemoteConfig = firebaseRemoteConfig,
                onConfigUpdated = onUpdate,
            ),
        )
        return Cancellable(registration::remove)
    }

    companion object {
        private const val MINIMUM_FETCH_INTERVAL_IN_SECONDS = 0L
    }
}
