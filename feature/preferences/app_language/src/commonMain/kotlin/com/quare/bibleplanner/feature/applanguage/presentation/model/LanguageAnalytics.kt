package com.quare.bibleplanner.feature.applanguage.presentation.model

import com.quare.bibleplanner.core.utils.locale.Language

internal fun Language.toAnalyticsLanguage(): String = when (this) {
    Language.ENGLISH -> "en"
    Language.PORTUGUESE_BRAZIL -> "pt"
    Language.SPANISH -> "es"
}
