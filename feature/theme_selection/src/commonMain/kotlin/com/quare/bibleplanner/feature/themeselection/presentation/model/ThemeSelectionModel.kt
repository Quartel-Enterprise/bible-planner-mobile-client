package com.quare.bibleplanner.feature.themeselection.presentation.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.quare.bibleplanner.ui.theme.model.Theme
import org.jetbrains.compose.resources.StringResource

data class ThemeSelectionModel(
    val title: StringResource,
    val subtitle: StringResource,
    val icon: ImageVector,
    val preference: Theme,
    val isActive: Boolean,
)
