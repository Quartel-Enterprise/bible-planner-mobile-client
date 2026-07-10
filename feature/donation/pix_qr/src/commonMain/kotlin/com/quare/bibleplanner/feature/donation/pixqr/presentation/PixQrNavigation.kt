package com.quare.bibleplanner.feature.donation.pixqr.presentation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import bibleplanner.feature.donation.pix_qr.generated.resources.Res
import com.quare.bibleplanner.core.model.route.PixQrNavRoute
import com.quare.bibleplanner.core.utils.shareContent
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.pixQr(onNavigateBack: () -> Unit) {
    entry<PixQrNavRoute>(metadata = DialogSceneStrategy.dialog()) {
        val viewModel = koinViewModel<PixQrViewModel>()

        PixQrActionCollector(
            onNavigateBack = onNavigateBack,
            flow = viewModel.uiAction,
        )

        PixQrDialog(
            onEvent = viewModel::onEvent,
        )
    }
}

@Composable
private fun PixQrActionCollector(
    onNavigateBack: () -> Unit,
    flow: Flow<PixQrUiAction>,
) {
    ActionCollector(flow) { action ->
        when (action) {
            PixQrUiAction.NavigateBack -> onNavigateBack()

            is PixQrUiAction.ShareQrCode -> {
                val bytes = Res.readBytes("drawable/qr_code_pix_share.png")
                shareContent(action.message, bytes)
            }
        }
    }
}
