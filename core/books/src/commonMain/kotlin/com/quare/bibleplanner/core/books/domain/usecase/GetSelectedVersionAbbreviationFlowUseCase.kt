package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import kotlinx.coroutines.flow.Flow

class GetSelectedVersionAbbreviationFlowUseCase(
    private val bibleVersionRepository: BibleVersionRepository,
) {
    operator fun invoke(): Flow<String> = bibleVersionRepository.getSelectedVersionAbbreviationFlow()
}
