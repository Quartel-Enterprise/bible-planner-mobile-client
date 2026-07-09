package com.quare.bibleplanner.feature.addnotesfreewarning.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun AddNotesFreeWarningUiActionCollector(
    uiActionFlow: Flow<AddNotesFreeWarningUiAction>,
    onNavigateBack: () -> Unit,
    onNavigateReplacingTop: (NavKey) -> Unit,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            AddNotesFreeWarningUiAction.NavigateBack -> {
                onNavigateBack()
            }

            AddNotesFreeWarningUiAction.NavigateToPro -> {
                onNavigateReplacingTop(PaywallNavRoute)
            }
        }
    }
}
