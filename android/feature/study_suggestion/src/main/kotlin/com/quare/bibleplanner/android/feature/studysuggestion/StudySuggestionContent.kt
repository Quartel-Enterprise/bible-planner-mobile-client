package com.quare.bibleplanner.android.feature.studysuggestion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quare.bibleplanner.android.designsystem.sheet.AppModalBottomSheet
import com.quare.bibleplanner.android.designsystem.study.ExpandableText
import com.quare.bibleplanner.android.designsystem.sync.SyncAcrossDevicesRow
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiEvent
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiState
import com.valentinilk.shimmer.shimmer
import software.quare.loadable.valueOrNull

private val scopeNoteShape = RoundedCornerShape(10.dp)
private val sectionLetterSpacing = 0.4.sp
private const val SOFT_PRIMARY_ALPHA = 0.12f
private const val DISABLED_MODES_ALPHA = 0.45f
private const val DESCRIPTION_COLLAPSED_MAX_LINES = 1

@Composable
internal fun StudySuggestionContent(
    uiState: StudySuggestionUiState,
    onEvent: (StudySuggestionUiEvent) -> Unit,
) {
    AppModalBottomSheet(
        title = stringResource(R.string.study_suggestion_title),
        onDismiss = { onEvent(StudySuggestionUiEvent.OnDismiss) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ScopeNoteRow()
            ExpandableText(
                text = stringResource(R.string.study_suggestion_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                collapsedMaxLines = DESCRIPTION_COLLAPSED_MAX_LINES,
            )
            val settings = uiState.settings.valueOrNull()
            if (settings == null) {
                LoadingSettingsSkeleton()
            } else {
                LoadedSettingsContent(
                    settings = settings,
                    onEvent = onEvent,
                )
            }
            HorizontalDivider()
        }
        SyncAcrossDevicesRow(
            description = stringResource(R.string.study_suggestion_sync_description),
            isChecked = uiState.isSyncEnabled,
            isLoggedIn = uiState.isLoggedIn,
            onToggle = { onEvent(StudySuggestionUiEvent.SyncToggleClicked(it)) },
            onBlockedClick = { onEvent(StudySuggestionUiEvent.SyncToggleBlockedClicked) },
        )
    }
}

@Composable
private fun ScopeNoteRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(scopeNoteShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA))
            .padding(
                horizontal = 12.dp,
                vertical = 9.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.study_suggestion_scope_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun LoadingSettingsSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(scopeNoteShape)
            .shimmer()
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@Composable
private fun LoadedSettingsContent(
    settings: StudySuggestionSettingsModel,
    onEvent: (StudySuggestionUiEvent) -> Unit,
) {
    ToggleRow(
        isEnabled = settings.isEnabled,
        onToggle = { onEvent(StudySuggestionUiEvent.OnToggleClick(it)) },
    )
    HorizontalDivider()
    ModesSection(
        settings = settings,
        onEvent = onEvent,
    )
}

@Composable
private fun ToggleRow(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = isEnabled,
                role = Role.Switch,
                onValueChange = onToggle,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.study_suggestion_toggle_title),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val subtitleRes = if (isEnabled) {
                R.string.study_suggestion_toggle_subtitle_on
            } else {
                R.string.study_suggestion_toggle_subtitle_off
            }
            Text(
                text = stringResource(subtitleRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = null,
        )
    }
}

@Composable
private fun ModesSection(
    settings: StudySuggestionSettingsModel,
    onEvent: (StudySuggestionUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .alpha(if (settings.isEnabled) 1f else DISABLED_MODES_ALPHA)
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            modifier = Modifier.semantics { heading() },
            text = stringResource(R.string.study_suggestion_how_it_appears),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = sectionLetterSpacing,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StudySuggestionMode.entries.forEach { mode ->
            StudySuggestionModeCard(
                mode = mode,
                isSelected = settings.mode == mode,
                onClick = { onEvent(settings.toModeClickEvent(mode)) },
            )
        }
    }
}

// Why: with the suggestion off the modes stay tappable, so the tap can explain why they can't be chosen.
private fun StudySuggestionSettingsModel.toModeClickEvent(mode: StudySuggestionMode): StudySuggestionUiEvent =
    if (isEnabled) {
        StudySuggestionUiEvent.OnModeClick(mode)
    } else {
        StudySuggestionUiEvent.OnBlockedModeClick
    }
