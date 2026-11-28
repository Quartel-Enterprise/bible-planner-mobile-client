package com.quare.bibleplanner.android.feature.addnotesfreewarning

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.viewmodel.AddNotesFreeWarningViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun AddNotesFreeWarningNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(),
) {
    val viewModel = koinViewModel<AddNotesFreeWarningViewModel> { parametersOf(this) }
    ExpressiveTheme {
        AddNotesFreeWarningContent(
            maxFreeNotesAmount = viewModel.maxFreeNotesAmount,
            type = viewModel.type,
            onEvent = viewModel::onEvent,
        )
    }
}
