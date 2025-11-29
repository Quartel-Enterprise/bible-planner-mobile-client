package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.calendar
import bibleplanner.feature.day.generated.resources.completed_date
import bibleplanner.feature.day.generated.resources.edit
import bibleplanner.feature.day.generated.resources.no_date_set
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CompletedDateSection(
    modifier: Modifier = Modifier,
    formattedReadDate: String?,
    onEvent: (DayUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(Res.string.completed_date),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = stringResource(Res.string.calendar),
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(
                text = formattedReadDate ?: stringResource(Res.string.no_date_set),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = {
                    onEvent(DayUiEvent.OnEditDateClick)
                },
            ) {
                Text(stringResource(Res.string.edit))
            }
        }
    }
}
