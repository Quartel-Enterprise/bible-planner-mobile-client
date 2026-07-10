package com.quare.bibleplanner.feature.applanguage.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.domain.usecase.SetAppLanguage
import com.quare.bibleplanner.feature.applanguage.domain.usecase.SetLanguageSyncEnabled
import com.quare.bibleplanner.feature.applanguage.presentation.factory.AppLanguageUiStateFactory
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiAction
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiEvent
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class AppLanguageViewModel(
    private val setAppLanguage: SetAppLanguage,
    private val setLanguageSyncEnabled: SetLanguageSyncEnabled,
    private val trackEvent: TrackEvent,
    factory: AppLanguageUiStateFactory,
) : ViewModel() {
    private val _uiAction: MutableSharedFlow<AppLanguageUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<AppLanguageUiAction> = _uiAction

    val uiState: StateFlow<AppLanguageUiState> = factory.create().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = factory.createInitial(),
    )

    fun onEvent(event: AppLanguageUiEvent) {
        when (event) {
            is AppLanguageUiEvent.OnLanguageSelected -> selectLanguage(event.language)
            AppLanguageUiEvent.OnDismiss -> emitAction(AppLanguageUiAction.NavigateUp)
            is AppLanguageUiEvent.SyncToggleClicked -> setSyncEnabled(event.isNewValueOn)
            AppLanguageUiEvent.SyncToggleBlockedClicked -> showLoginWarning()
        }
    }

    private fun setSyncEnabled(enabled: Boolean) {
        trackEvent(
            name = AnalyticsEventNames.SETTING_SYNC_TOGGLED,
            params = mapOf(
                AnalyticsParams.SETTING to SYNC_SETTING,
                AnalyticsParams.IS_ENABLED to enabled,
            ),
        )
        viewModelScope.launch {
            setLanguageSyncEnabled(enabled)
        }
    }

    private fun showLoginWarning() {
        emitAction(AppLanguageUiAction.NavigateToLoginWarning)
    }

    private fun selectLanguage(language: Language) {
        trackEvent(
            name = AnalyticsEventNames.LANGUAGE_CHANGED,
            params = mapOf(AnalyticsParams.LANGUAGE to language.toAnalyticsLanguage()),
        )
        viewModelScope.launch {
            setAppLanguage(language)
            emitAction(AppLanguageUiAction.ApplyAndNavigateUp(language))
        }
    }

    private fun Language.toAnalyticsLanguage(): String = when (this) {
        Language.ENGLISH -> "en"
        Language.PORTUGUESE_BRAZIL -> "pt"
        Language.SPANISH -> "es"
    }

    private fun emitAction(action: AppLanguageUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }

    private companion object {
        const val SYNC_SETTING = "language"
    }
}
