package com.quare.bibleplanner.core.books.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSelectedBibleNameFlowUseCase(
    private val getSelectedBibleFlow: GetSelectedBibleFlowUseCase,
) {
    operator fun invoke(): Flow<String> = getSelectedBibleFlow().map { it?.version?.name.orEmpty() }
}
