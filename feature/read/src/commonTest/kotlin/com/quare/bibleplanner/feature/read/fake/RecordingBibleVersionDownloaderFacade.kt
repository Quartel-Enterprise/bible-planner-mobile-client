package com.quare.bibleplanner.feature.read.fake

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade

internal class RecordingBibleVersionDownloaderFacade : BibleVersionDownloaderFacade {
    val downloadedVersionIds = mutableListOf<String>()

    override fun downloadVersion(versionId: String) {
        downloadedVersionIds += versionId
    }

    override suspend fun pauseDownload(versionId: String) = error("unused")

    override suspend fun deleteDownload(versionId: String) = error("unused")
}
