package com.quare.bibleplanner.feature.bibleversion.domain

import com.quare.bibleplanner.core.books.domain.usecase.InitializeBooksIfNeededUseCase
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DownloadBooksInParallelUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetRemoteContentVersionUseCase

class DownloadBibleUseCase(
    private val bibleVersionDao: BibleVersionDao,
    private val verseDao: VerseDao,
    private val initializeBooksIfNeeded: InitializeBooksIfNeededUseCase,
    private val getRemoteContentVersion: GetRemoteContentVersionUseCase,
    private val downloadBooksInParallel: DownloadBooksInParallelUseCase,
    private val trackEvent: TrackEvent,
) {
    suspend operator fun invoke(versionId: String): Result<Unit> = suspendRunCatching {
        // Why: the chapters to download come from the book rows, which a first launch may still be inserting.
        suspendRunCatching { initializeBooksIfNeeded() }.onFailure { throwable ->
            return trackFailure(
                versionId = versionId,
                throwable = throwable,
            )
        }
        val version = bibleVersionDao.getVersionById(versionId)
            ?: return trackFailure(
                versionId = versionId,
                throwable = IllegalStateException("Version not found"),
            )

        val isComplete =
            verseDao.countChaptersWithVersesByVersion(versionId) >= version.totalChapters
        if (version.status == DownloadStatus.DONE && isComplete) return Result.success(Unit)

        val remoteContentVersion = getRemoteContentVersion(versionId)
        downloadBooksInParallel(
            versionId = versionId,
            contentVersion = remoteContentVersion,
        ).fold(
            onSuccess = { checkEveryChapterDownloaded(version) },
            onFailure = Result.Companion::failure,
        ).onSuccess {
            if (remoteContentVersion.isNotEmpty()) {
                bibleVersionDao.updateContentVersion(
                    id = versionId,
                    contentVersion = remoteContentVersion,
                )
            }
            bibleVersionDao.updateStatus(versionId, DownloadStatus.DONE)
            trackEvent(
                name = AnalyticsEventNames.BIBLE_VERSION_DOWNLOAD_COMPLETED,
                params = mapOf(AnalyticsParams.VERSION_ID to versionId),
            )
        }.onFailure { throwable ->
            trackDownloadFailed(
                versionId = versionId,
                throwable = throwable,
            )
        }.getOrThrow()
    }

    /*
     * Why: a book with no chapter rows downloads nothing and still succeeds, and a version marked done
     * that way showed an update right after its first download.
     */
    private suspend fun checkEveryChapterDownloaded(version: BibleVersionEntity): Result<Unit> = suspendRunCatching {
        val downloadedChapters = verseDao.countChaptersWithVersesByVersion(version.id)
        if (downloadedChapters < version.totalChapters) {
            throw IncompleteBibleDownloadException(
                versionId = version.id,
                downloadedChapters = downloadedChapters,
                totalChapters = version.totalChapters,
            )
        }
    }

    private fun trackFailure(
        versionId: String,
        throwable: Throwable,
    ): Result<Unit> {
        trackDownloadFailed(
            versionId = versionId,
            throwable = throwable,
        )
        return Result.failure(throwable)
    }

    private fun trackDownloadFailed(
        versionId: String,
        throwable: Throwable,
    ) {
        trackEvent(
            name = AnalyticsEventNames.BIBLE_VERSION_DOWNLOAD_FAILED,
            params = mapOf(
                AnalyticsParams.VERSION_ID to versionId,
                AnalyticsParams.REASON to (throwable::class.simpleName ?: UNKNOWN_REASON),
            ),
        )
    }

    private companion object {
        const val UNKNOWN_REASON = "unknown"
    }
}
