package com.quare.bibleplanner.feature.applanguage.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import bibleplanner.feature.preferences.app_language.generated.resources.Res
import bibleplanner.feature.preferences.app_language.generated.resources.sync_across_devices_description
import com.quare.bibleplanner.core.utils.isLastIndex
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.presentation.component.AppLanguageItem
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiEvent
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiState
import com.quare.bibleplanner.ui.component.language.toStringResource
import com.quare.bibleplanner.ui.component.sync.SyncAcrossDevicesOption
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
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        ) {
            sortedLanguages.forEachIndexed { index, language ->
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
        SyncAcrossDevicesOption(
            description = stringResource(Res.string.sync_across_devices_description),
            isChecked = uiState.isSyncEnabled,
            isLoggedIn = uiState.isLoggedIn,
            onToggle = { onEvent(AppLanguageUiEvent.SyncToggleClicked(it)) },
            onBlockedClick = { onEvent(AppLanguageUiEvent.SyncToggleBlockedClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
        )
    }
}

private fun Language.toCode(): String = when (this) {
    Language.ENGLISH -> "EN"
    Language.PORTUGUESE_BRAZIL -> "PT"
    Language.SPANISH -> "ES"
}
