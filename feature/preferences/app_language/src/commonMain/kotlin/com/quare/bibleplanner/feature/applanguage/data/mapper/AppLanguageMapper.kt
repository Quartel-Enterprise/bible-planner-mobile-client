package com.quare.bibleplanner.feature.applanguage.data.mapper

import com.quare.bibleplanner.core.utils.locale.Language

interface AppLanguageMapper {
    fun mapPreferenceToModel(preference: String?): Language

    fun mapModelToPreference(language: Language): String
}
