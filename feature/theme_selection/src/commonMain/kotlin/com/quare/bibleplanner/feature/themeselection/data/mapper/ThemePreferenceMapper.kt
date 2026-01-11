package com.quare.bibleplanner.feature.themeselection.data.mapper

import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme

interface ThemePreferenceMapper {
    fun mapPreferenceToModel(preference: String?): Theme

    fun mapModelToPreference(theme: Theme): String

    fun mapContrastPreferenceToModel(preference: String?): ContrastType

    fun mapModelToContrastPreference(contrastType: ContrastType): String
}
