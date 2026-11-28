package com.quare.bibleplanner.android.feature.bibleversion

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.bibleversion.presentation.BibleVersionViewModel
import org.koin.compose.viewmodel.koinViewModel

internal fun BibleVersionSelectorRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<BibleVersionViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    ExpressiveTheme {
        BibleVersionContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
