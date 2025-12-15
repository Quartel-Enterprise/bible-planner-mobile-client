package com.quare.bibleplanner.feature.themeselection.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.isAndroid
import com.quare.bibleplanner.feature.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow
import com.quare.bibleplanner.feature.materialyou.domain.usecase.SetIsDynamicColorsEnabled
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.feature.themeselection.domain.usecase.SetThemeOption
import com.quare.bibleplanner.feature.themeselection.presentation.factory.ThemeOptionsFactory
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiAction
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiState
import com.quare.bibleplanner.ui.theme.model.Theme
import com.quare.bibleplanner.ui.utils.observe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ThemeSelectionViewModel(
    private val setThemeOption: SetThemeOption,
    private val setDynamicColorsEnabledFlow: SetIsDynamicColorsEnabled,
    getThemeOptionFlow: GetThemeOptionFlow,
    getIsDynamicColorsEnabledFlow: GetIsDynamicColorsEnabledFlow,
    platform: Platform,
) : ViewModel() {
    val themeOptions = ThemeOptionsFactory.themeOptions

    private val _uiAction: MutableSharedFlow<ThemeSelectionUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<ThemeSelectionUiAction> = _uiAction

    private val _uiState: MutableStateFlow<ThemeSelectionUiState> = MutableStateFlow(
        ThemeSelectionUiState(
            isMaterialYouToggleOn = null,
            options = themeOptions,
        ),
    )
    val uiState: StateFlow<ThemeSelectionUiState> = _uiState.asStateFlow()

    init {
        observe(getThemeOptionFlow()) { newTheme ->
            _uiState.update { currentState ->
                currentState.copy(
                    options = currentState.options.map { themeOption ->
                        themeOption.copy(isActive = themeOption.preference == newTheme)
                    },
                )
            }
        }
        observe(getIsDynamicColorsEnabledFlow()) { isDynamicColorsEnabled ->
            _uiState.update { currentState ->
                currentState.copy(
                    isMaterialYouToggleOn = isDynamicColorsEnabled.takeIf { platform.isAndroid() },
                )
            }
        }
    }

    fun onEvent(event: ThemeSelectionUiEvent) {
        when (event) {
            ThemeSelectionUiEvent.MaterialYouInfoClicked -> {
                emitUiAction(ThemeSelectionUiAction.NavigateToMaterialYou)
            }

            is ThemeSelectionUiEvent.MaterialYouToggleClicked -> {
                viewModelScope.launch {
                    setDynamicColorsEnabledFlow(event.isNewValueOn)
                }
            }

            ThemeSelectionUiEvent.OnBackClicked -> {
                emitUiAction(ThemeSelectionUiAction.NavigateBack)
            }

            is ThemeSelectionUiEvent.OnThemeSelected -> {
                setTheme(event.theme)
            }
        }
    }

    private fun setTheme(theme: Theme) {
        viewModelScope.launch {
            setThemeOption(theme)
        }
    }

    private fun emitUiAction(uiAction: ThemeSelectionUiAction) {
        viewModelScope.launch {
            _uiAction.emit(uiAction)
        }
    }
}
