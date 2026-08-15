package com.quare.bibleplanner.feature.verse.selectionmenu.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute
import com.quare.bibleplanner.core.model.route.verseSelectionPane
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.component.SelectionSheet
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.utils.VerseSelectionUiActionCollector
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.verseSelection(
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
) {
    entry<VerseSelectionNavRoute>(metadata = verseSelectionPane()) {
        val viewModel = koinViewModel<VerseSelectionViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        VerseSelectionUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            onNavigate = onNavigate,
            onNavigateBack = onNavigateBack,
        )
        uiState?.let { safeUiState ->
            SelectionSheet(
                selection = safeUiState,
                onEvent = viewModel::onEvent,
                isWide = LocalIsWideLayout.current,
            )
        }
    }
}
