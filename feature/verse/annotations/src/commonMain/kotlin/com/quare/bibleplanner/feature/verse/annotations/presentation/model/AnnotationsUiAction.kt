package com.quare.bibleplanner.feature.verse.annotations.presentation.model

sealed interface AnnotationsUiAction {
    data object NotifyAnnotationRemoved : AnnotationsUiAction
}
