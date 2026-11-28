package com.quare.bibleplanner.android.feature.themeselection

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.sheet.AppModalBottomSheet
import com.quare.bibleplanner.android.designsystem.sync.SyncAcrossDevicesRow
import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiState

private val contrastOptions = listOf(
    ContrastType.Standard,
    ContrastType.Medium,
    ContrastType.High,
)

@Composable
internal fun ThemeSelectionContent(
    uiState: ThemeSelectionUiState,
    onEvent: (ThemeSelectionUiEvent) -> Unit,
) {
    AppModalBottomSheet(
        title = stringResource(R.string.theme_selection_title),
        onDismiss = { onEvent(ThemeSelectionUiEvent.OnDismiss) },
    ) {
        SectionLabel(
            modifier = Modifier.padding(bottom = 10.dp),
            text = stringResource(R.string.theme_selection_appearance),
        )
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            uiState.options.forEach { option ->
                ThemeOptionCard(
                    modifier = Modifier.weight(1f),
                    model = option,
                    onClick = { onEvent(ThemeSelectionUiEvent.OnThemeSelected(it)) },
                )
            }
        }
        uiState.isMaterialYouToggleOn?.let { isMaterialYouOn ->
            DynamicColorsRow(
                modifier = Modifier.padding(top = 16.dp),
                isChecked = isMaterialYouOn,
                onToggle = { onEvent(ThemeSelectionUiEvent.MaterialYouToggleClicked(it)) },
                onInfoClick = { onEvent(ThemeSelectionUiEvent.MaterialYouInfoClicked) },
            )
        }
        AnimatedVisibility(
            visible = uiState.isMaterialYouToggleOn != true,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                SectionLabel(
                    modifier = Modifier.padding(bottom = 10.dp),
                    text = stringResource(R.string.theme_selection_contrast),
                )
                ContrastPicker(
                    selectedContrast = uiState.selectedContrast,
                    onContrastSelected = { onEvent(ThemeSelectionUiEvent.OnContrastSelected(it)) },
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
        SyncAcrossDevicesRow(
            modifier = Modifier.padding(top = 4.dp),
            description = stringResource(R.string.theme_selection_sync_description),
            isChecked = uiState.isSyncEnabled,
            isLoggedIn = uiState.isLoggedIn,
            onToggle = { onEvent(ThemeSelectionUiEvent.SyncToggleClicked(it)) },
            onBlockedClick = { onEvent(ThemeSelectionUiEvent.SyncToggleBlockedClicked) },
        )
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DynamicColorsRow(
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Why: the info button sits between the label and the Switch, so the row can't be one toggleable target.
    val switchLabel = stringResource(R.string.theme_selection_dynamic_colors)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.theme_selection_dynamic_colors),
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(onClick = onInfoClick) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.theme_selection_dynamic_colors_info),
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
        ) {
            Switch(
                checked = isChecked,
                onCheckedChange = onToggle,
                modifier = Modifier.semantics { contentDescription = switchLabel },
            )
        }
    }
}

@Composable
private fun ContrastPicker(
    selectedContrast: ContrastType,
    onContrastSelected: (ContrastType) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        contrastOptions.forEachIndexed { index, contrastType ->
            SegmentedButton(
                selected = contrastType == selectedContrast,
                onClick = { onContrastSelected(contrastType) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = contrastOptions.size,
                ),
            ) {
                Text(
                    text = stringResource(contrastType.toTitleRes()),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@StringRes
private fun ContrastType.toTitleRes(): Int = when (this) {
    ContrastType.Standard -> R.string.theme_selection_contrast_standard
    ContrastType.Medium -> R.string.theme_selection_contrast_medium
    ContrastType.High -> R.string.theme_selection_contrast_high
}
