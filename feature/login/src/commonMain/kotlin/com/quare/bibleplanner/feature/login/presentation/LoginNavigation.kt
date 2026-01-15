package com.quare.bibleplanner.feature.login.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.loginRoot(navController: NavController) {
    dialog<LoginNavRoute> {
        val viewModel = koinViewModel<LoginViewModel>()
        ActionCollector(viewModel.uiAction) { uiAction ->
            when (uiAction) {
                LoginUiAction.Dismiss -> navController.navigateUp()
            }
        }
        LoginBottomSheet(viewModel::onEvent)
    }
}
