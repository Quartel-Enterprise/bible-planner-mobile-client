package com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen.model

sealed interface ReleaseNotesNativeContent {
    data object Loading : ReleaseNotesNativeContent

    data class Error(
        val message: String,
    ) : ReleaseNotesNativeContent

    data class Success(
        val tabs: List<ReleaseNotesNativeTab>,
        val notes: List<ReleaseNotesNativeNote>,
    ) : ReleaseNotesNativeContent
}
