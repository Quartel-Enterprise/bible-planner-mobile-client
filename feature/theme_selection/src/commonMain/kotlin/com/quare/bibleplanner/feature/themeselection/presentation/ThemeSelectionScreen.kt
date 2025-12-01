package com.quare.bibleplanner.feature.themeselection.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.themeselection.presentation.component.ThemeOptionCard
import com.quare.bibleplanner.feature.themeselection.presentation.component.ThemeSelectionTopBar
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiState
import com.quare.bibleplanner.ui.theme.model.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionScreen(
    uiState: ThemeSelectionUiState,
    onEvent: (ThemeSelectionUiEvent) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ThemeSelectionTopBar(
                isMaterialYouChecked = uiState.isMaterialYouToggleOn,
                onEvent = onEvent,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { paddingValues: PaddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            val isWide = maxWidth > 600.dp
            val commonModifier = Modifier.align(Alignment.Center)
            val setTheme = { theme: Theme ->
                onEvent(ThemeSelectionUiEvent.OnThemeSelected(theme))
            }
            val options = uiState.options
            if (isWide) {
                Row(
                    modifier = commonModifier,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    options.forEach {
                        ThemeOptionCard(
                            model = it,
                            onClick = setTheme,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                LazyColumn(modifier = commonModifier, verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
                    items(options) {
                        ThemeOptionCard(
                            model = it,
                            onClick = setTheme,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
