package com.quare.bibleplanner.feature.bibleversion.domain

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal class BibleVersionDownloaderFacadeImpl(
    private val bibleVersionDao: BibleVersionDao,
    private val verseDao: VerseDao,
    private val downloadBible: DownloadBibleUseCase,
) : BibleVersionDownloaderFacade {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeDownloads = mutableMapOf<String, Job>()

    override fun downloadVersion(versionId: String) {
        if (activeDownloads.containsKey(versionId)) return

        val job = scope.launch {
            bibleVersionDao.updateStatus(
                id = versionId,
                status = DownloadStatus.IN_PROGRESS,
            )
            downloadBible(versionId).onFailure {
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
        updateStatusToPause(versionId)
    }

    private suspend fun updateStatusToPause(versionId: String) {
        bibleVersionDao.updateStatus(
            id = versionId,
            status = DownloadStatus.PAUSED,
        )
    }
}
