package com.quare.bibleplanner.feature.accountdetails.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun AccountDetailsUiActionCollector(uiActionFlow: Flow<AccountDetailsUiAction>) {
    val snackbarHostState = LocalSnackbarHostState.current
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            is AccountDetailsUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                getString(action.message),
                withDismissAction = true,
            )
        }
    }
}
