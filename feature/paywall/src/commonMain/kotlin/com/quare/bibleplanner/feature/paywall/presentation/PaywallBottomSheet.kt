package com.quare.bibleplanner.feature.paywall.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.paywall.presentation.component.PaywallItemsComponent
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallBottomSheet(
    uiState: PaywallUiState,
    onEvent: (PaywallUiEvent) -> Unit,
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = { onEvent(PaywallUiEvent.OnDismiss) }
    ) {
        PaywallItemsComponent(
            uiState = uiState,
            onEvent = onEvent,
        )
    }
}
