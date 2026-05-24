package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.loading
import bibleplanner.feature.reading_plan.generated.resources.plan_progress_days_completed
import bibleplanner.feature.reading_plan.generated.resources.plan_progress_days_remaining
import bibleplanner.feature.reading_plan.generated.resources.plan_progress_motivation_default
import bibleplanner.feature.reading_plan.generated.resources.plan_progress_title
import com.quare.bibleplanner.ui.component.progress.AppLinearProgressBar
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.round

@Composable
fun PlanProgress(
    isLoading: Boolean,
    progress: Float,
    readDaysCount: Int,
    totalDaysCount: Int,
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

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlanProgressHeader()
            PlanProgressBody(
                progressText = if (isLoading) {
                    stringResource(Res.string.loading)
                } else {
                    "${animatedProgress.formatProgress()}%"
                },
                readDaysCount = readDaysCount,
                totalDaysCount = totalDaysCount,
            )
            AppLinearProgressBar(
                modifier = Modifier.fillMaxWidth().height(8.dp),
                progress = animatedProgress / 100,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.plan_progress_motivation_default),
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
private fun PlanProgressBody(
    progressText: String,
    readDaysCount: Int,
    totalDaysCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = progressText,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(
                    Res.string.plan_progress_days_completed,
                    readDaysCount,
                    totalDaysCount,
                ),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(
                    Res.string.plan_progress_days_remaining,
                    (totalDaysCount - readDaysCount).coerceAtLeast(0),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

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
