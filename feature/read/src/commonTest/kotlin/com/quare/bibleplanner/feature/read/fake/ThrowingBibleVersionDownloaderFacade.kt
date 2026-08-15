package com.quare.bibleplanner.feature.read.fake

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade

internal object ThrowingBibleVersionDownloaderFacade : BibleVersionDownloaderFacade {
    override val shouldShowDownloadTip: Boolean get() = error("unused")

    override fun downloadVersion(versionId: String) = error("unused")

    override suspend fun pauseDownload(versionId: String) = error("unused")

    override suspend fun deleteDownload(versionId: String) = error("unused")
}
