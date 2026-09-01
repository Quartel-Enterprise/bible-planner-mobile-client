package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository

internal class InitializeBibleVersionsUseCaseImpl(
    private val metadataRepository: BibleVersionRepository,
    private val syncBibleVersions: SyncBibleVersionsUseCase,
) : InitializeBibleVersionsUseCase {
    override suspend fun invoke() {
        metadataRepository
            .getVersions(forceRefresh = false)
            .getOrNull()
            ?.let { versions -> syncBibleVersions(versions) }
    }
}
