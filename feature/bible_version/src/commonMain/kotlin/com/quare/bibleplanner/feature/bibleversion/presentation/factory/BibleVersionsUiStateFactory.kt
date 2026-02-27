package com.quare.bibleplanner.feature.bibleversion.presentation.factory

import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetBibleVersionsByLanguageUseCase
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionsUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BibleVersionsUiStateFactory(
    private val getBibleVersions: GetBibleVersionsByLanguageUseCase,
) {
    fun create(): Flow<BibleVersionsUiState> = getBibleVersions()
        .map { data ->
            if (data.isEmpty()) {
                BibleVersionsUiState.Error
            } else {
                BibleVersionsUiState.Success(data)
            }
        }
}
