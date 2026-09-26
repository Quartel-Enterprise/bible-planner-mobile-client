package com.quare.bibleplanner.ui.component.date

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable

@Composable
actual fun <T> PickerContentCrossfadeComponent(
    targetState: T,
    content: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { state ->
        content(state)
    }
}
