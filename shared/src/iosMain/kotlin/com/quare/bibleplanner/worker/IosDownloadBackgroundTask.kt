package com.quare.bibleplanner.worker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.UIKit.UIApplication
import platform.UIKit.UIBackgroundTaskIdentifier
import platform.UIKit.UIBackgroundTaskInvalid

internal class IosDownloadBackgroundTask {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Why: resuming on every return to the foreground hands back a download already kept running.
    private val keptDownloads = mutableSetOf<Job>()

    /*
     * Why: iOS suspends the app shortly after it leaves the foreground, and a whole version downloads
     * in seconds, so asking for that extra time is enough to finish one started just before.
     */
    fun keepAppRunningUntil(
        download: Job,
        onExpired: () -> Unit,
    ) {
        scope.launch {
            if (!keptDownloads.add(download)) return@launch
            var task: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
            val endTask = {
                if (task != UIBackgroundTaskInvalid) {
                    UIApplication.sharedApplication.endBackgroundTask(task)
                    task = UIBackgroundTaskInvalid
                }
            }
            task = UIApplication.sharedApplication.beginBackgroundTaskWithName(
                taskName = TASK_NAME,
                expirationHandler = {
                    onExpired()
                    endTask()
                },
            )
            try {
                download.join()
            } finally {
                endTask()
                keptDownloads.remove(download)
            }
        }
    }

    private companion object {
        const val TASK_NAME = "bible_version_download"
    }
}
