package com.quare.bibleplanner.feature.more.presentation.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavController
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.no_progress_to_delete_message
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun MoreUiActionCollector(
    uiActionFlow: Flow<MoreUiAction>,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
) {
    val uriHandler = LocalUriHandler.current
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            MoreUiAction.GoToTheme -> navController.navigate(ThemeNavRoute)

            MoreUiAction.GoToPaywall -> navController.navigate(PaywallNavRoute)

            is MoreUiAction.OpenLink -> uriHandler.openUri(action.url)

            MoreUiAction.GoToDeleteProgress -> navController.navigate(DeleteAllProgressNavRoute)

            MoreUiAction.GoToEditPlanStartDay -> navController.navigate(EditPlanStartDateNavRoute)

            MoreUiAction.ShowNoProgressToDelete -> snackbarHostState.showSnackbar(
                getString(Res.string.no_progress_to_delete_message),
            )
        }
    }
}
