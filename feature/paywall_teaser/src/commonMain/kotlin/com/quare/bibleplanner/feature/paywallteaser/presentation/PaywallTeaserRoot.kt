package com.quare.bibleplanner.feature.paywallteaser.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.feature.paywallteaser.presentation.model.PaywallTeaserUiEvent
import com.quare.bibleplanner.feature.paywallteaser.presentation.viewmodel.PaywallTeaserViewModel
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.paywallTeaser() {
    entry<PaywallTeaserNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) { route ->
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
