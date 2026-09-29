package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BibleRepository

class SetSelectedVersionUseCase(
    private val bibleRepository: BibleRepository,
) : SetSelectedVersion {
    override suspend fun invoke(versionId: String) {
        bibleRepository.setSelectedVersionId(versionId)
    }
}
