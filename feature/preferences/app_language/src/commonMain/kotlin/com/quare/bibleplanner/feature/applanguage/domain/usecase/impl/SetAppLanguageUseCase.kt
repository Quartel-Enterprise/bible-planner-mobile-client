package com.quare.bibleplanner.feature.applanguage.domain.usecase.impl

import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.feature.applanguage.domain.usecase.SetAppLanguage

internal class SetAppLanguageUseCase(
    private val repository: AppLanguageRepository,
) : SetAppLanguage {
    override suspend fun invoke(language: Language) {
        repository.setLanguage(language)
    }
}
