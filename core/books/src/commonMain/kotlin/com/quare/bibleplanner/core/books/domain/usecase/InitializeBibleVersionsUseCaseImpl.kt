package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
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
    private val downloaderFacade: BibleVersionDownloaderFacade,
) : InitializeBibleVersionsUseCase {
    override suspend fun invoke() {
        metadataRepository
            .getVersions()
            .getOrNull()
            ?.forEach { versionModel ->
                val existingVersion = bibleVersionDao.getVersionById(versionModel.id)
                if (existingVersion == null) {
                    insertVersion(versionModel)
                } else if (existingVersion.contentVersion != versionModel.version) {
                    refreshOutdatedVersion(
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

    private suspend fun refreshOutdatedVersion(
        existingVersion: BibleVersionEntity,
        remoteVersion: VersionModel,
    ) {
        bibleVersionDao.updateVersion(
            existingVersion.copy(
                totalChapters = remoteVersion.chapters,
                contentVersion = remoteVersion.version,
            ),
        )
        val hasDownloadedContent = verseDao.countChaptersWithVersesByVersion(existingVersion.id) > 0
        if (hasDownloadedContent) {
            verseDao.deleteVerseTextsByVersion(existingVersion.id)
            downloaderFacade.downloadVersion(existingVersion.id)
        }
    }
}
