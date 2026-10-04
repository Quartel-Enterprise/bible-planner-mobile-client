package com.quare.bibleplanner.feature.subscriptiondetails.presentation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.SubscriptionDetailsNavRoute
import org.koin.compose.koinInject

fun EntryProviderScope<NavKey>.subscriptionDetails() {
    entry<SubscriptionDetailsNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) {
        val navigator = koinInject<Navigator>()
        SubscriptionDetailsDialog(onDismiss = navigator::navigateBack)
    }
}
