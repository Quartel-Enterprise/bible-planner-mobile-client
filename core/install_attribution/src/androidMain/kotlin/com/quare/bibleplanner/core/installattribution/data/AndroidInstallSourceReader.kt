package com.quare.bibleplanner.core.installattribution.data

import android.content.Context
import co.touchlab.kermit.Logger
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.quare.bibleplanner.core.installattribution.domain.model.InstallSource
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class AndroidInstallSourceReader(
    private val context: Context,
) {
    private val referrerTimeout: Duration = 10.seconds

    suspend fun read(): InstallSource = withContext(Dispatchers.IO) {
        InstallSource(
            platform = PLATFORM_ANDROID,
            referrer = readInstallReferrer(),
            advertisingId = readAdvertisingId(),
        )
    }

    private suspend fun readInstallReferrer(): String? {
        val client = InstallReferrerClient.newBuilder(context).build()
        return try {
            suspendRunCatching {
                val responseCode = withTimeoutOrNull(referrerTimeout) { startConnection(client) }
                if (responseCode == InstallReferrerResponse.OK) {
                    client.installReferrer.installReferrer
                } else {
                    Logger.i(tag = TAG) { "Install referrer unavailable, response code $responseCode" }
                    null
                }
            }.onFailure { throwable ->
                Logger.w(tag = TAG, throwable = throwable, messageString = "Failed to read the install referrer")
            }.getOrNull()
        } finally {
            client.endConnection()
        }
    }

    private suspend fun startConnection(client: InstallReferrerClient): Int =
        suspendCancellableCoroutine { continuation ->
            client.startConnection(
                object : InstallReferrerStateListener {
                    override fun onInstallReferrerSetupFinished(responseCode: Int) {
                        if (continuation.isActive) continuation.resume(responseCode)
                    }

                    override fun onInstallReferrerServiceDisconnected() {
                        if (continuation.isActive) continuation.resume(InstallReferrerResponse.SERVICE_DISCONNECTED)
                    }
                },
            )
        }

    private suspend fun readAdvertisingId(): String? = suspendRunCatching {
        val info = AdvertisingIdClient.getAdvertisingIdInfo(context)
        info.id?.takeUnless { id -> info.isLimitAdTrackingEnabled || id.isBlank() || id == ZEROED_ADVERTISING_ID }
    }.onFailure { throwable ->
        Logger.w(tag = TAG, throwable = throwable, messageString = "Failed to read the advertising id")
    }.getOrNull()

    private companion object {
        const val TAG = "InstallSourceReader"
        const val PLATFORM_ANDROID = "android"
        const val ZEROED_ADVERTISING_ID = "00000000-0000-0000-0000-000000000000"
    }
}
