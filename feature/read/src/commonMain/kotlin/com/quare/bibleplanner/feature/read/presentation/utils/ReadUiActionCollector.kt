package com.quare.bibleplanner.feature.read.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun ReadUiActionCollector(
    uiActionFlow: Flow<ReadUiAction>,
    onNavigate: (Any) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateReplacingTop: (Any) -> Unit,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            ReadUiAction.NavigateBack -> {
                onNavigateBack()
            }

            is ReadUiAction.NavigateToRoute -> {
                if (uiAction.replace) {
                    onNavigateReplacingTop(uiAction.route)
                } else {
                    onNavigate(uiAction.route)
                }
            }
        }
    }
}
