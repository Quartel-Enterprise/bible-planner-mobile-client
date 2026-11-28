package com.quare.bibleplanner.android.designsystem.study

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.R

private const val TRACK_ALPHA = 0.12f
private const val PULSE_MAX_SCALE = 1.09f
private const val PULSE_MIN_ALPHA = 0.82f
private const val PULSE_HALF_CYCLE_MILLIS = 950

@Composable
fun AiStudyGeneratingContent(
    subtitle: String,
    phases: List<String>,
    currentPhaseIndex: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PulsingIcon()
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.study_generating_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            modifier = Modifier.widthIn(max = 280.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(18.dp))
        LinearProgressIndicator(
            modifier = Modifier
                .width(220.dp)
                .height(4.dp),
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = TRACK_ALPHA),
            strokeCap = StrokeCap.Round,
        )
        Spacer(modifier = Modifier.height(26.dp))
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth(),
        ) {
            phases.forEachIndexed { index, phase ->
                PhaseRow(
                    title = phase,
                    isDone = index < currentPhaseIndex,
                    isActive = index == currentPhaseIndex,
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
    AiStudyHeroIcon(
        icon = Icons.Rounded.AutoAwesome,
        iconModifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        },
    )
}

@Composable
private fun PhaseRow(
    title: String,
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
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
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
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(
                color = if (isDone) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape,
            ).border(
                width = 2.dp,
                color = if (isDone || isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
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
            )
        }
    }
}
