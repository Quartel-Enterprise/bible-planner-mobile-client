package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BibleRepository

class SetSelectedVersionUseCase(
    private val repository: BibleRepository,
) {
    suspend operator fun invoke(selectedId: String) {
        repository.setSelectedVersionId(selectedId)
    }
}
