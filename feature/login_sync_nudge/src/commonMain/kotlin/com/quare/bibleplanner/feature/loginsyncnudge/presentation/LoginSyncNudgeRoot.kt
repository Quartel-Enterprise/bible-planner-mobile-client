package com.quare.bibleplanner.feature.loginsyncnudge.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.LoginSyncNudgeNavRoute
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.utils.LoginSyncNudgeActionCollector
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.viewmodel.LoginSyncNudgeViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.loginSyncNudge(
    onNavigateBack: () -> Unit,
    onNavigateReplacingTop: (NavKey) -> Unit,
) {
    entry<LoginSyncNudgeNavRoute>(metadata = DialogSceneStrategy.dialog()) {
        val viewModel = koinViewModel<LoginSyncNudgeViewModel>()
        val isDontShowAgainChecked by viewModel.dontShowAgain.collectAsState()
        LoginSyncNudgeActionCollector(
            uiActionFlow = viewModel.uiAction,
            onNavigateBack = onNavigateBack,
            onNavigateReplacingTop = onNavigateReplacingTop,
        )
        LoginSyncNudgeDialog(
            isDontShowAgainChecked = isDontShowAgainChecked,
            onEvent = viewModel::onEvent,
        )
    }
}
