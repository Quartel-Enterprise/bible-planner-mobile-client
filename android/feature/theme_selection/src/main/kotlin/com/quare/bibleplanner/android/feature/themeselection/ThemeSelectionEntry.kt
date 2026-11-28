package com.quare.bibleplanner.android.feature.themeselection

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.themeselection.presentation.ThemeSelectionViewModel
import org.koin.compose.viewmodel.koinViewModel

internal fun ThemeNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<ThemeSelectionViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    ExpressiveTheme {
        ThemeSelectionContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
