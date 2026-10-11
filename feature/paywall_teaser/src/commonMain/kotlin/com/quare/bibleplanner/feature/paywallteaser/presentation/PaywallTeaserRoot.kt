package com.quare.bibleplanner.feature.paywallteaser.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.paywallteaser.presentation.model.PaywallTeaserUiEvent
import com.quare.bibleplanner.feature.paywallteaser.presentation.viewmodel.PaywallTeaserViewModel
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.paywallTeaser() {
    entry<PaywallTeaserNavRoute>(metadata = getSheetPane()) { route ->
        val viewModel = koinViewModel<PaywallTeaserViewModel> { parametersOf(route) }
        ResponsiveDialogSheet(
            onCloseClick = { viewModel.onEvent(PaywallTeaserUiEvent.OnDismiss) },
        ) {
            PaywallTeaserSheet(
                reason = viewModel.reason,
                onEvent = viewModel::onEvent,
            )
        }
    }
}
