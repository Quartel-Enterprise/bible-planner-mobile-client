package com.quare.bibleplanner.android.feature.loginwarning

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.feature.loginwarning.presentation.viewmodel.LoginWarningViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun LoginWarningNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(),
) {
    val viewModel = koinViewModel<LoginWarningViewModel> { parametersOf(this) }
    ExpressiveTheme {
        LoginWarningContent(
            reason = viewModel.reason,
            onEvent = viewModel::onEvent,
        )
    }
}
