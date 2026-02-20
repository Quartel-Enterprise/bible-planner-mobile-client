package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSelectedBibleUseCase(
    private val bibleRepository: BibleRepository,
) {
    operator fun invoke(): Flow<BibleModel?> = bibleRepository.getBiblesFlow().map { selectableVersions ->
        selectableVersions.find { it.isSelected }
    }
}
