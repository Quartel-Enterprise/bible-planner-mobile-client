package com.quare.bibleplanner.feature.day.presentation.component.completeddate

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.completed_date
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.ui.component.date.DatePresentationModel
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CompletedDateSection(
    modifier: Modifier = Modifier,
    formattedReadDate: DatePresentationModel?,
    onEvent: (DayUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(Res.string.completed_date),
            style = MaterialTheme.typography.labelMedium,
        )
        VerticalSpacer(8)
        CompletedDateCard(
            formattedReadDate = formattedReadDate,
            onEvent = onEvent,
        )
    }
}
