package com.quare.bibleplanner.feature.verse.selectionmenu.presentation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute
import com.quare.bibleplanner.core.model.route.getVerseSelectionPane
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.component.SelectionSheet
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.utils.VerseSelectionUiActionCollector
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import org.koin.compose.viewmodel.koinViewModel

private val instantTransition: ContentTransform = EnterTransition.None togetherWith ExitTransition.None

/**
 * The scene swap is declared instant. NavDisplay keys its transition on the scene's class, so
 * going from the reader alone to reader-plus-panel would run the default 700ms cross-fade — and
 * because the reader is movable content it can only be composed in one of the two scenes at a
 * time, so it vanishes for the duration. The sheet animates itself into place instead.
 */
fun EntryProviderScope<NavKey>.verseSelection(
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
) {
    entry<VerseSelectionNavRoute>(
        metadata = getVerseSelectionPane() +
            NavDisplay.transitionSpec { instantTransition } +
            NavDisplay.popTransitionSpec { instantTransition } +
            NavDisplay.predictivePopTransitionSpec { instantTransition },
    ) {
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
                platform = viewModel.platform,
            )
        }
    }
}
