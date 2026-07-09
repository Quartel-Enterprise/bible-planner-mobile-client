package com.quare.bibleplanner.feature.bibleversion.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.feature.bibleversion.presentation.component.BibleVersionsContent
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.feature.bibleversion.presentation.utils.BibleVersionsActionCollector
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.bibleVersionSelectionRoot(navController: NavHostController) {
    dialog<BibleVersionSelectorRoute>(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val viewModel: BibleVersionViewModel = koinViewModel()
        val onEvent = viewModel::onEvent
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        BibleVersionsActionCollector(
            navController = navController,
            uiActionFlow = viewModel.uiAction,
            snackbarHostState = snackbarHostState,
        )
        ResponsiveDialogSheet(onCloseClick = { onEvent(BibleVersionUiEvent.OnDismiss) }) {
            Box {
                BibleVersionsContent(
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .padding(horizontal = 16.dp)
                        .navigationBarsPadding(),
                    uiState = uiState,
                    onEvent = onEvent,
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}
