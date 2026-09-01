package com.quare.bibleplanner.feature.readingplan.presentation.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.no_progress_to_delete_message
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun ReadingPlanUiActionCollector(
    snackbarHostState: SnackbarHostState,
    flow: Flow<ReadingPlanUiAction>,
) {
    val uriHandler = LocalUriHandler.current
    ActionCollector(flow) { uiAction ->
        when (uiAction) {
            is ReadingPlanUiAction.OpenLink -> uriHandler.openUri(uiAction.url)

            ReadingPlanUiAction.ShowNoProgressToDelete -> {
                snackbarHostState.showSnackbar(
                    getString(Res.string.no_progress_to_delete_message),
                )
            }
        }
    }
}
