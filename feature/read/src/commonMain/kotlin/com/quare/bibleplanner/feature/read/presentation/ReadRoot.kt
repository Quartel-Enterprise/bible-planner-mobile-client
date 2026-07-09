package com.quare.bibleplanner.feature.read.presentation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.feature.read.presentation.screen.ReadScreen
import com.quare.bibleplanner.feature.read.presentation.utils.ReadUiActionCollector
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.read(
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateReplacingTop: (NavKey) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
) {
    entry<ReadNavRoute> { route ->
        val viewModel = koinViewModel<ReadViewModel> { parametersOf(route) }
        val state by viewModel.uiState.collectAsState()
        ReadUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            onNavigate = onNavigate,
            onNavigateBack = onNavigateBack,
            onNavigateReplacingTop = onNavigateReplacingTop,
        )
        val onEvent = viewModel::onEvent
        ReadScreen(
            platform = viewModel.platform,
            state = state,
            onEvent = onEvent,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
        )
    }
}
