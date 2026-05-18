package com.quare.bibleplanner.feature.applanguage.presentation

import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.domain.ApplyLocale
import platform.Foundation.NSUserDefaults

internal class IosApplyLocale : ApplyLocale {
    override fun invoke(language: Language) {
        NSUserDefaults.standardUserDefaults.setObject(
            listOf(language.toLocaleTag()),
            forKey = "AppleLanguages",
        )
        NSUserDefaults.standardUserDefaults.synchronize()
    }

    private fun Language.toLocaleTag(): String = when (this) {
        Language.ENGLISH -> "en"
        Language.PORTUGUESE_BRAZIL -> "pt-BR"
        Language.SPANISH -> "es"
    }
}
