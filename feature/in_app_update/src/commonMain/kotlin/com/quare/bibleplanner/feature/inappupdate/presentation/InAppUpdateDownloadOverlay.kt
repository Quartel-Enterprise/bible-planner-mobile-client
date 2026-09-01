package com.quare.bibleplanner.feature.inappupdate.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import bibleplanner.feature.in_app_update.generated.resources.Res
import bibleplanner.feature.in_app_update.generated.resources.update_download_failed
import com.quare.bibleplanner.feature.inappupdate.presentation.content.UpdateProgressSnackbar
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateDownloadUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InAppUpdateDownloadOverlay(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<InAppUpdateDownloadViewModel>()
    val appSnackbarController = koinInject<AppSnackbarController>()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            InAppUpdateDownloadUiAction.ShowDownloadFailed -> appSnackbarController.show(
                AppSnackbarMessage(
                    stringResource = Res.string.update_download_failed,
                    isDismissible = true,
                ),
            )
        }
    }

    downloadProgress?.let { progress ->
        UpdateProgressSnackbar(
            progress = progress,
            modifier = modifier,
        )
    }
}
