package com.quare.bibleplanner.feature.materialyou.presentation.model

sealed interface AndroidColorSchemeUiAction {
    data object CloseBottomSheet : AndroidColorSchemeUiAction
}
