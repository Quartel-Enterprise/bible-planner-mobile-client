package com.quare.bibleplanner.core.books.domain

interface BibleVersionDownloaderFacade {
    fun downloadVersion(versionId: String)

    suspend fun pauseDownload(versionId: String)

    suspend fun deleteDownload(versionId: String)
}
