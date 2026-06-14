package com.quare.bibleplanner.feature.applanguage.domain.usecase.impl

import com.quare.bibleplanner.core.provider.language.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.feature.applanguage.domain.usecase.GetLanguageSyncEnabledFlow
import kotlinx.coroutines.flow.Flow

internal class GetLanguageSyncEnabledFlowUseCase(
    private val repository: AppLanguageRepository,
) : GetLanguageSyncEnabledFlow {
    override fun invoke(): Flow<Boolean> = repository.getLanguageSyncEnabledFlow()
}
