package com.quare.bibleplanner.feature.applanguage.domain.usecase.impl

import com.quare.bibleplanner.core.provider.language.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.feature.applanguage.domain.usecase.SetLanguageSyncEnabled

internal class SetLanguageSyncEnabledUseCase(
    private val repository: AppLanguageRepository,
) : SetLanguageSyncEnabled {
    override suspend fun invoke(enabled: Boolean) {
        repository.setLanguageSyncEnabled(enabled)
    }
}
