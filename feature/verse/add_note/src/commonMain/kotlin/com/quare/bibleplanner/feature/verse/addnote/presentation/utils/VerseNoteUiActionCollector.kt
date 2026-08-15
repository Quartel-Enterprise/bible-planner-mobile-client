package com.quare.bibleplanner.feature.verse.addnote.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.verse.addnote.presentation.model.VerseNoteUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject

@Composable
internal fun VerseNoteUiActionCollector(
    uiActionFlow: Flow<VerseNoteUiAction>,
    onNavigateBack: () -> Unit,
) {
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            VerseNoteUiAction.NavigateBack -> onNavigateBack()

            is VerseNoteUiAction.NavigateBackWithMessage -> {
                onNavigateBack()
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
