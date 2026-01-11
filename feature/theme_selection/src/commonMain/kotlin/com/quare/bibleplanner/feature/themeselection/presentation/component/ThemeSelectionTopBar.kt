package com.quare.bibleplanner.feature.themeselection.presentation.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import bibleplanner.feature.theme_selection.generated.resources.Res
import bibleplanner.feature.theme_selection.generated.resources.select_theme
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.quare.bibleplanner.ui.component.icon.BackIcon
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionTopBar(
    onEvent: (ThemeSelectionUiEvent) -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            BackIcon(
                onBackClick = {
                    onEvent(ThemeSelectionUiEvent.OnDismiss)
                },
            )
        },
        title = {
            Text(text = stringResource(Res.string.select_theme))
        },
    )
}
