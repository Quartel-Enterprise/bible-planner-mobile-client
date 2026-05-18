package com.quare.bibleplanner.feature.applanguage.presentation.factory

import com.quare.bibleplanner.feature.applanguage.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class AppLanguageUiStateFactory(
    private val getAppLanguageFlow: GetAppLanguageFlow,
) {
    fun create(): Flow<AppLanguageUiState> = getAppLanguageFlow().map { AppLanguageUiState(it) }
}
