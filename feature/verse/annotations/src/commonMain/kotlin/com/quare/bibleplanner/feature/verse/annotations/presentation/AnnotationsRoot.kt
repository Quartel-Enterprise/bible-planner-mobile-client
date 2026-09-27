package com.quare.bibleplanner.feature.verse.annotations.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.AnnotationsNavRoute
import com.quare.bibleplanner.feature.verse.annotations.presentation.content.AnnotationsScreen
import com.quare.bibleplanner.feature.verse.annotations.presentation.utils.AnnotationsUiActionCollector
import com.quare.bibleplanner.feature.verse.annotations.presentation.viewmodel.AnnotationsViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.annotations() {
    entry<AnnotationsNavRoute> {
        val viewModel = koinViewModel<AnnotationsViewModel>()
        val state by viewModel.uiState.collectAsState()
        AnnotationsUiActionCollector(uiActionFlow = viewModel.uiAction)
        AnnotationsScreen(
            platform = viewModel.platform,
            state = state,
            onEvent = viewModel::onEvent,
        )
    }
}
