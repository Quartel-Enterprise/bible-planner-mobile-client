package com.quare.bibleplanner.feature.materialyou.domain.usecase.impl

import com.quare.bibleplanner.feature.materialyou.domain.repository.MaterialYouRepository
import com.quare.bibleplanner.feature.materialyou.domain.usecase.SetIsDynamicColorsEnabled

internal class SetIsDynamicColorsEnabledUseCase(
    private val repository: MaterialYouRepository,
) : SetIsDynamicColorsEnabled {
    override suspend fun invoke(isEnabled: Boolean) {
        repository.setIsDynamicColorsEnabled(isEnabled)
    }
}
