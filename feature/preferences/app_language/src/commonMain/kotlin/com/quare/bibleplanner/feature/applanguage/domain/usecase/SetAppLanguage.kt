package com.quare.bibleplanner.feature.applanguage.domain.usecase

import com.quare.bibleplanner.core.utils.locale.Language

fun interface SetAppLanguage {
    suspend operator fun invoke(language: Language)
}
