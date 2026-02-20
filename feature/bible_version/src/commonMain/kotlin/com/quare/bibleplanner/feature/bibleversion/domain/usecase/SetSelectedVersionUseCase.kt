package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.feature.bibleversion.domain.BibleVersionDownloaderFacade
import kotlinx.coroutines.flow.first

class SetSelectedVersionUseCase(
    private val repository: BibleRepository,
    private val bibleVersionDownloaderFacade: BibleVersionDownloaderFacade,
) {
    suspend operator fun invoke(selectedId: String) {
        val bibles = repository.getBiblesFlow().first()
        bibles.forEach { bible ->
            val versionId = bible.version.id
            bibleVersionDownloaderFacade.run {
                if (versionId == selectedId) {
                    downloadVersion(versionId)
                } else if (bible.downloadStatus is DownloadStatusModel.InProgress.Downloading) {
                    pauseDownload(versionId)
                }
            }
        }
        repository.setSelectedVersionId(selectedId)
    }
}
