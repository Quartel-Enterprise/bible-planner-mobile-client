package com.quare.bibleplanner.feature.verse.annotations.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject

@Composable
internal fun AnnotationsUiActionCollector(uiActionFlow: Flow<AnnotationsUiAction>) {
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            is AnnotationsUiAction.ShowMessage -> appSnackbarController.show(
                AppSnackbarMessage(
                    stringResource = uiAction.stringResource,
                    isDismissible = true,
                ),
            )
        }
    }
}
