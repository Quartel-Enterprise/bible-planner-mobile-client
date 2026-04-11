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
        downloadedChaptersMap: Map<String, Int>,
    ): List<BibleModel> = supportedVersions.mapNotNull { versionModel: VersionModel ->
        dataBaseVersions.find { it.id == versionModel.id }?.toDomain(versionModel, downloadedChaptersMap)
    }

    private fun BibleVersionEntity.toDomain(
        versionModel: VersionModel,
        downloadedChaptersMap: Map<String, Int>,
    ): BibleModel = BibleModel(
        version = versionModel,
        downloadStatus = toStatus(downloadedChaptersMap[id] ?: 0),
        isSelected = false,
    )

    private fun BibleVersionEntity.toStatus(downloadedChapters: Int): DownloadStatusModel {
        val progress = downloadedChapters.toFloat() / totalChapters
        return downloadStatusMapper.map(
            status = status,
            progress = progress,
        )
    }
}
