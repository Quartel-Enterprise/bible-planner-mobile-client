package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl

import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.preferences.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.GetContrastTypeFlow
import kotlinx.coroutines.flow.Flow

internal class GetContrastTypeFlowUseCase(
    private val repository: ThemeSelectionRepository,
) : GetContrastTypeFlow {
    override fun invoke(): Flow<ContrastType> = repository.getContrastTypeFlow()
}
