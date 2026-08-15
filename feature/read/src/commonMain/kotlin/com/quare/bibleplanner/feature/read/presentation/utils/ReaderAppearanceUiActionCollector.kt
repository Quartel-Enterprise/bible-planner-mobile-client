package com.quare.bibleplanner.feature.read.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.read.presentation.appearance.ReaderAppearanceUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun ReaderAppearanceUiActionCollector(
    uiActionFlow: Flow<ReaderAppearanceUiAction>,
    onNavigateBack: () -> Unit,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            ReaderAppearanceUiAction.NavigateBack -> onNavigateBack()
        }
    }
}
