package com.quare.bibleplanner.feature.loginwarning.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.feature.loginwarning.presentation.viewmodel.LoginWarningViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.loginWarning() {
    entry<LoginWarningNavRoute>(metadata = DialogSceneStrategy.dialog()) { route ->
        val viewModel = koinViewModel<LoginWarningViewModel> { parametersOf(route) }
        LoginWarningDialog(
            reason = viewModel.reason,
            onEvent = viewModel::onEvent,
        )
    }
}
