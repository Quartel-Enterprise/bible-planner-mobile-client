package com.quare.bibleplanner.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao

internal class AndroidBibleVersionDownloaderFacade(
    context: Context,
    private val bibleVersionDao: BibleVersionDao,
    private val verseDao: VerseDao,
    private val notifier: BibleVersionDownloadNotifier,
    private val requestFactory: AndroidBibleVersionDownloadRequestFactory,
) : BibleVersionDownloaderFacade {
    private val workManager = WorkManager.getInstance(context)

    override fun downloadVersion(versionId: String) {
        workManager.enqueueUniqueWork(
            uniqueWorkName = BibleVersionDownloadWorker.workName(versionId),
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = requestFactory.create(versionId),
        )
    }

    override suspend fun pauseDownload(versionId: String) {
        workManager.cancelUniqueWork(BibleVersionDownloadWorker.workName(versionId))
        notifier.dismiss(versionId)
        bibleVersionDao.updateStatus(id = versionId, status = DownloadStatus.PAUSED)
    }

    override suspend fun deleteDownload(versionId: String) {
        workManager.cancelUniqueWork(BibleVersionDownloadWorker.workName(versionId))
        notifier.dismiss(versionId)
        verseDao.deleteVerseTextsByVersion(versionId)
        bibleVersionDao.updateDownloadProgress(id = versionId, progress = 0f)
        bibleVersionDao.updateStatus(id = versionId, status = DownloadStatus.NOT_STARTED)
    }
}
