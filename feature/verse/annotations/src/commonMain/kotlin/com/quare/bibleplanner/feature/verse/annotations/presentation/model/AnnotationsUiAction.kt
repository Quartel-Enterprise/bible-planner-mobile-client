package com.quare.bibleplanner.feature.verse.annotations.presentation.model

internal sealed interface AnnotationsUiAction {
    data object NotifyAnnotationRemoved : AnnotationsUiAction
}
