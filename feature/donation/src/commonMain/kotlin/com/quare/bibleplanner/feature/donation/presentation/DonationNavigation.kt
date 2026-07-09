package com.quare.bibleplanner.feature.donation.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.DonationNavRoute
import com.quare.bibleplanner.core.model.route.PixQrNavRoute
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.toClipEntry
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.donation(
    onNavigate: (Any) -> Unit,
    onNavigateBack: () -> Unit,
) {
    entry<DonationNavRoute>(metadata = DialogSceneStrategy.dialog()) {
        val viewModel = koinViewModel<DonationViewModel>()
        val state by viewModel.uiState.collectAsState()

        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        DonationActionCollector(
            sheetState = sheetState,
            onNavigate = onNavigate,
            onNavigateBack = onNavigateBack,
            flow = viewModel.uiAction,
        )

        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(DonationUiEvent.Dismiss) },
            sheetState = sheetState,
        ) {
            DonationBottomSheetContent(
                state = state,
                onEvent = viewModel::onEvent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DonationActionCollector(
    sheetState: SheetState,
    onNavigate: (Any) -> Unit,
    onNavigateBack: () -> Unit,
    flow: Flow<DonationUiAction>,
) {
    val clipboardManager = LocalClipboard.current
    val uriHandler = LocalUriHandler.current
    ActionCollector(flow) { action ->
        when (action) {
            DonationUiAction.NavigateBack -> {
                onNavigateBack()
            }

            is DonationUiAction.Copy -> {
                clipboardManager.setClipEntry(
                    clipEntry = action.text.toClipEntry(),
                )
            }

            is DonationUiAction.OpenUrl -> {
                uriHandler.openUri(action.url)
            }

            DonationUiAction.Close -> {
                sheetState.hide()
            }

            DonationUiAction.NavigateToPixQr -> {
                onNavigate(PixQrNavRoute)
            }
        }
    }
}
