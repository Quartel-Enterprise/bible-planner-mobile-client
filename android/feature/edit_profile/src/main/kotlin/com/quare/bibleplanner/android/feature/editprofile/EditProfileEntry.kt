package com.quare.bibleplanner.android.feature.editprofile

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.EditProfileNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.EditProfileViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

internal fun EditProfileNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<EditProfileViewModel>()
    val navigator = koinInject<Navigator>()
    ActionCollector(viewModel.uiAction) { action ->
        navigator.navigateReplacingTop(action.route)
    }
    ExpressiveTheme {
        EditProfileContent(
            onDismiss = navigator::navigateBack,
            onEvent = viewModel::onEvent,
        )
    }
}
