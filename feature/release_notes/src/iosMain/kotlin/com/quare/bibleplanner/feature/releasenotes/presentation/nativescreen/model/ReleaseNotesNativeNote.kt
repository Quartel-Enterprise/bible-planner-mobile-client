package com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen.model

data class ReleaseNotesNativeNote(
    val version: String,
    val dateText: String?,
    val changes: List<String>,
    val tagText: String?,
    val isUpcoming: Boolean,
)
