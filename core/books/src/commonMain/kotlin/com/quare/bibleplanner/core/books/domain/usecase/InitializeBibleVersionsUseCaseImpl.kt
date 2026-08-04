package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity

internal class InitializeBibleVersionsUseCaseImpl(
    private val bibleVersionDao: BibleVersionDao,
    private val verseDao: VerseDao,
    private val metadataRepository: BibleVersionRepository,
) : InitializeBibleVersionsUseCase {
    override suspend fun invoke() {
        metadataRepository
            .getVersions(forceRefresh = false)
            .getOrNull()
            ?.forEach { versionModel ->
                val existingVersion = bibleVersionDao.getVersionById(versionModel.id)
                if (existingVersion == null) {
                    insertVersion(versionModel)
                } else {
                    adoptRemoteVersionIfNothingDownloaded(
                        existingVersion = existingVersion,
                        remoteVersion = versionModel,
                    )
                }
            }
    }

    private suspend fun insertVersion(versionModel: VersionModel) {
        bibleVersionDao.insertVersion(
            BibleVersionEntity(
                id = versionModel.id,
                status = DownloadStatus.NOT_STARTED,
                totalChapters = versionModel.chapters,
                contentVersion = versionModel.version,
            ),
        )
    }

    private suspend fun adoptRemoteVersionIfNothingDownloaded(
        existingVersion: BibleVersionEntity,
        remoteVersion: VersionModel,
    ) {
        val isContentOutdated = remoteVersion.version.isNotEmpty() &&
            existingVersion.contentVersion != remoteVersion.version
        if (!isContentOutdated) return
        val downloadedChapters = verseDao.countChaptersWithVersesByVersion(existingVersion.id)
        if (downloadedChapters == 0) {
            bibleVersionDao.updateVersion(
                existingVersion.copy(
                    totalChapters = remoteVersion.chapters,
                    contentVersion = remoteVersion.version,
                ),
            )
        }
    }
}
