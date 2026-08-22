package com.quare.bibleplanner.feature.read.presentation.deletecolor

import org.jetbrains.compose.resources.StringResource

sealed interface DeleteHighlightColorUiAction {
    data object NavigateBack : DeleteHighlightColorUiAction

    data class NavigateBackWithMessage(
        val stringResource: StringResource,
    ) : DeleteHighlightColorUiAction
}
