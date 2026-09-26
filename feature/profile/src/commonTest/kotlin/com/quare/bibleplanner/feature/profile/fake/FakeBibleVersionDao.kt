package com.quare.bibleplanner.feature.profile.fake

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeBibleVersionDao(
    private val versions: List<BibleVersionEntity>,
) : BibleVersionDao {
    override fun getAllVersionsFlow(): Flow<List<BibleVersionEntity>> = flowOf(versions)

    override suspend fun getAllVersions(): List<BibleVersionEntity> = error("unused")

    override suspend fun getVersionById(id: String): BibleVersionEntity? = error("unused")

    override suspend fun insertVersion(version: BibleVersionEntity) = error("unused")

    override suspend fun updateVersion(version: BibleVersionEntity) = error("unused")

    override suspend fun updateStatus(
        id: String,
        status: DownloadStatus,
    ) = error("unused")

    override suspend fun updateContentVersion(
        id: String,
        contentVersion: String,
    ) = error("unused")

    override suspend fun deleteVersion(id: String) = error("unused")
}
