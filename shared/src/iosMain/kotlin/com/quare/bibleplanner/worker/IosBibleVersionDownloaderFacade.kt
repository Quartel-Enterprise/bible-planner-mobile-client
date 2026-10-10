package com.quare.bibleplanner.worker

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBooksIfNeededUseCase
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DeleteBibleVersionDownloadUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.PauseBibleVersionDownloadUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal class IosBibleVersionDownloaderFacade(
    private val downloadSession: IosDownloadSession,
    private val bridge: IosBackgroundDownloadBridge,
    private val bibleVersionDao: BibleVersionDao,
    private val notifier: BibleVersionDownloadNotifier,
    private val bibleRepository: BibleRepository,
    private val pauseBibleVersion: PauseBibleVersionDownloadUseCase,
    private val deleteBibleVersion: DeleteBibleVersionDownloadUseCase,
    private val initializeBooksIfNeeded: InitializeBooksIfNeededUseCase,
) : BibleVersionDownloaderFacade {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val shouldShowDownloadTip: Boolean = true

    init {
        downloadSession.setBridge(bridge)
    }

    override fun downloadVersion(versionId: String) {
        scope.launch {
            val previousStatus = bibleVersionDao.getVersionById(versionId)?.status
            bibleVersionDao.updateStatus(id = versionId, status = DownloadStatus.IN_PROGRESS)
            val versionName = resolveVersionName(versionId)
            notifier.showProgress(versionId, versionName, 0f)
            // Why: the pending chapters come from the book rows, which a first launch may still be inserting.
            initializeBooksIfNeeded()
            val tasks = bridge.getPendingDownloads(versionId)
            if (tasks.isEmpty()) {
                // Why: no pending chapter does not prove the version is complete; let the chapter count decide.
                bridge.finalizeVersionIfComplete(versionId) { downloadSession.endLiveActivity(versionId) }
                return@launch
            }
            if (previousStatus == DownloadStatus.PAUSED) {
                downloadSession.resumeLiveActivity(versionId)
            } else {
                downloadSession.startLiveActivity(versionId, versionName)
            }
            downloadSession.notifyAllTasksRegistered(versionId, tasks.size)
            tasks.forEach { task ->
                downloadSession.addDownloadTask(
                    url = task.url,
                    versionId = task.versionId,
                    chapterId = task.chapterId,
                )
            }
        }
    }

    override suspend fun pauseDownload(versionId: String) {
        /*
         * Why: mark PAUSED in the DB first so in-flight processDownloadedChapter coroutines see it and
         * skip writing chapters after this point.
         */
        pauseBibleVersion(versionId)
        downloadSession.cancelDownloads(versionId)
        downloadSession.pauseLiveActivity(versionId)
    }

    override suspend fun deleteDownload(versionId: String) {
        downloadSession.cancelDownloads(versionId)
        downloadSession.endLiveActivity(versionId)
        deleteBibleVersion(versionId)
    }

    private suspend fun resolveVersionName(versionId: String): String = bibleRepository
        .getBiblesFlow()
        .first()
        .find { it.version.id == versionId }
        ?.version
        ?.name ?: versionId
}
