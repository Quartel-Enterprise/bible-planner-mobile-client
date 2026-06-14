package com.quare.bibleplanner.feature.themeselection.domain.usecase.impl

import com.quare.bibleplanner.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetThemeSyncEnabledFlow
import kotlinx.coroutines.flow.Flow

internal class GetThemeSyncEnabledFlowUseCase(
    private val repository: ThemeSelectionRepository,
) : GetThemeSyncEnabledFlow {
    override fun invoke(): Flow<Boolean> = repository.getThemeSyncEnabledFlow()
}
