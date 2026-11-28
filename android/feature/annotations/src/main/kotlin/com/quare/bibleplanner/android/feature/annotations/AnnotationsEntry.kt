package com.quare.bibleplanner.android.feature.annotations

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.AnnotationsNavRoute
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiAction
import com.quare.bibleplanner.feature.verse.annotations.presentation.viewmodel.AnnotationsViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

internal fun AnnotationsNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(key = this) {
    val viewModel = koinViewModel<AnnotationsViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val appSnackbarController = koinInject<AppSnackbarController>()
    val context = LocalContext.current
    ActionCollector(viewModel.uiAction) { uiAction ->
        when (uiAction) {
            AnnotationsUiAction.NotifyAnnotationRemoved -> appSnackbarController.show(context.createRemovedMessage())
        }
    }
    ExpressiveTheme {
        AnnotationsContent(
            state = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}

private fun Context.createRemovedMessage(): AppSnackbarMessage = AppSnackbarMessage.Text(
    text = getString(R.string.annotations_annotation_removed),
    isDismissible = true,
)
