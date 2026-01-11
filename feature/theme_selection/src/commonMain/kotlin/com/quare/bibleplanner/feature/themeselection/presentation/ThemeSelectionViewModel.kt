package com.quare.bibleplanner.feature.themeselection.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.feature.materialyou.domain.usecase.SetIsDynamicColorsEnabled
import com.quare.bibleplanner.feature.themeselection.domain.usecase.SetContrastType
import com.quare.bibleplanner.feature.themeselection.domain.usecase.SetThemeOption
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
        ),
    )

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

            ThemeSelectionUiEvent.OnDismiss -> {
                emitUiAction(ThemeSelectionUiAction.NavigateBack)
            }

            is ThemeSelectionUiEvent.OnThemeSelected -> {
                setTheme(event.theme)
            }

            is ThemeSelectionUiEvent.OnContrastSelected -> {
                setContrast(event.contrastType)
            }
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

    private fun emitUiAction(uiAction: ThemeSelectionUiAction) {
        viewModelScope.launch {
            _uiAction.emit(uiAction)
        }
    }
}
