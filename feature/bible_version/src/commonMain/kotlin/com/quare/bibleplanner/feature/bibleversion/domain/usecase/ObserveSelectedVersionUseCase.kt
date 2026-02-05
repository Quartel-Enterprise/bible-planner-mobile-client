package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.feature.bibleversion.domain.BibleVersionDownloaderFacade

class ObserveSelectedVersionUseCase(
    private val bibleVersionRepository: BibleVersionRepository,
    private val bibleVersionDao: BibleVersionDao,
    private val bibleVersionDownloaderFacade: BibleVersionDownloaderFacade,
) {
    suspend operator fun invoke() {
        bibleVersionRepository.getSelectedVersionAbbreviationFlow().collect { abbreviation ->
            bibleVersionDao.getVersionById(abbreviation)?.let { version ->
                if (shouldDownloadAfterLaunchApp(version.status)) {
                    bibleVersionDownloaderFacade.downloadVersion(version.id)
                }
            }
        }
    }

    private fun shouldDownloadAfterLaunchApp(status: DownloadStatus): Boolean =
        status == DownloadStatus.NOT_STARTED || status == DownloadStatus.IN_PROGRESS
}
