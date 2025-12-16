package com.quare.bibleplanner.feature.readingplan.presentation.component.week

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.week_progress_complete
import com.quare.bibleplanner.ui.component.AnimatedIntText
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WeekProgressLabel(
    modifier: Modifier = Modifier,
    readDaysCount: Int,
    totalDays: Int,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        AnimatedIntText(
            value = readDaysCount,
            label = "readDaysCountAnimation",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "/$totalDays",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = " ${stringResource(Res.string.week_progress_complete)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}
