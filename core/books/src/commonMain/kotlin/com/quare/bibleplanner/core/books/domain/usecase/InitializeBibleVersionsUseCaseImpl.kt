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
        val versions = metadataRepository.getVersions().getOrNull() ?: return
        var hasScheduledDownload = false
        versions.forEach { versionModel ->
            val existingVersion = bibleVersionDao.getVersionById(versionModel.id)
            if (existingVersion == null) {
                insertVersion(versionModel)
            } else {
                val hasScheduledNow = reconcileVersion(
                    existingVersion = existingVersion,
                    remoteVersion = versionModel,
                    canScheduleDownload = !hasScheduledDownload,
                )
                hasScheduledDownload = hasScheduledDownload || hasScheduledNow
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

    private suspend fun reconcileVersion(
        existingVersion: BibleVersionEntity,
        remoteVersion: VersionModel,
        canScheduleDownload: Boolean,
    ): Boolean {
        val isContentOutdated = remoteVersion.version.isNotEmpty() &&
            existingVersion.contentVersion != remoteVersion.version
        val downloadedChapters = verseDao.countChaptersWithVersesByVersion(existingVersion.id)
        if (isContentOutdated && downloadedChapters == 0) {
            bibleVersionDao.updateVersion(
                existingVersion.copy(
                    totalChapters = remoteVersion.chapters,
                    contentVersion = remoteVersion.version,
                ),
            )
            return false
        }
        val hasMissingChapters = existingVersion.status == DownloadStatus.DONE &&
            downloadedChapters < existingVersion.totalChapters
        if (canScheduleDownload && (isContentOutdated || hasMissingChapters)) {
            downloaderFacade.downloadVersion(existingVersion.id)
            return true
        }
        return false
    }
}
