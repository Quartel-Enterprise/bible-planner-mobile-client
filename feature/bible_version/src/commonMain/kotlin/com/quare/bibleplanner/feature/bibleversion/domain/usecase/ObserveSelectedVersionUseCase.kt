package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionAbbreviationFlowUseCase
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.feature.bibleversion.domain.BibleVersionDownloaderFacade

class ObserveSelectedVersionUseCase(
    private val bibleVersionDao: BibleVersionDao,
    private val bibleVersionDownloaderFacade: BibleVersionDownloaderFacade,
    private val getSelectedVersionAbbreviationFlow: GetSelectedVersionAbbreviationFlowUseCase,
) {
    suspend operator fun invoke() {
        getSelectedVersionAbbreviationFlow().collect { abbreviation ->
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
