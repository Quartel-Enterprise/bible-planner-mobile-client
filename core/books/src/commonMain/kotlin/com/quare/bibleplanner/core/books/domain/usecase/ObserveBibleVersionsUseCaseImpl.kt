package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository

internal class ObserveBibleVersionsUseCaseImpl(
    private val metadataRepository: BibleVersionRepository,
    private val syncBibleVersions: SyncBibleVersionsUseCase,
) : ObserveBibleVersionsUseCase {
    override suspend fun invoke() {
        metadataRepository.observeVersions().collect(syncBibleVersions::invoke)
    }
}
