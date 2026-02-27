package com.quare.bibleplanner.feature.read.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.mark_as_read
import bibleplanner.feature.read.generated.resources.mark_as_unread
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NavigationSuggestionRow(
    modifier: Modifier = Modifier,
    state: ReadUiState,
    onEvent: (ReadUiEvent) -> Unit,
) {
    val (previous, next) = state.navigationSuggestions.run { previous to next }
    Row(
        modifier = modifier.widthIn(max = 600.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val toggleReadClick = { onEvent(ReadUiEvent.ToggleReadStatus) }
        previous?.let { safePrevious ->
            NavigationSuggestionButton(
                suggestion = safePrevious,
                isNext = false,
                onClick = { onEvent(ReadUiEvent.OnNavigationSuggestionClick(safePrevious)) },
            )
        }
        val readButtonModifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)
        if (state.isChapterRead) {
            OutlinedButton(
                modifier = readButtonModifier,
                onClick = toggleReadClick,
            ) {
                Text(text = stringResource(Res.string.mark_as_unread))
            }
        } else {
            Button(
                modifier = readButtonModifier,
                onClick = toggleReadClick,
            ) {
                Text(text = stringResource(Res.string.mark_as_read))
            }
        }
        next?.let { safeNext ->
            NavigationSuggestionButton(
                suggestion = safeNext,
                isNext = true,
                onClick = { onEvent(ReadUiEvent.OnNavigationSuggestionClick(safeNext)) },
            )
        }
    }
}
