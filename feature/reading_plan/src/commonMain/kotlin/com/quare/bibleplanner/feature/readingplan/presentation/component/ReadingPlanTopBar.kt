package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.delete_progress_option
import bibleplanner.feature.reading_plan.generated.resources.start_date
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOption
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingPlanTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val calendarIcon = Icons.Default.EditCalendar
    val startDayText = stringResource(Res.string.start_date)
    TopAppBar(
        title = {},
        scrollBehavior = scrollBehavior,
        actions = {
            OutlinedButton(
                onClick = {
                    onEvent(ReadingPlanUiEvent.OnOverflowOptionClick(OverflowOption.EDIT_START_DAY))
                },
            ) {
                Icon(
                    imageVector = calendarIcon,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = startDayText)
            }

            CommonIconButton(
                imageVector = Icons.Default.Delete,
                onClick = {
                    onEvent(ReadingPlanUiEvent.OnOverflowOptionClick(OverflowOption.DELETE_PROGRESS))
                },
                contentDescription = stringResource(Res.string.delete_progress_option),
            )
        },
    )
}
