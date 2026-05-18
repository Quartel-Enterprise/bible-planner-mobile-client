package com.quare.bibleplanner.feature.themeselection.domain.usecase.impl

import com.quare.bibleplanner.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.feature.themeselection.domain.usecase.SetContrastType
import com.quare.bibleplanner.ui.theme.model.ContrastType

internal class SetContrastTypeUseCase(
    private val repository: ThemeSelectionRepository,
) : SetContrastType {
    override suspend fun invoke(contrastType: ContrastType) {
        repository.setContrastType(contrastType)
    }
}
