package com.quare.bibleplanner.feature.dayreadingcomplete.presentation

import bibleplanner.feature.day_reading_complete.generated.resources.Res
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_never_show_confirmation
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_offline_message
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteMessage
import org.jetbrains.compose.resources.StringResource

internal fun DayReadingCompleteMessage.toStringResource(): StringResource = when (this) {
    DayReadingCompleteMessage.OFFLINE -> Res.string.day_reading_complete_offline_message
    DayReadingCompleteMessage.NEVER_SHOW_CONFIRMATION -> Res.string.day_reading_complete_never_show_confirmation
}
