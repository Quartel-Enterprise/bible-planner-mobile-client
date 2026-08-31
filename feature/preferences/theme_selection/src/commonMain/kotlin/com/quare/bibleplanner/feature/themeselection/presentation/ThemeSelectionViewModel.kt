package com.quare.bibleplanner.feature.themeselection.presentation

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.MaterialYouBottomSheetNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.materialyou.domain.usecase.SetIsDynamicColorsEnabled
import com.quare.bibleplanner.feature.themeselection.domain.usecase.SetContrastType
import com.quare.bibleplanner.feature.themeselection.domain.usecase.SetThemeOption
import com.quare.bibleplanner.feature.themeselection.domain.usecase.SetThemeSyncEnabled
import com.quare.bibleplanner.feature.themeselection.presentation.factory.ThemeSelectionUiStateFactory
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiState
import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class ThemeSelectionViewModel(
    private val setThemeOption: SetThemeOption,
    private val setDynamicColorsEnabledFlow: SetIsDynamicColorsEnabled,
    private val setContrastType: SetContrastType,
    private val setThemeSyncEnabled: SetThemeSyncEnabled,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
    factory: ThemeSelectionUiStateFactory,
) : TrackedViewModel<ThemeSelectionUiEvent>(trackEvent) {
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

    override fun handleEvent(event: ThemeSelectionUiEvent) {
        when (event) {
            ThemeSelectionUiEvent.MaterialYouInfoClicked -> navigator.navigate(MaterialYouBottomSheetNavRoute)

            is ThemeSelectionUiEvent.MaterialYouToggleClicked -> {
                viewModelScope.launch {
                    setDynamicColorsEnabledFlow(event.isNewValueOn)
                }
            }

            ThemeSelectionUiEvent.OnDismiss -> navigator.navigateBack()

            is ThemeSelectionUiEvent.OnThemeSelected -> setTheme(event.theme)

            is ThemeSelectionUiEvent.OnContrastSelected -> setContrast(event.contrastType)

            is ThemeSelectionUiEvent.SyncToggleClicked -> {
                viewModelScope.launch {
                    setThemeSyncEnabled(event.isNewValueOn)
                }
            }

            ThemeSelectionUiEvent.SyncToggleBlockedClicked -> navigator.navigate(
                LoginWarningNavRoute(LoginWarningReason.Preferences.Theme.key),
            )
        }
    }

    private fun setTheme(theme: Theme) {
        viewModelScope.launch {
            setThemeOption(theme)
        }
    }

    private fun setContrast(contrastType: ContrastType) {
        viewModelScope.launch {
            setContrastType(contrastType)
        }
    }
}
