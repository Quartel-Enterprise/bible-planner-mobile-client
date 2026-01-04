package com.quare.bibleplanner.feature.more.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavController
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun MoreUiActionCollector(
    uiActionFlow: Flow<MoreUiAction>,
    navController: NavController,
) {
    val uriHandler = LocalUriHandler.current
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            MoreUiAction.GoToTheme -> navController.navigate(ThemeNavRoute)
            MoreUiAction.GoToPaywall -> navController.navigate(PaywallNavRoute)
            is MoreUiAction.OpenLink -> uriHandler.openUri(action.url)
        }
    }
}
