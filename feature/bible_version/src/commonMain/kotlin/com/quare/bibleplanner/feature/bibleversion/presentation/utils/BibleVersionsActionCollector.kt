package com.quare.bibleplanner.feature.bibleversion.presentation.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import bibleplanner.feature.bible_version.generated.resources.Res
import bibleplanner.feature.bible_version.generated.resources.download_tip_keep_app_open
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun BibleVersionsActionCollector(
    navController: NavHostController,
    uiActionFlow: Flow<BibleVersionUiAction>,
    snackbarHostState: SnackbarHostState,
) {
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            BibleVersionUiAction.BackToPreviousRoute -> navController.navigateUp()
            is BibleVersionUiAction.NavigateToRoute -> navController.navigate(action.route)
            BibleVersionUiAction.ShowDownloadTip -> snackbarHostState.showSnackbar(getString(Res.string.download_tip_keep_app_open))
        }
    }
}
