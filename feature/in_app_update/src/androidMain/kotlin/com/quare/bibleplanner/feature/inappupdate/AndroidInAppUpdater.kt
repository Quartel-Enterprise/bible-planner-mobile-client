package com.quare.bibleplanner.feature.inappupdate

import co.touchlab.kermit.Logger
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.quare.bibleplanner.core.provider.platform.CurrentActivityProvider
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateDownloadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.android.play.core.install.model.UpdateAvailability as PlayUpdateAvailability

internal class AndroidInAppUpdater(
    private val appUpdateManager: AppUpdateManager,
    private val activityProvider: CurrentActivityProvider,
) {
    private val _downloadState = MutableStateFlow<UpdateDownloadState>(UpdateDownloadState.Idle)
    val downloadState: StateFlow<UpdateDownloadState> = _downloadState.asStateFlow()

    private val listener = InstallStateUpdatedListener(::onInstallState)

    init {
        appUpdateManager.registerListener(listener)
    }

    suspend fun check(): UpdateAvailability = suspendRunCatching {
        val info = appUpdateManager.appUpdateInfo.await()
        if (info.isFlexibleUpdateAvailable()) {
            UpdateAvailability.Available(versionName = null)
        } else {
            UpdateAvailability.NotAvailable
        }
    }.getOrElse { throwable ->
        Logger.w(tag = TAG, throwable = throwable, messageString = "Failed to check for in-app update")
        UpdateAvailability.NotAvailable
    }

    suspend fun start() {
        suspendRunCatching {
            val activity = activityProvider.activity ?: return
            val info = appUpdateManager.appUpdateInfo.await()
            if (info.isFlexibleUpdateAvailable()) {
                appUpdateManager.startUpdateFlow(
                    info,
                    activity,
                    AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE),
                )
            }
        }.onFailure { throwable ->
            Logger.w(tag = TAG, throwable = throwable, messageString = "Failed to start in-app update")
        }
    }

    fun complete() {
        appUpdateManager.completeUpdate()
    }

    private fun onInstallState(state: InstallState) {
        _downloadState.value = when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> UpdateDownloadState.Downloading(state.downloadPercent())
            InstallStatus.DOWNLOADED -> UpdateDownloadState.Downloaded
            InstallStatus.FAILED -> UpdateDownloadState.Failed
            else -> _downloadState.value
        }
    }

    private fun AppUpdateInfo.isFlexibleUpdateAvailable(): Boolean =
        updateAvailability() == PlayUpdateAvailability.UPDATE_AVAILABLE &&
            isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

    private fun InstallState.downloadPercent(): Int {
        val total = totalBytesToDownload()
        return if (total > 0) ((bytesDownloaded() * PERCENT_MAX) / total).toInt() else 0
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it) }
        addOnFailureListener { continuation.resumeWithException(it) }
        addOnCanceledListener { continuation.cancel() }
    }

    private companion object {
        const val PERCENT_MAX = 100
        const val TAG = "AndroidInAppUpdater"
    }
}
