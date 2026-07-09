package com.quare.bibleplanner.feature.contactsupport.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.ContactSupportNavRoute
import com.quare.bibleplanner.feature.contactsupport.presentation.content.ContactSupportDialogContent
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiEvent
import com.quare.bibleplanner.feature.contactsupport.presentation.utils.ContactSupportUiActionCollector
import com.quare.bibleplanner.feature.contactsupport.presentation.viewmodel.ContactSupportViewModel
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.contactSupport(onNavigateBack: () -> Unit) {
    entry<ContactSupportNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) {
        val viewModel = koinViewModel<ContactSupportViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val onEvent = viewModel::onEvent
        val snackbarHostState = remember { SnackbarHostState() }
        ContactSupportUiActionCollector(
            actionsFlow = viewModel.uiAction,
            onNavigateBack = onNavigateBack,
            snackbarHostState = snackbarHostState,
        )
        ResponsiveDialogSheet(
            onCloseClick = { onEvent(ContactSupportUiEvent.OnDismiss) },
            skipPartiallyExpanded = true,
        ) {
            Box {
                ContactSupportDialogContent(state = uiState, onEvent = onEvent)
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}
