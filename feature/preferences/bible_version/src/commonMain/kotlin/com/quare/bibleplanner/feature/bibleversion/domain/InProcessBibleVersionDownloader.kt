package com.quare.bibleplanner.feature.bibleversion.domain

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.books.domain.usecase.ObserveBibleVersionDownloadProgress
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update

@OptIn(ExperimentalAtomicApi::class)
class InProcessBibleVersionDownloader(
    private val bibleVersionDao: BibleVersionDao,
    private val downloadBible: DownloadBibleUseCase,
    private val notifier: BibleVersionDownloadNotifier,
    private val bibleRepository: BibleRepository,
    private val observeDownloadProgress: ObserveBibleVersionDownloadProgress,
) {
    // Why: an uncaught exception in a download would otherwise end the whole app on iOS.
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { "Bible version download failed unexpectedly" }
    }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob() + exceptionHandler)

    // Why: downloads start and finish on different threads, so the map is only ever swapped whole.
    private val activeDownloads = AtomicReference(emptyMap<String, Job>())

    fun startDownload(versionId: String): Job {
        val download = scope.launch(start = CoroutineStart.LAZY) { download(versionId) }
        var activeDownload = download
        activeDownloads.update { current ->
            activeDownload = current[versionId] ?: download
            if (activeDownload === download) current + (versionId to download) else current
        }
        if (activeDownload === download) {
            download.invokeOnCompletion { forget(versionId = versionId, download = download) }
            download.start()
        } else {
            download.cancel()
        }
        return activeDownload
    }

    suspend fun cancelDownload(versionId: String) {
        stopDownload(versionId)?.join()
    }

    // Why: stops the download without waiting for it, for callers that can't suspend.
    fun stopDownload(versionId: String): Job? {
        var stopped: Job? = null
        activeDownloads.update { current ->
            stopped = current[versionId]
            current - versionId
        }
        return stopped?.apply { cancel() }
    }

    fun cancelAllDownloads() {
        activeDownloads.exchange(emptyMap()).values.forEach(Job::cancel)
    }

    fun hasActiveDownload(versionId: String): Boolean = versionId in activeDownloads.load()

    fun hasAnyActiveDownload(): Boolean = activeDownloads.load().isNotEmpty()

    private suspend fun download(versionId: String) {
        val versionName = resolveVersionName(versionId)
        bibleVersionDao.updateStatus(id = versionId, status = DownloadStatus.IN_PROGRESS)
        notifier.showProgress(versionId, versionName, 0f)

        val result = coroutineScope {
            val progressObserver = launch {
                observeDownloadProgress(versionId)
                    .onEach { progress -> notifier.showProgress(versionId, versionName, progress) }
                    .catch { error -> Logger.w(error) { "Progress observation failed; download continues" } }
                    .collect()
            }
            downloadBible(versionId).also { progressObserver.cancel() }
        }

        result
            .onSuccess { notifier.showComplete(versionId, versionName) }
            .onFailure {
                // Why: the version is paused before anyone hears of the error, so whoever reacts to it reads PAUSED.
                bibleVersionDao.updateStatus(id = versionId, status = DownloadStatus.PAUSED)
                notifier.showError(versionId, versionName)
            }
    }

    private fun forget(
        versionId: String,
        download: Job,
    ) {
        activeDownloads.update { current ->
            if (current[versionId] === download) current - versionId else current
        }
    }

    private suspend fun resolveVersionName(versionId: String): String = bibleRepository
        .getBiblesFlow()
        .first()
        .find { it.version.id == versionId }
        ?.version
        ?.name ?: versionId
}
