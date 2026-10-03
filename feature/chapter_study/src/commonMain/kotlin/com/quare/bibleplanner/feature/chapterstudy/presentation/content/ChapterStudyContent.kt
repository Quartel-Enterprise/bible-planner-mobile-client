package com.quare.bibleplanner.feature.chapterstudy.presentation.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.chapter_study.generated.resources.Res
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_generating_subtitle
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_phase_context
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_phase_questions
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_phase_reading
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_phase_summary
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.feature.chapterstudy.presentation.component.ChapterStudyHeroContent
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyContentUiState
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiEvent
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiState
import com.quare.bibleplanner.ui.component.study.AiStudyErrorContent
import com.quare.bibleplanner.ui.component.study.AiStudyGeneratingContent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChapterStudyContent(
    uiState: ChapterStudyUiState,
    onEvent: (ChapterStudyUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (val content = uiState.content) {
            ChapterStudyContentUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                AdaptiveCircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            is ChapterStudyContentUiState.NotGenerated -> ChapterStudyHeroContent(
                hero = content.hero,
                isStarting = content.isStarting,
                onClick = { onEvent(ChapterStudyUiEvent.OnGenerateClick) },
                modifier = Modifier.fillMaxSize(),
            )

            is ChapterStudyContentUiState.Generating -> ChapterStudyGeneratingContent(
                uiState = uiState,
                currentPhaseIndex = content.currentPhaseIndex,
            )

            is ChapterStudyContentUiState.Failed -> AiStudyErrorContent(
                isOffline = content.isOffline,
                onRetryClick = { onEvent(ChapterStudyUiEvent.OnRetryClick) },
                modifier = Modifier.fillMaxSize(),
            )

            is ChapterStudyContentUiState.Loaded -> ChapterStudyLoadedContent(
                uiState = uiState,
                content = content,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun ChapterStudyGeneratingContent(
    uiState: ChapterStudyUiState,
    currentPhaseIndex: Int,
) {
    val chapterLabel = verseReferenceLabel(
        bookId = uiState.bookId,
        chapterNumber = uiState.chapterNumber,
        verseNumbers = emptyList(),
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        AiStudyGeneratingContent(
            subtitle = stringResource(Res.string.chapter_study_generating_subtitle, chapterLabel),
            phases = listOf(
                stringResource(Res.string.chapter_study_phase_reading, chapterLabel),
                stringResource(Res.string.chapter_study_phase_summary),
                stringResource(Res.string.chapter_study_phase_context),
                stringResource(Res.string.chapter_study_phase_questions),
            ),
            currentPhaseIndex = currentPhaseIndex,
            modifier = Modifier.padding(34.dp),
        )
    }
}
