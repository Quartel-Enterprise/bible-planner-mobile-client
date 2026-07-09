package com.quare.bibleplanner.feature.congrats.presentation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.CongratsNavRoute
import com.quare.bibleplanner.feature.congrats.presentation.model.CongratsUiAction
import com.quare.bibleplanner.feature.congrats.presentation.viewmodel.CongratsViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.congrats(onNavigateBack: () -> Unit) {
    entry<CongratsNavRoute>(metadata = DialogSceneStrategy.dialog()) {
        val viewModel: CongratsViewModel = koinViewModel()
        val onEvent = viewModel::onEvent

        CongratsActionCollector(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
        )

        CongratsBottomSheet(
            onEvent = onEvent,
        )
    }
}

@Composable
private fun CongratsActionCollector(
    viewModel: CongratsViewModel,
    onNavigateBack: () -> Unit,
) {
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            CongratsUiAction.Close -> onNavigateBack()
        }
    }
}
