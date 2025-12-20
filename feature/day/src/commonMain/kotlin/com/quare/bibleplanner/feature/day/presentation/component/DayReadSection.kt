package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.component.completeddate.CompletedDateSection
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.ui.component.date.DatePresentationModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
internal fun DayReadSection(
    isRead: Boolean,
    formattedReadDate: DatePresentationModel?,
    onEvent: (DayUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Completed date section - show when day is marked as read
        AnimatedVisibility(
            visible = isRead,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
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
