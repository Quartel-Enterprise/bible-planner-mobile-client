package com.quare.bibleplanner.feature.releasenotes.presentation.model

enum class ReleaseVersionType {
    LATEST,
    UPCOMING,
    PAST,
    ;

    // Why: an upcoming version has no GitHub release page to open yet.
    val isClickable: Boolean
        get() = this != UPCOMING
}
