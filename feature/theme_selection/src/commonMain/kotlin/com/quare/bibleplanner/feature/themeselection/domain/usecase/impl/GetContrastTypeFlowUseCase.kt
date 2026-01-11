package com.quare.bibleplanner.feature.themeselection.domain.usecase.impl

import com.quare.bibleplanner.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetContrastTypeFlow
import com.quare.bibleplanner.ui.theme.model.ContrastType
import kotlinx.coroutines.flow.Flow

internal class GetContrastTypeFlowUseCase(
    private val repository: ThemeSelectionRepository,
) : GetContrastTypeFlow {
    override fun invoke(): Flow<ContrastType> = repository.getContrastTypeFlow()
}
