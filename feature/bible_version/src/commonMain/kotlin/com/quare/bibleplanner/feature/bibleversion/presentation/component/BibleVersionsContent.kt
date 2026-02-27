package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.bible_version.generated.resources.Res
import bibleplanner.feature.bible_version.generated.resources.bible_versions
import bibleplanner.feature.bible_version.generated.resources.download_bible_versions_error
import bibleplanner.feature.bible_version.generated.resources.manage_bible_versions_description
import bibleplanner.feature.bible_version.generated.resources.try_again
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionsUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BibleVersionsContent(
    modifier: Modifier = Modifier,
    uiState: BibleVersionsUiState,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            modifier = Modifier.padding(
                vertical = 8.dp,
            ),
            text = stringResource(Res.string.bible_versions),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(Res.string.manage_bible_versions_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalSpacer(8)
        when (uiState) {
            BibleVersionsUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    VerticalSpacer()
                    Text(
                        text = stringResource(Res.string.download_bible_versions_error),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    VerticalSpacer()
                    Button(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        onClick = { onEvent(BibleVersionUiEvent.TryToDownloadBibleVersionsAgain) },
                    ) {
                        Text(text = stringResource(Res.string.try_again))
                    }
                }
            }

            BibleVersionsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is BibleVersionsUiState.Success -> {
                BibleVersionsListComponent(
                    selectionMap = uiState.data,
                    onEvent = onEvent,
                )
            }
        }
    }
}
