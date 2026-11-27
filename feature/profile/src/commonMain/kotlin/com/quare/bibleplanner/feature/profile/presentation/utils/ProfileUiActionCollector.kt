package com.quare.bibleplanner.feature.profile.presentation.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.delete_account_requires_internet
import bibleplanner.feature.profile.generated.resources.login_requires_internet
import bibleplanner.feature.profile.generated.resources.logout_requires_internet
import bibleplanner.feature.profile.generated.resources.no_progress_to_delete_message
import bibleplanner.feature.profile.generated.resources.up_to_date_message
import bibleplanner.feature.profile.generated.resources.update_check_failed_message
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileMessage
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.toClipEntry
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

@Composable
internal fun ProfileUiActionCollector(
    uiActionFlow: Flow<ProfileUiAction>,
    snackbarHostState: SnackbarHostState,
) {
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboard.current
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            is ProfileUiAction.OpenLink -> uriHandler.openUri(action.url)

            ProfileUiAction.ShowNoProgressToDelete -> snackbarHostState.showSnackbar(
                getString(Res.string.no_progress_to_delete_message),
            )

            is ProfileUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                getString(action.message.toStringResource()),
            )

            is ProfileUiAction.Copy -> clipboard.setClipEntry(action.text.toClipEntry())
        }
    }
}

private fun ProfileMessage.toStringResource(): StringResource = when (this) {
    ProfileMessage.DELETE_ACCOUNT_REQUIRES_INTERNET -> Res.string.delete_account_requires_internet
    ProfileMessage.LOGIN_REQUIRES_INTERNET -> Res.string.login_requires_internet
    ProfileMessage.LOGOUT_REQUIRES_INTERNET -> Res.string.logout_requires_internet
    ProfileMessage.UP_TO_DATE -> Res.string.up_to_date_message
    ProfileMessage.UPDATE_CHECK_FAILED -> Res.string.update_check_failed_message
}
