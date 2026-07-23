package com.quare.bibleplanner.feature.daystudy.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_bg_done_subtitle
import bibleplanner.feature.day_study.generated.resources.ai_study_bg_done_title
import bibleplanner.feature.day_study.generated.resources.ai_study_bg_generating_title
import bibleplanner.feature.day_study.generated.resources.ai_study_bg_header_done
import bibleplanner.feature.day_study.generated.resources.ai_study_bg_header_generating
import bibleplanner.feature.day_study.generated.resources.ai_study_bg_header_mixed
import bibleplanner.feature.day_study.generated.resources.ai_study_bg_multi_done_subtitle
import bibleplanner.feature.day_study.generated.resources.ai_study_bg_open
import bibleplanner.feature.day_study.generated.resources.ai_study_phase_chapters
import bibleplanner.feature.day_study.generated.resources.ai_study_phase_context
import bibleplanner.feature.day_study.generated.resources.ai_study_phase_questions
import bibleplanner.feature.day_study.generated.resources.ai_study_phase_reading
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPhaseModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyBackgroundGenerationUiAction
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyBackgroundGenerationUiEvent
import com.quare.bibleplanner.feature.daystudy.presentation.viewmodel.DayStudyBackgroundGenerationViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.mainContentBottomInset
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val wideMinWidth = 700.dp
private val desktopCardWidth = 392.dp
private val desktopEdgePadding = 24.dp
private val mobileHorizontalPadding = 12.dp
private val mobileBottomPadding = 24.dp
private const val TRACK_ALPHA = 0.12f
private const val DIVIDER_ALPHA = 0.5f
private const val PULSE_MAX_SCALE = 1.09f
private const val PULSE_HALF_CYCLE_MILLIS = 950

/**
 * App-wide floating card that surfaces day-study generations running in the background (i.e. not
 * currently shown in a foreground sheet/pane on the day). Rendered once at the root over any screen.
 */
@Composable
fun DayStudyBackgroundGenerationOverlay(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<DayStudyBackgroundGenerationViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is DayStudyBackgroundGenerationUiAction.NavigateToRoute -> onNavigate(action.route)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth > wideMinWidth
        val cardAlignment = if (isWide) Alignment.BottomEnd else Alignment.BottomCenter
        AnimatedVisibility(
            visible = uiState.isVisible,
            modifier = Modifier.align(cardAlignment),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            val cardModifier = if (isWide) {
                Modifier
                    .padding(desktopEdgePadding)
                    .width(desktopCardWidth)
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = mobileHorizontalPadding)
                    .padding(bottom = mainContentBottomInset() + mobileBottomPadding)
            }
            BackgroundCard(
                jobs = uiState.jobs,
                modifier = cardModifier,
                onOpen = { job -> viewModel.onEvent(DayStudyBackgroundGenerationUiEvent.OnOpenClick(job)) },
                onDismiss = { keys -> viewModel.onEvent(DayStudyBackgroundGenerationUiEvent.OnDismissClick(keys)) },
            )
        }
    }
}

@Composable
private fun BackgroundCard(
    jobs: List<DayStudyGenerationJob>,
    onOpen: (DayStudyGenerationJob) -> Unit,
    onDismiss: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMulti = jobs.size > 1
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 8.dp,
    ) {
        Column {
            if (isMulti) {
                MultiJobHeader(
                    jobs = jobs,
                    onDismissAll = { onDismiss(jobs.map(DayStudyGenerationJob::key)) },
                )
                DividerLine()
            }
            jobs.forEachIndexed { index, job ->
                if (index > 0) DividerLine()
                JobRow(
                    job = job,
                    isMulti = isMulti,
                    onOpen = { onOpen(job) },
                    onDismiss = { onDismiss(listOf(job.key)) },
                )
            }
        }
    }
}

@Composable
private fun MultiJobHeader(
    jobs: List<DayStudyGenerationJob>,
    onDismissAll: () -> Unit,
) {
    val total = jobs.size
    val done = jobs.count { it.status is DayStudyGenerationStatus.Done }
    val text = when {
        done >= total -> stringResource(Res.string.ai_study_bg_header_done, total)
        done > 0 -> stringResource(Res.string.ai_study_bg_header_mixed, total, done)
        else -> stringResource(Res.string.ai_study_bg_header_generating, total)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, top = 12.dp, end = 12.dp, bottom = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        CloseButton(onClick = onDismissAll)
    }
}

@Composable
private fun JobRow(
    job: DayStudyGenerationJob,
    isMulti: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isDone = job.status is DayStudyGenerationStatus.Done
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(start = 14.dp, top = 11.dp, end = 12.dp, bottom = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JobIcon(isDone = isDone)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = jobTitle(job, isMulti, isDone),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = jobSubtitle(job, isMulti, isDone),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!isDone) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = TRACK_ALPHA),
                    strokeCap = StrokeCap.Round,
                )
            }
        }
        when {
            isDone -> Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OpenButton(onClick = onOpen)
                CloseButton(onClick = onDismiss)
            }

            !isMulti -> CloseButton(onClick = onDismiss)
        }
    }
}

@Composable
private fun jobTitle(
    job: DayStudyGenerationJob,
    isMulti: Boolean,
    isDone: Boolean,
): String = when {
    isMulti -> job.label
    isDone -> stringResource(Res.string.ai_study_bg_done_title, job.label)
    else -> stringResource(Res.string.ai_study_bg_generating_title, job.label)
}

@Composable
private fun jobSubtitle(
    job: DayStudyGenerationJob,
    isMulti: Boolean,
    isDone: Boolean,
): String = when {
    isDone && isMulti -> stringResource(Res.string.ai_study_bg_multi_done_subtitle)
    isDone -> stringResource(Res.string.ai_study_bg_done_subtitle)
    else -> stringResource(job.phase.subtitleRes())
}

private fun DayStudyPhaseModel?.subtitleRes(): StringResource = when (this) {
    DayStudyPhaseModel.CHAPTERS -> Res.string.ai_study_phase_chapters
    DayStudyPhaseModel.CONTEXT -> Res.string.ai_study_phase_context
    DayStudyPhaseModel.QUESTIONS -> Res.string.ai_study_phase_questions
    DayStudyPhaseModel.READING, null -> Res.string.ai_study_phase_reading
}

@Composable
private fun JobIcon(isDone: Boolean) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = if (isDone) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
                shape = RoundedCornerShape(12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isDone) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(21.dp),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            val transition = rememberInfiniteTransition(label = "bg_job_pulse")
            val scale by transition.animateFloat(
                initialValue = 1f,
                targetValue = PULSE_MAX_SCALE,
                animationSpec = infiniteRepeatable(
                    animation = tween(PULSE_HALF_CYCLE_MILLIS),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "bg_job_pulse_scale",
            )
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier
                    .size(21.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun OpenButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Text(
            text = stringResource(Res.string.ai_study_bg_open),
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = DIVIDER_ALPHA))
}
