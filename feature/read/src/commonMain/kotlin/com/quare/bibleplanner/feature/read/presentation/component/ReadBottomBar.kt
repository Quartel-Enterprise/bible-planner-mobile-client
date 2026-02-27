package com.quare.bibleplanner.feature.read.presentation.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState

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
        windowInsets = WindowInsets.navigationBars,
    ) {
        NavigationSuggestionRow(
            state = state,
            onEvent = onEvent
        )
    }
}
