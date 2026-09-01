package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade

class UpdateBibleVersionUseCase(
    private val deleteBibleVersionDownload: DeleteBibleVersionDownloadUseCase,
    private val downloaderFacade: BibleVersionDownloaderFacade,
) {
    suspend operator fun invoke(versionId: String) {
        deleteBibleVersionDownload(versionId)
        downloaderFacade.downloadVersion(versionId)
    }
}
