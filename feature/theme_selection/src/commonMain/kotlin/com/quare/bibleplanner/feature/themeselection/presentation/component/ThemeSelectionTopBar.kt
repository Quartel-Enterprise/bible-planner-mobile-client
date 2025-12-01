package com.quare.bibleplanner.feature.themeselection.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import bibleplanner.feature.theme_selection.generated.resources.Res
import bibleplanner.feature.theme_selection.generated.resources.dynamic_colors_title
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionTopBar(
    isMaterialYouChecked: Boolean?,
    onEvent: (ThemeSelectionUiEvent) -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            BackIcon(
                onBackClick = {
                    onEvent(ThemeSelectionUiEvent.OnBackClicked)
                },
            )
        },
        title = {
            isMaterialYouChecked?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(Res.string.dynamic_colors_title))
                    CommonIconButton(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Information",
                        onClick = { onEvent(ThemeSelectionUiEvent.MaterialYouInfoClicked) },
                    )
                }
            }
        },
        actions = {
            isMaterialYouChecked?.let {
                Switch(
                    checked = it,
                    onCheckedChange = { newValue ->
                        onEvent(ThemeSelectionUiEvent.MaterialYouToggleClicked(newValue))
                    },
                )
            }
        },
    )
}
