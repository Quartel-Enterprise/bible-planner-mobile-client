package com.quare.bibleplanner.feature.bibleversion.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import bibleplanner.feature.preferences.bible_version.generated.resources.Res
import bibleplanner.feature.preferences.bible_version.generated.resources.update_available
import bibleplanner.feature.preferences.bible_version.generated.resources.update_available_description
import bibleplanner.feature.preferences.bible_version.generated.resources.update_available_title
import com.quare.bibleplanner.core.model.route.PendingBibleUpdatesNavRoute
import com.quare.bibleplanner.feature.bibleversion.presentation.component.PendingBibleUpdatesContent
import com.quare.bibleplanner.feature.bibleversion.presentation.model.PendingBibleUpdatesUiEvent
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.pendingBibleUpdates() {
    entry<PendingBibleUpdatesNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) {
        val viewModel = koinViewModel<PendingBibleUpdatesViewModel>()
        val pendingUpdates by viewModel.pendingUpdates.collectAsState()
        if (pendingUpdates.isNotEmpty()) {
            val title = if (pendingUpdates.size > 1) {
                stringResource(Res.string.update_available_title)
            } else {
                stringResource(Res.string.update_available)
            }
            ResponsiveDialogSheet(
                onCloseClick = { viewModel.onEvent(PendingBibleUpdatesUiEvent.OnDismissClick) },
                title = title,
                subtitle = stringResource(Res.string.update_available_description),
            ) {
                PendingBibleUpdatesContent(
                    pendingUpdates = pendingUpdates,
                    onEvent = viewModel::onEvent,
                )
            }
        }
    }
}
