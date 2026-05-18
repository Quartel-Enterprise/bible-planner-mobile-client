package com.quare.bibleplanner.feature.applanguage.domain.usecase.impl

import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.feature.applanguage.domain.usecase.GetAppLanguageFlow
import kotlinx.coroutines.flow.Flow

internal class GetAppLanguageFlowUseCase(
    private val repository: AppLanguageRepository,
) : GetAppLanguageFlow {
    override fun invoke(): Flow<Language> = repository.getLanguageFlow()
}
