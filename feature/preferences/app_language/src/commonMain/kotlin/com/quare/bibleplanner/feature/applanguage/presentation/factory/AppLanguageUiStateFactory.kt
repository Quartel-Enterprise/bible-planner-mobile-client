package com.quare.bibleplanner.feature.applanguage.presentation.factory

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class AppLanguageUiStateFactory(
    private val getAppLanguageFlow: GetAppLanguageFlow,
    private val languageProvider: LanguageProvider,
) {
    fun create(): Flow<AppLanguageUiState> = getAppLanguageFlow().map { selectedLanguage ->
        AppLanguageUiState(selectedLanguage = selectedLanguage, languages = Language.entries)
    }

    fun createInitial(): AppLanguageUiState = AppLanguageUiState(
        selectedLanguage = languageProvider.getAppLanguage(),
        languages = Language.entries,
    )
}
