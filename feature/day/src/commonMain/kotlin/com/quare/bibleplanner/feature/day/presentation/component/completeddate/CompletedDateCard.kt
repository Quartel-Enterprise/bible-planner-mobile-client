package com.quare.bibleplanner.feature.day.presentation.component.completeddate

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.no_date_set
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.ui.component.date.DatePresentationModel
import com.quare.bibleplanner.ui.component.date.DateText
import com.quare.bibleplanner.ui.component.icon.EditTextButton
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CompletedDateCard(
    modifier: Modifier = Modifier,
    formattedReadDate: DatePresentationModel?,
    onEvent: (DayUiEvent) -> Unit,
) {
    Card(
        modifier = modifier,
        onClick = {
            onEvent(DayUiEvent.OnEditDateClick)
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalendaryIcon()
            HorizontalSpacer(8)
            CompletedDateTextComponent(
                modifier = Modifier.weight(1f),
                formattedReadDate = formattedReadDate,
            )
            EditTextButton(
                onClick = {
                    onEvent(DayUiEvent.OnEditDateClick)
                },
            )
        }
    }
}

@Composable
private fun CompletedDateTextComponent(
    modifier: Modifier = Modifier,
    formattedReadDate: DatePresentationModel?,
) {
    val style = MaterialTheme.typography.bodyMedium
    formattedReadDate?.let {
        DateText(
            modifier = modifier,
            model = it,
            style = style,
        )
    } ?: Text(
        modifier = modifier,
        text = stringResource(Res.string.no_date_set),
        style = style,
    )
}
