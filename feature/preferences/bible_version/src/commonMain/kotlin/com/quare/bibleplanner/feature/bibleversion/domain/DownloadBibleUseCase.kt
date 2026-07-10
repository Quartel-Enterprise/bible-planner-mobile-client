package com.quare.bibleplanner.feature.bibleversion.domain

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DownloadBooksInParallelUseCase

class DownloadBibleUseCase(
    private val bibleVersionDao: BibleVersionDao,
    private val downloadBooksInParallel: DownloadBooksInParallelUseCase,
    private val trackEvent: TrackEvent,
) {
    suspend operator fun invoke(versionId: String): Result<Unit> = suspendRunCatching {
        val version = bibleVersionDao.getVersionById(versionId)
            ?: return trackedFailure(
                versionId = versionId,
                throwable = IllegalStateException("Version not found"),
            )

        if (version.status == DownloadStatus.DONE) return Result.success(Unit)

        downloadBooksInParallel(versionId)
            .onSuccess {
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

    private fun trackedFailure(
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
