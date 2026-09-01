package com.quare.bibleplanner.feature.paywall.presentation.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun PaywallUiActionCollector(
    actionsFlow: Flow<PaywallUiAction>,
    snackbarHostState: SnackbarHostState,
) {
    ActionCollector(actionsFlow) { uiAction ->
        when (uiAction) {
            is PaywallUiAction.ShowSnackbar -> {
                val message = getString(uiAction.message, *uiAction.args.toTypedArray())
                snackbarHostState.showSnackbar(message)
            }
        }
    }
}
