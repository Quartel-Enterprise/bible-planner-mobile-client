package com.quare.bibleplanner.feature.unlockpremium.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.unlockpremium.presentation.component.UnlockPremiumItemsComponent
import com.quare.bibleplanner.feature.unlockpremium.presentation.model.UnlockPremiumUiEvent
import com.quare.bibleplanner.feature.unlockpremium.presentation.model.UnlockPremiumUiState
import com.quare.bibleplanner.ui.component.icon.BackIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlockPremiumScreen(
    uiState: UnlockPremiumUiState,
    onEvent: (UnlockPremiumUiEvent) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                navigationIcon = { BackIcon(onBackClick = { onEvent(UnlockPremiumUiEvent.OnBackClick) }) },
                title = {},
            )
        },
    ) { paddingValues: PaddingValues ->
        UnlockPremiumItemsComponent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            uiState = uiState,
            onEvent = onEvent,
        )
    }
}
