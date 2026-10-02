package com.quare.bibleplanner.feature.chapterstudy.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.ChapterStudyNavRoute
import com.quare.bibleplanner.feature.chapterstudy.presentation.viewmodel.ChapterStudyViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.chapterStudy() {
    entry<ChapterStudyNavRoute> { route ->
        val viewModel = koinViewModel<ChapterStudyViewModel> { parametersOf(route) }
        val navigator = koinInject<Navigator>()
        val uiState by viewModel.uiState.collectAsState()

        ChapterStudyScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onNavigateBack = navigator::navigateBack,
        )
    }
}
