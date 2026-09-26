package com.quare.bibleplanner.feature.bibleversion.fake

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade

internal class FakeBibleVersionDownloaderFacade(
    override val shouldShowDownloadTip: Boolean = false,
) : BibleVersionDownloaderFacade {
    val calls = mutableListOf<String>()

    override fun downloadVersion(versionId: String) {
        calls += "download $versionId"
    }

    override suspend fun pauseDownload(versionId: String) {
        calls += "pause $versionId"
    }

    override suspend fun deleteDownload(versionId: String) {
        calls += "delete $versionId"
    }
}
