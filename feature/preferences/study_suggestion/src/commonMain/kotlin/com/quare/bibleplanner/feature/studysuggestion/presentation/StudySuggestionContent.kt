package com.quare.bibleplanner.feature.studysuggestion.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bibleplanner.feature.preferences.study_suggestion.generated.resources.Res
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_description
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_how_it_appears
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_mode_banner_description
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_mode_banner_title
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_mode_dialog_description
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_mode_dialog_title
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_scope_note
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_toggle_subtitle_off
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_toggle_subtitle_on
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_toggle_title
import bibleplanner.feature.preferences.study_suggestion.generated.resources.sync_across_devices_description
import bibleplanner.feature.preferences.study_suggestion.generated.resources.sync_across_devices_title
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.feature.studysuggestion.presentation.component.StudySuggestionModeCard
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiEvent
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiState
import com.quare.bibleplanner.ui.component.ExpandableText
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox
import org.jetbrains.compose.resources.stringResource

private val scopeNoteCornerRadius = 10.dp
private val scopeNoteIconSize = 18.dp
private val sectionLetterSpacing = 0.4.sp
private val loadingCardHeight = 120.dp
private const val SOFT_PRIMARY_ALPHA = 0.12f
private const val DISABLED_MODES_ALPHA = 0.45f
private const val LOGGED_OUT_SYNC_ALPHA = 0.55f
private const val DESCRIPTION_COLLAPSED_MAX_LINES = 1

@Composable
internal fun StudySuggestionContent(
    uiState: StudySuggestionUiState,
    onEvent: (StudySuggestionUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScopeNote()
        ExpandableText(
            text = stringResource(Res.string.study_suggestion_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            collapsedMaxLines = DESCRIPTION_COLLAPSED_MAX_LINES,
        )
        val settings = uiState.settings.valueOrNull()
        if (settings == null) {
            LoadingSettings()
        } else {
            LoadedSettings(
                settings = settings,
                onEvent = onEvent,
            )
        }
        HorizontalDivider()
        SyncOption(
            isChecked = uiState.isSyncEnabled,
            isLoggedIn = uiState.isLoggedIn,
            onToggle = { onEvent(StudySuggestionUiEvent.SyncToggleClicked(it)) },
            onBlockedClick = { onEvent(StudySuggestionUiEvent.SyncToggleBlockedClicked) },
        )
    }
}

@Composable
private fun ScopeNote() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(scopeNoteCornerRadius))
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
            modifier = Modifier.size(scopeNoteIconSize),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(Res.string.study_suggestion_scope_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun LoadingSettings() {
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(loadingCardHeight),
        shape = RoundedCornerShape(scopeNoteCornerRadius),
    )
}

@Composable
private fun LoadedSettings(
    settings: StudySuggestionSettingsModel,
    onEvent: (StudySuggestionUiEvent) -> Unit,
) {
    ToggleRow(
        settings = settings,
        onEvent = onEvent,
    )
    HorizontalDivider()
    ModesSection(
        settings = settings,
        onEvent = onEvent,
    )
}

@Composable
private fun ToggleRow(
    settings: StudySuggestionSettingsModel,
    onEvent: (StudySuggestionUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(Res.string.study_suggestion_toggle_title),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val subtitle = if (settings.isEnabled) {
                Res.string.study_suggestion_toggle_subtitle_on
            } else {
                Res.string.study_suggestion_toggle_subtitle_off
            }
            Text(
                text = stringResource(subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = settings.isEnabled,
            onCheckedChange = { isChecked -> onEvent(StudySuggestionUiEvent.OnToggleClick(isChecked)) },
        )
    }
}

@Composable
private fun ModesSection(
    settings: StudySuggestionSettingsModel,
    onEvent: (StudySuggestionUiEvent) -> Unit,
) {
    val modesAlpha = if (settings.isEnabled) 1f else DISABLED_MODES_ALPHA
    Column(
        modifier = Modifier.alpha(modesAlpha),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(Res.string.study_suggestion_how_it_appears),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = sectionLetterSpacing,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StudySuggestionModeCard(
            mode = StudySuggestionMode.DIALOG,
            title = stringResource(Res.string.study_suggestion_mode_dialog_title),
            description = stringResource(Res.string.study_suggestion_mode_dialog_description),
            isSelected = settings.mode == StudySuggestionMode.DIALOG,
            onClick = {
                onEvent(settings.toModeClickEvent(StudySuggestionMode.DIALOG))
            },
        )
        StudySuggestionModeCard(
            mode = StudySuggestionMode.BANNER,
            title = stringResource(Res.string.study_suggestion_mode_banner_title),
            description = stringResource(Res.string.study_suggestion_mode_banner_description),
            isSelected = settings.mode == StudySuggestionMode.BANNER,
            onClick = {
                onEvent(settings.toModeClickEvent(StudySuggestionMode.BANNER))
            },
        )
    }
}

private fun StudySuggestionSettingsModel.toModeClickEvent(mode: StudySuggestionMode): StudySuggestionUiEvent =
    if (isEnabled) {
        StudySuggestionUiEvent.OnModeClick(mode)
    } else {
        StudySuggestionUiEvent.OnBlockedModeClick
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
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isLoggedIn) 1f else LOGGED_OUT_SYNC_ALPHA),
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
