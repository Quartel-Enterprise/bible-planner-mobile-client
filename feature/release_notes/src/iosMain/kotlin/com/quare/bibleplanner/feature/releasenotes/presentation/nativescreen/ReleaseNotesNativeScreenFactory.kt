package com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen

import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiEvent

fun interface ReleaseNotesNativeScreenFactory {
    fun create(onEvent: (ReleaseNotesUiEvent) -> Unit): ReleaseNotesNativeScreen
}
