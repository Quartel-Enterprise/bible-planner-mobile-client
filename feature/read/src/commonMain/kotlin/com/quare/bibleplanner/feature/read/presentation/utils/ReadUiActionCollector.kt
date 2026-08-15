package com.quare.bibleplanner.feature.read.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboard
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import com.quare.bibleplanner.ui.utils.toClipEntry
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject

@Composable
internal fun ReadUiActionCollector(
    uiActionFlow: Flow<ReadUiAction>,
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateReplacingTop: (NavKey) -> Unit,
) {
    val clipboard = LocalClipboard.current
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            ReadUiAction.NavigateBack -> onNavigateBack()

            is ReadUiAction.NavigateToRoute -> {
                if (uiAction.replace) {
                    onNavigateReplacingTop(uiAction.route)
                } else {
                    onNavigate(uiAction.route)
                }
            }

            is ReadUiAction.CopyToClipboard -> clipboard.setClipEntry(uiAction.text.toClipEntry())

            is ReadUiAction.ShowMessage -> {
                appSnackbarController.show(
                    AppSnackbarMessage(
                        stringResource = uiAction.stringResource,
                        isDismissible = true,
                    ),
                )
            }
        }
    }
}
