package com.quare.bibleplanner.feature.bibleversion.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.feature.bibleversion.presentation.component.BibleVersionsContent
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.feature.bibleversion.presentation.utils.BibleVersionsActionCollector
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.bibleVersionSelectionRoot(navController: NavHostController) {
    dialog<BibleVersionSelectorRoute> {
        val viewModel: BibleVersionViewModel = koinViewModel()
        val onEvent = viewModel::onEvent
        val uiState by viewModel.uiState.collectAsState()
        BibleVersionsActionCollector(
            navController = navController,
            uiActionFlow = viewModel.uiAction,
        )
        ModalBottomSheet(onDismissRequest = { onEvent(BibleVersionUiEvent.OnDismiss) }) {
            BibleVersionsContent(
                modifier = Modifier.padding(bottom = 32.dp).padding(horizontal = 16.dp),
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
}
