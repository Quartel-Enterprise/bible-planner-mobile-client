package com.quare.bibleplanner.worker

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloader
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DeleteBibleVersionDownloadUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.PauseBibleVersionDownloadUseCase

internal class DesktopBibleVersionDownloaderFacade(
    private val downloader: InProcessBibleVersionDownloader,
    private val pauseBibleVersion: PauseBibleVersionDownloadUseCase,
    private val deleteBibleVersion: DeleteBibleVersionDownloadUseCase,
) : BibleVersionDownloaderFacade {

    override fun downloadVersion(versionId: String) {
        downloader.startDownload(versionId)
    }

    override suspend fun pauseDownload(versionId: String) {
        downloader.cancelDownload(versionId)
        pauseBibleVersion(versionId)
    }

    override suspend fun deleteDownload(versionId: String) {
        pauseDownload(versionId)
        deleteBibleVersion(versionId)
    }
}
