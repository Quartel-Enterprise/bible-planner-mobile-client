package com.quare.bibleplanner.android.feature.listeningplayer

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.ChapterListeningPlayerNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerViewModel
import org.koin.compose.viewmodel.koinViewModel

internal fun ChapterListeningPlayerNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<ChapterListeningPlayerViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    ExpressiveTheme {
        ListeningPlayerContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
