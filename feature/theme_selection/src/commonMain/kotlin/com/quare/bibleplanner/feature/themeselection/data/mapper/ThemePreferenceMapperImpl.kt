package com.quare.bibleplanner.feature.themeselection.data.mapper

import com.quare.bibleplanner.ui.theme.model.ContrastType
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

    override fun mapContrastPreferenceToModel(preference: String?): ContrastType = when (preference) {
        MEDIUM_CONTRAST -> ContrastType.Medium
        HIGH_CONTRAST -> ContrastType.High
        else -> ContrastType.Standard
    }

    override fun mapModelToContrastPreference(contrastType: ContrastType): String = when (contrastType) {
        ContrastType.Standard -> STANDARD_CONTRAST
        ContrastType.Medium -> MEDIUM_CONTRAST
        ContrastType.High -> HIGH_CONTRAST
    }

    companion object {
        private const val DARK_THEME = "dark_theme"
        private const val LIGHT_THEME = "light_theme"
        private const val SYSTEM_THEME = "system_theme"
        private const val STANDARD_CONTRAST = "standard_contrast"
        private const val MEDIUM_CONTRAST = "medium_contrast"
        private const val HIGH_CONTRAST = "high_contrast"
    }
}
