package com.quare.bibleplanner.feature.themeselection.presentation.factory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LightMode
import bibleplanner.feature.theme_selection.generated.resources.Res
import bibleplanner.feature.theme_selection.generated.resources.dark_description
import bibleplanner.feature.theme_selection.generated.resources.dark_title
import bibleplanner.feature.theme_selection.generated.resources.light_description
import bibleplanner.feature.theme_selection.generated.resources.light_title
import bibleplanner.feature.theme_selection.generated.resources.system_description
import bibleplanner.feature.theme_selection.generated.resources.system_title
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionModel
import com.quare.bibleplanner.ui.theme.model.Theme

object ThemeOptionsFactory {
    val themeOptions: List<ThemeSelectionModel> = listOf(
        ThemeSelectionModel(
            title = Res.string.light_title,
            subtitle = Res.string.light_description,
            icon = Icons.Filled.LightMode,
            preference = Theme.LIGHT,
            isActive = false,
        ),
        ThemeSelectionModel(
            title = Res.string.dark_title,
            subtitle = Res.string.dark_description,
            icon = Icons.Filled.DarkMode,
            preference = Theme.DARK,
            isActive = false,
        ),
        ThemeSelectionModel(
            title = Res.string.system_title,
            subtitle = Res.string.system_description,
            icon = Icons.Filled.Devices,
            preference = Theme.SYSTEM,
            isActive = true,
        ),
    )
}
