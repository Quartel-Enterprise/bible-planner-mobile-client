package com.quare.bibleplanner.feature.themeselection.presentation.factory

import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionModel

object ThemeOptionsFactory {
    val themeOptions: List<ThemeSelectionModel> = listOf(
        ThemeSelectionModel(
            preference = Theme.LIGHT,
            isActive = false,
        ),
        ThemeSelectionModel(
            preference = Theme.DARK,
            isActive = false,
        ),
        ThemeSelectionModel(
            preference = Theme.SYSTEM,
            isActive = true,
        ),
    )
}
