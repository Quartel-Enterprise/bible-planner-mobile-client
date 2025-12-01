package com.quare.bibleplanner.feature.materialyou.domain.usecase.impl

import com.quare.bibleplanner.feature.materialyou.domain.repository.MaterialYouRepository
import com.quare.bibleplanner.feature.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow
import kotlinx.coroutines.flow.Flow

internal class GetIsDynamicColorsEnabledFlowUseCase(
    private val repository: MaterialYouRepository,
) : GetIsDynamicColorsEnabledFlow {
    override fun invoke(): Flow<Boolean> = repository.getIsDynamicColorsEnabledFlow()
}
