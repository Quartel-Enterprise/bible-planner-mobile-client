package com.quare.bibleplanner.feature.bibleversion.data.repository

import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionDownloadStatus
import com.quare.bibleplanner.feature.bibleversion.domain.model.BibleVersion
import com.quare.bibleplanner.feature.bibleversion.domain.model.DownloadStatus
import com.quare.bibleplanner.feature.bibleversion.domain.repository.BibleVersionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BibleVersionRepositoryImpl(
    private val bibleVersionDao: BibleVersionDao,
) : BibleVersionRepository {
    private val supportedVersions = listOf(
        SupportedVersion("WEB", "World English Bible"),
        SupportedVersion("ACF", "Almeida Corrigida Fiel"),
    )

    override fun getBibleVersions(): Flow<List<BibleVersion>> = bibleVersionDao.getAllVersionsFlow().map { dbVersions ->
        supportedVersions.map { supported ->
            val db = dbVersions.find { it.id == supported.id }
            val progress = db?.downloadProgress ?: 0f
            val status = db?.status

            val domainStatus = when (status) {
                BibleVersionDownloadStatus.DONE -> DownloadStatus.Downloaded
                BibleVersionDownloadStatus.IN_PROGRESS -> DownloadStatus.Downloading(progress)
                BibleVersionDownloadStatus.PAUSED -> DownloadStatus.Paused(progress)
                else -> DownloadStatus.NotDownloaded
            }

            BibleVersion(
                id = supported.id,
                name = supported.name,
                status = domainStatus,
            )
        }
    }

    private data class SupportedVersion(
        val id: String,
        val name: String,
    )
}
