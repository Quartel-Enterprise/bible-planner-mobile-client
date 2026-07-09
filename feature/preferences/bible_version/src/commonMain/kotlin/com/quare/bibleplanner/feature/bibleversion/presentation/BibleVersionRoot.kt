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
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import bibleplanner.feature.preferences.bible_version.generated.resources.Res
import bibleplanner.feature.preferences.bible_version.generated.resources.bible_versions
import bibleplanner.feature.preferences.bible_version.generated.resources.manage_bible_versions_description
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.feature.bibleversion.presentation.component.BibleVersionsContent
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.feature.bibleversion.presentation.utils.BibleVersionsActionCollector
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.bibleVersionSelectionRoot(
    onNavigate: (Any) -> Unit,
    onNavigateBack: () -> Unit,
) {
    entry<BibleVersionSelectorRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) {
        val viewModel: BibleVersionViewModel = koinViewModel()
        val onEvent = viewModel::onEvent
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        BibleVersionsActionCollector(
            onNavigate = onNavigate,
            onNavigateBack = onNavigateBack,
            uiActionFlow = viewModel.uiAction,
            snackbarHostState = snackbarHostState,
        )
        ResponsiveDialogSheet(
            onCloseClick = { onEvent(BibleVersionUiEvent.OnDismiss) },
            title = stringResource(Res.string.bible_versions),
            subtitle = stringResource(Res.string.manage_bible_versions_description),
        ) {
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
