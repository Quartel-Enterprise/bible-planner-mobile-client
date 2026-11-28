package com.quare.bibleplanner.android.feature.chapterstudy

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.ChapterStudyNavRoute
import com.quare.bibleplanner.core.model.route.getChapterStudyPane
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiEvent
import com.quare.bibleplanner.feature.chapterstudy.presentation.viewmodel.ChapterStudyViewModel
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun ChapterStudyNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    // Why: the study panel scene puts the chapter study beside the reader on wide screens through this metadata.
    metadata = getChapterStudyPane(),
) {
    val viewModel = koinViewModel<ChapterStudyViewModel> { parametersOf(this) }
    val navigator = koinInject<Navigator>()
    val uiState by viewModel.uiState.collectAsState()
    val isWide = LocalIsWideLayout.current
    LaunchedEffect(isWide) {
        viewModel.onEvent(ChapterStudyUiEvent.OnWidthClassChanged(isWide))
    }
    ExpressiveTheme {
        ChapterStudyContent(
            state = uiState,
            isBesideReader = isWide,
            onEvent = viewModel::onEvent,
            onNavigateBack = navigator::navigateBack,
        )
    }
}
