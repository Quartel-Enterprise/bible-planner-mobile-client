package com.quare.bibleplanner.feature.accountdetails.presentation.utils

import androidx.compose.runtime.Composable
import bibleplanner.feature.account_details.generated.resources.Res
import bibleplanner.feature.account_details.generated.resources.account_details_action_error
import bibleplanner.feature.account_details.generated.resources.account_details_device_signed_out
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsMessage
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

@Composable
internal fun AccountDetailsUiActionCollector(uiActionFlow: Flow<AccountDetailsUiAction>) {
    val snackbarHostState = LocalSnackbarHostState.current
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            is AccountDetailsUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                getString(action.message.toStringResource()),
                withDismissAction = true,
            )
        }
    }
}

private fun AccountDetailsMessage.toStringResource(): StringResource = when (this) {
    AccountDetailsMessage.DEVICE_SIGNED_OUT -> Res.string.account_details_device_signed_out
    AccountDetailsMessage.ACTION_ERROR -> Res.string.account_details_action_error
}
