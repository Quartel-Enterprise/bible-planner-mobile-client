package com.quare.bibleplanner.core.provider.language.data.mapper

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.utils.locale.Language

internal class AppLanguageMapper(
    private val languageProvider: LanguageProvider,
) {
    fun mapPreferenceToModel(preference: String?): Language = when (preference) {
        ENGLISH -> Language.ENGLISH
        PORTUGUESE_BRAZIL -> Language.PORTUGUESE_BRAZIL
        SPANISH -> Language.SPANISH
        else -> languageProvider.getAppLanguage()
    }

    fun mapModelToPreference(language: Language): String = when (language) {
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
