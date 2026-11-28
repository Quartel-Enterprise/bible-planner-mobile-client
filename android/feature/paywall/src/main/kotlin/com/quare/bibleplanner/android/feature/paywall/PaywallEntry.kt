package com.quare.bibleplanner.android.feature.paywall

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiAction
import com.quare.bibleplanner.feature.paywall.presentation.viewmodel.PaywallViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun PaywallNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(key = this) {
    val viewModel = koinViewModel<PaywallViewModel> { parametersOf(this) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    ActionCollector(viewModel.uiAction) { uiAction ->
        when (uiAction) {
            is PaywallUiAction.ShowError -> {
                snackbarHostState.showSnackbar(
                    context.getString(uiAction.error.toMessage(), uiAction.storeName),
                )
            }
        }
    }
    ExpressiveTheme {
        PaywallContent(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onEvent = viewModel::onEvent,
        )
    }
}
