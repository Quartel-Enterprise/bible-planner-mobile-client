package com.quare.bibleplanner.feature.accountdetails.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun AccountDetailsUiActionCollector(
    uiActionFlow: Flow<AccountDetailsUiAction>,
    onNavigateReplacingTop: (NavKey) -> Unit,
    onNavigate: (NavKey) -> Unit,
) {
    val snackbarHostState = LocalSnackbarHostState.current
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            is AccountDetailsUiAction.NavigateToRoute -> onNavigate(action.route)

            is AccountDetailsUiAction.ReplaceWithRoute -> onNavigateReplacingTop(action.route)

            is AccountDetailsUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                getString(action.message),
                withDismissAction = true,
            )
        }
    }
}
