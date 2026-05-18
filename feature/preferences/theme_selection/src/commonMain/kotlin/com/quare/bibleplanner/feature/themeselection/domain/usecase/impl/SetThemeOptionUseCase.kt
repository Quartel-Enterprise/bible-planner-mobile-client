package com.quare.bibleplanner.feature.themeselection.domain.usecase.impl

import com.quare.bibleplanner.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.feature.themeselection.domain.usecase.SetThemeOption
import com.quare.bibleplanner.ui.theme.model.Theme

internal class SetThemeOptionUseCase(
    private val repository: ThemeSelectionRepository,
) : SetThemeOption {
    override suspend operator fun invoke(theme: Theme) {
        repository.setTheme(theme)
    }
}
