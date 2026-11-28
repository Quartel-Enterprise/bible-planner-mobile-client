package com.quare.bibleplanner.feature.releasenotes.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiAction
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiState
import com.quare.bibleplanner.feature.releasenotes.presentation.viewmodel.ReleaseNotesViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector

@Composable
internal fun ReleaseNotesStateLayout(
    viewModel: ReleaseNotesViewModel,
    openUrl: (String) -> Unit,
    content: @Composable (ReleaseNotesUiState) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is ReleaseNotesUiAction.OpenUrl -> openUrl(action.url)
        }
    }

    content(uiState)
}
