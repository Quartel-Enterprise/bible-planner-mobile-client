package com.quare.bibleplanner.feature.donation.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import bibleplanner.feature.donation.generated.resources.Res
import bibleplanner.feature.donation.generated.resources.donation_copied
import com.quare.bibleplanner.core.model.route.DonationNavRoute
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.MainScaffoldState
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.donation(
    navController: NavHostController,
    mainScaffoldState: MainScaffoldState,
) {
    dialog<DonationNavRoute> {
        val viewModel = koinViewModel<DonationViewModel>()
        val state by viewModel.uiState.collectAsState()

        val clipboardManager = LocalClipboardManager.current
        val uriHandler = LocalUriHandler.current

        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        DonationActionCollector(
            sheetState = sheetState,
            navController = navController,
            mainScaffoldState = mainScaffoldState,
            flow = viewModel.uiAction,
            onCopy = { text ->
                clipboardManager.setText(AnnotatedString(text))
            },
            onOpenUrl = { url ->
                uriHandler.openUri(url)
            },
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
    mainScaffoldState: MainScaffoldState,
    flow: Flow<DonationUiAction>,
    onCopy: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    ActionCollector(flow) { action ->
        when (action) {
            DonationUiAction.NavigateBack -> {
                navController.navigateUp()
            }

            is DonationUiAction.Copy -> {
                onCopy(action.text)
                mainScaffoldState.snackbarHostState.showSnackbar(getString(Res.string.donation_copied))
            }

            is DonationUiAction.OpenUrl -> {
                onOpenUrl(action.url)
            }

            DonationUiAction.Close -> {
                sheetState.hide()
            }

            DonationUiAction.NavigateToPixQr -> {
                navController.navigate(com.quare.bibleplanner.core.model.route.PixQrNavRoute)
            }
        }
    }
}
