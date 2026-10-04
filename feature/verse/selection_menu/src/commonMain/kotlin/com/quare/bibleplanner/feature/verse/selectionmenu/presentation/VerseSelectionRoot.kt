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

/*
 * Why: NavDisplay keys transitions on scene class, so reader -> reader+panel would run the
 * default 700ms cross-fade, and the reader (movable content) vanishes meanwhile. The swap
 * is instant and the sheet animates itself.
 */
fun EntryProviderScope<NavKey>.verseSelection() {
    entry<VerseSelectionNavRoute>(
        metadata = getVerseSelectionPane() +
            NavDisplay.transitionSpec { instantTransition } +
            NavDisplay.popTransitionSpec { instantTransition } +
            NavDisplay.predictivePopTransitionSpec { instantTransition },
    ) {
        val viewModel = koinViewModel<VerseSelectionViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        VerseSelectionUiActionCollector(uiActionFlow = viewModel.uiAction)
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
