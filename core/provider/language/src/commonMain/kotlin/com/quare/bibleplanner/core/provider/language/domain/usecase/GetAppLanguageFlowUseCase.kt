package com.quare.bibleplanner.core.provider.language.domain.usecase

import com.quare.bibleplanner.core.provider.language.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow

internal class GetAppLanguageFlowUseCase(
    private val repository: AppLanguageRepository,
) : GetAppLanguageFlow {
    override fun invoke(): Flow<Language> = repository.getLanguageFlow()
}
