package com.quare.bibleplanner.android.feature.chapterstudy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.quare.bibleplanner.android.designsystem.study.AiStudyBadge
import com.quare.bibleplanner.android.designsystem.study.AiStudyErrorContent
import com.quare.bibleplanner.android.designsystem.study.AiStudyGeneratingContent
import com.quare.bibleplanner.android.designsystem.study.AiStudyHeroContent
import com.quare.bibleplanner.android.designsystem.study.AiStudyLoadingContent
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyContentUiState
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyHeroUiModel
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiEvent
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiState
import com.quare.bibleplanner.ui.utils.asStable
import com.quare.bibleplanner.android.designsystem.R as DesignSystemR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChapterStudyContent(
    state: ChapterStudyUiState,
    isBesideReader: Boolean,
    onEvent: (ChapterStudyUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val chapterLabel = verseReferenceLabel(
        bookId = state.bookId,
        chapterNumber = state.chapterNumber,
        verseNumbers = emptyList(),
    )
    // Why: CMP-10789 crashes on unattached insets when Nav3 moves a screen between scenes.
    Scaffold(
        topBar = {
            // Why: beside the reader the chapter is already in sight and back leaves both panes.
            if (!isBesideReader) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.chapter_study_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = chapterLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(DesignSystemR.string.study_back),
                            )
                        }
                    },
                    windowInsets = TopAppBarDefaults.windowInsets.asStable(),
                )
            }
        },
        floatingActionButton = {
            if (state.content is ChapterStudyContentUiState.Loaded) {
                val label = stringResource(R.string.chapter_study_chat)
                ExtendedFloatingActionButton(
                    onClick = { onEvent(ChapterStudyUiEvent.OnAskAiClick) },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Forum,
                            contentDescription = null,
                        )
                    },
                    text = { Text(text = label) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.asStable(),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            ChapterStudyStateContent(
                state = state,
                chapterLabel = chapterLabel,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun ChapterStudyStateContent(
    state: ChapterStudyUiState,
    chapterLabel: String,
    onEvent: (ChapterStudyUiEvent) -> Unit,
) {
    when (val content = state.content) {
        ChapterStudyContentUiState.Loading -> AiStudyLoadingContent()

        is ChapterStudyContentUiState.NotGenerated -> ChapterStudyHeroContent(
            hero = content.hero,
            isStarting = content.isStarting,
            onClick = { onEvent(ChapterStudyUiEvent.OnGenerateClick) },
        )

        is ChapterStudyContentUiState.Generating -> AiStudyGeneratingContent(
            subtitle = stringResource(R.string.chapter_study_generating_subtitle, chapterLabel),
            phases = listOf(
                stringResource(R.string.chapter_study_phase_reading, chapterLabel),
                stringResource(R.string.chapter_study_phase_summary),
                stringResource(R.string.chapter_study_phase_context),
                stringResource(R.string.chapter_study_phase_questions),
            ),
            currentPhaseIndex = content.currentPhaseIndex,
        )

        is ChapterStudyContentUiState.Failed -> AiStudyErrorContent(
            isOffline = content.isOffline,
            onRetryClick = { onEvent(ChapterStudyUiEvent.OnRetryClick) },
            modifier = Modifier.fillMaxSize(),
        )

        is ChapterStudyContentUiState.Loaded -> ChapterStudyLoadedContent(
            state = state,
            content = content,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun ChapterStudyHeroContent(
    hero: ChapterStudyHeroUiModel,
    isStarting: Boolean,
    onClick: () -> Unit,
) {
    AiStudyHeroContent(
        icon = if (hero.isLocked) Icons.Rounded.Lock else Icons.Rounded.AutoAwesome,
        title = stringResource(R.string.chapter_study_hero_title),
        subtitle = when {
            hero.isLocked -> stringResource(R.string.chapter_study_exhausted_subtitle, hero.quota?.freeLimit ?: 0)
            hero.isPro -> stringResource(R.string.chapter_study_generate_hint_pro)
            else -> stringResource(R.string.chapter_study_generate_hint)
        },
        buttonIcon = if (hero.isLocked) Icons.Rounded.LockOpen else Icons.Rounded.AutoAwesome,
        buttonLabel = stringResource(
            when {
                !hero.isLocked -> R.string.chapter_study_generate
                hero.isRewardedUnlockOffered -> DesignSystemR.string.study_unlock
                else -> R.string.chapter_study_subscribe
            },
        ),
        isLoading = isStarting,
        onClick = onClick,
        modifier = Modifier.fillMaxSize(),
        badge = { ChapterStudyHeroBadge(hero) },
    )
}

@Composable
private fun ChapterStudyHeroBadge(hero: ChapterStudyHeroUiModel) {
    val freeLeft = hero.quota
        ?.takeUnless { quota -> quota.isUnlocked }
        ?.remainingFree
        ?.takeIf { remaining -> remaining > 0 }
    when {
        hero.isPro || hero.isLocked -> AiStudyBadge(
            text = stringResource(R.string.chapter_study_pro_badge),
            isAccent = true,
        )

        freeLeft != null -> AiStudyBadge(
            text = stringResource(R.string.chapter_study_quota_free, freeLeft),
            isAccent = false,
        )
    }
}
