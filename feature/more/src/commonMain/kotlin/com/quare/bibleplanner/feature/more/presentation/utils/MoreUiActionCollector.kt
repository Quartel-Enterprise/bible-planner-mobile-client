package com.quare.bibleplanner.feature.more.presentation.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.no_progress_to_delete_message
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.toClipEntry
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun MoreUiActionCollector(
    uiActionFlow: Flow<MoreUiAction>,
    onNavigate: (NavKey) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboard.current
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            is MoreUiAction.OpenLink -> uriHandler.openUri(action.url)

            MoreUiAction.ShowNoProgressToDelete -> snackbarHostState.showSnackbar(
                getString(Res.string.no_progress_to_delete_message),
            )

            is MoreUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(getString(action.message))

            is MoreUiAction.Copy -> clipboard.setClipEntry(action.text.toClipEntry())

            is MoreUiAction.GoToRoute -> onNavigate(action.route)
        }
    }
}
