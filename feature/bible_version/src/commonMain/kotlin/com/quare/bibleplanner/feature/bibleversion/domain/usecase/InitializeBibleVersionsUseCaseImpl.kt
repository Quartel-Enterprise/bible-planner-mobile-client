package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.feature.bibleversion.domain.repository.BibleVersionMetadataRepository

internal class InitializeBibleVersionsUseCaseImpl(
    private val bibleVersionDao: BibleVersionDao,
    private val metadataRepository: BibleVersionMetadataRepository,
) : InitializeBibleVersionsUseCase {
    override suspend fun invoke() {
        metadataRepository
            .getVersions()
            .getOrNull()
            ?.forEach { versionModel ->
                val existingVersion = bibleVersionDao.getVersionById(versionModel.id)
                if (existingVersion == null) {
                    bibleVersionDao.insertVersion(
                        BibleVersionEntity(
                            id = versionModel.id,
                            downloadProgress = 0f,
                            status = DownloadStatus.NOT_STARTED,
                        ),
                    )
                }
            }
    }
}
