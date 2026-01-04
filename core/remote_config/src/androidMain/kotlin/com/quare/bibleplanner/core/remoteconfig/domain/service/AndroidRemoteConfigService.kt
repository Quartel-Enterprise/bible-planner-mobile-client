package com.quare.bibleplanner.core.remoteconfig.domain.service

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

internal class AndroidRemoteConfigService(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
) : RemoteConfigService {

    init {
        firebaseRemoteConfig.fetchAndActivate()
    }

    override suspend fun getBoolean(key: String): Boolean = firebaseRemoteConfig.getBoolean(key)

    override suspend fun getInt(key: String): Int = firebaseRemoteConfig.getLong(key).toInt()
}
