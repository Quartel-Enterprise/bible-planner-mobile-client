package com.quare.bibleplanner.android.feature.bibleversion

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.sheet.AppModalBottomSheet
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionsUiState

private val dividerStartInset = 104.dp

@Composable
internal fun BibleVersionContent(
    uiState: BibleVersionsUiState,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    AppModalBottomSheet(
        title = stringResource(R.string.bible_version_title),
        subtitle = stringResource(R.string.bible_version_description),
        onDismiss = { onEvent(BibleVersionUiEvent.OnDismiss) },
    ) {
        when (uiState) {
            BibleVersionsUiState.Loading -> BibleVersionsLoadingIndicator()

            BibleVersionsUiState.Error -> BibleVersionsErrorContent(
                onRetry = { onEvent(BibleVersionUiEvent.TryToDownloadBibleVersionsAgain) },
            )

            is BibleVersionsUiState.Success -> BibleVersionGroupsList(
                versionsByLanguage = uiState.data,
                onEvent = onEvent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BibleVersionsLoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator()
    }
}

@Composable
private fun BibleVersionsErrorContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.bible_version_load_error),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = onRetry,
        ) {
            Text(text = stringResource(R.string.bible_version_try_again))
        }
    }
}

@Composable
private fun BibleVersionGroupsList(
    versionsByLanguage: Map<Language, List<BibleModel>>,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    Column(modifier = Modifier.selectableGroup()) {
        versionsByLanguage.forEach { (language, versions) ->
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = stringResource(language.toNameRes()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                versions.forEachIndexed { index, version ->
                    val previous = versions.getOrNull(index - 1)
                    if (previous != null && !previous.isSelected && !version.isSelected) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = dividerStartInset),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                    BibleVersionItem(
                        model = version,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@StringRes
private fun Language.toNameRes(): Int = when (this) {
    Language.ENGLISH -> R.string.bible_version_language_english
    Language.PORTUGUESE_BRAZIL -> R.string.bible_version_language_portuguese_brazil
    Language.SPANISH -> R.string.bible_version_language_spanish
}
