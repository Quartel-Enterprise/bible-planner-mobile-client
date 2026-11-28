package com.quare.bibleplanner.android.feature.editplanstartdate

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.dialog.HideSceneDialogScrimEffect
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
import com.quare.bibleplanner.feature.editplanstartdate.presentation.model.EditPlanStartDateUiState
import com.quare.bibleplanner.feature.editplanstartdate.presentation.viewmodel.EditPlanStartDateViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EditPlanStartDateNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(),
) {
    val viewModel = koinViewModel<EditPlanStartDateViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val state = uiState
    // Why: the scene window shows while the state loads, and its scrim must not flash before the picker's.
    HideSceneDialogScrimEffect()
    if (state is EditPlanStartDateUiState.Loaded) {
        ExpressiveTheme {
            EditPlanStartDateContent(
                state = state,
                onEvent = viewModel::onEvent,
            )
        }
    }
}
