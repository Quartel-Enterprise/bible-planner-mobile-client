package com.quare.bibleplanner.android.feature.readerappearance

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.ReaderAppearanceNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.read.presentation.appearance.ReaderAppearanceViewModel
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import org.koin.compose.viewmodel.koinViewModel

internal fun ReaderAppearanceNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<ReaderAppearanceViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    ExpressiveTheme {
        ReaderAppearanceContent(
            uiState = uiState,
            isWideLayout = LocalIsWideLayout.current,
            onEvent = viewModel::onEvent,
        )
    }
}
