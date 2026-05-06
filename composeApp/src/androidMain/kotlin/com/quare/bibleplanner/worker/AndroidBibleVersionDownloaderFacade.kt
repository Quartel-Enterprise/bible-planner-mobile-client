package com.quare.bibleplanner.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DeleteBibleVersionDownloadUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.PauseBibleVersionDownloadUseCase

internal class AndroidBibleVersionDownloaderFacade(
    context: Context,
    private val requestFactory: AndroidBibleVersionDownloadRequestFactory,
    private val pauseBibleVersion: PauseBibleVersionDownloadUseCase,
    private val deleteBibleVersion: DeleteBibleVersionDownloadUseCase,
) : BibleVersionDownloaderFacade {
    private val workManager = WorkManager.getInstance(context)

    override val shouldShowDownloadTip: Boolean = false

    override fun downloadVersion(versionId: String) {
        workManager.enqueueUniqueWork(
            uniqueWorkName = BibleVersionDownloadWorker.workName(versionId),
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = requestFactory.create(versionId),
        )
    }

    override suspend fun pauseDownload(versionId: String) {
        workManager.cancelUniqueWork(BibleVersionDownloadWorker.workName(versionId))
        pauseBibleVersion(versionId)
    }

    override suspend fun deleteDownload(versionId: String) {
        pauseDownload(versionId)
        deleteBibleVersion(versionId)
    }
}
