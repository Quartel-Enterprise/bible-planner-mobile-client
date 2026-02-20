package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.feature.bibleversion.domain.BibleVersionDownloaderFacade
import kotlinx.coroutines.flow.first

class SetSelectedVersionUseCase(
    private val repository: BibleVersionRepository,
    private val bibleVersionDownloaderFacade: BibleVersionDownloaderFacade,
) {
    suspend operator fun invoke(selectedId: String) {
        val selectionModels = repository.getSelectableBibleVersions().first()
        selectionModels.forEach { model ->
            val versionId = model.version.id
            bibleVersionDownloaderFacade.run {
                if (versionId == selectedId) {
                    downloadVersion(versionId)
                } else if (model.status is DownloadStatusModel.InProgress.Downloading) {
                    pauseDownload(versionId)
                }
            }
        }
        repository.setSelectedVersionId(selectedId)
    }
}
