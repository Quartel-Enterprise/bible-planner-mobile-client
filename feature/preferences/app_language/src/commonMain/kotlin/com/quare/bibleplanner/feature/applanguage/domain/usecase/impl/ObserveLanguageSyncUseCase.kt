package com.quare.bibleplanner.feature.applanguage.domain.usecase.impl

import com.quare.bibleplanner.core.provider.language.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.feature.applanguage.domain.usecase.ObserveLanguageSync
import kotlinx.coroutines.flow.combine

/**
 * App-scoped collector that applies a synced language into the device-local store while the sync flag
 * is on. The existing [ObserveAppLocaleUseCase] then re-applies the OS locale from the language flow,
 * so an inbound change takes effect with no extra wiring. Writing through `applySyncedLanguage`
 * (DataStore-only) avoids re-pushing the value.
 */
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
