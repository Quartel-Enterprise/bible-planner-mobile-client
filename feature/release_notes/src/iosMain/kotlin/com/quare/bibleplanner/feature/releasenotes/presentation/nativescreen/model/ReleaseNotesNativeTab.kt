package com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen.model

import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesTab

data class ReleaseNotesNativeTab(
    val tab: ReleaseNotesTab,
    val title: String,
    val isSelected: Boolean,
)
