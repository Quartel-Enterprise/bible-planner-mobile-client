package com.quare.bibleplanner.feature.chapterstudy.presentation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.ChapterStudyNavRoute
import com.quare.bibleplanner.core.model.route.getChapterStudyPane
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiEvent
import com.quare.bibleplanner.feature.chapterstudy.presentation.viewmodel.ChapterStudyViewModel
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.chapterStudy() {
    entry<ChapterStudyNavRoute>(metadata = getChapterStudyPane()) { route ->
        val viewModel = koinViewModel<ChapterStudyViewModel> { parametersOf(route) }
        val navigator = koinInject<Navigator>()
        val uiState by viewModel.uiState.collectAsState()
        val isWide = LocalIsWideLayout.current

        LaunchedEffect(isWide) {
            viewModel.onEvent(ChapterStudyUiEvent.OnWidthClassChanged(isWide))
        }

        ChapterStudyScreen(
            uiState = uiState,
            isBesideReader = isWide,
            onEvent = viewModel::onEvent,
            onNavigateBack = navigator::navigateBack,
        )
    }
}
