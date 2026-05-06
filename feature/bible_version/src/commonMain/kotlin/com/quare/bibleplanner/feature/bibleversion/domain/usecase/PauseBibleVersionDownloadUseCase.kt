package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao

class PauseBibleVersionDownloadUseCase(
    private val bibleVersionDao: BibleVersionDao,
    private val notifier: BibleVersionDownloadNotifier,
) {
    suspend operator fun invoke(versionId: String) {
        notifier.dismiss(versionId)
        bibleVersionDao.updateStatus(id = versionId, status = DownloadStatus.PAUSED)
    }
}
