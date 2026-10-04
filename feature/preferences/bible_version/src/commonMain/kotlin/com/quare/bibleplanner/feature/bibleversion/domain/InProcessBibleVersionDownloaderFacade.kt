package com.quare.bibleplanner.feature.bibleversion.domain

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DeleteBibleVersionDownloadUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.PauseBibleVersionDownloadUseCase

class InProcessBibleVersionDownloaderFacade(
    private val downloader: InProcessBibleVersionDownloader,
    private val pauseBibleVersion: PauseBibleVersionDownloadUseCase,
    private val deleteBibleVersion: DeleteBibleVersionDownloadUseCase,
) : BibleVersionDownloaderFacade {
    override val shouldShowDownloadTip: Boolean = false

    override fun downloadVersion(versionId: String) {
        downloader.startDownload(versionId)
    }

    override suspend fun pauseDownload(versionId: String) {
        downloader.cancelDownload(versionId)
        pauseBibleVersion(versionId)
    }

    override suspend fun deleteDownload(versionId: String) {
        downloader.cancelDownload(versionId)
        deleteBibleVersion(versionId)
    }
}
