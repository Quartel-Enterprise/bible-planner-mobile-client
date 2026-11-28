package com.quare.bibleplanner.android.feature.inappupdate

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.inappupdate.presentation.InAppUpdateViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun InAppUpdateNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<InAppUpdateViewModel> { parametersOf(this) }
    val uiState by viewModel.uiState.collectAsState()
    ExpressiveTheme {
        InAppUpdateContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
