package com.quare.bibleplanner.feature.profile.domain.usecase

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.utils.locale.Language

internal class GetInstagramUrlUseCase(
    private val languageProvider: LanguageProvider,
) {
    operator fun invoke(): String = when (languageProvider.getAppLanguage()) {
        Language.PORTUGUESE_BRAZIL -> INSTAGRAM_PT_BR
        Language.SPANISH -> INSTAGRAM_ES
        Language.ENGLISH -> INSTAGRAM_DEFAULT
    }

    companion object {
        private const val INSTAGRAM_DEFAULT = "https://www.instagram.com/bible.planner"
        private const val INSTAGRAM_PT_BR = "$INSTAGRAM_DEFAULT.brasil"
        private const val INSTAGRAM_ES = "$INSTAGRAM_DEFAULT.espanol"
    }
}
