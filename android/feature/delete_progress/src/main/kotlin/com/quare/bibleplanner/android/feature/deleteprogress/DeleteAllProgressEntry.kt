package com.quare.bibleplanner.android.feature.deleteprogress

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.feature.deleteprogress.presentation.viewmodel.DeleteAllProgressViewModel
import org.koin.compose.viewmodel.koinViewModel

internal fun DeleteAllProgressNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(),
) {
    val viewModel = koinViewModel<DeleteAllProgressViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    ExpressiveTheme {
        DeleteAllProgressContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
