package com.quare.bibleplanner.worker

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import platform.UIKit.UIApplication
import platform.UIKit.UIBackgroundTaskInvalid

internal class IosDownloadBackgroundTask {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Why: resuming on every return to the foreground hands back a download already kept running.
    private val keptDownloads = mutableSetOf<Job>()

    /*
     * Why: iOS suspends the app shortly after it leaves the foreground, and a whole version downloads
     * in seconds, so asking for that extra time is enough to finish one started just before. When it
     * runs out anyway, the task is held until onExpired is done, since the app is suspended right after.
     */
    fun keepAppRunningUntil(
        download: Job,
        onExpired: suspend () -> Unit,
    ) {
        scope.launch {
            if (!keptDownloads.add(download)) return@launch
            val expired = CompletableDeferred<Unit>()
            val task = UIApplication.sharedApplication.beginBackgroundTaskWithName(
                taskName = TASK_NAME,
                expirationHandler = { expired.complete(Unit) },
            )
            try {
                val hasExpired = select {
                    download.onJoin { false }
                    expired.onAwait { true }
                }
                if (hasExpired) onExpired()
            } finally {
                if (task != UIBackgroundTaskInvalid) UIApplication.sharedApplication.endBackgroundTask(task)
                keptDownloads.remove(download)
            }
        }
    }

    private companion object {
        const val TASK_NAME = "bible_version_download"
    }
}
