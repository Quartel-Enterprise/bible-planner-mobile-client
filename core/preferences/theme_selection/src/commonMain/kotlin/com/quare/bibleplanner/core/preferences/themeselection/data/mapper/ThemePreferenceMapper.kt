package com.quare.bibleplanner.core.preferences.themeselection.data.mapper

import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme

interface ThemePreferenceMapper {
    fun mapPreferenceToModel(preference: String?): Theme

    fun mapModelToPreference(theme: Theme): String

    fun mapContrastPreferenceToModel(preference: String?): ContrastType

    fun mapModelToContrastPreference(contrastType: ContrastType): String
}
