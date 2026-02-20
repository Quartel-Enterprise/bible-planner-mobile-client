package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.BibleVersionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import kotlinx.coroutines.flow.Flow

class GetBibleVersionsUseCase(
    private val repository: BibleVersionRepository,
) {
    operator fun invoke(): Flow<List<BibleVersionModel>> = repository.getBibleVersionsFlow()
}
