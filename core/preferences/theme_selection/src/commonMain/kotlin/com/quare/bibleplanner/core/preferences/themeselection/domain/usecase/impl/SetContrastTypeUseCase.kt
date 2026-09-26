package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl

import com.quare.bibleplanner.core.preferences.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.SetContrastType
import com.quare.bibleplanner.core.model.theme.ContrastType

internal class SetContrastTypeUseCase(
    private val repository: ThemeSelectionRepository,
) : SetContrastType {
    override suspend fun invoke(contrastType: ContrastType) {
        repository.setContrastType(contrastType)
    }
}
