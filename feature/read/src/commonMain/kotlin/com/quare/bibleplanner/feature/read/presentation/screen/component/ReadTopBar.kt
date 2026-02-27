package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.utils.SharedTransitionModifierFactory
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadTopBar(
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
                onBackClick = {
                    onEvent(ReadUiEvent.OnArrowBackClick)
                },
            )
        },
        actions = {
            ChangeBibleVersionIconButton(onEvent = onEvent)
        },
    )
}
