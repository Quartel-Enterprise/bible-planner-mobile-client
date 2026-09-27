package com.quare.bibleplanner.feature.verse.annotations.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface AnnotationsUiAction {
    data class ShowMessage(
        val stringResource: StringResource,
    ) : AnnotationsUiAction
}
