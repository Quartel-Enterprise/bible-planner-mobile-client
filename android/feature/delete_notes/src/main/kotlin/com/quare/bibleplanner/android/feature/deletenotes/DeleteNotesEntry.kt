package com.quare.bibleplanner.android.feature.deletenotes

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.feature.deletenotes.presentation.viewmodel.DeleteNotesViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun DeleteNotesRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(),
) {
    val viewModel = koinViewModel<DeleteNotesViewModel> { parametersOf(this) }
    ExpressiveTheme {
        DeleteNotesContent(onEvent = viewModel::onEvent)
    }
}
