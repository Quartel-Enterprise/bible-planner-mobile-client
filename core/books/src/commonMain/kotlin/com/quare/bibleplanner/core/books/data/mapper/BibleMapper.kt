package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusMapper
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity

internal class BibleMapper(
    private val downloadStatusMapper: DownloadStatusMapper,
) {
    fun map(
        dataBaseVersions: List<BibleVersionEntity>,
        supportedVersions: List<VersionModel>,
    ): List<BibleModel> = supportedVersions.mapNotNull { versionModel: VersionModel ->
        dataBaseVersions.find { it.id == versionModel.id }?.toDomain(versionModel)
    }

    private fun BibleVersionEntity.toDomain(versionModel: VersionModel): BibleModel = run {
        BibleModel(
            version = versionModel,
            downloadStatus = toStatus(),
            isSelected = false,
        )
    }

    private fun BibleVersionEntity.toStatus(): DownloadStatusModel = downloadStatusMapper.map(status, downloadProgress)
}
