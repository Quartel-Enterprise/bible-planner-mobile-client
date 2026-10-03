package com.quare.bibleplanner.feature.applanguage.domain.usecase.impl

import com.quare.bibleplanner.core.provider.language.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.feature.applanguage.domain.usecase.ObserveLanguageSync
import kotlinx.coroutines.flow.combine

// Why: written through applySyncedLanguage (DataStore-only) so an inbound value is not re-pushed;
// ObserveAppLocaleUseCase then re-applies the OS locale from the language flow.
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
