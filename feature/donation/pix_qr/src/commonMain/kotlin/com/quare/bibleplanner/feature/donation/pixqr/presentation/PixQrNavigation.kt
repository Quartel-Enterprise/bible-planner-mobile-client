package com.quare.bibleplanner.feature.donation.pixqr.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import bibleplanner.feature.donation.pix_qr.generated.resources.Res
import com.quare.bibleplanner.core.model.route.PixQrNavRoute
import com.quare.bibleplanner.core.utils.shareContent
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.pixQr(navController: NavHostController) {
    dialog<PixQrNavRoute> {
        val viewModel = koinViewModel<PixQrViewModel>()

        PixQrActionCollector(
            navController = navController,
            flow = viewModel.uiAction,
        )

        PixQrDialog(
            onEvent = viewModel::onEvent,
        )
    }
}

@Composable
private fun PixQrActionCollector(
    navController: NavHostController,
    flow: Flow<PixQrUiAction>,
) {
    ActionCollector(flow) { action ->
        when (action) {
            PixQrUiAction.NavigateBack -> {
                navController.navigateUp()
            }

            is PixQrUiAction.ShareQrCode -> {
                val bytes = Res.readBytes("drawable/qr_code_pix_share.png")
                shareContent(action.message, bytes)
            }
        }
    }
}
