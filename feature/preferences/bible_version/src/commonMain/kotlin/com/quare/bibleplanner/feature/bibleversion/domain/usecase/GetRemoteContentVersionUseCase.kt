package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository

class GetRemoteContentVersionUseCase(
    private val bibleVersionRepository: BibleVersionRepository,
) {
    suspend operator fun invoke(versionId: String): String = bibleVersionRepository
        .getVersions(forceRefresh = true)
        .getOrNull()
        ?.find { it.id.equals(versionId, ignoreCase = true) }
        ?.version
        .orEmpty()
}
