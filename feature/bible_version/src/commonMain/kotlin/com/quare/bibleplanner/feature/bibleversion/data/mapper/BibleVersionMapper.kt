package com.quare.bibleplanner.feature.bibleversion.data.mapper

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusMapper
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.feature.bibleversion.domain.model.BibleVersionModel
import com.quare.bibleplanner.feature.bibleversion.domain.model.VersionModel

internal class BibleVersionMapper(
    private val downloadStatusMapper: DownloadStatusMapper,
) {
    fun map(
        entity: BibleVersionEntity,
        versionModel: VersionModel,
    ): BibleVersionModel = entity.run {
        BibleVersionModel(
            id = versionModel.id,
            name = versionModel.name,
            status = toStatus(),
        )
    }

    private fun BibleVersionEntity.toStatus(): DownloadStatusModel = downloadStatusMapper.map(status, downloadProgress)
}
