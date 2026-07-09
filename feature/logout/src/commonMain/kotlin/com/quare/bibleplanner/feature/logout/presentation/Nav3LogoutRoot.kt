package com.quare.bibleplanner.feature.logout.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.LogoutNavRoute

fun EntryProviderScope<NavKey>.logout(
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    entry<LogoutNavRoute>(
        metadata = DialogSceneStrategy.dialog(
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        ),
    ) {
        LogoutRootContent(
            onNavigateBack = onNavigateBack,
            snackbarHostState = snackbarHostState,
        )
    }
}
