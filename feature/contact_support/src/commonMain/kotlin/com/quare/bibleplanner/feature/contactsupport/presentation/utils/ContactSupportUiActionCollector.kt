package com.quare.bibleplanner.feature.contactsupport.presentation.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.toClipEntry
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun ContactSupportUiActionCollector(
    actionsFlow: Flow<ContactSupportUiAction>,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboard.current
    ActionCollector(actionsFlow) { action ->
        when (action) {
            ContactSupportUiAction.NavigateBack -> onNavigateBack()
            is ContactSupportUiAction.OpenLink -> uriHandler.openUri(action.url)
            is ContactSupportUiAction.Copy -> clipboard.setClipEntry(action.text.toClipEntry())
            is ContactSupportUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(getString(action.message))
        }
    }
}
