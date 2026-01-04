package com.quare.bibleplanner.feature.main.presentation.screen.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBarComponent(
    onEvent: (MainScreenUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {},
        actions = {
        },
    )
}
