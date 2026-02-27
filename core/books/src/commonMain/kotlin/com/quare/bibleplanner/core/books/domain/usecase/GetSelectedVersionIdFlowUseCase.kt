package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import kotlinx.coroutines.flow.Flow

class GetSelectedVersionIdFlowUseCase(
    private val bibleRepository: BibleRepository,
) {
    operator fun invoke(): Flow<String> = bibleRepository.getSelectedVersionIdFlow()
}
