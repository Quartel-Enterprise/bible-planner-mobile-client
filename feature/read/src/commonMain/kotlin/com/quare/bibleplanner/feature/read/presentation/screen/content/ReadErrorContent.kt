package com.quare.bibleplanner.feature.read.presentation.screen.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.chapter_not_downloaded_error
import bibleplanner.feature.read.generated.resources.manage_bible_versions
import bibleplanner.feature.read.generated.resources.retry
import bibleplanner.feature.read.generated.resources.unknown_error_occurred
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReadErrorContent(
    modifier: Modifier = Modifier,
    state: ReadUiState.Error,
    onEvent: (ReadUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        val (errorMessage, buttonText) = when (state) {
            is ReadUiState.Error.ChapterNotFound -> stringResource(
                resource = Res.string.chapter_not_downloaded_error,
                stringResource(state.bookStringResource),
                state.chapterNumber,
                state.selectedBibleVersionName,
            ) to stringResource(Res.string.manage_bible_versions)

            is ReadUiState.Error.Unknown -> stringResource(Res.string.unknown_error_occurred) to
                stringResource(
                    Res.string.retry,
                )
        }
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = { onEvent(state.errorUiEvent) },
        ) {
            Text(text = buttonText)
        }
    }
}
