package com.quare.bibleplanner.feature.themeselection.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.materialyou.domain.usecase.SetIsDynamicColorsEnabled
import com.quare.bibleplanner.feature.themeselection.domain.usecase.SetContrastType
import com.quare.bibleplanner.feature.themeselection.domain.usecase.SetThemeOption
import com.quare.bibleplanner.feature.themeselection.domain.usecase.SetThemeSyncEnabled
import com.quare.bibleplanner.feature.themeselection.presentation.factory.ThemeSelectionUiStateFactory
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiAction
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiState
import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class ThemeSelectionViewModel(
    private val setThemeOption: SetThemeOption,
    private val setDynamicColorsEnabledFlow: SetIsDynamicColorsEnabled,
    private val setContrastType: SetContrastType,
    private val setThemeSyncEnabled: SetThemeSyncEnabled,
    private val trackEvent: TrackEvent,
    factory: ThemeSelectionUiStateFactory,
) : ViewModel() {
    private val _uiAction: MutableSharedFlow<ThemeSelectionUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<ThemeSelectionUiAction> = _uiAction

    val uiState: StateFlow<ThemeSelectionUiState> = factory.create().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeSelectionUiState(
            isMaterialYouToggleOn = null,
            options = emptyList(),
            selectedContrast = ContrastType.Standard,
            isSyncEnabled = false,
            isLoggedIn = false,
        ),
    )

    fun onEvent(event: ThemeSelectionUiEvent) {
        when (event) {
            ThemeSelectionUiEvent.MaterialYouInfoClicked -> emitUiAction(ThemeSelectionUiAction.NavigateToMaterialYou)

            is ThemeSelectionUiEvent.MaterialYouToggleClicked -> {
                trackEvent(
                    name = AnalyticsEventNames.DYNAMIC_COLORS_TOGGLED,
                    params = mapOf(
                        AnalyticsParams.IS_ENABLED to event.isNewValueOn,
                        AnalyticsParams.SOURCE to DYNAMIC_COLORS_SOURCE,
                    ),
                )
                viewModelScope.launch {
                    setDynamicColorsEnabledFlow(event.isNewValueOn)
                }
            }

            ThemeSelectionUiEvent.OnDismiss -> emitUiAction(ThemeSelectionUiAction.NavigateBack)

            is ThemeSelectionUiEvent.OnThemeSelected -> setTheme(event.theme)

            is ThemeSelectionUiEvent.OnContrastSelected -> setContrast(event.contrastType)

            is ThemeSelectionUiEvent.SyncToggleClicked -> {
                trackEvent(
                    name = AnalyticsEventNames.SETTING_SYNC_TOGGLED,
                    params = mapOf(
                        AnalyticsParams.SETTING to SYNC_SETTING,
                        AnalyticsParams.IS_ENABLED to event.isNewValueOn,
                    ),
                )
                viewModelScope.launch {
                    setThemeSyncEnabled(event.isNewValueOn)
                }
            }

            ThemeSelectionUiEvent.SyncToggleBlockedClicked ->
                emitUiAction(ThemeSelectionUiAction.NavigateToLoginWarning)
        }
    }

    private fun setTheme(theme: Theme) {
        trackEvent(
            name = AnalyticsEventNames.THEME_CHANGED,
            params = mapOf(AnalyticsParams.THEME to theme.name.lowercase()),
        )
        viewModelScope.launch {
            setThemeOption(theme)
        }
    }

    private fun setContrast(contrastType: ContrastType) {
        trackEvent(
            name = AnalyticsEventNames.CONTRAST_CHANGED,
            params = mapOf(AnalyticsParams.CONTRAST to contrastType.name.lowercase()),
        )
        viewModelScope.launch {
            setContrastType(contrastType)
        }
    }

    private fun emitUiAction(uiAction: ThemeSelectionUiAction) {
        viewModelScope.launch {
            _uiAction.emit(uiAction)
        }
    }

    private companion object {
        const val DYNAMIC_COLORS_SOURCE = "theme_selection"
        const val SYNC_SETTING = "theme"
    }
}
