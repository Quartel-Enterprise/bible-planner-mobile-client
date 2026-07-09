package com.quare.bibleplanner.feature.themeselection.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bibleplanner.feature.preferences.theme_selection.generated.resources.Res
import bibleplanner.feature.preferences.theme_selection.generated.resources.appearance
import bibleplanner.feature.preferences.theme_selection.generated.resources.dynamic_colors_title
import bibleplanner.feature.preferences.theme_selection.generated.resources.information
import bibleplanner.feature.preferences.theme_selection.generated.resources.select_contrast
import bibleplanner.feature.preferences.theme_selection.generated.resources.sync_across_devices_description
import bibleplanner.feature.preferences.theme_selection.generated.resources.sync_across_devices_title
import com.quare.bibleplanner.feature.themeselection.presentation.component.ContrastSelector
import com.quare.bibleplanner.feature.themeselection.presentation.component.ThemeOptionCard
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiState
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionContent(
    uiState: ThemeSelectionUiState,
    onEvent: (ThemeSelectionUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionLabel(text = stringResource(Res.string.appearance))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
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
        uiState.isMaterialYouToggleOn?.let { isMaterialYouOn ->
            DynamicColorOption(
                isChecked = isMaterialYouOn,
                onToggle = { onEvent(ThemeSelectionUiEvent.MaterialYouToggleClicked(it)) },
                onInfoClick = { onEvent(ThemeSelectionUiEvent.MaterialYouInfoClicked) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        // Contrast, the divider and the sync row share one column so the collapsed contrast adds
        // no extra arrangement gap between the sections around it.
        Column(modifier = Modifier.fillMaxWidth()) {
            AnimatedVisibility(
                visible = uiState.isMaterialYouToggleOn != true,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionLabel(
                        text = stringResource(Res.string.select_contrast),
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                    ContrastSelector(
                        selectedContrast = uiState.selectedContrast,
                        onContrastSelected = { onEvent(ThemeSelectionUiEvent.OnContrastSelected(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                    )
                }
            }
            HorizontalDivider()
            SyncOption(
                isChecked = uiState.isSyncEnabled,
                isLoggedIn = uiState.isLoggedIn,
                onToggle = { onEvent(ThemeSelectionUiEvent.SyncToggleClicked(it)) },
                onBlockedClick = { onEvent(ThemeSelectionUiEvent.SyncToggleBlockedClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.4.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
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

@Composable
private fun SyncOption(
    isChecked: Boolean,
    isLoggedIn: Boolean,
    onToggle: (Boolean) -> Unit,
    onBlockedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.alpha(if (isLoggedIn) 1f else 0.55f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.sync_across_devices_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(Res.string.sync_across_devices_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SyncSwitch(
            isChecked = isChecked,
            isLoggedIn = isLoggedIn,
            onToggle = onToggle,
            onBlockedClick = onBlockedClick,
        )
    }
}

/**
 * Locked when logged out: the switch is disabled and a transparent overlay captures taps over the
 * switch only (not the whole row), so the caller can prompt the user to sign in.
 */
@Composable
private fun SyncSwitch(
    isChecked: Boolean,
    isLoggedIn: Boolean,
    onToggle: (Boolean) -> Unit,
    onBlockedClick: () -> Unit,
) {
    Box {
        Switch(
            checked = isChecked && isLoggedIn,
            onCheckedChange = onToggle.takeIf { isLoggedIn },
            enabled = isLoggedIn,
        )
        if (!isLoggedIn) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBlockedClick,
                    ),
            )
        }
    }
}
