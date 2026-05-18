package com.quare.bibleplanner.feature.applanguage.presentation

import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.domain.ApplyLocale
import java.util.Locale

internal class JvmApplyLocale : ApplyLocale {
    override fun invoke(language: Language) {
        Locale.setDefault(Locale.forLanguageTag(language.toLocaleTag()))
    }

    private fun Language.toLocaleTag(): String = when (this) {
        Language.ENGLISH -> "en"
        Language.PORTUGUESE_BRAZIL -> "pt-BR"
        Language.SPANISH -> "es"
    }
}
