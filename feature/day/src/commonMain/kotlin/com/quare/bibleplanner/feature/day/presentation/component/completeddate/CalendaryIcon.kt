package com.quare.bibleplanner.feature.day.presentation.component.completeddate

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.calendar
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CalendaryIcon(modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier,
        imageVector = Icons.Default.CalendarToday,
        contentDescription = stringResource(Res.string.calendar),
    )
}
