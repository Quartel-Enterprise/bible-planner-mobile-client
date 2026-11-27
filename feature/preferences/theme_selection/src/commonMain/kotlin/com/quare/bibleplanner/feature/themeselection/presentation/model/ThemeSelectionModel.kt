package com.quare.bibleplanner.feature.themeselection.presentation.model

import com.quare.bibleplanner.core.model.theme.Theme

// Why: each UI (Compose, SwiftUI) picks the title and icon of the preference itself.
data class ThemeSelectionModel(
    val preference: Theme,
    val isActive: Boolean,
)
