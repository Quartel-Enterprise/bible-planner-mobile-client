package com.quare.bibleplanner.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.feature.bibleversion.domain.DownloadBibleUseCase
import com.quare.bibleplanner.notification.AndroidBibleVersionDownloadNotifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class BibleVersionDownloadWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params),
    KoinComponent {
    private val downloadBible: DownloadBibleUseCase by inject()
    private val notifier: AndroidBibleVersionDownloadNotifier by inject()
    private val bibleVersionDao: BibleVersionDao by inject()
    private val bibleRepository: BibleRepository by inject()

    override suspend fun doWork(): Result {
        val versionId = inputData.getString(KEY_VERSION_ID) ?: return Result.failure()

        bibleVersionDao.updateStatus(id = versionId, status = DownloadStatus.IN_PROGRESS)
        setForeground(notifier.buildForegroundInfo(versionId, versionId))

        val versionName = bibleRepository
            .getBiblesFlow()
            .first()
            .find { it.version.id == versionId }
            ?.version
            ?.name ?: versionId

        var lastProgress = 0f
        return try {
            val result = coroutineScope {
                val progressObserver = launch {
                    bibleVersionDao
                        .getAllVersionsFlow()
                        .mapNotNull { list -> list.find { it.id == versionId } }
                        .distinctUntilChangedBy { it.downloadProgress }
                        .collect { entity ->
                            lastProgress = entity.downloadProgress
                            notifier.showProgress(versionId, versionName, entity.downloadProgress)
                        }
                }
                val downloadResult = downloadBible(versionId)
                progressObserver.cancel()
                downloadResult
            }

            result
                .onSuccess {
                    notifier.showComplete(versionId, versionName)
                }.onFailure {
                    notifier.showError(versionId, versionName)
                    bibleVersionDao.updateStatus(id = versionId, status = DownloadStatus.PAUSED)
                }

            if (result.isSuccess) Result.success() else Result.failure()
        } catch (_: CancellationException) {
            withContext(NonCancellable) {
                notifier.showPaused(versionId, versionName, lastProgress)
                bibleVersionDao.updateStatus(id = versionId, status = DownloadStatus.PAUSED)
            }
            Result.failure()
        }
    }

    companion object {
        const val KEY_VERSION_ID = "version_id"

        fun workName(versionId: String) = "bible_version_download_$versionId"
    }
}
