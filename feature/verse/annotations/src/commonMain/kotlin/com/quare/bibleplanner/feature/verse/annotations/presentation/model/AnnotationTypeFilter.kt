package com.quare.bibleplanner.feature.verse.annotations.presentation.model

enum class AnnotationTypeFilter(
    val analyticsValue: String,
) {
    ALL("all"),
    HIGHLIGHTS("highlights"),
    SAVED("saved"),
    NOTES("notes"),
}
