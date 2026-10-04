package com.quare.bibleplanner.feature.addnotesfreewarning.presentation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.viewmodel.AddNotesFreeWarningViewModel
import com.quare.bibleplanner.ui.component.dialog.toNativeAlertDialogProperties
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.addNotesFreeWarning() {
    entry<AddNotesFreeWarningNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties().toNativeAlertDialogProperties()),
    ) { route ->
        val viewModel = koinViewModel<AddNotesFreeWarningViewModel> { parametersOf(route) }
        AddNotesFreeWarningDialog(
            maxFreeNotesAmount = viewModel.maxFreeNotesAmount,
            type = viewModel.type,
            onEvent = viewModel::onEvent,
        )
    }
}
