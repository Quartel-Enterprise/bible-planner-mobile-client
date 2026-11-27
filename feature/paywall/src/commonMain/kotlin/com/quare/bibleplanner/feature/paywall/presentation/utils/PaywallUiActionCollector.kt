package com.quare.bibleplanner.feature.paywall.presentation.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.error_browser_checkout_not_confirmed
import bibleplanner.feature.paywall.generated.resources.error_network
import bibleplanner.feature.paywall.generated.resources.error_payment_pending
import bibleplanner.feature.paywall.generated.resources.error_purchase_cancelled
import bibleplanner.feature.paywall.generated.resources.error_restore_purchase
import bibleplanner.feature.paywall.generated.resources.error_unknown
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallError
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

@Composable
internal fun PaywallUiActionCollector(
    actionsFlow: Flow<PaywallUiAction>,
    snackbarHostState: SnackbarHostState,
) {
    ActionCollector(actionsFlow) { uiAction ->
        when (uiAction) {
            is PaywallUiAction.ShowError -> {
                snackbarHostState.showSnackbar(getString(uiAction.error.toStringResource(), uiAction.storeName))
            }
        }
    }
}

private fun PaywallError.toStringResource(): StringResource = when (this) {
    PaywallError.PURCHASE_CANCELLED -> Res.string.error_purchase_cancelled
    PaywallError.NETWORK -> Res.string.error_network
    PaywallError.PAYMENT_PENDING -> Res.string.error_payment_pending
    PaywallError.RESTORE_PURCHASE_FAILED -> Res.string.error_restore_purchase
    PaywallError.BROWSER_CHECKOUT_NOT_CONFIRMED -> Res.string.error_browser_checkout_not_confirmed
    PaywallError.UNKNOWN -> Res.string.error_unknown
}
