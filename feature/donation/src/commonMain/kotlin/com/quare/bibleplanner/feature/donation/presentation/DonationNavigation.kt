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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.DonationNavRoute
import com.quare.bibleplanner.core.model.route.PixQrNavRoute
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.toClipEntry
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.donation(navController: NavHostController) {
    dialog<DonationNavRoute> {
        val viewModel = koinViewModel<DonationViewModel>()
        val state by viewModel.uiState.collectAsState()

        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        DonationActionCollector(
            sheetState = sheetState,
            navController = navController,
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
    navController: NavHostController,
    flow: Flow<DonationUiAction>,
) {
    val clipboardManager = LocalClipboard.current
    val uriHandler = LocalUriHandler.current
    ActionCollector(flow) { action ->
        when (action) {
            DonationUiAction.NavigateBack -> {
                navController.navigateUp()
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
                navController.navigate(PixQrNavRoute)
            }
        }
    }
}
