package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

/**
 * Reuses the shared Bible stream rather than counting chapters again: the count behind it is
 * expensive, and a download would otherwise pay for it twice on every write.
 */
class ObserveBibleVersionDownloadProgressUseCase(
    private val bibleRepository: BibleRepository,
) : ObserveBibleVersionDownloadProgress {
    override fun invoke(versionId: String): Flow<Float> = bibleRepository
        .getBiblesFlow()
        .mapNotNull { bibles -> bibles.find { it.version.id == versionId } }
        .map(::toProgress)
        .distinctUntilChanged()

    private fun toProgress(bible: BibleModel): Float =
        (bible.downloadStatus as? DownloadStatusModel.InProgress)?.progress ?: COMPLETE_PROGRESS

    private companion object {
        const val COMPLETE_PROGRESS = 1f
    }
}
