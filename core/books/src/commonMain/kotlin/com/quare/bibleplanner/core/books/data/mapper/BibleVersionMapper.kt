package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.books.domain.model.BibleVersionModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusMapper
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity

internal class BibleVersionMapper(
    private val downloadStatusMapper: DownloadStatusMapper,
) {
    fun map(
        dataBaseVersions: List<BibleVersionEntity>,
        supportedVersions: List<VersionModel>,
    ): List<BibleVersionModel> = supportedVersions.mapNotNull { versionModel: VersionModel ->
        dataBaseVersions.find { it.id == versionModel.id }?.toDomain(versionModel)
    }

    private fun BibleVersionEntity.toDomain(versionModel: VersionModel): BibleVersionModel = run {
        BibleVersionModel(
            id = versionModel.id,
            name = versionModel.name,
            status = toStatus(),
            isSelected = false,
        )
    }

    private fun BibleVersionEntity.toStatus(): DownloadStatusModel = downloadStatusMapper.map(status, downloadProgress)
}
