package com.quare.bibleplanner.feature.daystudy.presentation.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_generating_subtitle
import bibleplanner.feature.day_study.generated.resources.ai_study_generating_title
import bibleplanner.feature.day_study.generated.resources.ai_study_phase_chapters
import bibleplanner.feature.day_study.generated.resources.ai_study_phase_context
import bibleplanner.feature.day_study.generated.resources.ai_study_phase_questions
import bibleplanner.feature.day_study.generated.resources.ai_study_phase_reading
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationPhase
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val subtitleMaxWidth = 280.dp
private val progressBarWidth = 220.dp
private val progressBarHeight = 4.dp
private val phaseListMaxWidth = 300.dp
private val iconBoxSize = 64.dp
private val iconSize = 34.dp
private val phaseIndicatorSize = 24.dp
private const val TRACK_ALPHA = 0.12f
private const val PULSE_MAX_SCALE = 1.09f
private const val PULSE_MIN_ALPHA = 0.82f
private const val PULSE_HALF_CYCLE_MILLIS = 950

@Composable
internal fun DayStudyGeneratingContent(
    generation: DayStudyGenerationUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PulsingIcon()
        VerticalSpacer(18)
        Text(
            text = stringResource(Res.string.ai_study_generating_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        VerticalSpacer(6)
        Text(
            text = stringResource(Res.string.ai_study_generating_subtitle),
            modifier = Modifier.widthIn(max = subtitleMaxWidth),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(18)
        LinearProgressIndicator(
            modifier = Modifier
                .width(progressBarWidth)
                .height(progressBarHeight),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = TRACK_ALPHA),
            strokeCap = StrokeCap.Round,
        )
        VerticalSpacer(26)
        Column(
            modifier = Modifier
                .widthIn(max = phaseListMaxWidth)
                .fillMaxWidth(),
        ) {
            DayStudyGenerationPhase.entries.forEach { phase ->
                PhaseRow(
                    phase = phase,
                    isDone = generation.isDone(phase),
                    isActive = generation.isActive(phase),
                )
            }
        }
    }
}

@Composable
private fun PulsingIcon() {
    val transition = rememberInfiniteTransition(label = "generating_pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = PULSE_MAX_SCALE,
        animationSpec = infiniteRepeatable(
            animation = tween(PULSE_HALF_CYCLE_MILLIS),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = PULSE_MIN_ALPHA,
        animationSpec = infiniteRepeatable(
            animation = tween(PULSE_HALF_CYCLE_MILLIS),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )
    Box(
        modifier = Modifier
            .size(iconBoxSize)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                },
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun PhaseRow(
    phase: DayStudyGenerationPhase,
    isDone: Boolean,
    isActive: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 2.dp,
                vertical = 9.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PhaseIndicator(
            isDone = isDone,
            isActive = isActive,
        )
        HorizontalSpacer(12)
        Text(
            text = stringResource(phase.titleRes),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isDone || isActive) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun PhaseIndicator(
    isDone: Boolean,
    isActive: Boolean,
) {
    val borderColor = if (isDone || isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    Box(
        modifier = Modifier
            .size(phaseIndicatorSize)
            .background(
                color = if (isDone) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape,
            ).border(
                width = 2.dp,
                color = borderColor,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isDone -> Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onPrimary,
            )

            isActive -> CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

internal val DayStudyGenerationPhase.titleRes: StringResource
    get() = when (this) {
        DayStudyGenerationPhase.READING -> Res.string.ai_study_phase_reading
        DayStudyGenerationPhase.CHAPTERS -> Res.string.ai_study_phase_chapters
        DayStudyGenerationPhase.CONTEXT -> Res.string.ai_study_phase_context
        DayStudyGenerationPhase.QUESTIONS -> Res.string.ai_study_phase_questions
    }
