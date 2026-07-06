package com.quare.bibleplanner.feature.daystudy.domain.mapper

import com.quare.bibleplanner.core.utils.locale.Language

class LanguageCodeMapper {
    fun map(language: Language): String = when (language) {
        Language.ENGLISH -> "en"
        Language.PORTUGUESE_BRAZIL -> "pt-BR"
        Language.SPANISH -> "es"
    }
}
