package com.quare.bibleplanner.feature.materialyou.presentation.model

sealed interface AndroidColorSchemeUiEvent {
    data class OnIsDynamicColorsEnabledChange(
        val isEnabled: Boolean,
    ) : AndroidColorSchemeUiEvent

    data object OnInformationDialogDismiss : AndroidColorSchemeUiEvent

    data object BottomSheetGotItClick : AndroidColorSchemeUiEvent
}
