package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.mapNotNull

/*
 * Why: reuses the shared Bible stream instead of counting chapters again; that count is
 * expensive and a download would otherwise pay for it twice on every write.
 */
class ObserveBibleVersionDownloadProgressUseCase(
    private val bibleRepository: BibleRepository,
) : ObserveBibleVersionDownloadProgress {
    override fun invoke(versionId: String): Flow<Float> = bibleRepository
        .getBiblesFlow()
        .mapNotNull { bibles -> bibles.find { it.version.id == versionId } }
        /*
         * Why: right as a download starts the shared stream can still carry the version's previous state,
         * not started or downloaded, and reading it as complete made the Live Activity stick at 100%.
         */
        .dropWhile { bible -> bible.downloadStatus !is DownloadStatusModel.InProgress }
        .mapNotNull(::toProgressOrNull)
        .distinctUntilChanged()

    private fun toProgressOrNull(bible: BibleModel): Float? = when (val status = bible.downloadStatus) {
        is DownloadStatusModel.InProgress -> status.progress
        DownloadStatusModel.Downloaded -> COMPLETE_PROGRESS
        DownloadStatusModel.NotStarted -> null
    }

    private companion object {
        const val COMPLETE_PROGRESS = 1f
    }
}
