package com.quare.bibleplanner.core.remoteconfig.domain.service

import co.touchlab.kermit.Logger
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
            ).addOnCompleteListener { settingsTask ->
                if (!settingsTask.isSuccessful) {
                    Logger.w(
                        tag = TAG,
                        throwable = settingsTask.exception,
                        messageString = "Failed to apply the remote config settings",
                    )
                }
                firebaseRemoteConfig
                    .fetchAndActivate()
                    .addOnCompleteListener { fetchTask ->
                        if (!fetchTask.isSuccessful) {
                            Logger.w(
                                tag = TAG,
                                throwable = fetchTask.exception,
                                messageString = "Failed to fetch the remote config " +
                                    "(last fetch status: $lastFetchStatusName). Every key falls back " +
                                    "to its in-app default until the next successful fetch",
                            )
                        }
                        isInitialized.complete(Unit)
                    }
            }
    }

    override suspend fun getBoolean(key: String): Boolean? = getRemoteValue(key)?.asBoolean()

    override suspend fun getInt(key: String): Int? = getRemoteValue(key)?.asLong()?.toInt()

    override suspend fun getString(key: String): String? = getRemoteValue(key)?.asString()

    private val lastFetchStatusName: String
        get() = when (val status = firebaseRemoteConfig.info.lastFetchStatus) {
            FirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS -> "SUCCESS"
            FirebaseRemoteConfig.LAST_FETCH_STATUS_NO_FETCH_YET -> "NO_FETCH_YET"
            FirebaseRemoteConfig.LAST_FETCH_STATUS_FAILURE -> "FAILURE"
            FirebaseRemoteConfig.LAST_FETCH_STATUS_THROTTLED -> "THROTTLED"
            else -> "UNKNOWN ($status)"
        }

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
        private const val TAG = "AndroidRemoteConfigService"
        private const val MINIMUM_FETCH_INTERVAL_IN_SECONDS = 0L
    }
}
