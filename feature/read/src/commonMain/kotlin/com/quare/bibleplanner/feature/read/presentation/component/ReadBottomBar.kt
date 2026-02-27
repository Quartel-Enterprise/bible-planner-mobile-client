package com.quare.bibleplanner.feature.read.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.mark_as_read
import bibleplanner.feature.read.generated.resources.mark_as_unread
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadBottomBar(
    modifier: Modifier = Modifier,
    scrollBehavior: BottomAppBarScrollBehavior,
    state: ReadUiState,
    onEvent: (ReadUiEvent) -> Unit,
) {
    BottomAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
    ) {
        val (previous, next) = state.navigationSuggestions.run {
            previous to next
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (previous != null && next != null) {
                Arrangement.SpaceBetween
            } else {
                Arrangement.Start
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val toggleReadClick = { onEvent(ReadUiEvent.ToggleReadStatus) }
            previous?.let { safePrevious ->
                TextButton(onClick = { onEvent(ReadUiEvent.OnNavigationSuggestionClick(safePrevious)) }) {
                    val contentDescription = "${safePrevious.bookId.getBookName()} ${safePrevious.chapterNumber}"
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = contentDescription,
                    )
                    Text(text = "${safePrevious.bookId.getBookName()} ${safePrevious.chapterNumber}")
                }
            }
            if (state.isChapterRead) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onClick = toggleReadClick,
                ) {
                    Text(text = stringResource(Res.string.mark_as_unread))
                }
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onClick = toggleReadClick,
                ) {
                    Text(text = stringResource(Res.string.mark_as_read))
                }
            }
            next?.let { safeNext ->
                TextButton(onClick = { onEvent(ReadUiEvent.OnNavigationSuggestionClick(safeNext)) }) {
                    val contentDescription = "${safeNext.bookId.getBookName()} ${safeNext.chapterNumber}"
                    Text(text = "${safeNext.bookId.getBookName()} ${safeNext.chapterNumber}")
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = contentDescription,
                    )
                }
            }
        }
    }
}
