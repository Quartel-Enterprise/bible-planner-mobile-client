package com.quare.bibleplanner.feature.inappupdate.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.inappupdate.presentation.model.UpdateDownloadedUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun UpdateDownloadedUiActionCollector(
    uiActionFlow: Flow<UpdateDownloadedUiAction>,
    onNavigateBack: () -> Unit,
) {
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            UpdateDownloadedUiAction.NavigateBack -> onNavigateBack()
        }
    }
}
