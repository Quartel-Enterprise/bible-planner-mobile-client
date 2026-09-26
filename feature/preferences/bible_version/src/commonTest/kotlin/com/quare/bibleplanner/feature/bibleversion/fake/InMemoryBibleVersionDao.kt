package com.quare.bibleplanner.feature.bibleversion.fake

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity

internal class InMemoryBibleVersionDao(
    initialVersions: List<BibleVersionEntity> = emptyList(),
) : ThrowingBibleVersionDao() {
    val versions = initialVersions.associateBy { it.id }.toMutableMap()
    val statusUpdates = mutableListOf<Pair<String, DownloadStatus>>()

    override suspend fun getAllVersions(): List<BibleVersionEntity> = versions.values.toList()

    override suspend fun getVersionById(id: String): BibleVersionEntity? = versions[id]

    override suspend fun updateStatus(
        id: String,
        status: DownloadStatus,
    ) {
        statusUpdates += id to status
        versions[id]?.let { versions[id] = it.copy(status = status) }
    }

    override suspend fun updateContentVersion(
        id: String,
        contentVersion: String,
    ) {
        versions[id]?.let { versions[id] = it.copy(contentVersion = contentVersion) }
    }
}
