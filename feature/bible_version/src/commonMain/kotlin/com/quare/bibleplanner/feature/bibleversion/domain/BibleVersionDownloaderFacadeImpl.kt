package com.quare.bibleplanner.feature.bibleversion.domain

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
            try {
                // Ensure Entity Exists
                val version = bibleVersionDao.getVersionById(versionId) ?: return@launch

                // If already downloaded, skip
                if (version.status == DownloadStatus.DONE) return@launch

                bibleVersionDao.updateStatus(versionId, DownloadStatus.IN_PROGRESS)
                downloadBible(versionId)

                // Note: DownloadBibleUseCase now updates the status to DONE upon successful completion of all chapters.
                // We double-check here if it's not DONE (meaning it was canceled or failed midway)
                val updated = bibleVersionDao.getVersionById(versionId)
                if (updated != null && updated.status != DownloadStatus.DONE) {
                    bibleVersionDao.updateStatus(versionId, DownloadStatus.PAUSED)
                }
            } catch (_: Exception) {
                // If it's a cancellation, status is already PAUSED by the cancel method or should be set here
                bibleVersionDao.updateStatus(versionId, DownloadStatus.PAUSED)
            } finally {
                activeDownloads.remove(versionId)
            }
        }
        activeDownloads[versionId] = job
    }

    override suspend fun pauseDownload(versionId: String) {
        activeDownloads[versionId]?.cancel()
        activeDownloads.remove(versionId)
        bibleVersionDao.updateStatus(versionId, DownloadStatus.PAUSED)
    }

    override suspend fun deleteDownload(versionId: String) {
        pauseDownload(versionId)
        verseDao.deleteVerseTextsByVersion(versionId)
        bibleVersionDao.updateDownloadProgress(versionId, 0f)
        bibleVersionDao.updateStatus(versionId, DownloadStatus.NOT_STARTED)
    }
}
