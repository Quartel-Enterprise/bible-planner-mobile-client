package com.quare.bibleplanner.feature.chapterstudy.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import bibleplanner.feature.chapter_study.generated.resources.Res
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_title
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiState
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.utils.asStable
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChapterStudyTopBar(
    uiState: ChapterStudyUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(
                    text = stringResource(Res.string.chapter_study_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = verseReferenceLabel(
                        bookId = uiState.bookId,
                        chapterNumber = uiState.chapterNumber,
                        verseNumbers = emptyList(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        navigationIcon = {
            BackIcon(
                platform = uiState.platform,
                onBackClick = onNavigateBack,
            )
        },
        windowInsets = TopAppBarDefaults.windowInsets.asStable(),
    )
}
