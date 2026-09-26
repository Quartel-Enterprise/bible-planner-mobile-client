package com.quare.bibleplanner.ui.component.date

import androidx.compose.runtime.Composable

@Composable
expect fun <T> PickerContentCrossfadeComponent(
    targetState: T,
    content: @Composable (T) -> Unit,
)
