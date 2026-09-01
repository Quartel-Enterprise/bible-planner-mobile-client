package com.quare.bibleplanner.feature.addnotesfreewarning.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.viewmodel.AddNotesFreeWarningViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.addNotesFreeWarning() {
    entry<AddNotesFreeWarningNavRoute>(metadata = DialogSceneStrategy.dialog()) { route ->
        val viewModel = koinViewModel<AddNotesFreeWarningViewModel> { parametersOf(route) }
        AddNotesFreeWarningDialog(
            maxFreeNotesAmount = viewModel.maxFreeNotesAmount,
            onEvent = viewModel::onEvent,
        )
    }
}
