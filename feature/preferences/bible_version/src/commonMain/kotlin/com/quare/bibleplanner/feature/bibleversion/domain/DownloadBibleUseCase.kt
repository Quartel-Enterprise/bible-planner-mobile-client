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
        initializeBooksIfNeeded()
        val version = bibleVersionDao.getVersionById(versionId) ?: error("Version not found")
        if (version.status == DownloadStatus.DONE && countMissingChapters(version) <= 0) {
            return Result.success(Unit)
        }

        val remoteContentVersion = getRemoteContentVersion(versionId)
        downloadBooksInParallel(
            versionId = versionId,
            contentVersion = remoteContentVersion,
        ).getOrThrow()
        /*
         * Why: every book succeeding doesn't prove every chapter was stored, and a version marked done
         * short of chapters showed an update right after its first download.
         */
        val missingChapters = countMissingChapters(version)
        if (missingChapters > 0) {
            throw IncompleteBibleDownloadException(
                versionId = versionId,
                missingChapters = missingChapters,
                totalChapters = version.totalChapters,
            )
        }
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
    }

    private suspend fun countMissingChapters(version: BibleVersionEntity): Int =
        version.totalChapters - verseDao.countChaptersWithVersesByVersion(version.id)

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
