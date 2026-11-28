package com.quare.bibleplanner.android.feature.deletehighlightcolor

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.DeleteHighlightColorNavRoute
import com.quare.bibleplanner.feature.read.presentation.deletecolor.DeleteHighlightColorUiAction
import com.quare.bibleplanner.feature.read.presentation.deletecolor.DeleteHighlightColorViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun DeleteHighlightColorNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(),
) {
    val viewModel = koinViewModel<DeleteHighlightColorViewModel> { parametersOf(this) }
    val appSnackbarController = koinInject<AppSnackbarController>()
    val context = LocalContext.current
    ActionCollector(viewModel.uiAction) { uiAction ->
        when (uiAction) {
            is DeleteHighlightColorUiAction.NotifyDeletion -> appSnackbarController.show(
                context.createDeletionMessage(hasKeptHighlights = uiAction.hasKeptHighlights),
            )
        }
    }
    ExpressiveTheme {
        DeleteHighlightColorContent(
            color = viewModel.color,
            onEvent = viewModel::onEvent,
        )
    }
}

private fun Context.createDeletionMessage(hasKeptHighlights: Boolean): AppSnackbarMessage = AppSnackbarMessage.Text(
    text = getString(
        if (hasKeptHighlights) {
            R.string.delete_highlight_color_removed_kept
        } else {
            R.string.delete_highlight_color_removed
        },
    ),
    isDismissible = true,
)
