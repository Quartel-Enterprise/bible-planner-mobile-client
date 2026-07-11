package com.quare.bibleplanner.feature.inappupdate.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.UpdateDownloadedNavRoute
import com.quare.bibleplanner.feature.inappupdate.presentation.content.UpdateDownloadedContent
import com.quare.bibleplanner.feature.inappupdate.presentation.model.UpdateDownloadedUiEvent
import com.quare.bibleplanner.feature.inappupdate.presentation.utils.UpdateDownloadedUiActionCollector
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.updateDownloaded(onNavigateBack: () -> Unit) {
    entry<UpdateDownloadedNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) {
        val viewModel = koinViewModel<UpdateDownloadedViewModel>()
        UpdateDownloadedUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            onNavigateBack = onNavigateBack,
        )
        ResponsiveDialogSheet(
            onCloseClick = { viewModel.onEvent(UpdateDownloadedUiEvent.OnLaterClick) },
        ) {
            UpdateDownloadedContent(onEvent = viewModel::onEvent)
        }
    }
}
