package com.quare.bibleplanner.feature.themeselection.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.ui.graphics.vector.ImageVector
import bibleplanner.feature.preferences.theme_selection.generated.resources.Res
import bibleplanner.feature.preferences.theme_selection.generated.resources.dark_title
import bibleplanner.feature.preferences.theme_selection.generated.resources.light_title
import bibleplanner.feature.preferences.theme_selection.generated.resources.system_title
import com.quare.bibleplanner.core.model.theme.Theme
import org.jetbrains.compose.resources.StringResource

internal fun Theme.toTitle(): StringResource = when (this) {
    Theme.LIGHT -> Res.string.light_title
    Theme.DARK -> Res.string.dark_title
    Theme.SYSTEM -> Res.string.system_title
}

internal fun Theme.toIcon(): ImageVector = when (this) {
    Theme.LIGHT -> Icons.Filled.LightMode
    Theme.DARK -> Icons.Filled.DarkMode
    Theme.SYSTEM -> Icons.Filled.Devices
}
