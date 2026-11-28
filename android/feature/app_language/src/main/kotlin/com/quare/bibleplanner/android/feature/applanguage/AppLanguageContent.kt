package com.quare.bibleplanner.android.feature.applanguage

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.sheet.AppModalBottomSheet
import com.quare.bibleplanner.android.designsystem.sync.SyncAcrossDevicesRow
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiEvent
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiState

private const val SELECTED_CONTAINER_ALPHA = 0.12f
private val codeChipSize = 36.dp
private val dividerStartInset = 66.dp

@Composable
internal fun AppLanguageContent(
    uiState: AppLanguageUiState,
    onEvent: (AppLanguageUiEvent) -> Unit,
) {
    val languageNames = uiState.languages.associateWith { language -> stringResource(language.toNameRes()) }
    val sortedLanguages = uiState.languages.sortedWith(
        compareByDescending<Language> { it == uiState.selectedLanguage }.thenBy { languageNames[it] },
    )
    AppModalBottomSheet(
        title = stringResource(R.string.app_language_title),
        onDismiss = { onEvent(AppLanguageUiEvent.OnDismiss) },
    ) { sheetCloseGuard ->
        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.large,
                ).selectableGroup(),
        ) {
            sortedLanguages.forEachIndexed { index, language ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(start = dividerStartInset))
                }
                LanguageItem(
                    language = language,
                    name = languageNames[language].orEmpty(),
                    isSelected = language == uiState.selectedLanguage,
                    // Why: the ViewModel pops the sheet once the language is saved; a second tap would pop the screen below.
                    onClick = { sheetCloseGuard.close { onEvent(AppLanguageUiEvent.OnLanguageSelected(language)) } },
                )
            }
        }
        SyncAcrossDevicesRow(
            modifier = Modifier.padding(top = 4.dp),
            description = stringResource(R.string.app_language_sync_description),
            isChecked = uiState.isSyncEnabled,
            isLoggedIn = uiState.isLoggedIn,
            onToggle = { onEvent(AppLanguageUiEvent.SyncToggleClicked(it)) },
            onBlockedClick = { onEvent(AppLanguageUiEvent.SyncToggleBlockedClicked) },
        )
    }
}

@Composable
private fun LanguageItem(
    language: Language,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.selectable(
            selected = isSelected,
            role = Role.RadioButton,
            onClick = onClick,
        ),
        headlineContent = {
            Text(
                text = name,
                fontWeight = if (isSelected) FontWeight.SemiBold else null,
            )
        },
        leadingContent = {
            LanguageCodeChip(
                code = language.toCode(),
                isSelected = isSelected,
            )
        },
        trailingContent = {
            RadioButton(
                selected = isSelected,
                onClick = null,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = SELECTED_CONTAINER_ALPHA)
            } else {
                Color.Transparent
            },
        ),
    )
}

@Composable
private fun LanguageCodeChip(
    code: String,
    isSelected: Boolean,
) {
    Box(
        modifier = Modifier
            .size(codeChipSize)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
        )
    }
}

private fun Language.toCode(): String = when (this) {
    Language.ENGLISH -> "EN"
    Language.PORTUGUESE_BRAZIL -> "PT"
    Language.SPANISH -> "ES"
}

@StringRes
private fun Language.toNameRes(): Int = when (this) {
    Language.ENGLISH -> R.string.app_language_english
    Language.PORTUGUESE_BRAZIL -> R.string.app_language_portuguese_brazil
    Language.SPANISH -> R.string.app_language_spanish
}
