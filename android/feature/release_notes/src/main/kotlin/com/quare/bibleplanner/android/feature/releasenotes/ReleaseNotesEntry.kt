package com.quare.bibleplanner.android.feature.releasenotes

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiAction
import com.quare.bibleplanner.feature.releasenotes.presentation.viewmodel.ReleaseNotesViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

internal fun ReleaseNotesNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(key = this) {
    val viewModel = koinViewModel<ReleaseNotesViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is ReleaseNotesUiAction.OpenUrl -> uriHandler.openUri(action.url)
        }
    }
    ExpressiveTheme {
        ReleaseNotesContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
