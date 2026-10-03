package com.quare.bibleplanner.feature.daystudy.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_title
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationError
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel
import com.quare.bibleplanner.ui.component.study.AiStudyErrorContent
import com.quare.bibleplanner.ui.component.study.AiStudyHeroContent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DayStudyPane(
    cardState: Loadable<DayStudyCardUiModel>,
    openStudy: DayStudyModel?,
    generation: DayStudyGenerationUiModel?,
    generationError: DayStudyGenerationError?,
    isOpeningStudy: Boolean,
    onCardClick: () -> Unit,
    onRetryClick: () -> Unit,
    onAskAiClick: () -> Unit,
    showStudyHeader: Boolean,
    modifier: Modifier = Modifier,
) {
    val mode = cardState.valueOrNull()?.mode
    LaunchedEffect(mode, openStudy != null, generation != null, generationError) {
        if (openStudy == null && generation == null && generationError == null && mode == DayStudyCardMode.VIEW) {
            onCardClick()
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        when {
            generation != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center,
            ) {
                DayStudyGeneratingContent(
                    generation = generation,
                    modifier = Modifier.padding(34.dp),
                )
            }

            openStudy != null -> DayStudyInlinePane(
                study = openStudy,
                showHeader = showStudyHeader,
                onAskAiClick = onAskAiClick,
                modifier = Modifier.fillMaxSize(),
            )

            generationError != null -> AiStudyErrorContent(
                isOffline = generationError == DayStudyGenerationError.OFFLINE,
                onRetryClick = onRetryClick,
                modifier = Modifier.fillMaxSize(),
            )

            cardState is Loadable.Loaded -> DayStudyPaneHero(
                card = cardState.value,
                isOpening = isOpeningStudy,
                onClick = onCardClick,
                modifier = Modifier.fillMaxSize(),
            )

            else -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                AdaptiveCircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun DayStudyPaneHero(
    card: DayStudyCardUiModel,
    isOpening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLocked = card.mode == DayStudyCardMode.LOCKED
    AiStudyHeroContent(
        icon = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.AutoAwesome,
        title = stringResource(Res.string.ai_study_title),
        subtitle = dayStudyCardSubtitle(card),
        buttonIcon = getHeroButtonIcon(card.mode),
        buttonLabel = stringResource(card.toButtonLabel()),
        isLoading = isOpening,
        onClick = onClick,
        modifier = modifier,
        badge = { CardBadge(card) },
    )
}

private fun getHeroButtonIcon(mode: DayStudyCardMode?): ImageVector = when (mode) {
    DayStudyCardMode.LOCKED -> Icons.Rounded.LockOpen
    null, DayStudyCardMode.GENERATE, DayStudyCardMode.VIEW -> Icons.Rounded.AutoAwesome
}
