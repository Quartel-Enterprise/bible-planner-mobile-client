package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl

import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.GetThemeOptionFlow
import kotlinx.coroutines.flow.Flow

internal class GetThemeOptionFlowUseCase(
    private val repository: ThemeSelectionRepository,
) : GetThemeOptionFlow {
    override operator fun invoke(): Flow<Theme> = repository.getThemeFlow()
}
