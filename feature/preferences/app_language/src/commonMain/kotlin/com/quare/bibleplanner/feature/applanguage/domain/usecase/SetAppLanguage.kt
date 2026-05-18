package com.quare.bibleplanner.feature.applanguage.domain.usecase

import com.quare.bibleplanner.core.utils.locale.Language

interface SetAppLanguage {
    suspend operator fun invoke(language: Language)
}
