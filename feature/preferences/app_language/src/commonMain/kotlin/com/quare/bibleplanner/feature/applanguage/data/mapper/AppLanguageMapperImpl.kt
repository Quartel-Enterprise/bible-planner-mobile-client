package com.quare.bibleplanner.feature.applanguage.data.mapper

import com.quare.bibleplanner.core.utils.locale.Language

internal class AppLanguageMapperImpl : AppLanguageMapper {
    override fun mapPreferenceToModel(preference: String?): Language = when (preference) {
        PORTUGUESE_BRAZIL -> Language.PORTUGUESE_BRAZIL
        SPANISH -> Language.SPANISH
        else -> Language.ENGLISH
    }

    override fun mapModelToPreference(language: Language): String = when (language) {
        Language.ENGLISH -> ENGLISH
        Language.PORTUGUESE_BRAZIL -> PORTUGUESE_BRAZIL
        Language.SPANISH -> SPANISH
    }

    companion object {
        private const val ENGLISH = "en"
        private const val PORTUGUESE_BRAZIL = "pt-BR"
        private const val SPANISH = "es"
    }
}
