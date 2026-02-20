package com.quare.bibleplanner.feature.bibleversion.domain

interface BibleVersionDownloaderFacade {
    fun downloadVersion(versionId: String)

    suspend fun pauseDownload(versionId: String)

    suspend fun deleteDownload(versionId: String)
}
