package com.quare.bibleplanner.feature.inappupdate.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.UpdateDownloadedNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.inappupdate.presentation.content.UpdateDownloadedContent
import com.quare.bibleplanner.feature.inappupdate.presentation.model.UpdateDownloadedUiEvent
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.updateDownloaded() {
    entry<UpdateDownloadedNavRoute>(metadata = getSheetPane()) {
        val viewModel = koinViewModel<UpdateDownloadedViewModel>()
        ResponsiveDialogSheet(
            onCloseClick = { viewModel.onEvent(UpdateDownloadedUiEvent.OnLaterClick) },
        ) {
            UpdateDownloadedContent(onEvent = viewModel::onEvent)
        }
    }
}
