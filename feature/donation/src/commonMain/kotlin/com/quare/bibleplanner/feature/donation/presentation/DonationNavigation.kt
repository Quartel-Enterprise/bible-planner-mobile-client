package com.quare.bibleplanner.feature.donation.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.DonationNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard
import com.quare.bibleplanner.ui.utils.toClipEntry
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.donation() {
    entry<DonationNavRoute>(metadata = getSheetPane()) {
        val viewModel = koinViewModel<DonationViewModel>()
        val state by viewModel.uiState.collectAsState()

        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val sheetCloseGuard = rememberSheetCloseGuard()
        val onEvent: (DonationUiEvent) -> Unit = { event ->
            if (event == DonationUiEvent.Dismiss) {
                sheetCloseGuard.close { viewModel.onEvent(event) }
            } else {
                viewModel.onEvent(event)
            }
        }
        SheetExitAnimationEffect(
            animateOut = sheetState::hide,
            animateBackIn = sheetState::show,
        )

        DonationActionCollector(flow = viewModel.uiAction)

        ModalBottomSheet(
            onDismissRequest = { onEvent(DonationUiEvent.Dismiss) },
            sheetState = sheetState,
            sheetGesturesEnabled = !sheetCloseGuard.isClosing,
            modifier = Modifier.blockPointerInput(isBlocked = sheetCloseGuard.isClosing),
        ) {
            DonationBottomSheetContent(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DonationActionCollector(flow: Flow<DonationUiAction>) {
    val clipboardManager = LocalClipboard.current
    val uriHandler = LocalUriHandler.current
    ActionCollector(flow) { action ->
        when (action) {
            is DonationUiAction.Copy -> {
                clipboardManager.setClipEntry(
                    clipEntry = action.text.toClipEntry(),
                )
            }

            is DonationUiAction.OpenUrl -> uriHandler.openUri(action.url)
        }
    }
}
