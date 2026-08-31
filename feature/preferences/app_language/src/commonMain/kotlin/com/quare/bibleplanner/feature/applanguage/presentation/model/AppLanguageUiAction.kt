package com.quare.bibleplanner.feature.applanguage.presentation.model

import com.quare.bibleplanner.core.utils.locale.Language

internal sealed interface AppLanguageUiAction {
    data class ApplyLanguage(
        val language: Language,
    ) : AppLanguageUiAction
}
