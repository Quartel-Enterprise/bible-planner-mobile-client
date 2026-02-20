package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.books.domain.model.BibleSelectionModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusMapper
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity

internal class BibleSelectionMapper(
    private val downloadStatusMapper: DownloadStatusMapper,
) {
    fun map(
        dataBaseVersions: List<BibleVersionEntity>,
        supportedVersions: List<VersionModel>,
    ): List<BibleSelectionModel> = supportedVersions.mapNotNull { versionModel: VersionModel ->
        dataBaseVersions.find { it.id == versionModel.id }?.toDomain(versionModel)
    }

    private fun BibleVersionEntity.toDomain(versionModel: VersionModel): BibleSelectionModel = run {
        BibleSelectionModel(
            version = versionModel,
            status = toStatus(),
            isSelected = false,
        )
    }

    private fun BibleVersionEntity.toStatus(): DownloadStatusModel = downloadStatusMapper.map(status, downloadProgress)
}
