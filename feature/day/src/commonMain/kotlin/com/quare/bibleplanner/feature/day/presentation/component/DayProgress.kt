package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.passages_completed
import com.quare.bibleplanner.ui.component.AnimatedIntText
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DayProgress(
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        AnimatedIntText(
            value = completedCount,
            label = "completedCountAnimation",
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = "/$totalCount",
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = " ${stringResource(Res.string.passages_completed)}",
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
