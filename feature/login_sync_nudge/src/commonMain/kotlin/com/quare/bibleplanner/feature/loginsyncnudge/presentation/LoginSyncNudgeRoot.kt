package com.quare.bibleplanner.feature.loginsyncnudge.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.LoginSyncNudgeNavRoute
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.utils.LoginSyncNudgeActionCollector
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.viewmodel.LoginSyncNudgeViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.loginSyncNudge(navController: NavController) {
    dialog<LoginSyncNudgeNavRoute> {
        val viewModel = koinViewModel<LoginSyncNudgeViewModel>()
        val isDontShowAgainChecked by viewModel.dontShowAgain.collectAsState()
        LoginSyncNudgeActionCollector(
            uiActionFlow = viewModel.uiAction,
            navController = navController,
        )
        LoginSyncNudgeDialog(
            isDontShowAgainChecked = isDontShowAgainChecked,
            onEvent = viewModel::onEvent,
        )
    }
}
