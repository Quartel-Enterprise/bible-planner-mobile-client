package com.quare.bibleplanner.feature.read.presentation.screen.content

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.feature.read.presentation.screen.component.NavigationSuggestionComponent
import com.quare.bibleplanner.feature.read.presentation.screen.component.ReadToggleComponent
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@Composable
internal fun ReadScreenSuccessPortraitContent(
    modifier: Modifier,
    listState: LazyListState,
    state: ReadUiState.Success,
    onEvent: (ReadUiEvent) -> Unit,
) {
    val isScrollable by remember(listState) {
        derivedStateOf { listState.canScrollForward || listState.canScrollBackward }
    }
    LazyColumn(
        contentPadding = PaddingValues(bottom = 8.dp),
        modifier = modifier.padding(horizontal = 16.dp),
        state = listState,
    ) {
        versesContent(state)
        if (isScrollable) {
            item { VerticalSpacer() }
            item {
                NavigationSuggestionComponent(
                    modifier = Modifier.navigationBarsPadding(),
                    centerComponent = {
                        ReadToggleComponent(
                            modifier = Modifier.align(Alignment.Center),
                            isChecked = state.isChapterRead,
                            toggleReadStatus = { onEvent(ReadUiEvent.ToggleReadStatus) },
                        )
                    },
                    onEvent = onEvent,
                    navigationSuggestions = state.navigationSuggestions,
                )
            }
        }
    }
}
