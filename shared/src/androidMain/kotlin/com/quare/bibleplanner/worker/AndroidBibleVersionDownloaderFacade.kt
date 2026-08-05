package com.quare.bibleplanner.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DeleteBibleVersionDownloadUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.PauseBibleVersionDownloadUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AndroidBibleVersionDownloaderFacade(
    context: Context,
    private val requestFactory: AndroidBibleVersionDownloadRequestFactory,
    private val pauseBibleVersion: PauseBibleVersionDownloadUseCase,
    private val deleteBibleVersion: DeleteBibleVersionDownloadUseCase,
) : BibleVersionDownloaderFacade {
    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val shouldShowDownloadTip: Boolean = false

    override fun downloadVersion(versionId: String) {
        scope.launch {
            val workName = BibleVersionDownloadWorker.workName(versionId)
            val hasRunningWork = withContext(Dispatchers.IO) {
                workManager
                    .getWorkInfosForUniqueWork(workName)
                    .get()
                    .any { workInfo -> workInfo.state == WorkInfo.State.RUNNING }
            }
            if (!hasRunningWork) {
                workManager.enqueueUniqueWork(
                    uniqueWorkName = workName,
                    existingWorkPolicy = ExistingWorkPolicy.REPLACE,
                    request = requestFactory.create(versionId),
                )
            }
        }
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
