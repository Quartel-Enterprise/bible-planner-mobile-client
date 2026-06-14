package com.quare.bibleplanner.feature.applanguage.presentation.model

import com.quare.bibleplanner.core.utils.locale.Language

internal sealed interface AppLanguageUiEvent {
    data class OnLanguageSelected(
        val language: Language,
    ) : AppLanguageUiEvent

    data object OnDismiss : AppLanguageUiEvent

    data class SyncToggleClicked(
        val isNewValueOn: Boolean,
    ) : AppLanguageUiEvent

    data object SyncToggleBlockedClicked : AppLanguageUiEvent
}
