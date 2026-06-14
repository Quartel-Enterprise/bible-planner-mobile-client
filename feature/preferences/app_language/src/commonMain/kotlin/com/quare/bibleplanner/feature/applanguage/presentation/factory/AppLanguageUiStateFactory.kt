package com.quare.bibleplanner.feature.applanguage.presentation.factory

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.domain.usecase.GetLanguageSyncEnabledFlow
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal class AppLanguageUiStateFactory(
    private val getAppLanguageFlow: GetAppLanguageFlow,
    private val getLanguageSyncEnabledFlow: GetLanguageSyncEnabledFlow,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val languageProvider: LanguageProvider,
) {
    fun create(): Flow<AppLanguageUiState> = combine(
        getAppLanguageFlow(),
        getLanguageSyncEnabledFlow(),
        observeAuthenticatedUserId(),
    ) { selectedLanguage, isSyncEnabled, userId ->
        AppLanguageUiState(
            selectedLanguage = selectedLanguage,
            languages = Language.entries,
            isSyncEnabled = isSyncEnabled,
            isLoggedIn = userId != null,
        )
    }

    fun createInitial(): AppLanguageUiState = AppLanguageUiState(
        selectedLanguage = languageProvider.getAppLanguage(),
        languages = Language.entries,
        isSyncEnabled = false,
        isLoggedIn = false,
    )
}
