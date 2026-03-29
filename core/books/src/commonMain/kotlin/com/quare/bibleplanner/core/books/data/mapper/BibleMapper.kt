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
        downloadedChaptersPerVersion: Map<String, Int>,
    ): List<BibleModel> = supportedVersions.mapNotNull { versionModel: VersionModel ->
        val downloadedChapters = downloadedChaptersPerVersion[versionModel.id] ?: 0
        dataBaseVersions.find { it.id == versionModel.id }?.toDomain(versionModel, downloadedChapters)
    }

    private fun BibleVersionEntity.toDomain(versionModel: VersionModel, downloadedChapters: Int): BibleModel = run {
        BibleModel(
            version = versionModel,
            downloadStatus = toStatus(downloadedChapters, versionModel.chapters),
            isSelected = false,
        )
    }

    private fun BibleVersionEntity.toStatus(downloadedChapters: Int, totalChapters: Int): DownloadStatusModel {
        val progress = if (totalChapters > 0) downloadedChapters.toFloat() / totalChapters else 0f
        return downloadStatusMapper.map(status, progress)
    }
}
