package com.quare.bibleplanner.feature.read.presentation.deletecolor

import org.jetbrains.compose.resources.StringResource

sealed interface DeleteHighlightColorUiAction {
    data class NotifyDeletion(
        val stringResource: StringResource,
    ) : DeleteHighlightColorUiAction
}
