package com.quare.bibleplanner.feature.deletenotes.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.feature.deletenotes.presentation.utils.DeleteNotesUiActionCollector
import com.quare.bibleplanner.feature.deletenotes.presentation.viewmodel.DeleteNotesViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.deleteNotes(onNavigateBack: () -> Unit) {
    entry<DeleteNotesRoute>(metadata = DialogSceneStrategy.dialog()) { route ->
        val viewModel = koinViewModel<DeleteNotesViewModel> { parametersOf(route) }
        DeleteNotesUiActionCollector(
            uiActionFlow = viewModel.backUiAction,
            onNavigateBack = onNavigateBack,
        )
        DeleteNotesDialog(onEvent = viewModel::onEvent)
    }
}
