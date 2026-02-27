package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.feature.read.presentation.screen.component.ChangeReadStatusButton
import com.quare.bibleplanner.feature.read.presentation.screen.component.NavigationSuggestionComponent
import com.quare.bibleplanner.feature.read.presentation.screen.component.ReadLandscapeChapterHeader
import com.quare.bibleplanner.feature.read.presentation.screen.content.ReadErrorContent
import com.quare.bibleplanner.feature.read.presentation.screen.content.ReadLoadingContent
import com.quare.bibleplanner.feature.read.presentation.screen.content.versesContent
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReadLandscapeScreen(
    state: ReadUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (ReadUiEvent) -> Unit,
) {
    val bookName = stringResource(state.bookStringResource)
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ReadLandscapeLeftContent(
                    modifier = Modifier.fillMaxWidth(0.4f),
                    animatedVisibilityScope = animatedVisibilityScope,
                    sharedTransitionScope = sharedTransitionScope,
                    bookName = bookName,
                    state = state,
                    onEvent = onEvent,
                )
                VerticalDivider()
                ReadLandscapeRightContent(
                    state = state,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
internal fun ReadLandscapeLeftContent(
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    bookName: String,
    state: ReadUiState,
    onEvent: (ReadUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReadLandscapeChapterHeader(
            modifier = Modifier.fillMaxWidth(),
            animatedVisibilityScope = animatedVisibilityScope,
            sharedTransitionScope = sharedTransitionScope,
            bookName = bookName,
            state = state,
            onEvent = onEvent,
        )
        VerticalSpacer(8)
        ChangeReadStatusButton(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            isRead = state.isChapterRead,
            onClick = { onEvent(ReadUiEvent.ToggleReadStatus) },
        )
        NavigationSuggestionComponent(
            navigationSuggestions = state.navigationSuggestions,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun ReadLandscapeRightContent(
    modifier: Modifier = Modifier,
    state: ReadUiState,
    onEvent: (ReadUiEvent) -> Unit,
) {
    when (state) {
        is ReadUiState.Error -> ReadErrorContent(
            modifier = modifier,
            state = state,
            onEvent = onEvent,
        )

        is ReadUiState.Loading -> ReadLoadingContent(modifier)

        is ReadUiState.Success -> LazyColumn(modifier = modifier, contentPadding = PaddingValues(8.dp)) {
            versesContent(state)
        }
    }
}
