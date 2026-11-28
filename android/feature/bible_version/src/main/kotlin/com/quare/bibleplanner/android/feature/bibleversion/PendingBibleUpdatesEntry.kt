package com.quare.bibleplanner.android.feature.bibleversion

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.PendingBibleUpdatesNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.bibleversion.presentation.PendingBibleUpdatesViewModel
import org.koin.compose.viewmodel.koinViewModel

internal fun PendingBibleUpdatesNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<PendingBibleUpdatesViewModel>()
    val pendingUpdates by viewModel.pendingUpdates.collectAsState()
    // Why: the updates load asynchronously, and the sheet only opens once there is something to show.
    if (pendingUpdates.isNotEmpty()) {
        ExpressiveTheme {
            PendingBibleUpdatesContent(
                pendingUpdates = pendingUpdates,
                onEvent = viewModel::onEvent,
            )
        }
    }
}
