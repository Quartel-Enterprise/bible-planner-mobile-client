package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao

class DeleteBibleVersionDownloadUseCase(
    private val bibleVersionDao: BibleVersionDao,
    private val verseDao: VerseDao,
    private val notifier: BibleVersionDownloadNotifier,
) {
    suspend operator fun invoke(versionId: String) {
        notifier.dismiss(versionId)
        bibleVersionDao.updateStatus(id = versionId, status = DownloadStatus.NOT_STARTED)
        verseDao.deleteVerseTextsByVersion(versionId)
    }
}
