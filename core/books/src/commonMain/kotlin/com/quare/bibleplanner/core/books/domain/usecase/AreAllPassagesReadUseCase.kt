package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.plan.PassageModel

class AreAllPassagesReadUseCase(
    private val isPassageRead: IsPassageReadUseCase,
) {
    suspend operator fun invoke(passages: List<PassageModel>): Boolean = passages.all { passage ->
        isPassageRead(passage)
    }
}
