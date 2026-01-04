package com.quare.bibleplanner.feature.addnotesfreewarning.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun AddNotesFreeWarningUiActionCollector(
    maxFreeNotesAmount: Int,
    uiActionFlow: Flow<AddNotesFreeWarningUiAction>,
    navController: NavHostController,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            AddNotesFreeWarningUiAction.NavigateBack -> {
                navController.navigateUp()
            }

            AddNotesFreeWarningUiAction.NavigateToPremium -> {
                navController.navigate(PaywallNavRoute) {
                    popUpTo(AddNotesFreeWarningNavRoute(maxFreeNotesAmount)) {
                        inclusive = true
                    }
                }
            }
        }
    }
}
