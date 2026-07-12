package com.quare.bibleplanner.ui.utils

import androidx.compose.runtime.Composable
import bibleplanner.ui.utils.generated.resources.Res
import bibleplanner.ui.utils.generated.resources.relative_active_now
import bibleplanner.ui.utils.generated.resources.relative_hours_ago
import bibleplanner.ui.utils.generated.resources.relative_minutes_ago
import com.quare.bibleplanner.core.date.RelativeTime
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun RelativeTime.format(): String = when (this) {
    RelativeTime.JustNow -> stringResource(Res.string.relative_active_now)
    is RelativeTime.MinutesAgo -> pluralStringResource(Res.plurals.relative_minutes_ago, minutes, minutes)
    is RelativeTime.HoursAgo -> pluralStringResource(Res.plurals.relative_hours_ago, hours, hours)
    is RelativeTime.OlderThanADay -> representation.format()
}
