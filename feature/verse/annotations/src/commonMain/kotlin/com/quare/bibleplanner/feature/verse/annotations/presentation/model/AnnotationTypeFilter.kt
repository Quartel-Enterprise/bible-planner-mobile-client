package com.quare.bibleplanner.feature.verse.annotations.presentation.model

internal enum class AnnotationTypeFilter(
    val analyticsValue: String,
) {
    ALL("all"),
    HIGHLIGHTS("highlights"),
    SAVED("saved"),
    NOTES("notes"),
}
