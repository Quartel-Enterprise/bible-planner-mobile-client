package com.quare.bibleplanner.feature.themeselection.data.mapper

import com.quare.bibleplanner.ui.theme.model.Theme

internal class ThemePreferenceMapperImpl : ThemePreferenceMapper {
    override fun mapPreferenceToModel(preference: String?): Theme = when (preference) {
        LIGHT_THEME -> Theme.LIGHT
        DARK_THEME -> Theme.DARK
        else -> Theme.SYSTEM
    }

    override fun mapModelToPreference(theme: Theme): String = when (theme) {
        Theme.LIGHT -> LIGHT_THEME
        Theme.DARK -> DARK_THEME
        Theme.SYSTEM -> SYSTEM_THEME
    }

    companion object {
        private const val DARK_THEME = "dark_theme"
        private const val LIGHT_THEME = "light_theme"
        private const val SYSTEM_THEME = "system_theme"
    }
}
