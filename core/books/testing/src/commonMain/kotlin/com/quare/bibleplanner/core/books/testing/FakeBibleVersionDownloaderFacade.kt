package com.quare.bibleplanner.core.books.testing

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import kotlinx.coroutines.CompletableDeferred

class FakeBibleVersionDownloaderFacade : BibleVersionDownloaderFacade {
    var deletionGate: CompletableDeferred<Unit>? = null
    val calls = mutableListOf<String>()

    override fun downloadVersion(versionId: String) {
        calls += "download $versionId"
    }

    override suspend fun pauseDownload(versionId: String) {
        calls += "pause $versionId"
    }

    override suspend fun deleteDownload(versionId: String) {
        deletionGate?.await()
        calls += "delete $versionId"
    }
}
