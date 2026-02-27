package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer

@Composable
internal fun ReadLandscapeChapterHeader(
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    bookName: String,
    state: ReadUiState,
    onEvent: (ReadUiEvent) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackIcon(onBackClick = { onEvent(ReadUiEvent.OnArrowBackClick) })
            HorizontalSpacer(8)
            ReadScreenTitleComponent(
                animatedVisibilityScope = animatedVisibilityScope,
                sharedTransitionScope = sharedTransitionScope,
                bookName = bookName,
                chapterNumber = state.chapterNumber,
                style = MaterialTheme.typography.titleLarge,
            )
        }
        ChangeBibleVersionIconButton(onEvent = onEvent)
    }
}
