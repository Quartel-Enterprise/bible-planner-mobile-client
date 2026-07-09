package com.quare.bibleplanner.feature.applanguage.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import bibleplanner.feature.preferences.app_language.generated.resources.Res
import bibleplanner.feature.preferences.app_language.generated.resources.language_english
import bibleplanner.feature.preferences.app_language.generated.resources.language_portuguese_brazil
import bibleplanner.feature.preferences.app_language.generated.resources.language_spanish
import bibleplanner.feature.preferences.app_language.generated.resources.sync_across_devices_description
import bibleplanner.feature.preferences.app_language.generated.resources.sync_across_devices_title
import com.quare.bibleplanner.core.utils.isLastIndex
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.presentation.component.AppLanguageItem
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiEvent
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AppLanguageContent(
    modifier: Modifier = Modifier,
    uiState: AppLanguageUiState,
    onEvent: (AppLanguageUiEvent) -> Unit,
) {
    val languageNames = uiState.languages.associateWith { stringResource(it.toStringResource()) }
    val sortedLanguages = uiState.languages.sortedWith(
        compareByDescending<Language> { it == uiState.selectedLanguage }
            .thenBy { languageNames[it] },
    )
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        LazyColumn(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        ) {
            itemsIndexed(sortedLanguages) { index, language ->
                AppLanguageItem(
                    code = language.toCode(),
                    name = languageNames[language].orEmpty(),
                    isSelected = language == uiState.selectedLanguage,
                    onClick = { onEvent(AppLanguageUiEvent.OnLanguageSelected(language)) },
                )
                if (!sortedLanguages.isLastIndex(index)) {
                    HorizontalDivider(modifier = Modifier.padding(start = 66.dp))
                }
            }
        }
        SyncOption(
            isChecked = uiState.isSyncEnabled,
            isLoggedIn = uiState.isLoggedIn,
            onToggle = { onEvent(AppLanguageUiEvent.SyncToggleClicked(it)) },
            onBlockedClick = { onEvent(AppLanguageUiEvent.SyncToggleBlockedClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
        modifier = modifier,
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

private fun Language.toStringResource(): StringResource = when (this) {
    Language.ENGLISH -> Res.string.language_english
    Language.PORTUGUESE_BRAZIL -> Res.string.language_portuguese_brazil
    Language.SPANISH -> Res.string.language_spanish
}

private fun Language.toCode(): String = when (this) {
    Language.ENGLISH -> "EN"
    Language.PORTUGUESE_BRAZIL -> "PT"
    Language.SPANISH -> "ES"
}
