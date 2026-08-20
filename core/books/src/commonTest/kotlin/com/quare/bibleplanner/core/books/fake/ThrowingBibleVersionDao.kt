package com.quare.bibleplanner.core.books.fake

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import kotlinx.coroutines.flow.Flow

internal open class ThrowingBibleVersionDao : BibleVersionDao {
    override fun getAllVersionsFlow(): Flow<List<BibleVersionEntity>> = error("Unexpected call")

    override suspend fun getAllVersions(): List<BibleVersionEntity> = error("Unexpected call")

    override suspend fun getVersionById(id: String): BibleVersionEntity? = error("Unexpected call")

    override suspend fun insertVersion(version: BibleVersionEntity) {
        error("Unexpected call")
    }

    override suspend fun updateVersion(version: BibleVersionEntity) {
        error("Unexpected call")
    }

    override suspend fun updateStatus(
        id: String,
        status: DownloadStatus,
    ) {
        error("Unexpected call")
    }

    override suspend fun updateContentVersion(
        id: String,
        contentVersion: String,
    ) {
        error("Unexpected call")
    }

    override suspend fun deleteVersion(id: String) {
        error("Unexpected call")
    }
}
