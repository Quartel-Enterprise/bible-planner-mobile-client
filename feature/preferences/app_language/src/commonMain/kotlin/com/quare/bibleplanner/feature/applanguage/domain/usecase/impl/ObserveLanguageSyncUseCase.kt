package com.quare.bibleplanner.feature.applanguage.domain.usecase.impl

import com.quare.bibleplanner.core.provider.language.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.feature.applanguage.domain.usecase.ObserveLanguageSync
import kotlinx.coroutines.flow.combine

internal class ObserveLanguageSyncUseCase(
    private val repository: AppLanguageRepository,
) : ObserveLanguageSync {
    override suspend fun invoke() {
        combine(
            repository.getLanguageSyncEnabledFlow(),
            repository.observeSyncedLanguage(),
        ) { enabled, language ->
            enabled to language
        }.collect { (isEnabled, language) ->
            if (isEnabled && language != null) {
                repository.applySyncedLanguage(language)
            }
        }
    }
}
