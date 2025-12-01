package com.quare.bibleplanner.feature.themeselection.presentation.model

import com.quare.bibleplanner.ui.theme.model.Theme

sealed interface ThemeSelectionUiEvent {
    data class OnThemeSelected(
        val theme: Theme,
    ) : ThemeSelectionUiEvent

    data object OnBackClicked : ThemeSelectionUiEvent

    data object MaterialYouInfoClicked : ThemeSelectionUiEvent

    data class MaterialYouToggleClicked(
        val isNewValueOn: Boolean,
    ) : ThemeSelectionUiEvent
}
