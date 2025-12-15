package com.quare.bibleplanner.feature.day.presentation.util

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun DayUiActionCollector(
    uiActionFlow: Flow<Unit>,
    navController: NavController,
) {
    ActionCollector(uiActionFlow) {
        navController.navigateUp()
    }
}
