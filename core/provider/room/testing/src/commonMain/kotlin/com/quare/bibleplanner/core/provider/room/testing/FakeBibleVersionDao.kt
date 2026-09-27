package com.quare.bibleplanner.core.provider.room.testing

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeBibleVersionDao(
    versions: List<BibleVersionEntity>,
) : BibleVersionDao {
    val versions = MutableStateFlow(versions)

    override fun getAllVersionsFlow(): Flow<List<BibleVersionEntity>> = versions

    override suspend fun getAllVersions(): List<BibleVersionEntity> = versions.value

    override suspend fun getVersionById(id: String): BibleVersionEntity? = versions.value.find { version ->
        version.id ==
            id
    }

    override suspend fun insertVersion(version: BibleVersionEntity) {
        versions.value = versions.value.filterNot { current -> current.id == version.id } + version
    }

    override suspend fun updateVersion(version: BibleVersionEntity) {
        update(version.id) { version }
    }

    override suspend fun updateStatus(
        id: String,
        status: DownloadStatus,
    ) {
        update(id) { current -> current.copy(status = status) }
    }

    override suspend fun updateContentVersion(
        id: String,
        contentVersion: String,
    ) {
        update(id) { current -> current.copy(contentVersion = contentVersion) }
    }

    override suspend fun deleteVersion(id: String) {
        versions.value = versions.value.filterNot { current -> current.id == id }
    }

    private fun update(
        id: String,
        transform: (BibleVersionEntity) -> BibleVersionEntity,
    ) {
        versions.value = versions.value.map { current -> if (current.id == id) transform(current) else current }
    }
}
