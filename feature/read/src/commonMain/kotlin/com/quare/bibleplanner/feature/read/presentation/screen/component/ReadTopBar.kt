package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.component.icon.BackIcon
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadTopBar(
    platform: Platform,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    state: ReadUiState,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    onEvent: (ReadUiEvent) -> Unit,
) {
    val bookName = stringResource(state.bookStringResource)
    val chapterNumber = state.chapterNumber
    TopAppBar(
        scrollBehavior = topAppBarScrollBehavior,
        modifier = modifier,
        title = {
            ReadScreenTitleComponent(
                animatedVisibilityScope = animatedVisibilityScope,
                sharedTransitionScope = sharedTransitionScope,
                bookName = bookName,
                chapterNumber = chapterNumber,
            )
        },
        navigationIcon = {
            BackIcon(
                platform = platform,
                onBackClick = { onEvent(ReadUiEvent.OnArrowBackClick) },
            )
        },
        actions = {
            ChangeBibleVersionIconButton(onEvent = onEvent)
        },
    )
}
