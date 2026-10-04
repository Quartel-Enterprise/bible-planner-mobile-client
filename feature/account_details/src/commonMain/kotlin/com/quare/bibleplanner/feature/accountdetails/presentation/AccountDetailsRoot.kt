package com.quare.bibleplanner.feature.accountdetails.presentation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.AccountDetailsNavRoute
import com.quare.bibleplanner.ui.component.dialog.toSheetDialogProperties

fun EntryProviderScope<NavKey>.accountDetails() {
    entry<AccountDetailsNavRoute>(
        metadata = DialogSceneStrategy.dialog(
            DialogProperties(usePlatformDefaultWidth = false).toSheetDialogProperties(),
        ),
    ) {
        AccountDetailsSheet()
    }
}
