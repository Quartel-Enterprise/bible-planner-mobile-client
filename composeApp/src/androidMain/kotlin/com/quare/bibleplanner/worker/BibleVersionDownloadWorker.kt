package com.quare.bibleplanner.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.bibleversion.domain.DownloadBibleUseCase
import com.quare.bibleplanner.notification.AndroidBibleVersionDownloadNotifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
    private val verseDao: VerseDao by inject()
    private val bibleRepository: BibleRepository by inject()

    // Required for expedited work: used to run the worker as a foreground service / expedited job.
    override suspend fun getForegroundInfo(): ForegroundInfo {
        val versionId = inputData.getString(KEY_VERSION_ID).orEmpty()
        return notifier.buildForegroundInfo(
            versionId = versionId,
            versionName = versionId,
        )
    }

    override suspend fun doWork(): Result {
        val versionId = inputData.getString(KEY_VERSION_ID) ?: return Result.failure()

        bibleVersionDao.updateStatus(id = versionId, status = DownloadStatus.IN_PROGRESS)
        // If foreground promotion is refused (started while backgrounded on Android 12+), keep
        // downloading in the background instead of crashing.
        suspendRunCatching { setForeground(getForegroundInfo()) }
            .onFailure { error ->
                Log.w(LOG_TAG, "Foreground promotion refused; continuing download in background", error)
            }

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
                    combine(
                        bibleVersionDao.getAllVersionsFlow().mapNotNull { list -> list.find { it.id == versionId } },
                        verseDao.countChaptersWithVersesByVersionFlow(versionId),
                    ) { entity, count -> count.toFloat() / entity.totalChapters }
                        .distinctUntilChanged()
                        .collect { progress ->
                            lastProgress = progress
                            notifier.showProgress(versionId, versionName, progress)
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
        private const val LOG_TAG = "BibleVersionDownload"

        fun workName(versionId: String) = "bible_version_download_$versionId"
    }
}
