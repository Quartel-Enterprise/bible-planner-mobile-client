package com.quare.bibleplanner.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloader
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DeleteBibleVersionDownloadUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.PauseBibleVersionDownloadUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AndroidBibleVersionDownloaderFacade(
    private val context: Context,
    private val requestFactory: AndroidBibleVersionDownloadRequestFactory,
    private val inProcessDownloader: InProcessBibleVersionDownloader,
    private val pauseBibleVersion: PauseBibleVersionDownloadUseCase,
    private val deleteBibleVersion: DeleteBibleVersionDownloadUseCase,
) : BibleVersionDownloaderFacade {
    private val workManager: WorkManager? by lazy {
        runCatching { WorkManager.getInstance(context) }
            .onFailure { throwable ->
                Logger.e(throwable) { "WorkManager is unavailable, falling back to in-process downloads" }
            }.getOrNull()
    }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val shouldShowDownloadTip: Boolean = false

    override fun downloadVersion(versionId: String) {
        val manager = workManager
        if (manager == null) {
            inProcessDownloader.startDownload(versionId)
        } else {
            enqueueDownload(manager = manager, versionId = versionId)
        }
    }

    override suspend fun pauseDownload(versionId: String) {
        val manager = workManager
        if (manager == null) {
            inProcessDownloader.cancelDownload(versionId)
        } else {
            manager.cancelUniqueWork(BibleVersionDownloadWorker.workName(versionId))
        }
        pauseBibleVersion(versionId)
    }

    override suspend fun deleteDownload(versionId: String) {
        pauseDownload(versionId)
        deleteBibleVersion(versionId)
    }

    private fun enqueueDownload(
        manager: WorkManager,
        versionId: String,
    ) {
        scope.launch {
            val workName = BibleVersionDownloadWorker.workName(versionId)
            val hasRunningWork = withContext(Dispatchers.IO) {
                manager
                    .getWorkInfosForUniqueWork(workName)
                    .get()
                    .any { workInfo -> workInfo.state == WorkInfo.State.RUNNING }
            }
            if (!hasRunningWork) {
                manager.enqueueUniqueWork(
                    uniqueWorkName = workName,
                    existingWorkPolicy = ExistingWorkPolicy.REPLACE,
                    request = requestFactory.create(versionId),
                )
            }
        }
    }
}
