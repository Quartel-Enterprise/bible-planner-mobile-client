package com.quare.bibleplanner.feature.themeselection.data.mapper

import com.quare.bibleplanner.ui.theme.model.Theme

interface ThemePreferenceMapper {
    fun mapPreferenceToModel(preference: String?): Theme

    fun mapModelToPreference(theme: Theme): String
}
