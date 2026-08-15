package com.quare.bibleplanner.feature.read.presentation.appearance

sealed interface ReaderAppearanceUiAction {
    data object NavigateBack : ReaderAppearanceUiAction
}
