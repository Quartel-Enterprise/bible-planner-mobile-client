package com.quare.bibleplanner.worker

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloader
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DeleteBibleVersionDownloadUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.PauseBibleVersionDownloadUseCase

internal class IosBibleVersionDownloaderFacade(
    private val downloader: InProcessBibleVersionDownloader,
    private val pauseBibleVersion: PauseBibleVersionDownloadUseCase,
    private val deleteBibleVersion: DeleteBibleVersionDownloadUseCase,
    private val bgTaskScheduler: IosBackgroundTaskScheduler,
) : BibleVersionDownloaderFacade, BackgroundDownloadHandler {

    init {
        bgTaskScheduler.setHandler(this)
    }

    override fun downloadVersion(versionId: String) {
        downloader.startDownload(versionId)
        bgTaskScheduler.scheduleDownload()
    }

    override suspend fun pauseDownload(versionId: String) {
        downloader.cancelDownload(versionId)
        if (!downloader.hasAnyActiveDownload()) bgTaskScheduler.cancelDownload()
        pauseBibleVersion(versionId)
    }

    override suspend fun deleteDownload(versionId: String) {
        pauseDownload(versionId)
        deleteBibleVersion(versionId)
    }

    override fun onResume(onComplete: (Boolean) -> Unit) {
        downloader.resumePendingDownloads(onComplete)
    }

    override fun onExpire(onComplete: () -> Unit) {
        downloader.cancelAllDownloads()
        bgTaskScheduler.cancelDownload()
        onComplete()
    }
}
