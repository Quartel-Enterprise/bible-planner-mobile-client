package com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen.model

data class ReleaseNotesNativeState(
    val title: String,
    val backButtonLabel: String,
    val githubButtonLabel: String,
    val content: ReleaseNotesNativeContent,
    val colors: ReleaseNotesNativeColors,
)
