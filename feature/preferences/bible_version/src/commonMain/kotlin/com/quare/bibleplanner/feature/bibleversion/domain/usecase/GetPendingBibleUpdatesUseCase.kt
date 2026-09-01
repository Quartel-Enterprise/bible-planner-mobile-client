package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import kotlinx.coroutines.flow.first

class GetPendingBibleUpdatesUseCase(
    private val bibleRepository: BibleRepository,
) {
    suspend operator fun invoke(): List<BibleModel> = bibleRepository
        .getBiblesFlow()
        .first()
        .filter { it.hasPendingUpdate }
}
