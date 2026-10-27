package com.quare.bibleplanner.feature.verse.share.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.verse.share.generated.resources.Res
import bibleplanner.feature.verse.share.generated.resources.share_verse_image_title
import bibleplanner.feature.verse.share.generated.resources.share_verse_title
import com.quare.bibleplanner.core.model.route.ShareVerseImageNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiEvent
import com.quare.bibleplanner.feature.verse.share.presentation.utils.ShareVerseUiActionCollector
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val wideShareImageDialogWidth = 760.dp

fun EntryProviderScope<NavKey>.shareVerse() {
    entry<ShareVerseNavRoute>(metadata = getSheetPane()) { route ->
        val viewModel = koinViewModel<ShareVerseViewModel> { parametersOf(route) }
        val uiState by viewModel.uiState.collectAsState()
        val onEvent = viewModel::onEvent
        val snackbarHostState = remember { SnackbarHostState() }
        ShareVerseUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            snackbarHostState = snackbarHostState,
            onPhotoPicked = { file -> onEvent(ShareVerseUiEvent.OnPhotoPicked(file)) },
        )
        ResponsiveDialogSheet(
            onCloseClick = { onEvent(ShareVerseUiEvent.OnDismiss) },
            title = stringResource(Res.string.share_verse_title),
            sheetBottomBreathingRoom = 12.dp,
        ) {
            Box {
                ShareVerseContent(
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

    entry<ShareVerseImageNavRoute>(metadata = getSheetPane()) { route ->
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
        val onEvent = viewModel::onEvent
        val snackbarHostState = remember { SnackbarHostState() }
        ShareVerseUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            snackbarHostState = snackbarHostState,
            onPhotoPicked = { file -> onEvent(ShareVerseUiEvent.OnPhotoPicked(file)) },
        )
        val isWide = LocalIsWideLayout.current
        ResponsiveDialogSheet(
            onCloseClick = { onEvent(ShareVerseUiEvent.OnDismiss) },
            title = if (isWide) null else stringResource(Res.string.share_verse_image_title),
            cardMaxWidth = if (isWide) wideShareImageDialogWidth else 460.dp,
            sheetBottomBreathingRoom = 12.dp,
        ) {
            Box {
                if (isWide) {
                    ShareVerseImageWideContent(
                        uiState = uiState,
                        onEvent = onEvent,
                        platform = viewModel.platform,
                    )
                } else {
                    ShareVerseImageContent(
                        uiState = uiState,
                        onEvent = onEvent,
                        platform = viewModel.platform,
                    )
                }
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}
