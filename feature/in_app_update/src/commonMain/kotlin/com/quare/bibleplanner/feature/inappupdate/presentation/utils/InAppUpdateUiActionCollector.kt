package com.quare.bibleplanner.feature.inappupdate.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun InAppUpdateUiActionCollector(
    uiActionFlow: Flow<InAppUpdateUiAction>,
    onNavigateBack: () -> Unit,
) {
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            InAppUpdateUiAction.NavigateBack -> onNavigateBack()
        }
    }
}
