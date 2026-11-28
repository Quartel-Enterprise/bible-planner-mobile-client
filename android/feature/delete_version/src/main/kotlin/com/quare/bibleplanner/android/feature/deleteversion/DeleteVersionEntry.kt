package com.quare.bibleplanner.android.feature.deleteversion

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.feature.deleteversion.presentation.viewmodel.DeleteVersionViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun DeleteVersionNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(),
) {
    val viewModel = koinViewModel<DeleteVersionViewModel> { parametersOf(this) }
    val uiState by viewModel.uiState.collectAsState()
    ExpressiveTheme {
        DeleteVersionContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
