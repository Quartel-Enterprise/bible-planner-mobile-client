package com.quare.bibleplanner.feature.applanguage.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.preferences.app_language.generated.resources.Res
import bibleplanner.feature.preferences.app_language.generated.resources.app_language_title
import bibleplanner.feature.preferences.app_language.generated.resources.language_english
import bibleplanner.feature.preferences.app_language.generated.resources.language_portuguese_brazil
import bibleplanner.feature.preferences.app_language.generated.resources.language_spanish
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.presentation.component.AppLanguageItem
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiEvent
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AppLanguageContent(
    uiState: AppLanguageUiState,
    onEvent: (AppLanguageUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Text(
            text = stringResource(Res.string.app_language_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        val languages = Language.entries
        languages.forEachIndexed { index, language ->
            AppLanguageItem(
                name = stringResource(language.toStringResource()),
                isSelected = language == uiState.selectedLanguage,
                onClick = { onEvent(AppLanguageUiEvent.OnLanguageSelected(language)) },
            )
            if (index < languages.lastIndex) HorizontalDivider()
        }
    }
}

private fun Language.toStringResource(): StringResource = when (this) {
    Language.ENGLISH -> Res.string.language_english
    Language.PORTUGUESE_BRAZIL -> Res.string.language_portuguese_brazil
    Language.SPANISH -> Res.string.language_spanish
}
