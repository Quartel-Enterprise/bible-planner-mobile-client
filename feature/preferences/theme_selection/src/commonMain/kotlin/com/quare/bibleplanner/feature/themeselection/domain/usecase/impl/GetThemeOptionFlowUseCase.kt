package com.quare.bibleplanner.feature.themeselection.domain.usecase.impl

import com.quare.bibleplanner.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.flow.Flow

internal class GetThemeOptionFlowUseCase(
    private val repository: ThemeSelectionRepository,
) : GetThemeOptionFlow {
    override operator fun invoke(): Flow<Theme> = repository.getThemeFlow()
}
