package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.no_read_for_days
import bibleplanner.feature.reading_plan.generated.resources.plan_progress_days_completed
import bibleplanner.feature.reading_plan.generated.resources.plan_progress_days_remaining
import bibleplanner.feature.reading_plan.generated.resources.plan_progress_title
import bibleplanner.feature.reading_plan.generated.resources.streak_days_label
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMode
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanStatus
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.round

@Composable
internal fun PlanProgress(
    progress: Float,
    readDaysCount: Int,
    totalDaysCount: Int,
    motivationMessage: PlanMotivationMessage,
    planStatus: PlanStatus,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 400,
            easing = LinearEasing,
        ),
        label = "planProgressAnimation",
    )
    val animatedReadDays by animateIntAsState(
        targetValue = readDaysCount,
        label = "planReadDaysAnimation",
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlanProgressHeader()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                PlanProgressRing(
                    progress = animatedProgress,
                    text = "${animatedProgress.formatProgress()}%",
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(
                            Res.string.plan_progress_days_completed,
                            animatedReadDays,
                            totalDaysCount,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(
                            Res.string.plan_progress_days_remaining,
                            (totalDaysCount - animatedReadDays).coerceAtLeast(0),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PlanStatusChip(planStatus = planStatus)
                }
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = motivationMessage.resolveText(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun PlanProgressHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
        )
        Text(
            text = stringResource(Res.string.plan_progress_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun PlanProgressRing(
    progress: Float,
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(92.dp),
            progress = { (progress / 100f).coerceIn(0f, 1f) },
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 9.dp,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun PlanStatusChip(
    planStatus: PlanStatus,
    modifier: Modifier = Modifier,
) {
    val daysSinceLastRead = planStatus.daysSinceLastRead
    val animatedStreak by animateIntAsState(targetValue = planStatus.streakDays, label = "streakDaysAnimation")
    val animatedLapse by animateIntAsState(targetValue = daysSinceLastRead ?: 0, label = "lapseDaysAnimation")
    when {
        planStatus.mode == PlanMode.Behind && daysSinceLastRead != null -> StatusChip(
            modifier = modifier,
            icon = Icons.Default.Schedule,
            text = pluralStringResource(
                Res.plurals.no_read_for_days,
                daysSinceLastRead,
                animatedLapse,
            ),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        planStatus.mode.showsStreak() && planStatus.streakDays > 0 -> StatusChip(
            modifier = modifier,
            icon = Icons.Default.LocalFireDepartment,
            text = pluralStringResource(
                Res.plurals.streak_days_label,
                planStatus.streakDays,
                animatedStreak,
            ),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            iconColor = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun StatusChip(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(50),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun PlanMode.showsStreak(): Boolean =
    this == PlanMode.OnTrack || this == PlanMode.Ahead || this == PlanMode.CaughtUp

private fun Float.formatProgress(): String {
    val rounded = round(this * 100.0f) / 100.0f
    val tolerance = 0.0001f
    if (abs(rounded % 1.0f) < tolerance) {
        return rounded.toInt().toString()
    }
    val integerPart = rounded.toInt()
    val decimalPart = rounded - integerPart
    val decimalAsInt = (decimalPart * 100.0f + 0.5f).toInt()
    if (decimalAsInt % 10 == 0) {
        val firstDecimal = decimalAsInt / 10
        return "$integerPart.$firstDecimal"
    }
    val firstDecimal = decimalAsInt / 10
    val secondDecimal = decimalAsInt % 10
    return "$integerPart.$firstDecimal$secondDecimal"
}
