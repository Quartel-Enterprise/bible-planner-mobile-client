package com.quare.bibleplanner.feature.subscriptiondetails.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.SubscriptionDetailsNavRoute

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.subscriptionDetails(navController: NavHostController) {
    dialog<SubscriptionDetailsNavRoute>(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        SubscriptionDetailsDialog(onDismiss = { navController.popBackStack() })
    }
}
