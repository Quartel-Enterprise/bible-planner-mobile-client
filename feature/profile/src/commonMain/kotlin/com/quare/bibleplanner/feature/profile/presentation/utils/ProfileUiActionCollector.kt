package com.quare.bibleplanner.feature.profile.presentation.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.no_progress_to_delete_message
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.toClipEntry
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun ProfileUiActionCollector(
    uiActionFlow: Flow<ProfileUiAction>,
    onNavigate: (NavKey) -> Unit,
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

            is ProfileUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(getString(action.message))

            is ProfileUiAction.Copy -> clipboard.setClipEntry(action.text.toClipEntry())

            is ProfileUiAction.GoToRoute -> onNavigate(action.route)
        }
    }
}
