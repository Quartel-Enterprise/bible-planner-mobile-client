package com.quare.bibleplanner.feature.bibleversion.domain

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DownloadBooksInParallelUseCase

class DownloadBibleUseCase(
    private val bibleVersionDao: BibleVersionDao,
    private val downloadBooksInParallel: DownloadBooksInParallelUseCase,
) {
    suspend operator fun invoke(versionId: String): Result<Unit> = suspendRunCatching {
        val version = bibleVersionDao.getVersionById(versionId)
            ?: return Result.failure(IllegalStateException("Version not found"))

        if (version.status == DownloadStatus.DONE) return Result.success(Unit)

        downloadBooksInParallel(versionId)
            .onSuccess { bibleVersionDao.updateStatus(versionId, DownloadStatus.DONE) }
            .getOrThrow()
    }
}
