package com.quare.bibleplanner.android.feature.loginsyncnudge

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.LoginSyncNudgeNavRoute
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.viewmodel.LoginSyncNudgeViewModel
import org.koin.compose.viewmodel.koinViewModel

fun LoginSyncNudgeNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(),
) {
    val viewModel = koinViewModel<LoginSyncNudgeViewModel>()
    val isDontShowAgainChecked by viewModel.dontShowAgain.collectAsState()
    ExpressiveTheme {
        LoginSyncNudgeContent(
            isDontShowAgainChecked = isDontShowAgainChecked,
            onEvent = viewModel::onEvent,
        )
    }
}
