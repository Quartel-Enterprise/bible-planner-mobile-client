package com.quare.bibleplanner.feature.bibleversion.domain

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

internal class BibleVersionDownloaderFacadeImpl(
    private val bibleVersionDao: BibleVersionDao,
    private val verseDao: VerseDao,
    private val downloadBible: DownloadBibleUseCase,
    private val notifier: BibleVersionDownloadNotifier,
    private val bibleRepository: BibleRepository,
) : BibleVersionDownloaderFacade {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeDownloads = mutableMapOf<String, Job>()

    override fun downloadVersion(versionId: String) {
        if (activeDownloads.containsKey(versionId)) return

        val job = scope.launch {
            val versionName = bibleRepository
                .getBiblesFlow()
                .first()
                .find { it.version.id == versionId }
                ?.version
                ?.name
                ?: versionId

            bibleVersionDao.updateStatus(
                id = versionId,
                status = DownloadStatus.IN_PROGRESS,
            )
            notifier.showProgress(versionId, versionName, 0f)

            val progressObserver = launch {
                bibleVersionDao
                    .getAllVersionsFlow()
                    .mapNotNull { list -> list.find { it.id == versionId } }
                    .distinctUntilChangedBy { it.downloadProgress }
                    .collect { entity -> notifier.showProgress(versionId, versionName, entity.downloadProgress) }
            }

            val result = downloadBible(versionId)
            progressObserver.cancel()

            result
                .onSuccess {
                    notifier.showComplete(versionId, versionName)
                }.onFailure {
                    notifier.showError(versionId, versionName)
                    updateStatusToPause(versionId)
                }
            activeDownloads.remove(versionId)
        }
        activeDownloads[versionId] = job
    }

    override suspend fun deleteDownload(versionId: String) {
        pauseDownload(versionId)
        verseDao.deleteVerseTextsByVersion(versionId)
        bibleVersionDao.updateDownloadProgress(
            id = versionId,
            progress = 0f,
        )
        bibleVersionDao.updateStatus(
            id = versionId,
            status = DownloadStatus.NOT_STARTED,
        )
    }

    override suspend fun pauseDownload(versionId: String) {
        activeDownloads[versionId]?.cancel()
        activeDownloads.remove(versionId)
        notifier.dismiss(versionId)
        updateStatusToPause(versionId)
    }

    private suspend fun updateStatusToPause(versionId: String) {
        bibleVersionDao.updateStatus(
            id = versionId,
            status = DownloadStatus.PAUSED,
        )
    }
}
