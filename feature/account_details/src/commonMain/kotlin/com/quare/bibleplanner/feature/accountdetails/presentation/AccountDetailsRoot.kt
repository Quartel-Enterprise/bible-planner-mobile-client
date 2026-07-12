package com.quare.bibleplanner.feature.accountdetails.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.AccountDetailsNavRoute

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.accountDetails(
    onNavigateBack: () -> Unit,
    onNavigateReplacingTop: (NavKey) -> Unit,
    onNavigate: (NavKey) -> Unit,
) {
    entry<AccountDetailsNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) {
        AccountDetailsSheet(
            onNavigateBack = onNavigateBack,
            onNavigateReplacingTop = onNavigateReplacingTop,
            onNavigate = onNavigate,
        )
    }
}
