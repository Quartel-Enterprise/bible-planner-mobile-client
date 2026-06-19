package com.quare.bibleplanner.core.remoteconfig.domain.service

import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException

internal class RemoteConfigUpdateListener(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val onConfigUpdated: () -> Unit,
) : ConfigUpdateListener {
    override fun onUpdate(configUpdate: ConfigUpdate) {
        firebaseRemoteConfig
            .activate()
            .addOnCompleteListener { onConfigUpdated() }
    }

    override fun onError(error: FirebaseRemoteConfigException) = Unit
}
