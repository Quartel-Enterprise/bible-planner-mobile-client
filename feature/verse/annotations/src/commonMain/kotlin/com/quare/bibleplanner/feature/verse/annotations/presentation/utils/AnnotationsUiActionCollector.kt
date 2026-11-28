package com.quare.bibleplanner.feature.verse.annotations.presentation.utils

import androidx.compose.runtime.Composable
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.annotation_removed
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
            AnnotationsUiAction.NotifyAnnotationRemoved -> appSnackbarController.show(
                AppSnackbarMessage.Resource(
                    stringResource = Res.string.annotation_removed,
                    isDismissible = true,
                ),
            )
        }
    }
}
