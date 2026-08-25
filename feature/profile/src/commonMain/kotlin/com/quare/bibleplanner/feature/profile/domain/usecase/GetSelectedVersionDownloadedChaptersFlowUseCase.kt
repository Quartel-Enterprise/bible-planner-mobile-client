package com.quare.bibleplanner.feature.profile.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedBibleFlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class GetSelectedVersionDownloadedChaptersFlowUseCase(
    private val getSelectedBible: GetSelectedBibleFlowUseCase,
) {
    operator fun invoke(): Flow<Int> = getSelectedBible()
        .map { bible -> bible?.downloadedChapters ?: 0 }
        .distinctUntilChanged()
}
