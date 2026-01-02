package com.quare.bibleplanner.feature.onboardingstartdate.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
import com.quare.bibleplanner.core.model.route.OnboardingStartDateNavRoute
import com.quare.bibleplanner.feature.onboardingstartdate.presentation.component.OnboardingStartBottomSheet
import com.quare.bibleplanner.feature.onboardingstartdate.presentation.model.OnboardingStartDateUiAction
import com.quare.bibleplanner.feature.onboardingstartdate.presentation.utils.OnboardingStartDateUiActionCollector
import com.quare.bibleplanner.feature.onboardingstartdate.presentation.viewmodel.OnboardingStartDateViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.onboardingStartDate(navController: NavHostController) {
    dialog<OnboardingStartDateNavRoute> {
        val viewModel: OnboardingStartDateViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val onEvent = viewModel::onEvent

        OnboardingStartDateUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            navController = navController,
        )

        OnboardingStartBottomSheet(
            isDontShowAgainMarked = uiState.isDontShowAgainMarked,
            onEvent = onEvent,
        )
    }
}
