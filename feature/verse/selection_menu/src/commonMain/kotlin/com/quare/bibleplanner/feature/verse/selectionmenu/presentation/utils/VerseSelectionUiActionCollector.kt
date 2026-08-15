package com.quare.bibleplanner.feature.verse.selectionmenu.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboard
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import com.quare.bibleplanner.ui.utils.toClipEntry
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject

@Composable
internal fun VerseSelectionUiActionCollector(
    uiActionFlow: Flow<VerseSelectionUiAction>,
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            VerseSelectionUiAction.NavigateBack -> onNavigateBack()

            is VerseSelectionUiAction.NavigateToRoute -> onNavigate(uiAction.route)

            is VerseSelectionUiAction.CopyToClipboard -> clipboard.setClipEntry(uiAction.text.toClipEntry())

            is VerseSelectionUiAction.ShowMessage -> {
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
