package com.quare.bibleplanner.android.feature.daystudy

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.study.AiStudyBadge
import com.quare.bibleplanner.android.designsystem.study.AiStudyErrorContent
import com.quare.bibleplanner.android.designsystem.study.AiStudyGeneratingContent
import com.quare.bibleplanner.android.designsystem.study.AiStudyHeroContent
import com.quare.bibleplanner.android.designsystem.study.AiStudyLoadingContent
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationError
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationPhase
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiEvent
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiState
import com.quare.bibleplanner.ui.utils.asStable
import software.quare.loadable.Loadable
import software.quare.loadable.isLoading
import software.quare.loadable.valueOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayStudyContent(
    state: DayStudyRouteUiState,
    isWide: Boolean,
    snackbarHostState: SnackbarHostState,
    onEvent: (DayStudyRouteUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    if (isWide) {
        // Why: beside the day there is no top bar, and the panel still shows its messages at its own bottom.
        Box(modifier = Modifier.fillMaxSize()) {
            DayStudyPane(
                state = state,
                showStudyHeader = true,
                onEvent = onEvent,
            )
            // Why: the app draws edge to edge, so without this the snackbar sits under the navigation bar.
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars.asStable()),
            )
        }
    } else {
        // Why: CMP-10789 crashes on unattached insets when Nav3 moves a screen between scenes.
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        DayStudyHeader(
                            passageLabel = state.passageLabel,
                            showIcon = false,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.day_study_back),
                            )
                        }
                    },
                    windowInsets = TopAppBarDefaults.windowInsets.asStable(),
                )
            },
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets.asStable(),
        ) { paddingValues ->
            DayStudyPane(
                state = state,
                showStudyHeader = false,
                onEvent = onEvent,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun DayStudyPane(
    state: DayStudyRouteUiState,
    showStudyHeader: Boolean,
    onEvent: (DayStudyRouteUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val openStudy = state.openStudy
    val generation = state.generation
    val generationError = state.generationError
    val cardState = state.card
    val mode = cardState.valueOrNull()?.mode
    // Why: a study that already exists opens at once, so the panel never asks to view what it could show.
    LaunchedEffect(mode, openStudy != null, generation != null, generationError) {
        if (openStudy == null && generation == null && generationError == null && mode == DayStudyCardMode.VIEW) {
            onEvent(DayStudyRouteUiEvent.OnCardClick)
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        when {
            generation != null -> DayStudyGeneratingContent(generation = generation)

            openStudy != null -> DayStudyTabbedContent(
                study = openStudy,
                showHeader = showStudyHeader,
                onAskAiClick = { onEvent(DayStudyRouteUiEvent.OnAskAiClick) },
            )

            generationError != null -> AiStudyErrorContent(
                isOffline = generationError == DayStudyGenerationError.OFFLINE,
                onRetryClick = { onEvent(DayStudyRouteUiEvent.OnRetryClick) },
                modifier = Modifier.fillMaxSize(),
            )

            cardState is Loadable.Loaded -> DayStudyHeroContent(
                card = cardState.value,
                isOpening = state.isOpeningStudy,
                onClick = { onEvent(DayStudyRouteUiEvent.OnCardClick) },
            )

            else -> AiStudyLoadingContent()
        }
    }
}

@Composable
private fun DayStudyGeneratingContent(generation: DayStudyGenerationUiModel) {
    AiStudyGeneratingContent(
        subtitle = stringResource(R.string.day_study_generating_subtitle),
        phases = DayStudyGenerationPhase.entries.map { phase -> stringResource(phase.toTitleRes()) },
        currentPhaseIndex = generation.currentPhaseIndex,
    )
}

@StringRes
private fun DayStudyGenerationPhase.toTitleRes(): Int = when (this) {
    DayStudyGenerationPhase.READING -> R.string.day_study_phase_reading
    DayStudyGenerationPhase.CHAPTERS -> R.string.day_study_phase_chapters
    DayStudyGenerationPhase.CONTEXT -> R.string.day_study_phase_context
    DayStudyGenerationPhase.QUESTIONS -> R.string.day_study_phase_questions
}

@Composable
private fun DayStudyHeroContent(
    card: DayStudyCardUiModel,
    isOpening: Boolean,
    onClick: () -> Unit,
) {
    val isLocked = card.mode == DayStudyCardMode.LOCKED
    AiStudyHeroContent(
        icon = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.AutoAwesome,
        title = stringResource(R.string.day_study_title),
        subtitle = card.toSubtitle(),
        buttonIcon = if (isLocked) Icons.Rounded.LockOpen else Icons.Rounded.AutoAwesome,
        buttonLabel = stringResource(card.toButtonLabelRes()),
        isLoading = isOpening,
        onClick = onClick,
        modifier = Modifier.fillMaxSize(),
        badge = { DayStudyCardBadge(card) },
    )
}

@Composable
private fun DayStudyCardUiModel.toSubtitle(): String = when (mode) {
    null, DayStudyCardMode.GENERATE -> stringResource(
        if (isPro) R.string.day_study_generate_hint_pro else R.string.day_study_generate_hint,
    )

    DayStudyCardMode.VIEW -> stringResource(R.string.day_study_view_hint)

    DayStudyCardMode.LOCKED -> stringResource(
        R.string.day_study_exhausted_subtitle,
        quota.valueOrNull()?.freeLimit ?: 0,
    )
}

@StringRes
private fun DayStudyCardUiModel.toButtonLabelRes(): Int = when (mode) {
    null, DayStudyCardMode.GENERATE -> R.string.day_study_generate

    DayStudyCardMode.VIEW -> R.string.day_study_view

    DayStudyCardMode.LOCKED -> if (isRewardedUnlockOffered) {
        R.string.day_study_unlock
    } else {
        R.string.day_study_subscribe
    }
}

@Composable
private fun DayStudyCardBadge(card: DayStudyCardUiModel) {
    val mode = card.mode ?: return
    val isGenerated = mode == DayStudyCardMode.VIEW
    val isProBadge = card.isPro || mode == DayStudyCardMode.LOCKED
    if (!isGenerated && !isProBadge && card.quota.isLoading()) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(18.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(6.dp),
                ),
        )
        return
    }
    val remainingFree = card.quota.valueOrNull()?.remainingFree ?: 0
    val text = when {
        isGenerated -> stringResource(R.string.day_study_generated)
        isProBadge -> stringResource(R.string.day_study_pro_badge)
        remainingFree > 0 -> stringResource(R.string.day_study_quota_free, remainingFree)
        else -> return
    }
    AiStudyBadge(
        text = text,
        isAccent = isGenerated || isProBadge,
    )
}

@Composable
internal fun DayStudyHeader(
    passageLabel: String?,
    showIcon: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showIcon) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = stringResource(R.string.day_study_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        passageLabel?.let { label ->
            Text(
                text = label,
                modifier = Modifier.padding(
                    start = if (showIcon) 30.dp else 0.dp,
                    top = 3.dp,
                ),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
