package com.quare.bibleplanner.feature.bibleversion.presentation.model

internal sealed interface PendingBibleUpdatesUiAction {
    data object NavigateBack : PendingBibleUpdatesUiAction
}
