package com.quare.bibleplanner.feature.read.presentation.screen.content.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_day_progress
import bibleplanner.feature.read.generated.resources.listening_day_title
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningDayProgressUiModel
import org.jetbrains.compose.resources.stringResource

private val daySegmentHeight = 6.dp

@Composable
internal fun DayProgressCard(
    dayProgress: ListeningDayProgressUiModel,
    progress: Float,
) {
    PlayerSection(verticalPadding = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.listening_day_title),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(
                    Res.string.listening_day_progress,
                    dayProgress.currentIndex + 1,
                    dayProgress.chapters.size,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            dayProgress.chapters.indices.forEach { index ->
                val fill = when {
                    index < dayProgress.currentIndex -> 1f
                    index == dayProgress.currentIndex -> progress
                    else -> 0f
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(daySegmentHeight)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fill)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }
        }
    }
}
