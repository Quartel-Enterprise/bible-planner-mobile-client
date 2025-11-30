package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.mark_day_as_read
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
internal fun DayReadSection(
    isRead: Boolean,
    formattedReadDate: String?,
    onEvent: (DayUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 16.dp),
    ) {
        // Mark day as read toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onEvent(DayUiEvent.OnDayReadToggle(!isRead))
                }.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.mark_day_as_read),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = isRead,
                onCheckedChange = { newValue ->
                    onEvent(DayUiEvent.OnDayReadToggle(newValue))
                },
            )
        }

        // Completed date section - show when day is marked as read
        if (isRead) {
            CompletedDateSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                formattedReadDate = formattedReadDate,
                onEvent = onEvent,
            )
        }
    }
}
