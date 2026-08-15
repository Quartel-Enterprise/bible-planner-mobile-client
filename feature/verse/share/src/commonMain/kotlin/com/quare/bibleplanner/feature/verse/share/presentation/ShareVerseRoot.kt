package com.quare.bibleplanner.feature.verse.share.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import bibleplanner.feature.verse.share.generated.resources.Res
import bibleplanner.feature.verse.share.generated.resources.share_verse_image_title
import bibleplanner.feature.verse.share.generated.resources.share_verse_title
import com.quare.bibleplanner.core.model.route.ShareVerseImageNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiEvent
import com.quare.bibleplanner.feature.verse.share.presentation.utils.ShareVerseUiActionCollector
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.shareVerse(
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
) {
    entry<ShareVerseNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) { route ->
        val viewModel = koinViewModel<ShareVerseViewModel> { parametersOf(route) }
        val uiState by viewModel.uiState.collectAsState()
        ShareVerseUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            onNavigate = onNavigate,
            onNavigateBack = onNavigateBack,
        )
        val onEvent = viewModel::onEvent
        ResponsiveDialogSheet(
            onCloseClick = { onEvent(ShareVerseUiEvent.OnDismiss) },
            title = stringResource(Res.string.share_verse_title),
        ) {
            ShareVerseContent(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }

    entry<ShareVerseImageNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) { route ->
        val viewModel = koinViewModel<ShareVerseViewModel> {
            parametersOf(
                ShareVerseNavRoute(
                    bookId = route.bookId,
                    chapterNumber = route.chapterNumber,
                    verseNumbers = route.verseNumbers,
                ),
            )
        }
        val uiState by viewModel.uiState.collectAsState()
        ShareVerseUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            onNavigate = onNavigate,
            onNavigateBack = onNavigateBack,
        )
        val onEvent = viewModel::onEvent
        ResponsiveDialogSheet(
            onCloseClick = { onEvent(ShareVerseUiEvent.OnDismiss) },
            title = stringResource(Res.string.share_verse_image_title),
        ) {
            ShareVerseImageContent(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
}
