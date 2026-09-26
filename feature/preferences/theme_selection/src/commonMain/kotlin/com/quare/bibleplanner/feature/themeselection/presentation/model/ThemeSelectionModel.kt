package com.quare.bibleplanner.feature.themeselection.presentation.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.quare.bibleplanner.core.model.theme.Theme
import org.jetbrains.compose.resources.StringResource

data class ThemeSelectionModel(
    val title: StringResource,
    val icon: ImageVector,
    val preference: Theme,
    val isActive: Boolean,
)
