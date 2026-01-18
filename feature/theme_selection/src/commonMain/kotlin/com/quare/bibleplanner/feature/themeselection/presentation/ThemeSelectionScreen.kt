package com.quare.bibleplanner.feature.themeselection.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.theme_selection.generated.resources.Res
import bibleplanner.feature.theme_selection.generated.resources.dynamic_colors_title
import bibleplanner.feature.theme_selection.generated.resources.information
import bibleplanner.feature.theme_selection.generated.resources.select_contrast
import bibleplanner.feature.theme_selection.generated.resources.select_theme
import com.quare.bibleplanner.feature.themeselection.presentation.component.ContrastSelector
import com.quare.bibleplanner.feature.themeselection.presentation.component.ThemeOptionCard
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionModel
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiState
import com.quare.bibleplanner.ui.component.ResponsiveColumn
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionContent(
    uiState: ThemeSelectionUiState,
    onEvent: (ThemeSelectionUiEvent) -> Unit,
) {
    ResponsiveColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        portraitContent = {
            uiState.isMaterialYouToggleOn?.let { isMaterialYouOn ->
                item {
                    DynamicColorOption(
                        isChecked = isMaterialYouOn,
                        onToggle = { onEvent(ThemeSelectionUiEvent.MaterialYouToggleClicked(it)) },
                        onInfoClick = { onEvent(ThemeSelectionUiEvent.MaterialYouInfoClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    )
                }
            }
            item {
                Text(
                    text = stringResource(Res.string.select_theme),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                )
            }
            responsiveItem {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    uiState.options.forEach { option ->
                        ThemeOptionCard(
                            model = option,
                            onClick = { onEvent(ThemeSelectionUiEvent.OnThemeSelected(it)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            responsiveItem {
                AnimatedVisibility(
                    visible = uiState.isMaterialYouToggleOn != true,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(Res.string.select_contrast),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        )
                        ContrastSelector(
                            selectedContrast = uiState.selectedContrast,
                            onContrastSelected = { onEvent(ThemeSelectionUiEvent.OnContrastSelected(it)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
        landscapeContent = {
            responsiveItem {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    val isMaterialYouSupported = uiState.isMaterialYouToggleOn != null
                    if (isMaterialYouSupported) {
                        Column(modifier = Modifier.weight(1f)) {
                            ThemeSection(
                                options = uiState.options,
                                onThemeSelected = { onEvent(ThemeSelectionUiEvent.OnThemeSelected(it)) },
                            )

                            AnimatedVisibility(
                                visible = !uiState.isMaterialYouToggleOn,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut(),
                            ) {
                                ContrastSection(
                                    selectedContrast = uiState.selectedContrast,
                                    onContrastSelected = { onEvent(ThemeSelectionUiEvent.OnContrastSelected(it)) },
                                    modifier = Modifier.padding(top = 16.dp),
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 8.dp),
                        ) {
                            DynamicColorOption(
                                isChecked = uiState.isMaterialYouToggleOn,
                                onToggle = { onEvent(ThemeSelectionUiEvent.MaterialYouToggleClicked(it)) },
                                onInfoClick = { onEvent(ThemeSelectionUiEvent.MaterialYouInfoClicked) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                            )
                        }
                    } else {
                        Column(modifier = Modifier.weight(1f)) {
                            ThemeSection(
                                options = uiState.options,
                                onThemeSelected = { onEvent(ThemeSelectionUiEvent.OnThemeSelected(it)) },
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            ContrastSection(
                                selectedContrast = uiState.selectedContrast,
                                onContrastSelected = { onEvent(ThemeSelectionUiEvent.OnContrastSelected(it)) },
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun ThemeSection(
    options: List<ThemeSelectionModel>,
    onThemeSelected: (Theme) -> Unit,
) {
    Column {
        Text(
            text = stringResource(Res.string.select_theme),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            options.forEach { option ->
                ThemeOptionCard(
                    model = option,
                    onClick = { onThemeSelected(option.preference) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ContrastSection(
    selectedContrast: ContrastType,
    onContrastSelected: (ContrastType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.select_contrast),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        ContrastSelector(
            selectedContrast = selectedContrast,
            onContrastSelected = onContrastSelected,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DynamicColorOption(
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.dynamic_colors_title),
                style = MaterialTheme.typography.titleMedium,
            )
            CommonIconButton(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(Res.string.information),
                onClick = onInfoClick,
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
        )
    }
}
