package com.quare.bibleplanner.feature.applanguage.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import bibleplanner.feature.preferences.app_language.generated.resources.Res
import bibleplanner.feature.preferences.app_language.generated.resources.app_language_title
import bibleplanner.feature.preferences.app_language.generated.resources.language_english
import bibleplanner.feature.preferences.app_language.generated.resources.language_portuguese_brazil
import bibleplanner.feature.preferences.app_language.generated.resources.language_spanish
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
        Text(
            text = stringResource(Res.string.app_language_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        LazyColumn(
            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        ) {
            itemsIndexed(sortedLanguages) { index, language ->
                AppLanguageItem(
                    name = languageNames[language].orEmpty(),
                    isSelected = language == uiState.selectedLanguage,
                    onClick = { onEvent(AppLanguageUiEvent.OnLanguageSelected(language)) },
                )
                if (!sortedLanguages.isLastIndex(index)) HorizontalDivider()
            }
        }
    }
}

private fun Language.toStringResource(): StringResource = when (this) {
    Language.ENGLISH -> Res.string.language_english
    Language.PORTUGUESE_BRAZIL -> Res.string.language_portuguese_brazil
    Language.SPANISH -> Res.string.language_spanish
}
