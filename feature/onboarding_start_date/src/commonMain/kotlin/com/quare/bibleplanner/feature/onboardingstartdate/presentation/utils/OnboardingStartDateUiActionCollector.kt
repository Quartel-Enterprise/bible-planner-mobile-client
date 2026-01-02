package com.quare.bibleplanner.feature.onboardingstartdate.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
import com.quare.bibleplanner.core.model.route.OnboardingStartDateNavRoute
import com.quare.bibleplanner.feature.onboardingstartdate.presentation.model.OnboardingStartDateUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun OnboardingStartDateUiActionCollector(
    uiActionFlow: Flow<OnboardingStartDateUiAction>,
    navController: NavHostController,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            OnboardingStartDateUiAction.DISMISS -> {
                navController.navigateUp()
            }

            OnboardingStartDateUiAction.NAVIGATE_TO_SET_DATE -> {
                navController.navigate(EditPlanStartDateNavRoute) {
                    popUpTo(OnboardingStartDateNavRoute) { inclusive = true }
                }
            }
        }
    }
}
