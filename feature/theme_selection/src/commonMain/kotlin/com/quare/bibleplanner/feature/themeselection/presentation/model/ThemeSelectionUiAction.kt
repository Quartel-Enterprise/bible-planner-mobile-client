package com.quare.bibleplanner.feature.themeselection.presentation.model

sealed interface ThemeSelectionUiAction {
    data object NavigateBack : ThemeSelectionUiAction

    data object NavigateToMaterialYou : ThemeSelectionUiAction
}
