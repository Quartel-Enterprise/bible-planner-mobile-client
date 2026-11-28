package com.quare.bibleplanner.feature.read.presentation.utils

import androidx.compose.runtime.Composable
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.highlight_color_removed
import bibleplanner.feature.read.generated.resources.highlight_color_removed_kept
import com.quare.bibleplanner.feature.read.presentation.deletecolor.DeleteHighlightColorUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject

@Composable
internal fun DeleteHighlightColorUiActionCollector(uiActionFlow: Flow<DeleteHighlightColorUiAction>) {
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            is DeleteHighlightColorUiAction.NotifyDeletion -> {
                appSnackbarController.show(
                    AppSnackbarMessage.Resource(
                        stringResource = if (uiAction.hasKeptHighlights) {
                            Res.string.highlight_color_removed_kept
                        } else {
                            Res.string.highlight_color_removed
                        },
                        isDismissible = true,
                    ),
                )
            }
        }
    }
}
