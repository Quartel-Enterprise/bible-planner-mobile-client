package com.quare.bibleplanner.feature.subscriptiondetails.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.SubscriptionDetailsNavRoute

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.subscriptionDetails(onNavigateBack: () -> Unit) {
    entry<SubscriptionDetailsNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) {
        SubscriptionDetailsDialog(onDismiss = onNavigateBack)
    }
}
