package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.quare.bibleplanner.feature.read.presentation.screen.component.ReadBottomBar
import com.quare.bibleplanner.feature.read.presentation.screen.component.ReadTopBar
import com.quare.bibleplanner.feature.read.presentation.screen.content.ReadErrorContent
import com.quare.bibleplanner.feature.read.presentation.screen.content.ReadLoadingContent
import com.quare.bibleplanner.feature.read.presentation.screen.content.ReadScreenSuccessPortraitContent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadPortraitScreen(
    state: ReadUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (ReadUiEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val bottomBarScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(bottomBarScrollBehavior.nestedScrollConnection)
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            ReadTopBar(
                state = state,
                animatedVisibilityScope = animatedVisibilityScope,
                sharedTransitionScope = sharedTransitionScope,
                onEvent = onEvent,
                topAppBarScrollBehavior = topBarScrollBehavior,
            )
        },
        bottomBar = {
            ReadBottomBar(
                scrollBehavior = bottomBarScrollBehavior,
                state = state,
                onEvent = onEvent,
            )
        },
    ) { paddingValues ->
        val commonModifier = Modifier.fillMaxSize().padding(paddingValues)
        when (state) {
            is ReadUiState.Loading -> {
                ReadLoadingContent(commonModifier)
            }

            is ReadUiState.Error -> {
                ReadErrorContent(
                    modifier = commonModifier,
                    state = state,
                    onEvent = onEvent
                )
            }

            is ReadUiState.Success -> {
                ReadScreenSuccessPortraitContent(
                    modifier = commonModifier,
                    listState = listState,
                    state = state,
                    onEvent = onEvent
                )
            }
        }
    }
}
