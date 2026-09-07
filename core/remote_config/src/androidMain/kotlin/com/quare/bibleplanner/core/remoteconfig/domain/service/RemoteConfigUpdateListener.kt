package com.quare.bibleplanner.core.remoteconfig.domain.service

import co.touchlab.kermit.Logger
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
            .addOnCompleteListener { activateTask ->
                if (!activateTask.isSuccessful) {
                    Logger.w(
                        tag = TAG,
                        throwable = activateTask.exception,
                        messageString = "Failed to activate the updated remote config",
                    )
                }
                onConfigUpdated()
            }
    }

    override fun onError(error: FirebaseRemoteConfigException) {
        Logger.w(
            tag = TAG,
            throwable = error,
            messageString = "The remote config realtime stream failed",
        )
    }

    private companion object {
        const val TAG = "RemoteConfigUpdateListener"
    }
}
