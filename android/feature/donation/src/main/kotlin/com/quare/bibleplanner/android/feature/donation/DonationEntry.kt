package com.quare.bibleplanner.android.feature.donation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.DonationNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.donation.presentation.DonationUiAction
import com.quare.bibleplanner.feature.donation.presentation.DonationViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.toClipEntry
import org.koin.compose.viewmodel.koinViewModel

internal fun DonationNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<DonationViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val clipboard = LocalClipboard.current
    val uriHandler = LocalUriHandler.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is DonationUiAction.Copy -> clipboard.setClipEntry(clipEntry = action.text.toClipEntry())
            is DonationUiAction.OpenUrl -> uriHandler.openUri(action.url)
        }
    }
    ExpressiveTheme {
        DonationContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
