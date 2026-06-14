package com.quare.bibleplanner.feature.loginwarning.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.feature.loginwarning.presentation.utils.LoginWarningActionCollector
import com.quare.bibleplanner.feature.loginwarning.presentation.viewmodel.LoginWarningViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.loginWarning(navController: NavController) {
    dialog<LoginWarningNavRoute> {
        val viewModel = koinViewModel<LoginWarningViewModel>()
        LoginWarningActionCollector(
            uiActionFlow = viewModel.uiAction,
            navController = navController,
        )
        LoginWarningDialog(
            reason = viewModel.reason,
            onEvent = viewModel::onEvent,
        )
    }
}
