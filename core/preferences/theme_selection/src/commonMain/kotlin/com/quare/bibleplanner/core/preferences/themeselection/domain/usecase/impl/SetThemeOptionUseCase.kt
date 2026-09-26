package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl

import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.SetThemeOption

internal class SetThemeOptionUseCase(
    private val repository: ThemeSelectionRepository,
) : SetThemeOption {
    override suspend operator fun invoke(theme: Theme) {
        repository.setTheme(theme)
    }
}
