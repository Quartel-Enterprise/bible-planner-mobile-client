package com.quare.bibleplanner.worker

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloader
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloaderFacade
import com.quare.bibleplanner.notification.IosDownloadLiveActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

internal class IosBibleVersionDownloaderFacade(
    private val downloader: InProcessBibleVersionDownloader,
    private val inProcessFacade: InProcessBibleVersionDownloaderFacade,
    private val backgroundTask: IosDownloadBackgroundTask,
    private val liveActivity: IosDownloadLiveActivity,
    private val bibleVersionDao: BibleVersionDao,
) : BibleVersionDownloaderFacade {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val pausedShownTimeout = 2.seconds
    private val liveActivityStatuses = setOf(DownloadStatus.IN_PROGRESS, DownloadStatus.PAUSED)

    override fun downloadVersion(versionId: String) {
        backgroundTask.keepAppRunningUntil(
            download = downloader.startDownload(versionId),
            onExpired = { pauseUntilForeground(versionId) },
        )
    }

    override suspend fun pauseDownload(versionId: String) {
        /*
         * Why: stopping the download ends its background task, so iOS can suspend or end the app before
         * the pause is saved; a version still in progress would then resume against the user's pause.
         */
        bibleVersionDao.updateStatus(
            id = versionId,
            status = DownloadStatus.PAUSED,
        )
        inProcessFacade.pauseDownload(versionId)
        showPaused(versionId)
    }

    override suspend fun deleteDownload(versionId: String) {
        // Why: same as pausing, a version still in progress when iOS suspends the app would download again.
        bibleVersionDao.updateStatus(
            id = versionId,
            status = DownloadStatus.NOT_STARTED,
        )
        inProcessFacade.deleteDownload(versionId)
        liveActivity.dismiss(versionId)
    }

    /*
     * Why: a version still in progress at launch or on coming back was cut off by iOS, which either
     * ended the app or ran out of background time for it.
     */
    fun startResumingInterruptedDownloads() {
        resumeInterruptedDownloads()
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationWillEnterForegroundNotification,
            `object` = null,
            queue = null,
        ) { _ -> resumeInterruptedDownloads() }
    }

    private fun resumeInterruptedDownloads() {
        scope.launch {
            suspendRunCatching {
                val versions = bibleVersionDao.getAllVersions()
                // Why: a Live Activity left from an earlier run may belong to a version that finished or was deleted.
                liveActivity.keepOnly(
                    versions
                        .filter { version -> version.status in liveActivityStatuses }
                        .map { version -> version.id },
                )
                versions
                    .filter { version -> version.status == DownloadStatus.IN_PROGRESS }
                    .forEach { version -> downloadVersion(version.id) }
            }.onFailure { throwable -> Logger.e(throwable) { "Could not resume the interrupted Bible downloads" } }
        }
    }

    // Why: the version stays in progress so it resumes on return, and the Live Activity stops claiming it downloads.
    private suspend fun pauseUntilForeground(versionId: String) {
        downloader.stopDownload(versionId)
        showPaused(versionId)
    }

    // Why: bounded, since the background time may be about to end and the update must not hold it forever.
    private suspend fun showPaused(versionId: String) {
        withTimeoutOrNull(pausedShownTimeout) {
            suspendCancellableCoroutine { continuation ->
                liveActivity.showPaused(
                    versionId = versionId,
                    onShown = { continuation.resume(Unit) },
                )
            }
        }
    }
}
